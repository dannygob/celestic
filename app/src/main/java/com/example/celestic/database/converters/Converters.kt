package com.example.celestic.database.converters


import androidx.room.TypeConverter
import com.example.celestic.models.geometry.BoundingBox
import com.google.gson.Gson

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromBoundingBox(value: BoundingBox): String = gson.toJson(value)

    @TypeConverter
    fun toBoundingBox(value: String): BoundingBox = gson.fromJson(value, BoundingBox::class.java)

    @TypeConverter
    fun fromMap(value: Map<String, Float>): String = gson.toJson(value)

    @TypeConverter
    fun toMap(value: String): Map<String, Float> =
        gson.fromJson(value, Map::class.java) as Map<String, Float>
}
