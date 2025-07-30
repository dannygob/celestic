package com.example.celestic.opencv

import android.R
import android.util.Log
import androidx.compose.ui.geometry.Size
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class FrameAnalyzer {

    data class AnalysisResult(
        val contours: List<T>,
        val annotatedMat: Mat,
    )

    private var prevGrayMat: Mat? = null

    fun analyze(mat: Mat): AnalysisResult {
        val grayMat = Mat()
        val thresholdedImage = Mat()
        val edges = Mat()
        mutableListOf<MatOfPoint>()
        mutableListOf<MatOfPoint>()
        Mat()

        try {
            // Preprocesamiento
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.GaussianBlur(grayMat, grayMat, Size(5.0, 5.0), 0.0)

            val thresholdedImage = applyAdaptiveThresholding(grayMat)
            val markers = applyWatershed(thresholdedImage)
            val contours = findContours(markers)
            val filteredContours = filterContours(contours, 100.0)
            val deformations = detectDeformations(filteredContours)
            val holes = detectHoles(grayMat)
            val template = Utils.loadResource(context, R.drawable.countersink_template)
            detectCountersinks(grayMat, template)
            prevGrayMat?.let { detectDeformationsWithOpticalFlow(it, grayMat) }
            prevGrayMat = grayMat.clone()
            val scale = calibrationManager.getScaleFactor(1.0)
            calculateMeasurements(filteredContours, scale)

            // Dibujar resultados en una copia
            val annotatedMat = mat.clone()
            Imgproc.drawContours(annotatedMat, filteredContours, -1, Scalar(0.0, 255.0, 0.0), 2)
            Imgproc.drawContours(annotatedMat, deformations, -1, Scalar(255.0, 0.0, 0.0), 2)
            for (i in 0 until holes.cols()) {
                val circle = holes.get(0, i)
                val center = Point(circle[0], circle[1])
                val radius = circle[2].toInt()
                Imgproc.circle(annotatedMat, center, radius, Scalar(0.0, 0.0, 255.0), 2)
            }
            // TODO: Draw countersinks

            return AnalysisResult(contours, annotatedMat)

        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Error al analizar frame", e)
            return AnalysisResult(emptyList(), mat)
        } finally {
            grayMat.release()
            thresholdedImage.release()
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

    fun detectHoles(image: Mat): Mat {
        val circles = Mat()
        Imgproc.HoughCircles(
            image,
            circles,
            Imgproc.HOUGH_GRADIENT,
            1.0,
            image.rows().toDouble() / 8,
            200.0,
            100.0,
            0,
            0
        )
        return circles
    }

    fun detectDeformations(contours: List<MatOfPoint>): List<MatOfPoint> {
        val deformations = mutableListOf<MatOfPoint>()
        for (contour in contours) {
            val approx = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*contour.toArray())
            Imgproc.approxPolyDP(contour2f, approx, 0.04 * Imgproc.arcLength(contour2f, true), true)
            if (approx.toArray().size > 4) {
                deformations.add(MatOfPoint(*approx.toArray()))
            }
        }
        return deformations
    }

    fun applyAdaptiveThresholding(image: Mat): Mat {
        val thresholdedImage = Mat()
        Imgproc.adaptiveThreshold(
            image,
            thresholdedImage,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            11,
            2.0
        )
        return thresholdedImage
    }

    fun filterContours(contours: List<MatOfPoint>, minArea: Double): List<MatOfPoint> {
        val filteredContours = mutableListOf<MatOfPoint>()
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > minArea) {
                filteredContours.add(contour)
            }
        }
        return filteredContours
    }

    fun applyWatershed(image: Mat): Mat {
        val markers = Mat()
        Imgproc.connectedComponents(image, markers)
        Imgproc.watershed(image, markers)
        return markers
    }

    fun detectCountersinks(image: Mat, template: Mat): Mat {
        val result = Mat()
        Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED)
        return result
    }

    fun detectDeformationsWithOpticalFlow(prevFrame: Mat, nextFrame: Mat): MatOfPoint2f {
        val prevPts = MatOfPoint2f()
        Imgproc.goodFeaturesToTrack(prevFrame, prevPts, 100, 0.3, 7.0)
        val nextPts = MatOfPoint2f()
        val status = MatOfByte()
        val err = MatOfFloat()
        Video.calcOpticalFlowPyrLK(prevFrame, nextFrame, prevPts, nextPts, status, err)
        return nextPts
    }

    fun calculateMeasurements(contours: List<MatOfPoint>, scale: Double): List<Double> {
        val measurements = mutableListOf<Double>()
        for (contour in contours) {
            val rect = Imgproc.boundingRect(contour)
            measurements.add(rect.width * scale)
            measurements.add(rect.height * scale)
        }
        return measurements
    }
}