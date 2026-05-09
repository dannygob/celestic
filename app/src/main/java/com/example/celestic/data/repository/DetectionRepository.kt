package com.example.celestic.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import com.example.celestic.data.dao.CelesticDao
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.Inspection
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.calibration.CameraCalibrationData
import com.example.celestic.models.calibration.DetectedFeature
import com.example.celestic.models.enums.Orientation
import com.example.celestic.models.report.ReportConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionRepository @Inject constructor(
    private val dao: CelesticDao,
    @ApplicationContext private val context: Context
) {

    // ===== DETECTION ITEMS =====

    /** Inserts a detection item and returns its generated ID. */
    suspend fun insertDetection(item: DetectionItem): Long =
        dao.insertDetectionItem(item)

    /** Deletes a specific detection item. */
    suspend fun deleteDetection(item: DetectionItem) =
        dao.deleteDetectionItem(item)

    /** Retrieves all detection items ordered by timestamp. */
    fun getAllDetectionItems(): Flow<List<DetectionItem>> =
        dao.getAllDetectionItems()

    /** Retrieves all detection items linked to a specific inspection. */
    fun getDetectionItemsByInspection(inspectionId: Long): Flow<List<DetectionItem>> =
        dao.getDetectionItemsByInspection(inspectionId)

    /** Retrieves a detection item by its ID. */
    suspend fun getDetectionById(id: Long): DetectionItem? =
        dao.getDetectionItemById(id)

    // ===== DETECTED FEATURES =====

    /** Inserts a single detected feature. */
    suspend fun insertDetectedFeature(detection: DetectedFeature) {
        dao.insertDetectedFeature(detection)
    }

    /** Inserts a list of detected features. */
    suspend fun insertDetectedFeatures(detections: List<DetectedFeature>) {
        dao.insertDetectedFeatures(detections)
    }

    /** Retrieves all detected features ordered by timestamp. */
    fun getAllDetectedFeatures(): Flow<List<DetectedFeature>> =
        dao.getAllDetectedFeatures()

    /** Deletes all detected features from the database. */
    suspend fun clearDetectedFeatures() {
        dao.clearDetectedFeatures()
    }

    /** Retrieves all detected features associated with a detection item. */
    fun getFeaturesForDetection(detectionItemId: Long): Flow<List<DetectedFeature>> =
        dao.getFeaturesForDetection(detectionItemId)

    // ===== INSPECTIONS =====

    /**
     * Creates a new inspection with optional GPS coordinates.
     * Returns the generated inspection ID.
     */
    suspend fun startInspection(latitude: Double? = null, longitude: Double? = null): Long {
        val inspection = Inspection(
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )
        return dao.insertInspection(inspection)
    }

    /** Retrieves all inspections ordered by timestamp. */
    fun getAllInspections(): Flow<List<Inspection>> =
        dao.getAllInspections()

    /** Retrieves an inspection by its ID. */
    suspend fun getInspectionById(id: Long): Inspection? =
        dao.getInspectionById(id)

    // ===== SPECIFICATIONS =====

    /** Inserts a specification and returns its generated ID. */
    suspend fun insertSpecification(specification: Specification): Long {
        return dao.insertSpecification(specification)
    }

    /** Retrieves the most recently saved specification. */
    fun getLatestSpecification(): Flow<Specification?> =
        dao.getLatestSpecification()

    /** Retrieves all specifications. */
    fun getAllSpecifications(): Flow<List<Specification>> =
        dao.getAllSpecifications()

    /** Retrieves a specification by its ID. */
    suspend fun getSpecificationById(id: Long): Specification? =
        dao.getSpecificationById(id)

    // ===== SPECIFICATION FEATURES =====

    /** Inserts a list of specification features. */
    suspend fun insertSpecificationFeatures(features: List<SpecificationFeature>) {
        dao.insertSpecificationFeatures(features)
    }

    /** Retrieves features for a specification filtered by face orientation. */
    fun getFeaturesBySpecificationAndFace(
        specId: Long,
        face: Orientation
    ): Flow<List<SpecificationFeature>> =
        dao.getFeaturesBySpecificationAndFace(specId, face)

    /** Retrieves all features associated with a specification. */
    fun getAllFeaturesBySpecification(specId: Long): Flow<List<SpecificationFeature>> =
        dao.getAllFeaturesBySpecification(specId)

    /** Deletes all features linked to a specification. */
    suspend fun deleteFeaturesBySpecification(specId: Long) {
        dao.deleteFeaturesBySpecification(specId)
    }

    // ===== CAMERA CALIBRATION =====

    /** Inserts camera calibration data. */
    suspend fun insertCameraCalibrationData(cameraCalibrationData: CameraCalibrationData) {
        dao.insertCameraCalibrationData(cameraCalibrationData)
    }

    /** Retrieves the latest camera calibration data. */
    fun getCameraCalibrationData(): Flow<CameraCalibrationData?> =
        dao.getCameraCalibrationData()

    // ===== REPORT CONFIG =====

    /** Inserts a report configuration. */
    suspend fun insertReportConfig(reportConfig: ReportConfig) {
        dao.insertReportConfig(reportConfig)
    }

    /** Retrieves the latest report configuration. */
    fun getReportConfig(): Flow<ReportConfig?> =
        dao.getReportConfig()

    // ===== IMAGE MANAGEMENT =====

    /**
     * Saves a bitmap as a JPEG file inside the app's internal storage.
     * Returns the absolute file path or an empty string if saving fails.
     */
    fun saveImage(bitmap: Bitmap, filename: String): String {
        val dir = File(context.filesDir, "detection_images")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "$filename.jpg")

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // ===== TRANSACTIONAL OPERATIONS =====

    /**
     * Saves an inspection and all associated detection items.
     * Returns the generated inspection ID.
     */
    suspend fun saveInspectionWithDetections(
        inspection: Inspection,
        detections: List<DetectionItem>
    ): Long {
        val inspectionId = dao.insertInspection(inspection)

        detections.forEach { detection ->
            val detectionWithInspection = detection.copy(inspectionId = inspectionId)
            dao.insertDetectionItem(detectionWithInspection)
        }

        return inspectionId
    }

    /**
     * Saves a specification and all its features.
     * Returns the generated specification ID.
     */
    suspend fun saveSpecificationWithFeatures(
        specification: Specification,
        features: List<SpecificationFeature>
    ): Long {
        val specId = dao.insertSpecification(specification)

        val featuresWithSpec = features.map { it.copy(specificationId = specId) }
        dao.insertSpecificationFeatures(featuresWithSpec)

        return specId
    }
}
