package com.example.celestic.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Data class for traceability information (Digital Twin / Batch Info).
 * Named TraceabilityItem in English to avoid mixed languages in code.
 */
@Parcelize
@Entity(tableName = "traceability_items")
data class TraceabilityItem(
    @PrimaryKey
    val code: String,
    val partName: String,
    val operatorName: String,
    val inspectionDate: String,
    val finalStatus: String
) : Parcelable

