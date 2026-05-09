package com.example.celestic.models

/**
 * Combines a DetectionItem with its associated traceability information.
 *
 * Used when joining detection results with traceability metadata
 * (e.g., part number, batch, material, supplier, etc.).
 */
data class DetectionItemWithTraceability(
    val detectionItem: DetectionItem,
    val traceability: TraceabilityItem?,
)

/**
 * Extension function that attaches traceability information to a DetectionItem.
 *
 * @param info Optional TraceabilityItem associated with the detection.
 * @return A combined DetectionItemWithTraceability object.
 */
fun DetectionItem.withTraceability(info: TraceabilityItem?): DetectionItemWithTraceability {
    return DetectionItemWithTraceability(this, info)
}
