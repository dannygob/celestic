package com.example.celestic.opencv

import com.example.celestic.manager.CalibrationManager
import com.example.celestic.manager.TraceabilityManager
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import com.example.celestic.viewmodel.MarkerType
import org.opencv.core.Mat
import org.opencv.core.Point
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/**
 * Helper class responsible for basic Image conversion tasks.
 * Converts YUV_420_888 images from Android Camera2 API into standard OpenCV Mat formats (RGB).
 */
class ImageProcessor @Inject constructor(
    private val frameAnalyzer: FrameAnalyzer,
    private val calibrationManager: CalibrationManager,
    private val traceabilityManager: TraceabilityManager
) {

    /**
     * Processes an OpenCV Mat to detect holes, countersinks, and scratches, and associates
     * them with traceability information if a QR code or marker is present.
     *
     * @param mat The input OpenCV image matrix.
     * @param markerType The type of marker expected for alignment/calibration.
     * @return An [ImageProcessorResult] containing the detected items and orientation.
     */
    fun processImage(mat: Mat, markerType: MarkerType?): ImageProcessorResult {
        val result = frameAnalyzer.analyze(mat, markerType)
        val linkedCode = result.decodedQrCode ?: result.markers.firstOrNull()?.id?.toString()
        val traceabilityInfo = linkedCode?.let { traceabilityManager.lookup(it) }

        // 1. Calculate average side length of fiducial marker in pixels to estimate distance (Paso 3)
        fun getMarkerWidthPx(corners: Mat): Double {
            if (corners.empty() || corners.rows() < 4) return 100.0
            val p0 = Point(corners.get(0, 0)[0], corners.get(0, 0)[1])
            val p1 = Point(corners.get(1, 0)[0], corners.get(1, 0)[1])
            val dx = p1.x - p0.x
            val dy = p1.y - p0.y
            return kotlin.math.sqrt(dx * dx + dy * dy)
        }

        val distanceMm = if (result.markers.isNotEmpty()) {
            val detectedWidthPx = getMarkerWidthPx(result.markers.first().corners)
            val realMarkerSizeMm = 40.0 // Known physical size of standard marker (40mm / 4cm)
            calibrationManager.estimateDistance(detectedWidthPx, realMarkerSizeMm)
        } else {
            300.0 // Nominal focal inspection distance of 30cm
        }

        val scaleFactor = calibrationManager.getScaleFactor(distanceMm)

        val detectionItems = mutableListOf<DetectionItem>()

        // 1. Convertir cada arista y borde de la pieza (Pieza irregular / No cuadrada)
        result.sheetPolygon?.let { sheet ->
            val perimMm = sheet.perimeterPx * scaleFactor
            val box = sheet.boundingBox
            val boxWMm = box.width * scaleFactor
            val boxHMm = box.height * scaleFactor

            // Cota general del contorno poligonal de la pieza
            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "POLIGONO_PIEZA",
                    type = DetectionType.DIMENSION,
                    boundingBox = BoundingBox(
                        left = box.x.toFloat(),
                        top = box.y.toFloat(),
                        right = (box.x + box.width).toFloat(),
                        bottom = (box.y + box.height).toFloat()
                    ),
                    confidence = 0.96f,
                    status = DetectionStatus.OK,
                    measurementMm = perimMm.toFloat(),
                    timestamp = System.currentTimeMillis(),
                    linkedQrCode = linkedCode,
                    notes = "Contorno poligonal | ${sheet.edges.size} aristas evaluadas | Perímetro: ${"%.1f".format(perimMm)} mm | Envolvente: ${"%.1f".format(boxWMm)} x ${"%.1f".format(boxHMm)} mm [Cara: ${result.orientation}]" +
                            (traceabilityInfo?.let { " | Pieza: ${it.partName}" } ?: "")
                )
            )

            // Evaluación individual de cada arista / borde geométrico
            sheet.edges.forEach { edge ->
                val lenMm = edge.lengthPx * scaleFactor
                val minX = minOf(edge.startPoint.x, edge.endPoint.x).toFloat()
                val minY = minOf(edge.startPoint.y, edge.endPoint.y).toFloat()
                val maxX = maxOf(edge.startPoint.x, edge.endPoint.x).toFloat().coerceAtLeast(minX + 8f)
                val maxY = maxOf(edge.startPoint.y, edge.endPoint.y).toFloat().coerceAtLeast(minY + 8f)
                detectionItems.add(
                    DetectionItem(
                        id = 0,
                        inspectionId = 0,
                        frameId = "ARISTA_E${edge.edgeIndex}",
                        type = DetectionType.EDGE,
                        boundingBox = BoundingBox(left = minX, top = minY, right = maxX, bottom = maxY),
                        confidence = 0.95f,
                        status = DetectionStatus.OK,
                        measurementMm = lenMm.toFloat(),
                        timestamp = System.currentTimeMillis(),
                        linkedQrCode = linkedCode,
                        notes = "Arista E${edge.edgeIndex} | Longitud: ${"%.2f".format(lenMm)} mm | Ángulo: ${"%.1f".format(edge.angleDeg)}° [Cara: ${result.orientation}]"
                    )
                )
            }
        }

        // NOTA: El espesor se excluye por especificación del usuario (espesor irrelevante en esta inspección).

        // 2. Convert holes to DetectionItem con distancias a las aristas calculadas
        result.holes.forEachIndexed { index, hole ->
            val diameterPx = hole.radius * 2
            val diameterMm = diameterPx * scaleFactor
            val holeDistances = result.holeToEdgeDistances.filter { it.holeIndex == (index + 1) }
            val minEdgeDist = holeDistances.minByOrNull { it.distancePx }
            val marginNote = if (minEdgeDist != null) {
                val distMm = minEdgeDist.distancePx * scaleFactor
                " | Distancia mín a arista E${minEdgeDist.edgeIndex}: ${"%.2f".format(distMm)} mm"
            } else ""

            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "frame_${System.currentTimeMillis()}_hole_$index",
                    type = DetectionType.HOLE,
                    boundingBox = BoundingBox(
                        left = (hole.center.x - hole.radius).toFloat(),
                        top = (hole.center.y - hole.radius).toFloat(),
                        right = (hole.center.x + hole.radius).toFloat(),
                        bottom = (hole.center.y + hole.radius).toFloat()
                    ),
                    confidence = 0.9f,
                    status = if (hole.hasAlodine) DetectionStatus.WARNING else DetectionStatus.OK,
                    measurementMm = diameterMm.toFloat(),
                    timestamp = System.currentTimeMillis(),
                    linkedQrCode = linkedCode,
                    notes = (if (hole.hasAlodine) "Agujero con halo Alodine" else "Agujero cilíndrico") +
                            " | Diámetro: ${"%.2f".format(diameterMm)} mm" +
                            marginNote +
                            (traceabilityInfo?.let { " | Pieza: ${it.partName}" } ?: "")
                )
            )
        }

        // 3. Distancias entre centros de taladros (Pitch)
        result.interHoleDistances.take(4).forEachIndexed { idx, pitch ->
            val distMm = pitch.distancePx * scaleFactor
            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "PITCH_H${pitch.indexA}_H${pitch.indexB}",
                    type = DetectionType.DIMENSION,
                    boundingBox = BoundingBox(10f, 60f + idx * 30f, 150f, 90f + idx * 30f),
                    confidence = 0.92f,
                    status = DetectionStatus.OK,
                    measurementMm = distMm.toFloat(),
                    timestamp = System.currentTimeMillis(),
                    linkedQrCode = linkedCode,
                    notes = "Distancia entre centros H${pitch.indexA}-H${pitch.indexB}: ${"%.2f".format(distMm)} mm [Cara: ${result.orientation}]"
                )
            )
        }

        // 5. Convert countersinks to DetectionItem (Paso 4)
        result.countersinks.forEachIndexed { index, countersink ->
            val outerDiameterPx = countersink.outerRadius * 2
            val outerDiameterMm = outerDiameterPx * scaleFactor
            val innerDiameterPx = countersink.innerRadius * 2
            val innerDiameterMm = innerDiameterPx * scaleFactor
            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "frame_${System.currentTimeMillis()}_cs_$index",
                    type = DetectionType.COUNTERSINK,
                    boundingBox = BoundingBox(
                        left = (countersink.center.x - countersink.outerRadius).toFloat(),
                        top = (countersink.center.y - countersink.outerRadius).toFloat(),
                        right = (countersink.center.x + countersink.outerRadius).toFloat(),
                        bottom = (countersink.center.y + countersink.outerRadius).toFloat()
                    ),
                    confidence = 0.9f,
                    status = DetectionStatus.OK,
                    measurementMm = outerDiameterMm.toFloat(),
                    timestamp = System.currentTimeMillis(),
                    linkedQrCode = linkedCode,
                    notes = "Avellanado cónico | Ext: ${"%.2f".format(outerDiameterMm)} mm, Int: ${"%.2f".format(innerDiameterMm)} mm [Anverso]" +
                            (traceabilityInfo?.let { " | Pieza: ${it.partName}" } ?: "")
                )
            )
        }

        // 6. Deformaciones y Pliegues detectados en bordes
        result.deformations.take(3).forEachIndexed { index, defContour ->
            val pts2f = org.opencv.core.MatOfPoint2f(*defContour.toArray())
            val lenMm = org.opencv.imgproc.Imgproc.arcLength(pts2f, true) * scaleFactor
            pts2f.release()
            val bound = org.opencv.imgproc.Imgproc.boundingRect(defContour)
            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "DEFORMATION_$index",
                    type = DetectionType.BEND,
                    boundingBox = BoundingBox(
                        left = bound.x.toFloat(),
                        top = bound.y.toFloat(),
                        right = (bound.x + bound.width).toFloat(),
                        bottom = (bound.y + bound.height).toFloat()
                    ),
                    confidence = 0.85f,
                    status = DetectionStatus.WARNING,
                    measurementMm = lenMm.toFloat(),
                    timestamp = System.currentTimeMillis(),
                    linkedQrCode = linkedCode,
                    notes = "Deformación / Alabeo de borde detectado | Extensión: ${"%.2f".format(lenMm)} mm"
                )
            )
        }

        // 7. Convert scratches to DetectionItem (Paso 4)
        result.scratches.forEachIndexed { index, scratch ->
            val lengthMm = scratch.length * scaleFactor
            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "frame_${System.currentTimeMillis()}_scr_$index",
                    type = DetectionType.SCRATCH,
                    boundingBox = BoundingBox(
                        left = scratch.startPoint.x.toFloat(),
                        top = scratch.startPoint.y.toFloat(),
                        right = scratch.endPoint.x.toFloat(),
                        bottom = scratch.endPoint.y.toFloat()
                    ),
                    confidence = 0.9f,
                    status = DetectionStatus.WARNING,
                    measurementMm = lengthMm.toFloat(),
                    timestamp = System.currentTimeMillis(),
                    linkedQrCode = linkedCode,
                    notes = "Rayadura superficial | Longitud: ${"%.2f".format(lengthMm)} mm" +
                            (traceabilityInfo?.let { " | Pieza: ${it.partName}" } ?: "")
                )
            )
        }

        // Barrido Horizontal (De Izquierda a Derecha) para asignar Etiquetas
        val sweepSortedDetections = detectionItems.sortedBy { it.boundingBox.left }
            .mapIndexed { index, item ->
                val tagNumber = index + 1
                item.copy(
                    frameId = "ETIQUETA-$tagNumber",
                    notes = "[Elemento #$tagNumber] ${item.notes}"
                )
            }

        return ImageProcessorResult(result.orientation, sweepSortedDetections)
    }
}