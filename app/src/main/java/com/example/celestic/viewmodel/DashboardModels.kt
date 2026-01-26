package com.example.celestic.viewmodel

import android.graphics.Bitmap
import android.graphics.Rect

enum class DetectionStatus {
    OK, WARNING, NOT_ACCEPTED
}

data class FaceDetectionResult(
    val faceLabel: String,      // "anverso" o "reverso"
    val boundingBox: Rect,
    val roiBitmap: Bitmap
)

data class ClassificationResult(
    val status: DetectionStatus,
    val labels: List<String>
)

data class FrameFeature(
    val type: String,
    val measurement: Float?,
    val position: String?
)

data class FrameAnalysisResult(
    val features: List<FrameFeature>
)