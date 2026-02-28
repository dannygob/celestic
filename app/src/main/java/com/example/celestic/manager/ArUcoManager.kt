package com.example.celestic.manager

import com.example.celestic.models.FiducialMarker
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect

import javax.inject.Inject

/**
 * ArUcoManager handles detection of ArUco markers using OpenCV.
 * It wraps the native detection logic and returns structured marker data.
 */
class ArUcoManager @Inject constructor() {

    /**
     * Detects ArUco markers in the given image and returns a list of FiducialMarker objects.
     */
    fun detectMarkers(image: Mat): List<FiducialMarker> {
        val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_6X6_250)
        val corners = ArrayList<Mat>()
        val ids = MatOfInt()

        // Perform marker detection
        val detectorParams = org.opencv.objdetect.DetectorParameters()
        val detector = ArucoDetector(dictionary, detectorParams)
        detector.detectMarkers(image, corners, ids)

        val markers = mutableListOf<FiducialMarker>()
        if (ids.total() > 0) {
            val idsArray = IntArray(ids.total().toInt())
            ids.get(0, 0, idsArray)

            // Ensure safe indexing in case of mismatch
            val count = minOf(idsArray.size, corners.size)
            for (i in 0 until count) {
                markers.add(FiducialMarker(idsArray[i], corners[i]))
            }
        }

        return markers
    }
}