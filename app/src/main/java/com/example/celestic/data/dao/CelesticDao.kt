package com.example.celestic.data.dao

import androidx.room.*
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.Inspection
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.calibration.CameraCalibrationData
import com.example.celestic.models.calibration.DetectedFeature
import com.example.celestic.models.enums.Orientation
import com.example.celestic.models.report.ReportConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface CelesticDao {
    // ===== DETECTION ITEMS =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionItem(item: DetectionItem): Long

    @Query("SELECT * FROM detection_items ORDER BY timestamp DESC")
    fun getAllDetectionItems(): Flow<List<DetectionItem>>

    @Query("SELECT * FROM detection_items WHERE inspectionId = :inspectionId")
    fun getDetectionItemsByInspection(inspectionId: Long): Flow<List<DetectionItem>>

    @Delete
    suspend fun deleteDetectionItem(item: DetectionItem)

    @Query("SELECT * FROM detection_items WHERE id = :id")
    suspend fun getDetectionItemById(id: Long): DetectionItem?

    // ===== DETECTED FEATURES =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectedFeature(detection: DetectedFeature)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectedFeatures(detections: List<DetectedFeature>)

    @Query("SELECT * FROM detected_features ORDER BY timestamp DESC")
    fun getAllDetectedFeatures(): Flow<List<DetectedFeature>>

    @Query("DELETE FROM detected_features")
    suspend fun clearDetectedFeatures()

    @Query("SELECT * FROM detected_features WHERE detection_item_id = :detectionItemId")
    fun getFeaturesForDetection(detectionItemId: Long): Flow<List<DetectedFeature>>

    // ===== CAMERA CALIBRATION =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCameraCalibrationData(cameraCalibrationData: CameraCalibrationData)

    @Query("SELECT * FROM camera_calibration ORDER BY id DESC LIMIT 1")
    fun getCameraCalibrationData(): Flow<CameraCalibrationData?>

    // ===== REPORT CONFIG =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportConfig(reportConfig: ReportConfig)

    @Query("SELECT * FROM report_config ORDER BY id DESC LIMIT 1")
    fun getReportConfig(): Flow<ReportConfig?>

    // ===== INSPECTIONS =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: Inspection): Long

    @Query("SELECT * FROM inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<Inspection>>

    @Query("SELECT * FROM inspections WHERE id = :id")
    suspend fun getInspectionById(id: Long): Inspection?

    // ===== SPECIFICATIONS =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecification(specification: Specification): Long

    @Query("SELECT * FROM specifications ORDER BY id DESC LIMIT 1")
    fun getLatestSpecification(): Flow<Specification?>

    @Query("SELECT * FROM specifications")
    fun getAllSpecifications(): Flow<List<Specification>>

    @Query("SELECT * FROM specifications WHERE id = :id")
    suspend fun getSpecificationById(id: Long): Specification?

    // ===== SPECIFICATION FEATURES =====
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecificationFeatures(features: List<SpecificationFeature>)

    @Query("SELECT * FROM specification_features WHERE specificationId = :specId AND face = :face")
    fun getFeaturesBySpecificationAndFace(
        specId: Long,
        face: Orientation
    ): Flow<List<SpecificationFeature>>

    @Query("SELECT * FROM specification_features WHERE specificationId = :specId")
    fun getAllFeaturesBySpecification(specId: Long): Flow<List<SpecificationFeature>>

    @Query("DELETE FROM specification_features WHERE specificationId = :specId")
    suspend fun deleteFeaturesBySpecification(specId: Long)
}
