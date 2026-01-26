package com.example.celestic.utils


import com.example.celestic.models.DetectionItem

fun filterDetectionsByStatus(
    detections: List<DetectionItem>,
    status: com.example.celestic.models.enums.DetectionStatus,
): List<DetectionItem> = detections.filter { it.status == status }