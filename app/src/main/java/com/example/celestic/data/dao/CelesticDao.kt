package com.example.celestic.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.celestic.models.DetectionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CelesticDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(item: DetectionItem)

    @Query("SELECT * FROM detection_items ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DetectionItem>>

    @Delete
    suspend fun delete(item: DetectionItem)
}