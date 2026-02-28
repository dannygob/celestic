package com.example.celestic.manager


import com.example.celestic.models.FiducialMarker
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect

/**
 * AprilTagManager gestiona la detección de etiquetas AprilTag físicas
 * y la generación de etiquetas virtuales para elementos detectados.
 */
class AprilTagManager {

    // OpenCV ArucoDetector configurado para AprilTag 36h11
    private val detector: ArucoDetector by lazy {
        val dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_APRILTAG_36h11)
        val params = org.opencv.objdetect.DetectorParameters()
        ArucoDetector(dictionary, params)
    }

    /**
     * Inicialización opcional para configuración futura.
     */
    fun init() {
    }

    /**
     * Detecta etiquetas físicas AprilTag en una imagen.
     */
    fun detectMarkers(image: Mat): List<FiducialMarker> {
        val corners = ArrayList<Mat>()
        val ids = MatOfInt()

        detector.detectMarkers(image, corners, ids)

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
     * Genera una etiqueta virtual para un elemento detectado.
     * @param featureId ID único del elemento
     * @param position Coordenadas (x, y) del centro del elemento
     * @param size Tamaño visual del tag (por defecto 20 px)
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
     * Libera los recursos (ya no es necesario para ArucoDetector de OpenCV).
     */
    fun close() {
        // No-op
    }
}