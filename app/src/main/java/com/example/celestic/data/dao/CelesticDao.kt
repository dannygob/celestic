package com.example.celestic.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.calibration.DetectedFeature
import kotlinx.coroutines.flow.Flow

@Dao
interface CelesticDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DetectionItem)

    @Query("SELECT * FROM detection_items ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DetectionItem>>

    @Delete
    suspend fun delete(item: DetectionItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: DetectedFeature)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetections(detections: List<DetectedFeature>)

    @Query("SELECT * FROM detected_features ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectedFeature>>

    @Query("DELETE FROM detected_features")
    suspend fun clearDetections()
}