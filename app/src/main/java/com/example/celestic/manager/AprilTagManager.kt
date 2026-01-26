package com.example.celestic.manager

import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Objdetect

class AprilTagManager {

    data class Marker(val id: Int, val corners: Mat)

    fun detectMarkers(image: Mat): List<Marker> {
        // En OpenCV 4.x, AprilTag se detecta de forma más estable usando ArucoDetector
        // con el diccionario específico de AprilTag.
        val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_APRILTAG_36h11)
        val parameters = DetectorParameters()
        val detector = ArucoDetector(dictionary, parameters)

        val corners = ArrayList<Mat>()
        val ids = MatOfInt()
        val rejected = ArrayList<Mat>()

        detector.detectMarkers(image, corners, ids, rejected)

        val markers = mutableListOf<Marker>()
        if (ids.total() > 0) {
            val idsArray = ids.toArray()
            for (i in idsArray.indices) {
                markers.add(Marker(idsArray[i], corners[i]))
            }
        }
        return markers
    }
}
