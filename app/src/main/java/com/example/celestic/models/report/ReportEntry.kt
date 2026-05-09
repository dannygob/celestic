package com.example.celestic.models.report

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import kotlinx.parcelize.Parcelize

/**
 * Represents a single entry in the inspection history or generated report.
 *
 * Each entry corresponds to:
 * - A specific frame or detection event
 * - The detected feature type (hole, countersink, scratch, etc.)
 * - The evaluation status (approved, warning, failed)
 * - Confidence score of the detection
 * - Optional measurement in millimeters
 * - Optional notes added by the system or operator
 *
 * Stored in Room for traceability and report generation.
 */
@Entity(tableName = "report_entries")
@Parcelize
data class ReportEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Identifier of the frame or capture where the detection occurred. */
    @ColumnInfo(name = "frame_id")
    val frameId: String,

    /** Timestamp of the detection event (epoch milliseconds). */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    /** Type of detected feature (hole, countersink, scratch, etc.). */
    @ColumnInfo(name = "detected_type")
    val type: DetectionType,

    /** Status assigned to the detection (OK, WARNING, FAILED). */
    @ColumnInfo(name = "status")
    val status: DetectionStatus,

    /** Confidence score of the detection (0.0–1.0). */
    @ColumnInfo(name = "confidence")
    val confidence: Float,

    /** Optional measurement in millimeters (e.g., diameter, depth, deviation). */
    @ColumnInfo(name = "measurement_mm")
    val measurementMm: Float?,

    /** Optional notes or comments associated with the detection. */
    @ColumnInfo(name = "notes")
    val notes: String = ""
) : Parcelable