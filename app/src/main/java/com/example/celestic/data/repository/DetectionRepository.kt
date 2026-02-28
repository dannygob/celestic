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
    suspend fun insertDetection(item: DetectionItem): Long =
        dao.insertDetectionItem(item)

    suspend fun deleteDetection(item: DetectionItem) =
        dao.deleteDetectionItem(item)

    fun getAllDetectionItems(): Flow<List<DetectionItem>> =
        dao.getAllDetectionItems()

    fun getDetectionItemsByInspection(inspectionId: Long): Flow<List<DetectionItem>> =
        dao.getDetectionItemsByInspection(inspectionId)

    suspend fun getDetectionById(id: Long): DetectionItem? =
        dao.getDetectionItemById(id)

    // ===== DETECTED FEATURES =====
    suspend fun insertDetectedFeature(detection: DetectedFeature) {
        dao.insertDetectedFeature(detection)
    }

    suspend fun insertDetectedFeatures(detections: List<DetectedFeature>) {
        dao.insertDetectedFeatures(detections)
    }

    fun getAllDetectedFeatures(): Flow<List<DetectedFeature>> =
        dao.getAllDetectedFeatures()

    suspend fun clearDetectedFeatures() {
        dao.clearDetectedFeatures()
    }

    fun getFeaturesForDetection(detectionItemId: Long): Flow<List<DetectedFeature>> =
        dao.getFeaturesForDetection(detectionItemId)

    // ===== INSPECTIONS =====
    suspend fun startInspection(): Long {
        val inspection = Inspection(timestamp = System.currentTimeMillis())
        return dao.insertInspection(inspection)
    }

    fun getAllInspections(): Flow<List<Inspection>> =
        dao.getAllInspections()

    suspend fun getInspectionById(id: Long): Inspection? =
        dao.getInspectionById(id)

    // ===== SPECIFICATIONS =====
    suspend fun insertSpecification(specification: Specification): Long {
        return dao.insertSpecification(specification)
    }

    fun getLatestSpecification(): Flow<Specification?> =
        dao.getLatestSpecification()

    fun getAllSpecifications(): Flow<List<Specification>> =
        dao.getAllSpecifications()

    suspend fun getSpecificationById(id: Long): Specification? =
        dao.getSpecificationById(id)

    // ===== SPECIFICATION FEATURES =====
    suspend fun insertSpecificationFeatures(features: List<SpecificationFeature>) {
        dao.insertSpecificationFeatures(features)
    }

    fun getFeaturesBySpecificationAndFace(
        specId: Long,
        face: Orientation
    ): Flow<List<SpecificationFeature>> =
        dao.getFeaturesBySpecificationAndFace(specId, face)

    fun getAllFeaturesBySpecification(specId: Long): Flow<List<SpecificationFeature>> =
        dao.getAllFeaturesBySpecification(specId)

    suspend fun deleteFeaturesBySpecification(specId: Long) {
        dao.deleteFeaturesBySpecification(specId)
    }

    // ===== CAMERA CALIBRATION =====
    suspend fun insertCameraCalibrationData(cameraCalibrationData: CameraCalibrationData) {
        dao.insertCameraCalibrationData(cameraCalibrationData)
    }

    fun getCameraCalibrationData(): Flow<CameraCalibrationData?> =
        dao.getCameraCalibrationData()

    // ===== REPORT CONFIG =====
    suspend fun insertReportConfig(reportConfig: ReportConfig) {
        dao.insertReportConfig(reportConfig)
    }

    fun getReportConfig(): Flow<ReportConfig?> =
        dao.getReportConfig()

    // ===== IMAGE MANAGEMENT =====
    fun saveImage(bitmap: Bitmap, filename: String): String {
        val dir = File(context.filesDir, "detection_images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$filename.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(CompressFormat.JPEG, 90, out)
            }
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // ===== TRANSACTIONAL OPERATIONS =====
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
