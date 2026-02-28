package com.example.celestic.opencv

import com.example.celestic.manager.CalibrationManager
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import com.example.celestic.viewmodel.MarkerType
import org.opencv.core.Mat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessor @Inject constructor(
    private val frameAnalyzer: FrameAnalyzer,
    private val calibrationManager: CalibrationManager
) {

    fun processImage(mat: Mat, markerType: MarkerType?): List<DetectionItem> {
        val result = frameAnalyzer.analyze(mat, markerType)

        val detectionItems = mutableListOf<DetectionItem>()

        // Convert holes to DetectionItem
        result.holes.forEachIndexed { index, hole ->
            detectionItems.add(
                DetectionItem(
                    id = 0, // Will be set by database
                    inspectionId = 0, // Will be set by ViewModel
                    frameId = "frame_${System.currentTimeMillis()}_hole_$index",
                    type = DetectionType.HOLE,
                    confidence = 0.9f, // Temporary - improve with actual confidence
                    status = if (hole.hasAlodine) DetectionStatus.WARNING else DetectionStatus.OK,
                    timestamp = System.currentTimeMillis(),
                    boundingBox = BoundingBox(
                        left = (hole.center.x - hole.radius).toFloat(),
                        top = (hole.center.y - hole.radius).toFloat(),
                        right = (hole.center.x + hole.radius).toFloat(),
                        bottom = (hole.center.y + hole.radius).toFloat()
                    ),
                    notes = if (hole.hasAlodine) "Hole with alodine halo" else "Normal hole"
                )
            )
        }

        // Convert countersinks to DetectionItem
        result.countersinks.forEachIndexed { index, countersink ->
            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "frame_${System.currentTimeMillis()}_cs_$index",
                    type = DetectionType.COUNTERSINK,
                    confidence = 0.9f,
                    status = DetectionStatus.OK,
                    timestamp = System.currentTimeMillis(),
                    boundingBox = BoundingBox(
                        left = (countersink.center.x - countersink.outerRadius).toFloat(),
                        top = (countersink.center.y - countersink.outerRadius).toFloat(),
                        right = (countersink.center.x + countersink.outerRadius).toFloat(),
                        bottom = (countersink.center.y + countersink.outerRadius).toFloat()
                    ),
                    notes = "Countersink detected"
                )
            )
        }

        // Convert scratches to DetectionItem
        result.scratches.forEachIndexed { index, scratch ->
            detectionItems.add(
                DetectionItem(
                    id = 0,
                    inspectionId = 0,
                    frameId = "frame_${System.currentTimeMillis()}_scr_$index",
                    type = DetectionType.SCRATCH,
                    confidence = 0.9f,
                    status = DetectionStatus.WARNING,
                    timestamp = System.currentTimeMillis(),
                    boundingBox = BoundingBox(
                        left = scratch.startPoint.x.toFloat(),
                        top = scratch.startPoint.y.toFloat(),
                        right = scratch.endPoint.x.toFloat(),
                        bottom = scratch.endPoint.y.toFloat()
                    ),
                    notes = "Scratch length: ${"%.2f".format(scratch.length)} pixels"
                )
            )
        }

        return detectionItems
    }
}
