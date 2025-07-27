package com.example.celestic.database

import androidx.room.*
import com.celestic.database.converters.DetectionStatusConverter
import com.celestic.database.converters.DetectionTypeConverter
import com.celestic.model.DetectionItem
import com.example.celestic.data.dao.CelesticDao

@Database(entities = [DetectionItem::class], version = 1)
@TypeConverters(DetectionTypeConverter::class, DetectionStatusConverter::class)
abstract class CelesticDatabase : RoomDatabase() {
    abstract fun celesticDao(): CelesticDao
}