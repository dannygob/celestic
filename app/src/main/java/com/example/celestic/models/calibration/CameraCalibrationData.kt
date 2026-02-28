package com.example.celestic.models.calibration

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Representa los parámetros de calibración obtenidos mediante patrón Charuco.
 * Se guarda en Room para reutilización y validación.
 */
@Entity(tableName = "camera_calibration")
@Parcelize
data class CameraCalibrationData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "camera_matrix")
    val cameraMatrix: String,

    @ColumnInfo(name = "distortion_coeffs")
    val distortionCoeffs: String,

    @ColumnInfo(name = "resolution_width")
    val resolutionWidth: Int,

    @ColumnInfo(name = "resolution_height")
    val resolutionHeight: Int,

    @ColumnInfo(name = "calibration_date")
    val calibrationDate: String
) : Parcelable