package com.example.celestic.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an inspection session performed by the system.
 *
 * Each inspection stores:
 * - A unique ID
 * - Timestamp of when the inspection occurred
 * - Optional GPS coordinates (latitude/longitude)
 *
 * Used as the parent entity for DetectionItem entries.
 */
@Entity(tableName = "inspections")
data class Inspection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Timestamp of the inspection in epoch milliseconds. */
    val timestamp: Long,

    /** Optional latitude where the inspection took place. */
    val latitude: Double? = null,

    /** Optional longitude where the inspection took place. */
    val longitude: Double? = null
)
