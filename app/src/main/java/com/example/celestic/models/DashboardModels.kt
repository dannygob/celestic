package com.example.celestic.models

import android.graphics.Bitmap
import com.example.celestic.models.enums.DetectionStatus

data class FaceDetectionResult(
    val roiBitmap: Bitmap,
    val faceLabel: String,
    val confidence: Float,
    val boundingBox: com.example.celestic.models.geometry.BoundingBox
)

data class ClassificationResult(
    val status: DetectionStatus,
    val type: String,
    val score: Float,
    val probabilities: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassificationResult

        if (score != other.score) return false
        if (status != other.status) return false
        if (type != other.type) return false
        if (!probabilities.contentEquals(other.probabilities)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = score.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + probabilities.contentHashCode()
        return result
    }
}

data class FrameAnalysisResult(
    val contoursCount: Int,
    val markersDetected: Int,
    val hasDeformations: Boolean,
    val holesCount: Int,
    val countersinksCount: Int,
    val scratchesCount: Int,
    val orientation: com.example.celestic.models.enums.Orientation,
    val annotatedBitmap: Bitmap? = null
)

sealed class DashboardState {
    object Idle : DashboardState()
    object CameraReady : DashboardState()
    object Processing : DashboardState()
    data class Approved(val detectionId: Long) : DashboardState()
    data class NavigateToDetails(val detectionId: Long) : DashboardState()
    data class Error(val message: String) : DashboardState()
}
