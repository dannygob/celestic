package com.example.celestic.manager

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint3f
import org.opencv.core.Size
import org.opencv.objdetect.CharucoBoard
import org.opencv.objdetect.CharucoDetector
import org.opencv.objdetect.Objdetect
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CalibrationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    var cameraMatrix: Mat? = null
    var distortionCoeffs: Mat? = null
    var resolution: Pair<Int, Int>? = null
    var calibrationDate: String? = null

    private val calibrationFile = File(context.filesDir, "config/calibration.json")

    init {
        loadCalibration()
    }

    private fun loadCalibration(): Boolean {
        return try {
            if (!calibrationFile.exists()) return false
            val json = JSONObject(FileInputStream(calibrationFile).bufferedReader().use { it.readText() })

            // Parse cameraMatrix (esperamos un String dump de OpenCV o un array)
            val matrixData = json.getString("cameraMatrix")
            cameraMatrix = stringToMat(matrixData, 3, 3, CvType.CV_64F)

            val distData = json.getString("distortionCoeffs")
            distortionCoeffs = stringToMat(distData, 1, 5, CvType.CV_64F)

            calibrationDate = json.optString("calibrationDate")
            true
        } catch (e: Exception) {
            Log.e("CalibrationManager", "Error al cargar calibración", e)
            false
        }
    }

    private fun stringToMat(data: String, rows: Int, cols: Int, type: Int): Mat {
        val mat = Mat(rows, cols, type)
        val cleanData = data.replace("[", "").replace("]", "").replace(";", "").trim()
        val values = cleanData.split(Regex("\\s*,\\s*|\\s+")).filter { it.isNotBlank() }
        val doubleValues = values.map { it.toDouble() }.toDoubleArray()
        mat.put(0, 0, *doubleValues)
        return mat
    }

    fun getScaleFactor(pixelLength: Double): Double {
        val mat = cameraMatrix ?: return 0.0
        val focalLength = mat.get(0, 0)[0]
        val mmPerPixel = 1.0 / focalLength
        return pixelLength * mmPerPixel
    }

    // Listas para acumular capturas
    private val allCharucoCorners = mutableListOf<Mat>()
    private val allCharucoIds = mutableListOf<Mat>()
    private var imageSize: Size? = null

    fun resetData() {
        allCharucoCorners.forEach { it.release() }
        allCharucoIds.forEach { it.release() }
        allCharucoCorners.clear()
        allCharucoIds.clear()
    }

    fun addCalibrationFrame(image: Mat): Boolean {
        val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250)
        val board = CharucoBoard(Size(5.0, 7.0), 0.04f, 0.02f, dictionary)
        val detector = CharucoDetector(board)
        
        val charucoCorners = Mat()
        val charucoIds = Mat()
        val markerCorners = ArrayList<Mat>()
        val markerIds = Mat()

        detector.detectBoard(image, charucoCorners, charucoIds, markerCorners, markerIds)

        val success = if (charucoCorners.total() > 4) {
            // Clonamos los objetos Mat antes de guardarlos para evitar que sean liberados prematuramente
            allCharucoCorners.add(charucoCorners.clone())
            allCharucoIds.add(charucoIds.clone())
            imageSize = image.size()
            true
        } else {
            false
        }

        // Liberamos recursos temporales tras la detección
        charucoCorners.release()
        charucoIds.release()
        markerIds.release()
        markerCorners.forEach { it.release() }

        return success
    }

    fun runCalibration(): Double {
        val size = imageSize ?: return -1.0
        val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250)
        val board = CharucoBoard(Size(5.0, 7.0), 0.04f, 0.02f, dictionary)

        // Preparar listas de puntos 3D y 2D compatibles con Calib3d.calibrateCamera
        val allObjectPoints = ArrayList<Mat>()
        val allImagePoints = ArrayList<Mat>()

        for (i in allCharucoCorners.indices) {
            val corners = allCharucoCorners[i]
            val ids = allCharucoIds[i]

            if (corners.total() > 0) {
                val objPoints = MatOfPoint3f()
                val imgPoints = MatOfPoint2f()

                // La API de Java de OpenCV reqruiere listas para los corners e IDs incluso para un solo frame
                val cornersList = listOf(corners)
                val idsList = listOf(ids)

                try {
                    board.matchImagePoints(cornersList, idsList as Mat?, objPoints, imgPoints)

                    if (objPoints.total() > 4) {
                        allObjectPoints.add(objPoints)
                        allImagePoints.add(imgPoints)
                    } else {
                        objPoints.release()
                        imgPoints.release()
                    }
                } catch (e: Exception) {
                    Log.e("CalibrationManager", "Error en matchImagePoints frame $i", e)
                    objPoints.release()
                    imgPoints.release()
                }
            }
        }

        if (allObjectPoints.isEmpty()) return -2.0

        val cameraMat = Mat.eye(3, 3, CvType.CV_64F)
        val distCoeffs = Mat.zeros(5, 1, CvType.CV_64F)
        val rvecs = ArrayList<Mat>()
        val tvecs = ArrayList<Mat>()

        return try {
            // Calibración clásica usando Calib3d (Disponible siempre en el núcleo de OpenCV)
            val rms = Calib3d.calibrateCamera(
                allObjectPoints,
                allImagePoints,
                size,
                cameraMat,
                distCoeffs,
                rvecs,
                tvecs
            )

            if (rms > 0) {
                this.cameraMatrix = cameraMat
                this.distortionCoeffs = distCoeffs
                saveCalibrationToJson(
                    cameraMat,
                    distCoeffs,
                    Pair(size.width.toInt(), size.height.toInt())
                )
            }
            rms
        } catch (e: Exception) {
            Log.e("CalibrationManager", "Error fatal en calibrateCamera", e)
            -3.0
        } finally {
            // Opcional: liberar puntos capturados para ahorrar memoria tras una calibración exitosa
        }
    }

    fun saveCalibrationToJson(cameraMatrix: Mat, distortionCoeffs: Mat, resolution: Pair<Int, Int>) {
        val json = JSONObject()
        json.put("cameraMatrix", cameraMatrix.dump())
        json.put("distortionCoeffs", distortionCoeffs.dump())
        json.put(
            "calibrationDate",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        calibrationFile.parentFile?.mkdirs()
        calibrationFile.writeText(json.toString())
    }
}