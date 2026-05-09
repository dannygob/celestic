package com.example.celestic.models.calibration

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a single detected feature during calibration or inspection.
 *
 * This entity is stored in Room and is used to keep track of:
 * - Feature type (e.g., ChArUco corner, ArUco marker, edge, etc.)
 * - Pixel coordinates of the detected feature
 * - Detection confidence score
 * - Timestamp of when the feature was detected
 * - Optional measurement metadata (e.g., distances, angles, quality metrics)
 *
 * Each feature is linked to a parent detection item via `detectionItemId`.
 */
@Entity(tableName = "detected_features")
@Parcelize
data class DetectedFeature(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Foreign key linking this feature to a detection session or parent entity. */
    @ColumnInfo(name = "detection_item_id")
    val detectionItemId: Long,

    /** Type of detected feature (e.g., "charuco_corner", "aruco_marker", "edge"). */
    @ColumnInfo(name = "feature_type")
    val featureType: String,

    /** X coordinate of the detected feature in pixel space. */
    @ColumnInfo(name = "x_coord")
    val xCoord: Float,

    /** Y coordinate of the detected feature in pixel space. */
    @ColumnInfo(name = "y_coord")
    val yCoord: Float,

    /** Confidence score of the detection (0.0–1.0). */
    @ColumnInfo(name = "confidence")
    val confidence: Float,

    /** Timestamp of the detection in epoch milliseconds. */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    /**
     * Optional measurement metadata associated with the feature.
     * Example: {"radius": 3.2, "angle": 45.0}
     */
    @ColumnInfo(name = "measurements")
    val measurements: Map<String, Float> = emptyMap(),
) : Parcelable
