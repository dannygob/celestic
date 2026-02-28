package com.example.celestic.database.converters

import androidx.room.TypeConverter
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromBoundingBox(value: BoundingBox): String = gson.toJson(value)

    @TypeConverter
    fun toBoundingBox(value: String): BoundingBox = gson.fromJson(value, BoundingBox::class.java)

    // ❌ PROBLEMA: Este TypeConverter no se usa actualmente (DetectedFeature ya no tiene Map)
    // Pero lo mantenemos por si acaso
    @TypeConverter
    fun fromStringFloatMap(value: Map<String, Float>): String = gson.toJson(value)

    @TypeConverter
    fun toStringFloatMap(value: String): Map<String, Float> {
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromDetectionStatus(status: DetectionStatus): String = status.name

    @TypeConverter
    fun toDetectionStatus(value: String): DetectionStatus =
        DetectionStatus.valueOf(value)

    @TypeConverter
    fun fromDetectionType(type: DetectionType): String = type.name

    @TypeConverter
    fun toDetectionType(value: String): DetectionType =
        DetectionType.valueOf(value)
}
