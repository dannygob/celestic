package com.example.celestic.detector.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetectionItem(
    val id: Int,
    val type: DetectionType,       // Enum: ALODINE, COUNTERSINK, etc.
    val confidence: Float,         // Entre 0.0 y 1.0
    val timestamp: Long,           // Epoch millis
    val boundingBox: BoundingBox, // Posición en la imagen
    val notes: String = "",
) : Parcelable

@Parcelize
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) : Parcelable

enum class DetectionType {
    ALODINE, COUNTERSINK, HOLE, UNKNOWN
}