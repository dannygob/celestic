package com.example.celestic.data.repository


import com.example.celestic.data.dao.DetectionDao
import com.example.celestic.models.calibration.DetectedFeature

class DetectionRepository(private val detectionDao: DetectionDao) {

    suspend fun saveDetection(detection: DetectedFeature) {
        detectionDao.insertDetection(detection)
    }

    suspend fun saveDetections(detections: List<DetectedFeature>) {
        detectionDao.insertDetections(detections)
    }

    suspend fun loadDetections(): List<DetectedFeature> {
        return detectionDao.getAllDetections()
    }

    suspend fun clearAllDetections() {
        detectionDao.clearDetections()
    }
}