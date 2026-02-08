package com.example.celestic.opencv

import android.util.Log
import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
import com.example.celestic.viewmodel.MarkerType
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
import javax.inject.Inject

class FrameAnalyzer @Inject constructor(
    private val arucoManager: ArUcoManager,
    private val aprilTagManager: AprilTagManager
) {

    data class Marker(val id: Int, val corners: Mat)
    data class Hole(
        val center: org.opencv.core.Point,
        val radius: Double,
        val hasAlodine: Boolean = false
    )

    data class Countersink(
        val center: org.opencv.core.Point,
        val innerRadius: Double,
        val outerRadius: Double
    )

    data class Scratch(
        val startPoint: org.opencv.core.Point,
        val endPoint: org.opencv.core.Point,
        val length: Double
    )

    data class AlodineHalo(
        val center: org.opencv.core.Point,
        val radius: Double,
        val intensityInfo: Double
    )
    
    data class AnalysisResult(
        val contours: List<MatOfPoint>,
        val annotatedMat: Mat,
        val markers: List<Marker>,
        val holes: List<Hole>,
        val orientation: com.example.celestic.models.enums.Orientation,
        val countersinks: List<Countersink>,
        val scratches: List<Scratch>
    )

    private var prevGrayMat: Mat? = null

    fun analyze(mat: Mat, markerType: MarkerType): AnalysisResult {
        val grayMat = Mat()
        val thresholdedImage = Mat()
        val edges = Mat()

        try {
            // Preprocessing
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.GaussianBlur(grayMat, grayMat, org.opencv.core.Size(5.0, 5.0), 0.0)

            val thresholded = applyAdaptiveThresholding(grayMat)
            val contours = findContours(thresholded)
            val filteredContours = filterContours(contours, 100.0)
            val deformations = detectDeformations(filteredContours)
            val holesMat = detectHoles(grayMat)

            // Raw holes for internal logic
            val allCircles = ArrayList<Hole>()
            for (i in 0 until holesMat.cols()) {
                val circle = holesMat.get(0, i)
                val center = org.opencv.core.Point(circle[0], circle[1])
                val radius = circle[2]

                // Check Alodine (Color Analysis on outer ring)
                val hasAlodine = checkAlodine(mat, center, radius)

                allCircles.add(Hole(center, radius, hasAlodine))
            }

            // Distinguish Holes vs Countersinks
            val (simpleHoles, countersinks) = identifyCountersinks(allCircles)

            // Detect Scratches using edges
            Imgproc.Canny(grayMat, edges, 50.0, 150.0)
            val scratches = detectScratches(edges, filteredContours)

            // Detect Orientation
            val markers = when (markerType) {
                MarkerType.ARUCO -> arucoManager.detectMarkers(mat)
                    .map { Marker(it.id, it.corners) }

                MarkerType.APRILTAG -> aprilTagManager.detectMarkers(mat)
                    .map { Marker(it.id, it.corners) }
            }
            val orientation = detectOrientation(markers)

            prevGrayMat?.let { detectDeformationsWithOpticalFlow(it, grayMat) }
            prevGrayMat = grayMat.clone()

            // Draw results on a copy
            val annotatedMat = mat.clone()

            // Contours
            Imgproc.drawContours(annotatedMat, filteredContours, -1, Scalar(0.0, 255.0, 0.0), 2)

            // Deformations
            Imgproc.drawContours(annotatedMat, deformations, -1, Scalar(255.0, 0.0, 0.0), 2)

            // Holes
            for (hole in simpleHoles) {
                val color = if (hole.hasAlodine) Scalar(0.0, 165.0, 255.0) else Scalar(
                    0.0,
                    0.0,
                    255.0
                ) // Orange if it has Alodine
                Imgproc.circle(annotatedMat, hole.center, hole.radius.toInt(), color, 2)
            }

            // Avellanados (Countersinks)
            for (cs in countersinks) {
                Imgproc.circle(
                    annotatedMat,
                    cs.center,
                    cs.outerRadius.toInt(),
                    Scalar(0.0, 255.0, 255.0),
                    2
                ) // Yellow
                Imgproc.circle(
                    annotatedMat,
                    cs.center,
                    cs.innerRadius.toInt(),
                    Scalar(0.0, 165.0, 255.0),
                    1
                )
            }

            // Scratches
            for (scratch in scratches) {
                Imgproc.line(
                    annotatedMat,
                    scratch.startPoint,
                    scratch.endPoint,
                    Scalar(255.0, 0.0, 255.0),
                    2
                )
            }

            if (markers.isNotEmpty()){
                val cornersList = markers.map { it.corners }
                Objdetect.drawDetectedMarkers(annotatedMat, cornersList, Mat())
            }

            // Text info on screen
            Imgproc.putText(
                annotatedMat,
                "Orient: $orientation",
                org.opencv.core.Point(50.0, 50.0),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                1.0,
                Scalar(255.0, 255.0, 0.0),
                2
            )

            return AnalysisResult(
                filteredContours,
                annotatedMat,
                markers,
                simpleHoles,
                orientation,
                countersinks,
                scratches
            )

        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Error analyzing frame", e)
            return AnalysisResult(
                emptyList(),
                mat,
                emptyList(),
                emptyList(),
                com.example.celestic.models.enums.Orientation.UNKNOWN,
                emptyList(),
                emptyList()
            )
        } finally {
            grayMat.release()
            thresholdedImage.release()
            edges.release()
        }
    }

    private fun checkAlodine(image: Mat, center: org.opencv.core.Point, radius: Double): Boolean {
        // Analyze ring between radius and radius*1.5
        // Convert to HSV and look for saturation (Alodine is gold/yellow, aluminum is gray)
        try {
            val roiSize = (radius * 3).toInt()
            val x = (center.x - roiSize / 2).toInt().coerceAtLeast(0)
            val y = (center.y - roiSize / 2).toInt().coerceAtLeast(0)
            val w = roiSize.coerceAtMost(image.width() - x)
            val h = roiSize.coerceAtMost(image.height() - y)

            if (w <= 0 || h <= 0) return false

            val roiRect = org.opencv.core.Rect(x, y, w, h)
            val roi = Mat(image, roiRect)

            val hsvRoi = Mat()
            Imgproc.cvtColor(roi, hsvRoi, Imgproc.COLOR_RGB2HSV) // Assuming RGB/BGR input

            val saturation = Mat()
            org.opencv.core.Core.extractChannel(hsvRoi, saturation, 1) // Canal S

            val meanSat = org.opencv.core.Core.mean(saturation).`val`[0]

            // Cleanup
            roi.release()
            hsvRoi.release()
            saturation.release()

            // Empirical threshold: 40 to distinguish metallic gray from soft gold
            return meanSat > 40.0

        } catch (e: Exception) {
            return false
        }
    }

    // Identify pairs of concentric circles
    private fun identifyCountersinks(circles: List<Hole>): Pair<List<Hole>, List<Countersink>> {
        val isolatedHoles = ArrayList<Hole>()
        val countersinks = ArrayList<Countersink>()
        val usedIndices = HashSet<Int>()

        for (i in circles.indices) {
            if (usedIndices.contains(i)) continue

            var matchFound = false
            for (j in circles.indices) {
                if (i == j || usedIndices.contains(j)) continue

                val c1 = circles[i]
                val c2 = circles[j]

                // Distance between centers
                val dx = c1.center.x - c2.center.x
                val dy = c1.center.y - c2.center.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                // If centers are very close (< 5 px), they are concentric
                if (dist < 5.0) {
                    val inner = if (c1.radius < c2.radius) c1.radius else c2.radius
                    val outer = if (c1.radius < c2.radius) c2.radius else c1.radius
                    countersinks.add(
                        Countersink(
                            c1.center,
                            inner,
                            outer
                        )
                    ) // We use c1 center approx
                    usedIndices.add(i)
                    usedIndices.add(j)
                    matchFound = true
                    break
                }
            }
            if (!matchFound) {
                isolatedHoles.add(circles[i])
            }
        }

        // Add those that remained without a pair but we have marked as 'no match'
        // Logic fix: isolatedHoles loop above adds circles[i] only if no match for i found
        // But if i was matched as second pair (j), it is in usedIndices.
        // We need to verify if i is in usedIndices in the else branch? No, simple logic:
        // We rebuild isolatedHoles from scratch based on usedIndices logic?
        // Better:
        // Return detected countersinks and remaining circles.

        val finalHoles = circles.filterIndexed { index, _ -> !usedIndices.contains(index) }

        return Pair(finalHoles, countersinks)
    }

    private fun detectScratches(edges: Mat, contours: List<MatOfPoint>): List<Scratch> {
        val lines = Mat()
        // Probabilistic line detection
        Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 50.0, 10.0)

        val scratches = ArrayList<Scratch>()

        for (i in 0 until lines.rows()) {
            val l = lines.get(i, 0)
            val pt1 = org.opencv.core.Point(l[0], l[1])
            val pt2 = org.opencv.core.Point(l[2], l[3])

            // Verify if line is INSIDE a part contour (to avoid outer edges)
            // Simplifying: A scratch is usually a straight line.
            // Assuming all HoughLines are scratches for now.
            val dx = pt2.x - pt1.x
            val dy = pt2.y - pt1.y
            val len = kotlin.math.sqrt(dx * dx + dy * dy)

            scratches.add(Scratch(pt1, pt2, len))
        }
        lines.release()
        return scratches
    }

    private fun detectOrientation(markers: List<Marker>): com.example.celestic.models.enums.Orientation {
        if (markers.isEmpty()) return com.example.celestic.models.enums.Orientation.UNKNOWN

        // Simple logic based on hypothetical business rules
        // If ID < 10 detected it is ANVERSO, ID >= 10 is REVERSO
        // Or if markers present in general it is ANVERSO.
        // For now: Marker present = ANVERSO.

        return com.example.celestic.models.enums.Orientation.ANVERSO
    }

    // ... rest of existing methods (applyCalibration, etc) ...

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