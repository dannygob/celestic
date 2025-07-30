package com.example.celestic.manager


import org.opencv.aruco.Aruco
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.objdetect.Dictionary
import java.util.Dictionary

class ArUcoManager {

    fun detectMarkers(image: Mat): List<Mat> {
        val dictionary: Dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250)
        val corners = ArrayList<Mat>()
        val ids = MatOfInt()
        Aruco.detectMarkers(image, dictionary, corners, ids)
        return corners
    }
}
