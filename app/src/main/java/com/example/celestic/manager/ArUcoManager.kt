package com.example.celestic.manager

import org.opencv.aruco.Aruco
import org.opencv.aruco.Dictionary
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import java.util.ArrayList

class ArUcoManager {

    fun detectMarkers(image: Mat): List<Mat> {
        val dictionary: Dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250)
        val corners = ArrayList<Mat>()
        val ids = MatOfInt()
        Aruco.detectMarkers(image, dictionary, corners, ids)
        return corners
    }
}
