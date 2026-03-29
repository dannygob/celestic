package com.example.celestic.viewmodel

enum class DetectionStatus {
    OK, WARNING, NOT_ACCEPTED
}


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