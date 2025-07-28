package com.example.celestic.data.repository

import com.example.celestic.data.dao.CelesticDao
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.calibration.DetectedFeature

class DetectionRepository(private val dao: CelesticDao) {

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

    suspend fun insertDetection(item: DetectionItem) = dao.insert(item)

    suspend fun deleteDetection(item: DetectionItem) = dao.delete(item)
}