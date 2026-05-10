package com.example.celestic.models.geometry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represent el área deliminator de una characteristic detected
 * en coordenadas relatives (pixels o escala normalize).
 */
@Parcelize
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) : Parcelable {
    fun width(): Float = right - left
    fun height(): Float = bottom - top
}