package com.example.celestic.opencv

import android.util.Log
import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
import com.example.celestic.models.FiducialMarker
import com.example.celestic.models.enums.Orientation
import com.example.celestic.viewmodel.MarkerType
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.Objdetect
import javax.inject.Inject

class FrameAnalyzer @Inject constructor(
    private val arucoManager: ArUcoManager,
    private val aprilTagManager: AprilTagManager
) {

    data class Marker(val id: Int, val corners: Mat)
    data class Hole(
        val center: Point,
        val radius: Double,
        val hasAlodine: Boolean = false
    )

    data class Countersink(
        val center: Point,
        val innerRadius: Double,
        val outerRadius: Double
    )

    data class Scratch(
        val startPoint: Point,
        val endPoint: Point,
        val length: Double
    )

    data class AlodineHalo(
        val center: Point,
        val radius: Double,
        val intensityInfo: Double
    )
    
    data class AnalysisResult(
        val contours: List<MatOfPoint>,
        val annotatedMat: Mat,
        val markers: List<Marker>,
        val holes: List<Hole>,
        val orientation: Orientation,
        val countersinks: List<Countersink>,
        val scratches: List<Scratch>,
        val decodedQrCode: String? = null
    )

    private var prevGrayMat: Mat? = null

    fun analyze(mat: Mat, markerType: MarkerType?): AnalysisResult {
        val grayMat = Mat()
        val thresholdedImage = Mat()
        val edges = Mat()
        val holesMat = Mat()

        try {
            // Preprocessing
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.GaussianBlur(grayMat, grayMat, Size(5.0, 5.0), 0.0)

            val thresholded = applyAdaptiveThresholding(grayMat)
            val contours = findContours(thresholded)
            val filteredContours = filterContours(contours, 100.0)
            val deformations = detectDeformations(filteredContours)

            // Detect holes
            Imgproc.HoughCircles(
                grayMat,
                holesMat,
                Imgproc.HOUGH_GRADIENT,
                1.0,
                grayMat.rows().toDouble() / 8,
                200.0,
                100.0,
                0,
                0
            )

            val allCircles = ArrayList<Hole>()
            for (i in 0 until holesMat.cols()) {
                val circle = holesMat.get(0, i)
                val center = Point(circle[0], circle[1])
                val radius = circle[2]
                val hasAlodine = checkAlodine(mat, center, radius)
                allCircles.add(Hole(center, radius, hasAlodine))
            }

            val (simpleHoles, countersinks) = identifyCountersinks(allCircles)

            Imgproc.Canny(grayMat, edges, 50.0, 150.0)
            val scratches = detectScratches(edges, filteredContours)

            // ✅ CORREGIDO: Ahora usando FiducialMarker
            val tempMarkers: List<FiducialMarker> = when (markerType) {
                MarkerType.ARUCO -> arucoManager.detectMarkers(mat)
                MarkerType.APRILTAG -> aprilTagManager.detectMarkers(mat)
                null -> emptyList()
                else -> emptyList()
            }

            // Convertir FiducialMarker a Marker interno
            val markers = tempMarkers.map {
                Marker(it.id, it.corners.clone())
            }

            // Liberar los Mats temporales
            tempMarkers.forEach { it.corners.release() }

            val orientation = detectOrientation(markers)

            // OPTICAL FLOW: Corregir fuga de memoria de los Mats de retorno
            prevGrayMat?.let { prev ->
                val flowPts = detectDeformationsWithOpticalFlow(prev, grayMat)
                flowPts.release() // No lo usamos por ahora, así que liberamos
            }

            // Liberar previo antes de asignar nuevo
            prevGrayMat?.release()
            prevGrayMat = grayMat.clone()

            val annotatedMat = mat.clone()

            // Dibujado
            Imgproc.drawContours(annotatedMat, filteredContours, -1, Scalar(0.0, 255.0, 0.0), 2)
            Imgproc.drawContours(annotatedMat, deformations, -1, Scalar(255.0, 0.0, 0.0), 2)

            for (hole in simpleHoles) {
                val color = if (hole.hasAlodine) Scalar(0.0, 165.0, 255.0) else Scalar(0.0, 0.0, 255.0)
                Imgproc.circle(annotatedMat, hole.center, hole.radius.toInt(), color, 2)
            }

            for (cs in countersinks) {
                Imgproc.circle(annotatedMat, cs.center, cs.outerRadius.toInt(), Scalar(0.0, 255.0, 255.0), 2)
                Imgproc.circle(annotatedMat, cs.center, cs.innerRadius.toInt(), Scalar(0.0, 165.0, 255.0), 1)
            }

            for (scratch in scratches) {
                Imgproc.line(annotatedMat, scratch.startPoint, scratch.endPoint, Scalar(255.0, 0.0, 255.0), 2)
            }

            if (markers.isNotEmpty()) {
                val cornersList = markers.map { it.corners }
                Objdetect.drawDetectedMarkers(annotatedMat, cornersList, Mat())
            }

            Imgproc.putText(
                annotatedMat,
                "Orient: $orientation",
                Point(50.0, 50.0),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                1.0,
                Scalar(255.0, 255.0, 0.0),
                2
            )

            // MUY IMPORTANTE: Liberar contornos que no van en el resultado
            // Pero como van en AnalysisResult, el llamador debe liberarlos.
            // Para ser más seguros, devolvemos solo lo necesario en el DTO final.

            // Detect QR Code
            val qrDetector = org.opencv.objdetect.QRCodeDetector()
            val points = Mat()
            val decodedQr = try {
                val data = qrDetector.detectAndDecode(mat, points)
                if (data.isNotEmpty()) data else null
            } catch (e: Exception) {
                null
            } finally {
                points.release()
            }

            return AnalysisResult(
                filteredContours,
                annotatedMat,
                markers,
                simpleHoles,
                orientation,
                countersinks,
                scratches,
                decodedQr
            )

        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Error analyzing frame", e)
            return AnalysisResult(
                emptyList(),
                mat.clone(),
                emptyList(),
                emptyList(),
                Orientation.UNKNOWN,
                emptyList(),
                emptyList()
            )
        } finally {
            grayMat.release()
            thresholdedImage.release()
            edges.release()
            holesMat.release()
            // Nota: Contours y markers en AnalysisResult DEBEN ser liberados por el DashboardViewModel
        }
    }

    private fun checkAlodine(image: Mat, center: Point, radius: Double): Boolean {
        try {
            val roiSize = (radius * 3).toInt()
            val x = (center.x - roiSize / 2).toInt().coerceAtLeast(0)
            val y = (center.y - roiSize / 2).toInt().coerceAtLeast(0)
            val w = roiSize.coerceAtMost(image.width() - x)
            val h = roiSize.coerceAtMost(image.height() - y)

            if (w <= 0 || h <= 0) return false

            val roiRect = Rect(x, y, w, h)
            val roi = image.submat(roiRect) // submat es más eficiente que crear nuevo Mat(image, rect)

            val hsvRoi = Mat()
            Imgproc.cvtColor(roi, hsvRoi, Imgproc.COLOR_RGB2HSV)

            val saturation = Mat()
            Core.extractChannel(hsvRoi, saturation, 1)

            val meanSat = Core.mean(saturation).`val`[0]

            // Cleanup sistemático
            roi.release()
            hsvRoi.release()
            saturation.release()

            return meanSat > 40.0
        } catch (e: Exception) {
            return false
        }
    }

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
                val dx = c1.center.x - c2.center.x
                val dy = c1.center.y - c2.center.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                if (dist < 5.0) {
                    val inner = if (c1.radius < c2.radius) c1.radius else c2.radius
                    val outer = if (c1.radius < c2.radius) c2.radius else c1.radius
                    countersinks.add(Countersink(c1.center, inner, outer))
                    usedIndices.add(i)
                    usedIndices.add(j)
                    matchFound = true
                    break
                }
            }
            if (!matchFound) isolatedHoles.add(circles[i])
        }
        return Pair(isolatedHoles, countersinks)
    }

    private fun detectScratches(edges: Mat, contours: List<MatOfPoint>): List<Scratch> {
        val lines = Mat()
        Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 50.0, 10.0)
        val scratches = ArrayList<Scratch>()
        for (i in 0 until lines.rows()) {
            val l = lines.get(i, 0)
            val pt1 = Point(l[0], l[1])
            val pt2 = Point(l[2], l[3])
            val dx = pt2.x - pt1.x
            val dy = pt2.y - pt1.y
            val len = kotlin.math.sqrt(dx * dx + dy * dy)
            scratches.add(Scratch(pt1, pt2, len))
        }
        lines.release()
        return scratches
    }

    private fun detectOrientation(markers: List<Marker>): Orientation {
        return if (markers.isEmpty()) Orientation.UNKNOWN
        else Orientation.ANVERSO
    }

    fun applyCalibration(image: Mat, cameraMatrix: Mat, distortionCoeffs: Mat): Mat {
        val undistortedImage = Mat()
        org.opencv.calib3d.Calib3d.undistort(image, undistortedImage, cameraMatrix, distortionCoeffs)
        return undistortedImage
    }

    private fun findContours(image: Mat): List<MatOfPoint> {
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release() // Siempre liberar el hierarchy si no se usa después
        return contours
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
            // Liberar Mats temporales de la iteración
            approx.release()
            contour2f.release()
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
        return contours.filter { Imgproc.contourArea(it) > minArea }
    }

    fun detectDeformationsWithOpticalFlow(prevFrame: Mat, nextFrame: Mat): MatOfPoint2f {
        val corners = MatOfPoint()
        Imgproc.goodFeaturesToTrack(prevFrame, corners, 100, 0.3, 7.0)
        val prevPts2f = MatOfPoint2f(*corners.toArray())
        val nextPts2f = MatOfPoint2f()
        val status = MatOfByte()
        val err = MatOfFloat()
        if (prevPts2f.total() > 0) {
            org.opencv.video.Video.calcOpticalFlowPyrLK(prevFrame, nextFrame, prevPts2f, nextPts2f, status, err)
        }
        // Cleanup local
        corners.release()
        prevPts2f.release()
        status.release()
        err.release()
        return nextPts2f
    }
}
