package com.example.celestic.opencv

import android.util.Log
import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
import com.example.celestic.viewmodel.MarkerType
import com.example.celestic.viewmodel.SharedViewModel
import org.opencv.calib3d.Calib3d
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.Objdetect
import org.opencv.video.Video

class FrameAnalyzer(private val sharedViewModel: SharedViewModel) {

    data class Marker(val id: Int, val corners: Mat)

    data class AnalysisResult(
        val contours: List<MatOfPoint>,
        val annotatedMat: Mat,
        val markers: List<Marker>
    )

    private var prevGrayMat: Mat? = null
    private val arucoManager = ArUcoManager()
    private val aprilTagManager = AprilTagManager()

    fun analyze(mat: Mat): AnalysisResult {
        val grayMat = Mat()
        val thresholdedImage = Mat()
        val edges = Mat()

        try {
            // Preprocesamiento
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.GaussianBlur(grayMat, grayMat, org.opencv.core.Size(5.0, 5.0), 0.0)

            val thresholded = applyAdaptiveThresholding(grayMat)
            val contours = findContours(thresholded)
            val filteredContours = filterContours(contours, 100.0)
            val deformations = detectDeformations(filteredContours)
            val holes = detectHoles(grayMat)

            prevGrayMat?.let { detectDeformationsWithOpticalFlow(it, grayMat) }
            prevGrayMat = grayMat.clone()

            // Detección de marcadores
            val markers = when (sharedViewModel.markerType.value) {
                MarkerType.ARUCO -> arucoManager.detectMarkers(mat).map { Marker(it.id, it.corners) }
                MarkerType.APRILTAG -> aprilTagManager.detectMarkers(mat)
                    .map { Marker(it.id, it.corners) }
            }

            // Dibujar resultados en una copia
            val annotatedMat = mat.clone()

            // Forzamos el tipo List<MatOfPoint> para drawContours
            val contoursToDraw: MutableList<MatOfPoint> = ArrayList()
            contoursToDraw.addAll(filteredContours)

            val deformationsToDraw: MutableList<MatOfPoint> = ArrayList()
            deformationsToDraw.addAll(deformations)

            if (contoursToDraw.isNotEmpty()) {
                Imgproc.drawContours(annotatedMat, contoursToDraw, -1, Scalar(0.0, 255.0, 0.0), 2)
            }
            if (deformationsToDraw.isNotEmpty()) {
                Imgproc.drawContours(
                    annotatedMat,
                    deformationsToDraw,
                    -1,
                    Scalar(255.0, 0.0, 0.0),
                    2
                )
            }
            
            for (i in 0 until holes.cols()) {
                val circle = holes.get(0, i)
                val center = org.opencv.core.Point(circle[0], circle[1])
                val radius = circle[2].toInt()
                Imgproc.circle(annotatedMat, center, radius, Scalar(0.0, 0.0, 255.0), 2)
            }

            if (markers.isNotEmpty()){
                val cornersList = markers.map { it.corners }
                Objdetect.drawDetectedMarkers(annotatedMat, cornersList, Mat())
            }

            return AnalysisResult(contours, annotatedMat, markers)

        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Error al analizar frame", e)
            return AnalysisResult(emptyList(), mat, emptyList())
        } finally {
            grayMat.release()
            thresholdedImage.release()
            edges.release()
        }
    }

    fun applyCalibration(image: Mat, cameraMatrix: Mat, distortionCoeffs: Mat): Mat {
        val undistortedImage = Mat()
        Calib3d.undistort(image, undistortedImage, cameraMatrix, distortionCoeffs)
        return undistortedImage
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

    fun detectDeformationsWithOpticalFlow(prevFrame: Mat, nextFrame: Mat): MatOfPoint2f {
        MatOfPoint2f()
        // GoodFeaturesToTrack expects MatOfPoint for Corners but we can adapt it.
        // Actually it returns MatOfPoint.
        val corners = MatOfPoint()
        Imgproc.goodFeaturesToTrack(prevFrame, corners, 100, 0.3, 7.0)

        val prevPts2f = MatOfPoint2f(*corners.toArray())
        val nextPts2f = MatOfPoint2f()
        val status = MatOfByte()
        val err = MatOfFloat()
        if (prevPts2f.total() > 0) {
            Video.calcOpticalFlowPyrLK(prevFrame, nextFrame, prevPts2f, nextPts2f, status, err)
        }
        return nextPts2f
    }
}