package com.example.celestic.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de plano de referencia para una lámina
 * Define las especificaciones y características esperadas
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

    @SerializedName("position_tolerance")
    val positionTolerance: Double = 5.0, // mm

    @SerializedName("requires_alodine")
    val requiresAlodine: Boolean = false
)

/**
 * Dimensiones de la lámina
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
 * Agujero esperado en el plano
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
 * Avellanado esperado en el plano
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
 * Tolerancias permitidas
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
