package com.example.celestic.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.enums.Orientation

/**
 * Represents a specific expected feature defined inside a Specification.
 *
 * A SpecificationFeature describes:
 * - Which face of the sheet it belongs to (front/back)
 * - The type of feature (hole, countersink, scratch, etc.)
 * - Its expected position in millimeters
 * - Dimensional requirements and tolerances
 * - Optional quality requirements (alodine, countersink presence)
 *
 * Stored in Room and linked to a parent Specification.
 */
@Entity(
    tableName = "specification_features",
    foreignKeys = [
        ForeignKey(
            entity = Specification::class,
            parentColumns = ["id"],
            childColumns = ["specificationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["specificationId"]) // Index for faster queries
    ]
)
data class SpecificationFeature(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID of the parent specification this feature belongs to. */
    val specificationId: Long,

    /** Face of the sheet where the feature is expected (FRONT or BACK). */
    val face: Orientation,

    /** Type of feature (HOLE, COUNTERSINK, SCRATCH, etc.). */
    val type: DetectionType,

    // ───────────────────────────────────────────────
    // Position (in millimeters)
    // ───────────────────────────────────────────────

    /** X‑coordinate of the expected feature position in millimeters. */
    val positionX_mm: Double,

    /** Y‑coordinate of the expected feature position in millimeters. */
    val positionY_mm: Double,

    // ───────────────────────────────────────────────
    // Dimensions and Tolerances
    // ───────────────────────────────────────────────

    /** Expected diameter of the feature in millimeters. */
    val diameter_mm: Double,

    /** Allowed tolerance for the feature's diameter. */
    val tolerance_mm: Double = 0.5,

    // ───────────────────────────────────────────────
    // Quality Requirements
    // ───────────────────────────────────────────────

    /** Whether alodine coating is required for this feature. */
    val requireAlodine: Boolean = false,

    /** Whether a countersink is required for this feature. */
    val requireCountersink: Boolean = false
)
