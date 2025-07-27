package com.example.celestic.database.converters

import androidx.room.TypeConverter
import com.example.celestic.models.enums.DetectionStatus

/**
 * Conversor Room que permite guardar DetectionStatus como texto en la base de datos.
 */
class DetectionStatusConverter {
    @TypeConverter
    fun from(status: DetectionStatus): String = status.name

    @TypeConverter
    fun to(value: String): DetectionStatus = DetectionStatus.valueOf(value)
}