package com.example.celestic.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a specification profile for a sheet or manufactured part.
 *
 * A specification defines:
 * - Dimensional limits (width, height)
 * - Hole requirements (count, diameter ranges, tolerances)
 * - Countersink requirements
 * - Allowed defect thresholds (scratches, deformations)
 * - Alodine coating requirements
 *
 * Stored in Room and used to validate inspection results.
 */
@Entity(tableName = "specifications")
data class Specification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Name of the specification profile (e.g., "Sheet A", "Part Type 12"). */
    val name: String,

    /** Type or category of sheet this specification applies to. */
    val sheetType: String,

    // ───────────────────────────────────────────────
    // Dimensions
    // ───────────────────────────────────────────────

    /** Minimum allowed width in millimeters. */
    val minWidthMm: Double,

    /** Maximum allowed width in millimeters. */
    val maxWidthMm: Double,

    /** Minimum allowed height in millimeters. */
    val minHeightMm: Double,

    /** Maximum allowed height in millimeters. */
    val maxHeightMm: Double,

    // ───────────────────────────────────────────────
    // Holes
    // ───────────────────────────────────────────────

    /** Expected number of holes. */
    val expectedHoleCount: Int,

    /** Minimum allowed hole diameter in millimeters. */
    val holeMinDiameterMm: Double,

    /** Maximum allowed hole diameter in millimeters. */
    val holeMaxDiameterMm: Double,

    /** Allowed tolerance for hole diameter. */
    val holeTolerance: Double,

    /** Nominal (ideal) hole diameter in millimeters. */
    val holeNominalDiameterMm: Double,

    // ───────────────────────────────────────────────
    // Countersinks
    // ───────────────────────────────────────────────

    /** Expected number of countersinks. */
    val expectedCountersinkCount: Int,

    /** Minimum allowed countersink diameter in millimeters. */
    val countersinkMinDiameterMm: Double,

    /** Maximum allowed countersink diameter in millimeters. */
    val countersinkMaxDiameterMm: Double,

    // ───────────────────────────────────────────────
    // Defects
    // ───────────────────────────────────────────────

    /** Maximum number of scratches allowed. */
    val maxAllowedScratches: Int,

    /** Maximum allowed scratch length in millimeters. */
    val maxScratchLengthMm: Double,

    /** Maximum number of deformations allowed. */
    val maxAllowedDeformations: Int,

    // ───────────────────────────────────────────────
    // Alodine
    // ───────────────────────────────────────────────

    /** Whether the part requires an alodine halo. */
    val requireAlodineHalo: Boolean,

    /** Minimum required uniformity score for alodine coating. */
    val minAlodineUniformity: Double
)
