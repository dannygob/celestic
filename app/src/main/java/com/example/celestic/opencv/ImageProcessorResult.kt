package com.example.celestic.opencv

import com.example.celestic.models.DetectionItem
import com.example.celestic.models.enums.Orientation

data class ImageProcessorResult(
    val orientation: Orientation,
    val detections: List<DetectionItem>
)
