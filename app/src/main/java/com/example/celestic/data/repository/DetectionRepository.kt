package com.example.celestic.data.repository

import android.content.Context
import com.example.celestic.data.dao.CelesticDao
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.calibration.DetectedFeature
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionRepository @Inject constructor(
    private val dao: CelesticDao,
    @field:ApplicationContext private val context: Context
) {

    suspend fun saveDetection(detection: DetectedFeature) {
        dao.insertDetection(detection)
    }

    suspend fun saveDetections(detections: List<DetectedFeature>) {
        dao.insertDetections(detections)
    }

    fun loadDetections(): kotlinx.coroutines.flow.Flow<List<DetectedFeature>> {
        return dao.getAllDetections()
    }

    suspend fun clearAllDetections() {
        dao.clearDetections()
    }

    suspend fun insertDetection(item: DetectionItem): Long = dao.insert(item)

    suspend fun deleteDetection(item: DetectionItem) = dao.delete(item)

    suspend fun insertCameraCalibrationData(cameraCalibrationData: com.example.celestic.models.calibration.CameraCalibrationData) {
        dao.insertCameraCalibrationData(cameraCalibrationData)
    }

    fun getCameraCalibrationData(): kotlinx.coroutines.flow.Flow<com.example.celestic.models.calibration.CameraCalibrationData?> {
        return dao.getCameraCalibrationData()
    }

    fun saveImage(bitmap: android.graphics.Bitmap, filename: String): String {
        val dir = File(context.filesDir, "detection_images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$filename.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    suspend fun insertReportConfig(reportConfig: com.example.celestic.models.report.ReportConfig) {
        dao.insertReportConfig(reportConfig)
    }

    fun getReportConfig(): kotlinx.coroutines.flow.Flow<com.example.celestic.models.report.ReportConfig?> {
        return dao.getReportConfig()
    }

    fun getAll(): kotlinx.coroutines.flow.Flow<List<DetectionItem>> {
        return dao.getAll()
    }

    fun getFeaturesForDetection(detectionItemId: Long): kotlinx.coroutines.flow.Flow<List<DetectedFeature>> {
        return dao.getFeaturesForDetection(detectionItemId)
    }

    suspend fun startInspection(): Long {
        val inspection = com.example.celestic.models.Inspection(timestamp = System.currentTimeMillis())
        return dao.insertInspection(inspection)
    }

    fun getAllInspections(): kotlinx.coroutines.flow.Flow<List<com.example.celestic.models.Inspection>> {
        return dao.getAllInspections()
    }
    suspend fun insertSpecification(specification: com.example.celestic.models.Specification): Long {
        return dao.insertSpecification(specification)
    }

    fun getLatestSpecification(): kotlinx.coroutines.flow.Flow<com.example.celestic.models.Specification?> {
        return dao.getLatestSpecification()
    }

    fun getAllFeaturesBySpecification(specId: Long): kotlinx.coroutines.flow.Flow<List<com.example.celestic.models.SpecificationFeature>> {
        return dao.getAllFeaturesBySpecification(specId)
    }

    suspend fun getDetectionById(id: Long): DetectionItem? {
        return dao.getDetectionItemById(id)
    }
}