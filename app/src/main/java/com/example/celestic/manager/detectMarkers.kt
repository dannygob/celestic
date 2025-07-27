package com.example.celestic.manager

import org.opencv.aruco.*
import org.opencv.core.Mat
import org.opencv.core.MatOfInt

object ArUcoManager {

    fun detectMarkers(mat: Mat): List<Int> {
        val dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50)
        val corners = mutableListOf<Mat>()
        val ids = Mat()

        Aruco.detectMarkers(mat, dictionary, corners, ids)

        if (ids.empty()) return emptyList()
        val idsList = MatOfInt()
        ids.convertTo(idsList, ids.type())
        return idsList.toArray().toList()
    }
}