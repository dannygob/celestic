package com.example.celestic.ui.screen

import android.util.Log
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class FrameAnalyzer {

    data class AnalysisResult(
        val contours: List<MatOfPoint>,
        val annotatedMat: Mat,
    )

    fun analyze(mat: Mat): AnalysisResult {
        val grayMat = Mat()
        val edges = Mat()
        val contours = mutableListOf<MatOfPoint>()

        try {
            // Preprocesamiento
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_GRAY2BGR)
            Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.GaussianBlur(grayMat, grayMat, Size(5.0, 5.0), 0.0)

            val edges = detectEdges(grayMat)
            val contours = findContours(edges)

            // Dibujar resultados en una copia
            val annotatedMat = mat.clone()
            Imgproc.drawContours(annotatedMat, contours, -1, Scalar(0.0, 255.0, 0.0), 2)

            return AnalysisResult(contours, annotatedMat)

        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Error al analizar frame", e)
            return AnalysisResult(emptyList(), mat)
        } finally {
            grayMat.release()
            edges.release()
        }
    }

    fun detectEdges(image: Mat): Mat {
        val edges = Mat()
        Imgproc.Canny(image, edges, 100.0, 200.0)
        return edges
    }

    fun applyCalibration(image: Mat, cameraMatrix: Mat, distortionCoeffs: Mat): Mat {
        val undistortedImage = Mat()
        Imgproc.undistort(image, undistortedImage, cameraMatrix, distortionCoeffs)
        return undistortedImage
    }

    fun extractDimensionsFromContours(contours: List<MatOfPoint>): List<Double> {
        val dimensions = mutableListOf<Double>()
        for (contour in contours) {
            val rect = Imgproc.boundingRect(contour)
            dimensions.add(rect.width.toDouble())
            dimensions.add(rect.height.toDouble())
        }
        return dimensions
    }

    private fun findContours(image: Mat): List<MatOfPoint> {
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            image,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )
        return contours
    }
}