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

/**
 * Manages camera calibration using OpenCV's ChArUco board detection.
 *
 * Responsibilities:
 * - Load and save calibration data (camera matrix + distortion coefficients)
 * - Collect calibration frames using ChArUco markers
 * - Run full camera calibration
 * - Estimate scale factors and distances using calibration results
 * - Detect low‑end devices and adjust processing limits
 */
@javax.inject.Singleton
class CalibrationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Intrinsic camera matrix (3x3) loaded from calibration or computed during calibration. */
    var cameraMatrix: Mat? = null

    /** Lens distortion coefficients (k1, k2, p1, p2, k3). */
    var distortionCoeffs: Mat? = null

    /** Resolution used during calibration (width, height). */
    var resolution: Pair<Int, Int>? = null

    /** Timestamp of the last calibration. */
    var calibrationDate: String? = null

    /** File where calibration data is stored in JSON format. */
    private val calibrationFile = File(context.filesDir, "config/calibration.json")

    init {
        loadCalibration()
    }

    /**
     * Loads calibration data from disk if available.
     * @return true if calibration was successfully loaded.
     */
    private fun loadCalibration(): Boolean {
        return try {
            if (!calibrationFile.exists()) return false

            val json = JSONObject(FileInputStream(calibrationFile).bufferedReader().use { it.readText() })

            // Parse camera matrix
            val matrixData = json.getString("cameraMatrix")
            cameraMatrix?.release()
            cameraMatrix = stringToMat(matrixData, 3, 3)

            // Parse distortion coefficients
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

    /**
     * Converts a serialized matrix string (OpenCV dump format) into a Mat object.
     */
    private fun stringToMat(data: String, rows: Int, cols: Int): Mat {
        val mat = Mat(rows, cols, CvType.CV_64F)
        val cleanData = data.replace("[", "").replace("]", "").replace(";", "").trim()
        val values = cleanData.split(Regex("\\s*,\\s*|\\s+")).filter { it.isNotBlank() }
        val doubleValues = values.map { it.toDouble() }.toDoubleArray()
        mat.put(0, 0, *doubleValues)
        return mat
    }

    /**
     * Computes the scale factor (mm per pixel) based on calibration and distance.
     * If calibration is missing, returns a fallback approximation.
     */
    fun getScaleFactor(distanceMm: Double): Double {
        val mat = cameraMatrix ?: return 0.264 // Fallback: ~96 DPI

        val fx = mat.get(0, 0)[0]
        val scale = distanceMm / fx

        return if (scale.isNaN() || scale.isInfinite() || scale == 0.0) 0.264 else scale
    }

    /**
     * Estimates the distance from the camera to a marker using pinhole projection.
     *
     * Z = (real_size_mm * focal_length_px) / detected_size_px
     */
    fun estimateDistance(detectedMarkerWidthPx: Double, realMarkerSizeMm: Double): Double {
        val mat = cameraMatrix ?: return 1000.0 // Default 1 meter
        val fx = mat.get(0, 0)[0]
        return (realMarkerSizeMm * fx) / detectedMarkerWidthPx
    }

    // ===== HARDWARE PROFILING =====

    /**
     * Detects whether the device is low‑end based on CPU cores and RAM.
     * Used to reduce calibration frame count on weaker devices.
     */
    val isLowEndDevice: Boolean by lazy {
        val cores = Runtime.getRuntime().availableProcessors()
        val totalMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)

        val actManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val ramGB = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)

        cores <= 4 || ramGB < 3.5 || totalMemoryMB < 256
    }

    /** Maximum number of calibration frames allowed based on device performance. */
    val maxFrames: Int by lazy {
        if (isLowEndDevice) 10 else 20
    }

    // ===== CHARUCO CALIBRATION DATA =====

    private val allCharucoCorners = mutableListOf<Mat>()
    private val allCharucoIds = mutableListOf<Mat>()
    private var imageSize: Size? = null

    private val dictionary by lazy { Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250) }
    private val board by lazy { CharucoBoard(Size(5.0, 7.0), 0.04f, 0.02f, dictionary) }
    private val detector by lazy { CharucoDetector(board) }

    /** Returns how many calibration frames have been captured. */
    fun getCapturedFramesCount(): Int = allCharucoCorners.size

    /** Clears all stored calibration frames. */
    fun resetData() {
        allCharucoCorners.forEach { it.release() }
        allCharucoIds.forEach { it.release() }
        allCharucoCorners.clear()
        allCharucoIds.clear()
    }

    /**
     * Adds a calibration frame by detecting ChArUco corners.
     * Returns true if the frame contains enough valid corners.
     */
    fun addCalibrationFrame(image: Mat): Boolean {
        if (allCharucoCorners.size >= maxFrames) return false

        val gray = Mat()
        org.opencv.imgproc.Imgproc.cvtColor(image, gray, org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY)

        val charucoCorners = Mat()
        val charucoIds = Mat()
        val markerCorners = ArrayList<Mat>()
        val markerIds = Mat()

        try {
            detector.detectBoard(gray, charucoCorners, charucoIds, markerCorners, markerIds)

            return if (charucoCorners.total() > 4) {
                allCharucoCorners.add(charucoCorners.clone())
                allCharucoIds.add(charucoIds.clone())
                imageSize = gray.size()
                true
            } else false

        } catch (e: Exception) {
            Log.e("CalibrationManager", "Error processing calibration frame", e)
            return false

        } finally {
            gray.release()
            charucoCorners.release()
            charucoIds.release()
            markerIds.release()
            markerCorners.forEach { it.release() }
        }
    }

    /**
     * Runs full camera calibration using all collected ChArUco frames.
     * @return RMS reprojection error, or negative value on failure.
     */
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

            val rms = Calib3d.calibrateCamera(
                allObjectPoints, allImagePoints, size,
                cameraMat, distCoeffs, rvecs, tvecs
            )

            if (rms > 0) {
                this.cameraMatrix?.release()
                this.cameraMatrix = cameraMat

                this.distortionCoeffs?.release()
                this.distortionCoeffs = distCoeffs

                saveCalibrationToJson(cameraMat, distCoeffs, Pair(size.width.toInt(), size.height.toInt()))
            }

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

    /**
     * Saves calibration results to a JSON file for persistent storage.
     */
    fun saveCalibrationToJson(cameraMatrix: Mat, distortionCoeffs: Mat, resolution: Pair<Int, Int>) {
        val json = JSONObject()
        json.put("cameraMatrix", cameraMatrix.dump())
        json.put("distortionCoeffs", distortionCoeffs.dump())
        json.put("resolution_width", resolution.first)
        json.put("resolution_height", resolution.second)
        json.put(
            "calibrationDate",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        calibrationFile.parentFile?.mkdirs()
        calibrationFile.writeText(json.toString())
    }
}
