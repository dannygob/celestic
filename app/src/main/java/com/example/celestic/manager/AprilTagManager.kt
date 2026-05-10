package com.example.celestic.manager

import com.example.celestic.models.FiducialMarker
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect
import javax.inject.Inject

/**
 * Manages the detection of physical AprilTag markers and the generation
 * of virtual fiducial markers used for visualization or feature tracking.
 *
 * This class wraps OpenCV's ArUco/AprilTag detector and provides a clean
 * interface for marker detection and virtual tag creation.
 */
class AprilTagManager @Inject constructor() {

    /**
     * Lazily initialized OpenCV ArucoDetector configured for AprilTag 36h11.
     *
     * DICT_APRILTAG_36h11 is one of the most common AprilTag families used
     * in robotics, AR, and computer vision applications.
     */
    private val detector: ArucoDetector by lazy {
        val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_APRILTAG_36h11)
        val params = org.opencv.objdetect.DetectorParameters()
        ArucoDetector(dictionary, params)
    }

    /**
     * Optional initialization hook for future configuration needs.
     * Currently unused but kept for extensibility.
     */
    fun init() {
        // No initialization required at the moment
    }

    /**
     * Detects physical AprilTag markers in the provided image.
     *
     * @param image Input image (RGBA Mat) where markers will be detected.
     * @return A list of FiducialMarker objects containing marker IDs and corner coordinates.
     *
     * Steps:
     * - Converts the image to grayscale for faster processing.
     * - Runs the ArUco/AprilTag detector.
     * - Extracts marker IDs and corner positions.
     */
    fun detectMarkers(image: Mat): List<FiducialMarker> {
        // Convert too grayscale to reduce CPU load
        val gray = Mat()
        org.opencv.imgproc.Imgproc.cvtColor(image, gray, org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY)

        val corners = ArrayList<Mat>()
        val ids = MatOfInt()

        detector.detectMarkers(gray, corners, ids)
        gray.release()

        val markers = mutableListOf<FiducialMarker>()

        if (ids.total() > 0) {
            val idsArray = IntArray(ids.total().toInt())
            ids.get(0, 0, idsArray)

            val count = minOf(idsArray.size, corners.size)
            for (i in 0 until count) {
                markers.add(FiducialMarker(idsArray[i], corners[i]))
            }
        }

        return markers
    }

    /**
     * Generates a virtual fiducial marker for a detected feature.
     *
     * This is useful for:
     * - Overlaying markers in augmented reality
     * - Visual debugging
     * - Representing detected features as synthetic tags
     *
     * @param featureId Unique ID for the virtual marker.
     * @param position Center position of the marker (x, y).
     * @param size Visual size of the marker in pixels (default: 20px).
     * @return A FiducialMarker containing synthetic corner coordinates.
     */
    fun generateVirtualTagForFeature(
        featureId: Int,
        position: Pair<Double, Double>,
        size: Double = 20.0
    ): FiducialMarker {
        val (x, y) = position
        val half = size / 2.0

        val cornersMat = Mat(1, 4, org.opencv.core.CvType.CV_32FC2)
        val floats = floatArrayOf(
            (x - half).toFloat(), (y - half).toFloat(),
            (x + half).toFloat(), (y - half).toFloat(),
            (x + half).toFloat(), (y + half).toFloat(),
            (x - half).toFloat(), (y + half).toFloat()
        )
        cornersMat.put(0, 0, floats)

        return FiducialMarker(
            id = featureId,
            corners = cornersMat
        )
    }

    /**
     * Releases resources if needed.
     * Currently, a no-op because ArucoDetector does not require manual cleanup.
     */
    fun close() {
        // No resources to release
    }
}
