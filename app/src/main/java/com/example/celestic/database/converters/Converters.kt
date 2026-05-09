package com.example.celestic.database.converters

import androidx.room.TypeConverter
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters used to convert complex objects into storable formats
 * (usually JSON strings) and back into Kotlin objects.
 *
 * These converters allow Room to persist custom data types that are not
 * natively supported by SQLite.
 */
class Converters {

    private val gson = Gson()

    // ===== BOUNDING BOX =====

    /**
     * Converts a BoundingBox object into a JSON string for database storage.
     */
    @TypeConverter
    fun fromBoundingBox(value: BoundingBox): String = gson.toJson(value)

    /**
     * Converts a JSON string back into a BoundingBox object.
     */
    @TypeConverter
    fun toBoundingBox(value: String): BoundingBox =
        gson.fromJson(value, BoundingBox::class.java)

    // ===== STRING-FLOAT MAP (LEGACY) =====

    /**
     * Converts a Map<String, Float> into a JSON string.
     *
     * NOTE: This converter is currently unused because DetectedFeature
     * no longer stores a map. It is kept for backward compatibility.
     */
    @TypeConverter
    fun fromStringFloatMap(value: Map<String, Float>): String = gson.toJson(value)

    /**
     * Converts a JSON string back into a Map<String, Float>.
     * Returns an empty map if parsing fails.
     */
    @TypeConverter
    fun toStringFloatMap(value: String): Map<String, Float> {
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    // ===== DETECTION STATUS =====

    /**
     * Converts a DetectionStatus enum into its string name.
     */
    @TypeConverter
    fun fromDetectionStatus(status: DetectionStatus): String = status.name

    /**
     * Converts a string back into a DetectionStatus enum.
     */
    @TypeConverter
    fun toDetectionStatus(value: String): DetectionStatus =
        DetectionStatus.valueOf(value)

    // ===== DETECTION TYPE =====

    /**
     * Converts a DetectionType enum into its string name.
     */
    @TypeConverter
    fun fromDetectionType(type: DetectionType): String = type.name

    /**
     * Converts a string back into a DetectionType enum.
     */
    @TypeConverter
    fun toDetectionType(value: String): DetectionType =
        DetectionType.valueOf(value)
}
