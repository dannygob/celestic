package com.example.celestic.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents traceability information associated with a part or inspection.
 *
 * This includes:
 * - Unique traceability code (QR, batch code, serial number, etc.)
 * - Part name or identifier
 * - Operator responsible for the inspection
 * - Date of inspection
 * - Final status assigned to the part
 *
 * Stored in Room and linked to detection items when needed.
 */
@Parcelize
@Entity(tableName = "traceability_items")
data class TraceabilityItem(
    @PrimaryKey
    val code: String,

    /** Name of the part or component being inspected. */
    val partName: String,

    /** Name of the operator who performed the inspection. */
    val operatorName: String,

    /** Date of the inspection (formatted string). */
    val inspectionDate: String,

    /** Final status assigned to the part (e.g., OK, FAILED, REWORK). */
    val finalStatus: String
) : Parcelable
