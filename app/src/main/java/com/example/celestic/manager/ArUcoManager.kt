package com.example.celestic.manager

import com.example.celestic.models.FiducialMarker
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect
import javax.inject.Inject

/**
 * ArUcoManager handles the detection of ArUco markers using OpenCV.
 *
 * This class wraps the native OpenCV ArUco detection pipeline and returns
 * structured marker data (IDs + corner coordinates) in the form of FiducialMarker objects.
 *
 * It is used for:
 * - Marker‑based tracking
 * - Calibration workflows
 * - Augmented reality overlays
 * - Pose estimation (when combined with camera calibration)
 */
class ArUcoManager @Inject constructor() {

    /**
     * Detects ArUco markers in the given image.
     *
     * @param image Input image (Mat) in which markers will be detected.
     *              The image should ideally be grayscale or RGB/RGBA.
     *
     * @return A list of FiducialMarker objects, each containing:
     *         - The marker ID
     *         - The 4 corner points of the detected marker
     *
     * Detection steps:
     * 1. Loads the predefined dictionary (DICT_6X6_250).
     * 2. Creates a detector with default parameters.
     * 3. Runs marker detection on the input image.
     * 4. Converts OpenCV results into your custom FiducialMarker model.
     */
    fun detectMarkers(image: Mat): List<FiducialMarker> {
        // Load the ArUco dictionary (6x6 markers, 250 IDs)
        val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250)

        val corners = ArrayList<Mat>()
        val ids = MatOfInt()

        // Create detector with default parameters
        val detectorParams = org.opencv.objdetect.DetectorParameters()
        val detector = ArucoDetector(dictionary, detectorParams)

        // Perform detection
        detector.detectMarkers(image, corners, ids)

        val markers = mutableListOf<FiducialMarker>()

        if (ids.total() > 0) {
            val idsArray = IntArray(ids.total().toInt())
            ids.get(0, 0, idsArray)

            // Ensure safe indexing in case OpenCV returns mismatched arrays
            val count = minOf(idsArray.size, corners.size)

            for (i in 0 until count) {
                markers.add(FiducialMarker(idsArray[i], corners[i]))
            }
        }

        return markers
    }
}
