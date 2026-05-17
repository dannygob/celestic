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

        // Convert holes to DetectionItem (Paso 4)
        result.holes.forEachIndexed { index, hole ->
            val diameterPx = hole.radius * 2
            val diameterMm = diameterPx * scaleFactor
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
                    notes = (if (hole.hasAlodine) "Hole with alodine halo" else "Normal hole") +
                            " | Diameter: ${"%.2f".format(diameterMm)} mm" +
                            (traceabilityInfo?.let { " | Part: ${it.partName}" } ?: "")
                )
            )
        }

        // Convert countersinks to DetectionItem (Paso 4)
        result.countersinks.forEachIndexed { index, countersink ->
            val outerDiameterPx = countersink.outerRadius * 2
            val outerDiameterMm = outerDiameterPx * scaleFactor
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
                    notes = "Countersink detected | Outer Diameter: ${"%.2f".format(outerDiameterMm)} mm" +
                            (traceabilityInfo?.let { " | Part: ${it.partName}" } ?: "")
                )
            )
        }

        // Convert scratches to DetectionItem (Paso 4)
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
                    notes = "Scratch | Length: ${"%.2f".format(lengthMm)} mm" +
                            (traceabilityInfo?.let { " | Part: ${it.partName}" } ?: "")
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