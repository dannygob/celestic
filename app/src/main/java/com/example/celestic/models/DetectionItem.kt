package com.example.celestic.models

import android.os.Parcelable
import androidx.room.*
import androidx.room.vo.Entity
import com.example.celestic.detector.model.DetectionType
import com.example.celestic.models.enums.DetectionStatus
import kotlinx.parcelize.Parcelize

@Entity(tableName = "detection_items")
@Parcelize
data class DetectionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val frameId: String,
    val type: DetectionType,
    @Embedded val boundingBox: com.example.celestic.models.geometry.BoundingBox,
    val confidence: Float,
    val status: DetectionStatus,
    val measurementMm: Float? = null,
    val timestamp: Long,
    val linkedQrCode: String? = null,
    val notes: String = "",
) : Parcelable