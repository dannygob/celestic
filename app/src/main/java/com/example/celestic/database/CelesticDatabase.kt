package com.example.celestic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.celestic.data.dao.CelesticDao
import com.example.celestic.database.converters.Converters
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.Inspection
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.TraceabilityItem
import com.example.celestic.models.calibration.CameraCalibrationData
import com.example.celestic.models.calibration.DetectedFeature
import com.example.celestic.models.report.ReportConfig

/**
 * Main Room database for the Celestic application.
 *
 * This database stores all core entities related to inspections, detections,
 * specifications, calibration data, and reporting.
 *
 * It exposes a single DAO (CelesticDao) that provides access to all tables.
 */
@Database(
    entities = [
        DetectionItem::class,
        DetectedFeature::class,
        CameraCalibrationData::class,
        ReportConfig::class,
        Inspection::class,
        Specification::class,
        SpecificationFeature::class,
        TraceabilityItem::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CelesticDatabase : RoomDatabase() {

    /** Provides access to all database operations through the CelesticDao. */
    abstract fun celesticDao(): CelesticDao

    companion object {
        @Volatile
        private var INSTANCE: CelesticDatabase? = null

        /**
         * Returns the singleton instance of the database.
         *
         * Uses double-checked locking to ensure thread safety and avoid
         * unnecessary reinitialization.
         *
         * fallbackToDestructiveMigration() is enabled, meaning that if the
         * schema changes without a migration, the database will be cleared
         * and recreated.
         */
        fun getDatabase(context: Context): CelesticDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CelesticDatabase::class.java,
                    "celestic_database"
                )
                    // Destroys and rebuilds the database if migration is missing
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
