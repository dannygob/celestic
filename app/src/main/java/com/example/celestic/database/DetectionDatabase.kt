package com.example.celestic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.celestic.data.dao.DetectionDao
import com.example.celestic.models.calibration.DetectedFeature

@Database(entities = [DetectedFeature::class], version = 1, exportSchema = false)
abstract class DetectionDatabase : RoomDatabase() {

    abstract fun detectionDao(): DetectionDao

    companion object {
        @Volatile
        private var INSTANCE: DetectionDatabase? = null

        fun getDatabase(context: Context): DetectionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DetectionDatabase::class.java,
                    "detection_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}