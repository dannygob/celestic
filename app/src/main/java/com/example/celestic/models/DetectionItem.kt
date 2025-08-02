package com.example.celestic.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import org.opencv.core.Point

@Parcelize
data class DetectionItem(
    val type: String, // Tipo: "lamina", "agujero", "avellanado", etc.

    @IgnoredOnParcel // OpenCV Point is not Parcelable, so ignore it for Parcelization
    var position: Point? = null, // Posición del elemento en la imagen

    val x: Double? = null, // X coordinate, derived from position
    val y: Double? = null, // Y coordinate, derived from position

    val width: Int? = null, // Anchura (solo aplicable a láminas)
    val height: Int? = null, // Altura (solo aplicable a láminas)
    val diameter: Int? = null // Diámetro (solo aplicable a agujeros)
) : Parcelable {
    // Secondary constructor or factory method could be used to initialize x, y from Point
    // For simplicity, assume x and y are populated when DetectionItem is created if position is known.
    // Or, ensure that before sending via Intent, x and y are set.
    // Let's modify the primary constructor to take x, y and set position if needed,
    // or expect the creator to handle this.
    // For now, assume creator of DetectionItem will populate x and y if position is available.
    // The HoleDetector currently creates DetectionItem with 'position'.
    // We'll need to adjust HoleDetector or how DetectionItem is created/used.

    // Let's refine: If HoleDetector provides Point, we should store x,y from it.
    // The primary constructor should perhaps not have x,y if Point is the source of truth during detection.
    // Then, before sending as Parcelable, x,y would be copied.
    // OR, make x,y the primary way and Point a transient helper.

    // Simpler for this step: modify DetectionItem to primarily use x, y.
    // The `HoleDetector` will need to be updated to create `DetectionItem` with x, y.
    // This change will propagate.

    // Constructor to maintain compatibility with existing HoleDetector creating with Point
    // and also allow parcelization.
    constructor(type: String, position: Point?, diameter: Int?) : this(
        type,
        position, // position will be ignored by parceler
        position?.x, // Store x
        position?.y, // Store y
        null, null, // width, height not for holes
        diameter
    )

    // Need a default constructor or ensure all properties have defaults if used by frameworks needing them
    // The primary constructor has defaults for position, x, y, width, height, diameter which is fine.
}
