package com.example.celestic.opencv

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import com.example.celestic.models.DetectionItem

object HoleDetector {

    fun detectHoles(frame: Mat, gray: Mat): List<DetectionItem> {
        val circles = Mat()
        val detectedHoles = mutableListOf<DetectionItem>()

        Imgproc.HoughCircles(
            gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, gray.rows() / 4.0,
            100.0, 30.0, 10, 50
        )

        if (circles.cols() > 0) {
            for (i in 0 until circles.cols()) {
                val data = circles.get(0, i)
                val center = Point(data[0], data[1])
                val radius = data[2].toInt()
                val diameter = radius * 2

                // Dibujar el agujero detectado
                Imgproc.circle(frame, center, radius, Scalar(0.0, 255.0, 0.0), 2)
                Imgproc.circle(frame, center, 3, Scalar(0.0, 0.0, 255.0), 2)

                // Agregar el agujero a la lista de detecciones
                detectedHoles.add(DetectionItem(type = "agujero", position = center, diameter = diameter))
            }
        }
        return detectedHoles
    }
}
