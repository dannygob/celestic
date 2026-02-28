package com.example.celestic.manager

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.objdetect.CharucoBoard
import org.opencv.objdetect.CharucoDetector
import org.opencv.objdetect.Objdetect
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@javax.inject.Singleton
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

            // Parse cameraMatrix (expect an OpenCV String dump or an array)
            val matrixData = json.getString("cameraMatrix")
            cameraMatrix?.release()
            cameraMatrix = stringToMat(matrixData, 3, 3)

            val distData = json.getString("distortionCoeffs")
            distortionCoeffs?.release()
            distortionCoeffs = stringToMat(distData, 1, 5)

            calibrationDate = json.optString("calibrationDate")
            true
        } catch (e: Exception) {
            Log.e("CalibrationManager", "Error loading calibration", e)
            false
        }
    }

    private fun stringToMat(data: String, rows: Int, cols: Int): Mat {
        val mat = Mat(rows, cols, CvType.CV_64F)
        val cleanData = data.replace("[", "").replace("]", "").replace(";", "").trim()
        val values = cleanData.split(Regex("\\s*,\\s*|\\s+")).filter { it.isNotBlank() }
        val doubleValues = values.map { it.toDouble() }.toDoubleArray()
        mat.put(0, 0, *doubleValues)
        return mat
    }

    /**
     * Calculates the scale factor (mm per pixel) based on calibration and distance to the object.
     * If no calibration exists, returns 1.0 (pixel = pixel).
     * @param distanceMm Approximate distance from camera to object in mm.
     * @return Scale factor (mm/pixel)
     */
    fun getScaleFactor(distanceMm: Double): Double {
        val mat = cameraMatrix
            ?: return 0.264 // Default approx: 1 px ~= 0.264 mm (96 DPI) as fallback if totally unknown

        // fx: focal distance in pixels (x-axis)
        val fx = mat.get(0, 0)[0]

        // Relationship: x_mm / z_mm = u_px / fx_px
        // x_mm/u_px = z_mm / fx_px
        // scale (mm/px) = Z / fx

        val scale = distanceMm / fx
        return if (scale.isNaN() || scale.isInfinite() || scale == 0.0) 0.264 else scale
    }

    /**
     * Calculates estimated distance (Z) to a known marker.
     * @param detectedMarkerWidthPx Width of detected marker in image (pixels)
     * @param realMarkerSizeMm Real size of the marker (mm)
     * @return distance Z in mm
     */
    fun estimateDistance(detectedMarkerWidthPx: Double, realMarkerSizeMm: Double): Double {
        val mat = cameraMatrix ?: return 1000.0 // Default 1m
        val fx = mat.get(0, 0)[0]

        // Z = (real_size * fx) / pixel_size
        return (realMarkerSizeMm * fx) / detectedMarkerWidthPx
    }

    // Lists to accumulate captures
    private val allCharucoCorners = mutableListOf<Mat>()
    private val allCharucoIds = mutableListOf<Mat>()
    private var imageSize: Size? = null

    private val dictionary by lazy { Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250) }
    private val board by lazy { CharucoBoard(Size(5.0, 7.0), 0.04f, 0.02f, dictionary) }
    private val detector by lazy { CharucoDetector(board) }

    fun resetData() {
        allCharucoCorners.forEach { it.release() }
        allCharucoIds.forEach { it.release() }
        allCharucoCorners.clear()
        allCharucoIds.clear()
    }

    fun addCalibrationFrame(image: Mat): Boolean {
        val charucoCorners = Mat()
        val charucoIds = Mat()
        val markerCorners = ArrayList<Mat>()
        val markerIds = Mat()

        try {
            detector.detectBoard(image, charucoCorners, charucoIds, markerCorners, markerIds)

            return if (charucoCorners.total() > 4) {
                allCharucoCorners.add(charucoCorners.clone())
                allCharucoIds.add(charucoIds.clone())
                imageSize = image.size()
                true
            } else {
                false
            }
        } finally {
            charucoCorners.release()
            charucoIds.release()
            markerIds.release()
            markerCorners.forEach { it.release() }
        }
    }

    fun runCalibration(): Double {
        val size = imageSize ?: return -1.0

        val allObjectPoints = ArrayList<Mat>()
        val allImagePoints = ArrayList<Mat>()

        try {
            for (i in allCharucoCorners.indices) {
                val corners = allCharucoCorners[i]
                val ids = allCharucoIds[i]

                if (corners.total() > 0) {
                    val objPoints = MatOfPoint3f()
                    val imgPoints = MatOfPoint2f()

                    try {
                        board.matchImagePoints(listOf(corners), ids, objPoints, imgPoints)

                        if (objPoints.total() > 4) {
                            allObjectPoints.add(objPoints)
                            allImagePoints.add(imgPoints)
                        } else {
                            objPoints.release()
                            imgPoints.release()
                        }
                    } catch (e: Exception) {
                        Log.e("CalibrationManager", "Error matching points frame $i", e)
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

            val rms =
                Calib3d.calibrateCamera(allObjectPoints, allImagePoints, size, cameraMat, distCoeffs, rvecs, tvecs)

            if (rms > 0) {
                this.cameraMatrix?.release()
                this.cameraMatrix = cameraMat
                this.distortionCoeffs?.release()
                this.distortionCoeffs = distCoeffs
                saveCalibrationToJson(cameraMat, distCoeffs, Pair(size.width.toInt(), size.height.toInt()))
            }

            // Cleanup rotation and translation vectors as they are huge and not saved
            rvecs.forEach { it.release() }
            tvecs.forEach { it.release() }

            return rms
        } catch (e: Exception) {
            Log.e("CalibrationManager", "Fatal error in calibrateCamera", e)
            return -3.0
        } finally {
            allObjectPoints.forEach { it.release() }
            allImagePoints.forEach { it.release() }
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