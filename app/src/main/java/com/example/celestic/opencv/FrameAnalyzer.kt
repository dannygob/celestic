package com.example.celestic.opencv
/*
**Estado:** Pospuesta (Para después).
    *   **¿Por qué se deja?** (Nota de Ingeniería): El cálculo de Optical Flow (flujo óptico de Lucas-Kanade) es un proceso de visión artificial de alta carga computacional que corre cuadro por cuadro en el hilo de análisis. Actualmente no existe ningún visualizador, gráfica de vibración o indicador de deformación activa en la UI de producción. Mantenerlo encendido degrada innecesariamente el rendimiento de la cámara (FPS) del dispositivo móvil sin aportar valor práctico en esta fase.
    *   **¿Para qué se deja?** (Nota de Ingeniería): Se conserva como cimiento algorítmico para una futura actualización del sistema destinada a pruebas de tolerancia dinámica estructural y detección de vibraciones/fatiga de materiales en chapas bajo carga física.

 */

import android.util.Log
import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
import com.example.celestic.manager.CalibrationManager
import com.example.celestic.models.FiducialMarker
import com.example.celestic.models.enums.Orientation
import com.example.celestic.viewmodel.MarkerType
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.Objdetect
import javax.inject.Inject

class FrameAnalyzer @Inject constructor(
    private val arucoManager: ArUcoManager,
    private val aprilTagManager: AprilTagManager,
    private val calibrationManager: CalibrationManager
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

    /**
     * ==================================================================================
     * ⚠️ NOTA DE INGENIERÍA - CLASE MODELO EN DESUSO (CONCEPTO REEMPLAZADO)
     * ==================================================================================
     * ¿POR QUÉ APARECE SIN USO?
     * La evaluación del tratamiento químico de Alodine SÍ se ejecuta y evalúa de forma 
     * activa en cada fotograma mediante el algoritmo `checkAlodine(image, center, radius)`.
     * Sin embargo, para simplificar el modelo matemático, la presencia del halo se almacena 
     * directamente como un flag lógico (`hasAlodine: Boolean`) dentro del objeto `Hole`.
     * 
     * ¿PARA QUÉ SE DEJA?
     * Se conserva este modelo específico `AlodineHalo` para futuras ampliaciones de metrología
     * donde se requiera medir y persistir el ancho del halo en mm o la uniformidad del color 
     * alrededor del agujero de forma independiente.
     * ==================================================================================
     */
    data class AlodineHalo(
        val center: Point,
        val radius: Double,
        val intensityInfo: Double
    )
    
    data class PolygonEdge(
        val edgeIndex: Int,
        val startPoint: Point,
        val endPoint: Point,
        val lengthPx: Double,
        val angleDeg: Double,
        val straightnessErrorPx: Double = 0.0
    )

    data class SheetPolygon(
        val vertices: List<Point>,
        val edges: List<PolygonEdge>,
        val perimeterPx: Double,
        val areaPx: Double,
        val center: Point,
        val boundingBox: Rect
    )

    data class HoleToEdgeDistance(
        val holeIndex: Int,
        val edgeIndex: Int,
        val distancePx: Double
    )

    data class InterHoleDistance(
        val indexA: Int,
        val indexB: Int,
        val distancePx: Double
    )

    data class AnalysisResult(
        val contours: List<MatOfPoint>,
        val annotatedMat: Mat,
        val markers: List<Marker>,
        val holes: List<Hole>,
        val orientation: Orientation,
        val countersinks: List<Countersink>,
        val scratches: List<Scratch>,
        val decodedQrCode: String? = null,
        val sheetPolygon: SheetPolygon? = null,
        val holeToEdgeDistances: List<HoleToEdgeDistance> = emptyList(),
        val interHoleDistances: List<InterHoleDistance> = emptyList(),
        val deformations: List<MatOfPoint> = emptyList()
    )

    private var prevGrayMat: Mat? = null

    fun analyze(mat: Mat, markerType: MarkerType?): AnalysisResult {
        val grayMat = Mat()

        // ==================================================================================
        // ⚠️ NOTA DE INGENIERÍA - VARIABLE PRE-ASIGNADA PARA OPTIMIZACIÓN JNI
        // ==================================================================================
        // ¿POR QUÉ SE DEJA?
        // Pre-asignar contenedores Mat en OpenCV evita la recolección de basura del heap de C++
        // en cada iteración de 30 FPS. Se mantiene aquí para optimización de binarización adaptativa.
        // ==================================================================================
        val thresholdedImage = Mat()

        val edges = Mat()
        val holesMat = Mat()

        // 1. apply calibration if loaded to correct lens distortion (Paso 2)
        val camMatrix = calibrationManager.cameraMatrix
        val distCoeffs = calibrationManager.distortionCoeffs
        val processedMat = if (camMatrix != null && distCoeffs != null) {
            applyCalibration(mat, camMatrix, distCoeffs)
        } else {
            mat.clone()
        }

        try {
            // Preprocessing
            Imgproc.cvtColor(processedMat, grayMat, Imgproc.COLOR_BGR2GRAY)
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
                val hasAlodine = checkAlodine(processedMat, center, radius)
                allCircles.add(Hole(center, radius, hasAlodine))
            }

            val (simpleHoles, countersinks) = identifyCountersinks(allCircles)

            Imgproc.Canny(grayMat, edges, 50.0, 150.0)
            val scratches = detectScratches(edges, filteredContours)

            // ✅ CORREGIDO: Ahora usando FiducialMarker
            val tempMarkers: List<FiducialMarker> = when (markerType) {
                MarkerType.ARUCO -> arucoManager.detectMarkers(processedMat)
                MarkerType.APRILTAG -> aprilTagManager.detectMarkers(processedMat)
                null -> emptyList()
                else -> emptyList()
            }

            // Convertir FiducialMarker a Marker interno
            val markers = tempMarkers.map {
                Marker(it.id, it.corners.clone())
            }

            // Liberar los Mats temporales
            tempMarkers.forEach { it.corners.release() }

            val orientation = detectOrientation(markers, countersinks)

            // OPTICAL FLOW: Pospuesto para modo de fatiga dinámica (evita degradar FPS a 30fps)
            // Si en el futuro se activa un visor de vibraciones en vivo, se invoca detectDeformationsWithOpticalFlow
            /*
            prevGrayMat?.let { prev ->
                val flowPts = detectDeformationsWithOpticalFlow(prev, grayMat)
                flowPts.release()
            }
            */

            // Liberar previo antes de asignar nuevo
            prevGrayMat?.release()
            prevGrayMat = grayMat.clone()

            // ── CÁLCULO DE ARISTAS Y CONTORNO POLIGONAL DE LA PIEZA (NO CUADRADA) ──
            val mainContour = filteredContours.maxByOrNull { Imgproc.contourArea(it) }
            val sheetPolygon: SheetPolygon? = if (mainContour != null && Imgproc.contourArea(mainContour) > 1500.0) {
                val contour2f = MatOfPoint2f(*mainContour.toArray())
                val peri = Imgproc.arcLength(contour2f, true)
                val approx = MatOfPoint2f()
                // Epsilon adaptativo para detectar polígonos irregulares reales (aristas y esquinas)
                Imgproc.approxPolyDP(contour2f, approx, 0.015 * peri, true)
                val rawVertices = approx.toArray().toList()
                approx.release()
                val boundRect = Imgproc.boundingRect(mainContour)
                val area = Imgproc.contourArea(mainContour)

                val vertices = if (rawVertices.size >= 3) rawVertices else {
                    listOf(
                        Point(boundRect.x.toDouble(), boundRect.y.toDouble()),
                        Point((boundRect.x + boundRect.width).toDouble(), boundRect.y.toDouble()),
                        Point((boundRect.x + boundRect.width).toDouble(), (boundRect.y + boundRect.height).toDouble()),
                        Point(boundRect.x.toDouble(), (boundRect.y + boundRect.height).toDouble())
                    )
                }

                val edges = mutableListOf<PolygonEdge>()
                for (i in vertices.indices) {
                    val p1 = vertices[i]
                    val p2 = vertices[(i + 1) % vertices.size]
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    val len = kotlin.math.sqrt(dx * dx + dy * dy)
                    var angle = Math.toDegrees(kotlin.math.atan2(dy, dx))
                    if (angle < 0) angle += 360.0
                    edges.add(
                        PolygonEdge(
                            edgeIndex = i + 1,
                            startPoint = p1,
                            endPoint = p2,
                            lengthPx = len,
                            angleDeg = angle
                        )
                    )
                }

                val moments = Imgproc.moments(mainContour)
                val cx = if (moments.m00 != 0.0) moments.m10 / moments.m00 else (boundRect.x + boundRect.width / 2.0)
                val cy = if (moments.m00 != 0.0) moments.m01 / moments.m00 else (boundRect.y + boundRect.height / 2.0)
                contour2f.release()

                SheetPolygon(
                    vertices = vertices,
                    edges = edges,
                    perimeterPx = peri,
                    areaPx = area,
                    center = Point(cx, cy),
                    boundingBox = boundRect
                )
            } else null

            // ── CÁLCULO DE DISTANCIA DE CADA AGUJERO A CADA ARISTA DE LA PIEZA ──
            val holeToEdgeDistances = mutableListOf<HoleToEdgeDistance>()
            sheetPolygon?.edges?.forEach { edge ->
                simpleHoles.forEachIndexed { hIdx, hole ->
                    val dist = pointToSegmentDistance(hole.center, edge.startPoint, edge.endPoint)
                    holeToEdgeDistances.add(HoleToEdgeDistance(hIdx + 1, edge.edgeIndex, dist))
                }
            }

            // ── CÁLCULO DE DISTANCIAS ENTRE CENTROS DE AGUJEROS (PITCH) ──
            val interHoleDistances = mutableListOf<InterHoleDistance>()
            for (i in 0 until simpleHoles.size) {
                for (j in i + 1 until simpleHoles.size) {
                    val p1 = simpleHoles[i].center
                    val p2 = simpleHoles[j].center
                    val dx = p1.x - p2.x
                    val dy = p1.y - p2.y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    interHoleDistances.add(InterHoleDistance(i + 1, j + 1, dist))
                }
            }

            val annotatedMat = processedMat.clone()

            // Dibujado de contornos y deformaciones
            Imgproc.drawContours(annotatedMat, filteredContours, -1, Scalar(0.0, 255.0, 0.0), 1)
            Imgproc.drawContours(annotatedMat, deformations, -1, Scalar(255.0, 0.0, 0.0), 2)

            // Dibujar cada arista/borde con su numeración E1, E2... y vértices
            sheetPolygon?.edges?.forEach { edge ->
                Imgproc.line(annotatedMat, edge.startPoint, edge.endPoint, Scalar(0.0, 255.0, 255.0), 2)
                Imgproc.circle(annotatedMat, edge.startPoint, 4, Scalar(0.0, 255.0, 0.0), -1)
                val midX = (edge.startPoint.x + edge.endPoint.x) / 2.0
                val midY = (edge.startPoint.y + edge.endPoint.y) / 2.0
                Imgproc.putText(
                    annotatedMat,
                    "E${edge.edgeIndex}",
                    Point(midX, midY),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.55,
                    Scalar(0.0, 255.0, 255.0),
                    2
                )
            }

            for ((center, radius, hasAlodine) in simpleHoles) {
                val color = if (hasAlodine) Scalar(0.0, 165.0, 255.0) else Scalar(0.0, 0.0, 255.0)
                Imgproc.circle(annotatedMat, center, radius.toInt(), color, 2)
            }

            for ((center, innerRadius, outerRadius) in countersinks) {
                Imgproc.circle(
                    annotatedMat,
                    center, outerRadius.toInt(), Scalar(0.0, 255.0, 255.0), 2
                )
                Imgproc.circle(
                    annotatedMat,
                    center, innerRadius.toInt(), Scalar(0.0, 165.0, 255.0), 1
                )
            }

            for ((startPoint, endPoint) in scratches) {
                Imgproc.line(annotatedMat, startPoint, endPoint, Scalar(255.0, 0.0, 255.0), 2)
            }

            if (markers.isNotEmpty()) {
                val cornersList = markers.map { it.corners }
                Objdetect.drawDetectedMarkers(annotatedMat, cornersList, Mat())
            }

            val faceLabel = if (orientation == Orientation.ANVERSO) "ANVERSO (Cara A)" else "REVERSO (Cara B)"
            Imgproc.putText(
                annotatedMat,
                "Cara: $faceLabel",
                Point(40.0, 45.0),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.9,
                Scalar(255.0, 255.0, 0.0),
                2
            )

            if (sheetPolygon != null) {
                Imgproc.putText(
                    annotatedMat,
                    "Pieza: ${sheetPolygon.edges.size} aristas | Perímetro: ${sheetPolygon.perimeterPx.toInt()}px",
                    Point(40.0, 80.0),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.6,
                    Scalar(0.0, 255.0, 255.0),
                    2
                )
            }

            // Detect QR Code
            val qrDetector = org.opencv.objdetect.QRCodeDetector()
            val points = Mat()
            val decodedQr = try {
                val data = qrDetector.detectAndDecode(processedMat, points)
                if (data.isNotEmpty()) data else null
            } catch (e: Exception) {
                null
            } finally {
                points.release()
            }

            return AnalysisResult(
                contours = filteredContours,
                annotatedMat = annotatedMat,
                markers = markers,
                holes = simpleHoles,
                orientation = orientation,
                countersinks = countersinks,
                scratches = scratches,
                decodedQrCode = decodedQr,
                sheetPolygon = sheetPolygon,
                holeToEdgeDistances = holeToEdgeDistances,
                interHoleDistances = interHoleDistances,
                deformations = deformations
            )

        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Error analyzing frame", e)
            return AnalysisResult(
                emptyList(),
                processedMat.clone(),
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
            processedMat.release()
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

    /**
     * ==================================================================================
     * ⚠️ NOTA DE INGENIERÍA - PARÁMETRO RESERVADO PARA FUTURA MEJORA
     * ==================================================================================
     * ¿POR QUÉ APARECE SIN USO?
     * El parámetro 'contours' no se lee actualmente porque la detección de rayaduras utiliza
     * la transformada probabilística de Hough sobre el mapa de bordes 'edges'.
     * 
     * ¿PARA QUÉ SE DEJA?
     * Se mantiene para la siguiente fase del proyecto, donde se usará 'contours' para filtrar
     * falsos positivos (por ejemplo, evitar que las circunferencias de los agujeros se detecten
     * erróneamente como rayaduras si sus contornos están segmentados o imperfectos).
     * ==================================================================================
     */
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

    private fun detectOrientation(
        markers: List<Marker>,
        countersinks: List<Countersink> = emptyList()
    ): Orientation {
        if (markers.isNotEmpty()) {
            val firstMarker = markers.first()
            if (firstMarker.id == 0) return Orientation.ANVERSO
            if (firstMarker.id == 1) return Orientation.REVERSO
        }
        // En piezas mecánicas y aeronáuticas, los avellanados cónicos se mecanizan en la cara frontal (ANVERSO)
        if (countersinks.isNotEmpty()) {
            return Orientation.ANVERSO
        }
        return Orientation.ANVERSO
    }

    private fun pointToSegmentDistance(p: Point, a: Point, b: Point): Double {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0.0) {
            val dpx = p.x - a.x
            val dpy = p.y - a.y
            return kotlin.math.sqrt(dpx * dpx + dpy * dpy)
        }
        val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq).coerceIn(0.0, 1.0)
        val projX = a.x + t * dx
        val projY = a.y + t * dy
        val distDx = p.x - projX
        val distDy = p.y - projY
        return kotlin.math.sqrt(distDx * distDx + distDy * distDy)
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

    /**
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? Debido a su alta carga computacional al correr cuadro por cuadro en el
     *   hilo de análisis dinámico, y dado que la UI actual no cuenta con pantallas o gráficas de
     *   diagnóstico de deformación estructural/vibración.
     * - ¿Para qué se deja? Como cimiento para futuras actualizaciones destinadas a pruebas de
     *   tolerancia dinámica estructural y fatiga de materiales bajo esfuerzo.
     */
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
