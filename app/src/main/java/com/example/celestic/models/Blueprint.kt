package com.example.celestic.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a reference blueprint for a sheet or metal part.
 *
 * A blueprint defines:
 * - Expected geometry (holes, countersinks, dimensions)
 * - Tolerances for validation
 * - Whether alodine coating is required
 *
 * This model is loaded from JSON and used for:
 * - Template matching
 * - Feature validation
 * - Report generation
 */
data class Blueprint(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("dimensions")
    val dimensions: Dimensions,

    @SerializedName("expected_hole_count")
    val expectedHoleCount: Int,

    @SerializedName("expected_holes")
    val expectedHoles: List<ExpectedHole> = emptyList(),

    @SerializedName("expected_countersinks")
    val expectedCountersinks: List<ExpectedCountersink> = emptyList(),

    @SerializedName("tolerances")
    val tolerances: Tolerances = Tolerances(),

    /** Maximum allowed deviation (in mm) when matching hole positions. */
    @SerializedName("position_tolerance")
    val positionTolerance: Double = 5.0,

    /** Whether the part requires alodine coating. */
    @SerializedName("requires_alodine")
    val requiresAlodine: Boolean = false
)

/**
 * Physical dimensions of the sheet or metal part.
 */
data class Dimensions(
    @SerializedName("width_mm")
    val widthMm: Double,

    @SerializedName("height_mm")
    val heightMm: Double,

    @SerializedName("thickness_mm")
    val thicknessMm: Double = 0.0
)

/**
 * Expected hole definition inside the blueprint.
 *
 * Includes:
 * - Position (x, y)
 * - Diameter
 * - Allowed tolerance
 */
data class ExpectedHole(
    @SerializedName("id")
    val id: String,

    @SerializedName("x")
    val x: Double,

    @SerializedName("y")
    val y: Double,

    @SerializedName("diameter_mm")
    val diameterMm: Double,

    @SerializedName("tolerance_mm")
    val toleranceMm: Double = 0.1
)

/**
 * Expected countersink definition inside the blueprint.
 *
 * Includes:
 * - Position (x, y)
 * - Inner and outer diameters
 * - Allowed tolerance
 */
data class ExpectedCountersink(
    @SerializedName("id")
    val id: String,

    @SerializedName("x")
    val x: Double,

    @SerializedName("y")
    val y: Double,

    @SerializedName("outer_diameter_mm")
    val outerDiameterMm: Double,

    @SerializedName("inner_diameter_mm")
    val innerDiameterMm: Double,

    @SerializedName("tolerance_mm")
    val toleranceMm: Double = 0.1
)

/**
 * Global tolerances applied to the blueprint.
 *
 * Includes:
 * - Dimensional tolerance
 * - Hole diameter tolerance
 * - Maximum allowed scratch length
 * - Maximum number of scratches
 * - Maximum allowed deformation
 */
data class Tolerances(
    @SerializedName("dimension_tolerance_mm")
    val dimensionToleranceMm: Double = 0.5,

    @SerializedName("hole_diameter_tolerance_mm")
    val holeDiameterToleranceMm: Double = 0.1,

    @SerializedName("max_scratch_length_mm")
    val maxScratchLengthMm: Double = 5.0,

    @SerializedName("max_allowed_scratches")
    val maxAllowedScratches: Int = 2,

    @SerializedName("max_deformation_mm")
    val maxDeformationMm: Double = 0.5
)
