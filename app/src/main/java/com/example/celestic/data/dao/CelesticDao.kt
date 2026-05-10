package com.example.celestic.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    /** Inserts a detection item into the database. Replaces it if it already exists. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionItem(item: DetectionItem): Long

    /** Returns all detection items ordered by timestamp (newest first). */
    @Query("SELECT * FROM detection_items ORDER BY timestamp DESC")
    fun getAllDetectionItems(): Flow<List<DetectionItem>>

    /** Returns a list of unique QR codes/batch numbers from detections. */
    @Query("SELECT DISTINCT linkedQrCode FROM detection_items WHERE linkedQrCode IS NOT NULL")
    fun getUniqueBatches(): Flow<List<String>>

    /** Returns all detection items associated with a specific inspection. */
    @Query("SELECT * FROM detection_items WHERE inspectionId = :inspectionId")
    fun getDetectionItemsByInspection(inspectionId: Long): Flow<List<DetectionItem>>

    /** Deletes a specific detection item. */
    @Delete
    suspend fun deleteDetectionItem(item: DetectionItem)

    /** Retrieves a detection item by its ID. Returns null if not found. */
    @Query("SELECT * FROM detection_items WHERE id = :id")
    suspend fun getDetectionItemById(id: Long): DetectionItem?

    // ===== DETECTED FEATURES =====

    /** Inserts a detected feature. Replaces it if it already exists. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectedFeature(detection: DetectedFeature)

    /** Inserts a list of detected features. Replaces existing ones if needed. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectedFeatures(detections: List<DetectedFeature>)

    /** Returns all detected features ordered by timestamp (newest first). */
    @Query("SELECT * FROM detected_features ORDER BY timestamp DESC")
    fun getAllDetectedFeatures(): Flow<List<DetectedFeature>>

    /** Deletes all detected features from the table. */
    @Query("DELETE FROM detected_features")
    suspend fun clearDetectedFeatures()

    /** Returns all detected features linked to a specific detection item. */
    @Query("SELECT * FROM detected_features WHERE detection_item_id = :detectionItemId")
    fun getFeaturesForDetection(detectionItemId: Long): Flow<List<DetectedFeature>>

    // ===== CAMERA CALIBRATION =====

    /** Inserts camera calibration data. Replaces existing data if needed. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCameraCalibrationData(cameraCalibrationData: CameraCalibrationData)

    /** Retrieves the most recent camera calibration data. */
    @Query("SELECT * FROM camera_calibration ORDER BY id DESC LIMIT 1")
    fun getCameraCalibrationData(): Flow<CameraCalibrationData?>

    // ===== REPORT CONFIG =====

    /** Inserts a report configuration. Replaces it if it already exists. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportConfig(reportConfig: ReportConfig)

    /** Retrieves the latest saved report configuration. */
    @Query("SELECT * FROM report_config ORDER BY id DESC LIMIT 1")
    fun getReportConfig(): Flow<ReportConfig?>

    // ===== INSPECTIONS =====

    /** Inserts an inspection and returns its generated ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: Inspection): Long

    /** Returns all inspections ordered by timestamp (newest first). */
    @Query("SELECT * FROM inspections ORDER BY timestamp DESC")
    fun getAllInspections(): Flow<List<Inspection>>

    /** Retrieves an inspection by its ID. Returns null if not found. */
    @Query("SELECT * FROM inspections WHERE id = :id")
    suspend fun getInspectionById(id: Long): Inspection?

    // ===== SPECIFICATIONS =====

    /** Inserts a specification and returns its generated ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecification(specification: Specification): Long

    /** Retrieves the most recently saved specification. */
    @Query("SELECT * FROM specifications ORDER BY id DESC LIMIT 1")
    fun getLatestSpecification(): Flow<Specification?>

    /** Returns all specifications stored in the database. */
    @Query("SELECT * FROM specifications")
    fun getAllSpecifications(): Flow<List<Specification>>

    /** Retrieves a specification by its ID. Returns null if not found. */
    @Query("SELECT * FROM specifications WHERE id = :id")
    suspend fun getSpecificationById(id: Long): Specification?

    // ===== SPECIFICATION FEATURES =====

    /** Inserts a list of specification features. Replaces existing ones if needed. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecificationFeatures(features: List<SpecificationFeature>)

    /** Returns all features for a given specification and face orientation. */
    @Query("SELECT * FROM specification_features WHERE specificationId = :specId AND face = :face")
    fun getFeaturesBySpecificationAndFace(
        specId: Long,
        face: Orientation
    ): Flow<List<SpecificationFeature>>

    /** Returns all features associated with a specific specification. */
    @Query("SELECT * FROM specification_features WHERE specificationId = :specId")
    fun getAllFeaturesBySpecification(specId: Long): Flow<List<SpecificationFeature>>

    /** Deletes all features associated with a specific specification. */
    @Query("DELETE FROM specification_features WHERE specificationId = :specId")
    suspend fun deleteFeaturesBySpecification(specId: Long)
}
