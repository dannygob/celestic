package com.example.celestic.manager

import com.example.celestic.models.FiducialMarker
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters
import org.opencv.objdetect.Dictionary

/**
 * Base abstracta para gestores de marcadores fiduciales.
 * Centraliza la lógica de detección para evitar duplicación.
 */
abstract class BaseMarkerManager(private val dictionary: Dictionary) {

    private val parameters by lazy { DetectorParameters() }
    private val detector by lazy { ArucoDetector(dictionary, parameters) }

    fun detectMarkers(image: Mat): List<FiducialMarker> {
        val corners = ArrayList<Mat>()
        val ids = MatOfInt()
        val rejected = ArrayList<Mat>()

        try {
            detector.detectMarkers(image, corners, ids, rejected)

            val markers = mutableListOf<FiducialMarker>()
            if (ids.total() > 0) {
                val idsArray = ids.toArray()
                for (i in idsArray.indices) {
                    // El Mat de corners ahora es propiedad del FiducialMarker
                    markers.add(FiducialMarker(idsArray[i], corners[i]))
                }
            }
            return markers
        } catch (e: Exception) {
            // En caso de error, liberar los Mats que se hayan podido crear en corners
            corners.forEach { it.release() }
            return emptyList()
        } finally {
            ids.release()
            rejected.forEach { it.release() }
        }
    }
}
