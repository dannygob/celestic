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
)

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
