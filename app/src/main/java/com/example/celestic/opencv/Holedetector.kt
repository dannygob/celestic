package com.example.celestic.opencv

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import com.example.celestic.models.DetectionItem

object HoleDetector {

    fun detectHoles(grayFrame: Mat): List<DetectionItem> { // Renamed gray to grayFrame for clarity, removed unused 'frame'
        val circles = Mat()
        val detectedHoles = mutableListOf<DetectionItem>()

        Imgproc.HoughCircles(
            grayFrame, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, grayFrame.rows() / 4.0, // Use grayFrame.rows()
            100.0, 30.0, 10, 50 // Parameters might need tuning
        )

        if (circles.cols() > 0) {
            for (i in 0 until circles.cols()) {
                val data = circles.get(0, i)
                val center = Point(data[0], data[1])
                val radius = data[2].toInt()
                val diameter = radius * 2

                // Agregar el agujero a la lista de detecciones
                // Drawing is now handled by CameraHandler
                detectedHoles.add(DetectionItem(type = "agujero", position = center, diameter = diameter))
            }
        }
        circles.release() // Release the Mat object when done
        return detectedHoles
    }
}
