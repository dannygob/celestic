package com.example.celestic.models

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import kotlinx.parcelize.Parcelize

/**
 * Represents a single detected feature or defect within an inspection session.
 *
 * Each DetectionItem belongs to a parent Inspection and contains:
 * - The frame where it was detected
 * - The type of detection (hole, countersink, scratch, etc.)
 * - Bounding box geometry
 * - Confidence score
 * - Status (approved, warning, failed)
 * - Optional measurement in millimeters
 * - Timestamp of detection
 * - Optional linked QR code
 * - Optional notes
 *
 * Stored in Room for traceability and detailed inspection history.
 */
@Entity(
    tableName = "detection_items",
    foreignKeys = [
        ForeignKey(
            entity = Inspection::class,
            parentColumns = ["id"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["inspectionId"]) // Index for faster queries
    ]
)
@Parcelize
data class DetectionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** ID of the parent inspection this detection belongs to. */
    val inspectionId: Long,

    /** Identifier of the frame where the detection occurred. */
    val frameId: String,

    /** Type of detection (hole, countersink, scratch, deformation, etc.). */
    val type: DetectionType,

    /** Bounding box of the detected feature. */
    @Embedded
    val boundingBox: BoundingBox,

    /** Confidence score of the detection (0.0–1.0). */
    val confidence: Float,

    /** Status assigned to the detection (OK, WARNING, FAILED). */
    val status: DetectionStatus,

    /** Optional measurement in millimeters (e.g., diameter, depth, deviation). */
    val measurementMm: Float? = null,

    /** Timestamp of the detection in epoch milliseconds. */
    val timestamp: Long,

    /** Optional QR code associated with the detected part. */
    val linkedQrCode: String? = null,

    /** Optional notes or comments. */
    val notes: String = ""
) : Parcelable
