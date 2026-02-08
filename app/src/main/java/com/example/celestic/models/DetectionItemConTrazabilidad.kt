package com.example.celestic.models


data class DetectionItemConTrazabilidad(
    val detectionItem: DetectionItem,
    val trazabilidad: TraceabilityItem?,
)

fun DetectionItem.conTrazabilidad(info: TraceabilityItem?): DetectionItemConTrazabilidad {
    return DetectionItemConTrazabilidad(this, info)
}