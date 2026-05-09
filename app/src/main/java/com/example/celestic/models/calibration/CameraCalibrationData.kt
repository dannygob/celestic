package com.example.celestic.models.calibration

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents the camera calibration parameters obtained using a ChArUco pattern.
 *
 * This entity is stored in Room so calibration can be reused across sessions
 * without recalculating it every time. It includes:
 * - Intrinsic camera matrix (3x3)
 * - Distortion coefficients (k1, k2, p1, p2, k3)
 * - Image resolution used during calibration
 * - Timestamp of when the calibration was performed
 */
@Entity(tableName = "camera_calibration")
@Parcelize
data class CameraCalibrationData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Serialized 3×3 intrinsic camera matrix (OpenCV dump format). */
    @ColumnInfo(name = "camera_matrix")
    val cameraMatrix: String,

    /** Serialized distortion coefficients (1×5 vector). */
    @ColumnInfo(name = "distortion_coeffs")
    val distortionCoeffs: String,

    /** Width of the image resolution used during calibration. */
    @ColumnInfo(name = "resolution_width")
    val resolutionWidth: Int,

    /** Height of the image resolution used during calibration. */
    @ColumnInfo(name = "resolution_height")
    val resolutionHeight: Int,

    /** Timestamp of the calibration (formatted as yyyy-MM-dd HH:mm:ss). */
    @ColumnInfo(name = "calibration_date")
    val calibrationDate: String
) : Parcelable
