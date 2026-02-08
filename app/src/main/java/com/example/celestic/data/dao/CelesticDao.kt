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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCameraCalibrationData(cameraCalibrationData: com.example.celestic.models.calibration.CameraCalibrationData)

    @Query("SELECT * FROM camera_calibration ORDER BY id DESC LIMIT 1")
    fun getCameraCalibrationData(): Flow<com.example.celestic.models.calibration.CameraCalibrationData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportConfig(reportConfig: com.example.celestic.models.report.ReportConfig)

    @Query("SELECT * FROM report_config ORDER BY id DESC LIMIT 1")
    fun getReportConfig(): Flow<com.example.celestic.models.report.ReportConfig?>

    @Query("SELECT * FROM detected_features WHERE detection_item_id = :detectionItemId")
    fun getFeaturesForDetection(detectionItemId: Long): Flow<List<DetectedFeature>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: com.example.celestic.models.Inspection): Long

    @Query("SELECT * FROM inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<com.example.celestic.models.Inspection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecification(specification: com.example.celestic.models.Specification): Long

    @Query("SELECT * FROM specifications ORDER BY id DESC LIMIT 1")
    fun getLatestSpecification(): Flow<com.example.celestic.models.Specification?>

    @Query("SELECT * FROM specifications")
    fun getAllSpecifications(): Flow<List<com.example.celestic.models.Specification>>

    @Query("SELECT * FROM detection_items WHERE id = :id")
    suspend fun getDetectionItemById(id: Long): DetectionItem?

    // Specification Features (Digital Twin Map)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecificationFeatures(features: List<com.example.celestic.models.SpecificationFeature>)

    @Query("SELECT * FROM specification_features WHERE specificationId = :specId AND face = :face")
    fun getFeaturesBySpecificationAndFace(
        specId: Long,
        face: com.example.celestic.models.enums.Orientation
    ): Flow<List<com.example.celestic.models.SpecificationFeature>>

    @Query("SELECT * FROM specification_features WHERE specificationId = :specId")
    fun getAllFeaturesBySpecification(specId: Long): Flow<List<com.example.celestic.models.SpecificationFeature>>
}