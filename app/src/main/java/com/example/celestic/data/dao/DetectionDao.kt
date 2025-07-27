package com.example.celestic.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.calibration.DetectedFeature

@Dao
interface DetectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: DetectedFeature)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetections(detections: List<DetectedFeature>)

    @Delete
    suspend fun deleteDetection(item: DetectionItem)




    @Query("SELECT * FROM detected_features ORDER BY timestamp DESC")
    suspend fun getAllDetections(): List<DetectedFeature>

    @Query("DELETE FROM detected_features")
    suspend fun clearDetections()
}
