package com.example.celestic.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.data.repository.SharedDataRepository
import com.example.celestic.manager.CalibrationManager
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.Specification
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.opencv.FrameAnalyzer
import com.example.celestic.opencv.ImageProcessor
import com.example.celestic.utils.saveBitmapToFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DetectionRepository,
    private val calibrationManager: CalibrationManager,
    private val frameAnalyzer: FrameAnalyzer,
    private val imageProcessor: ImageProcessor,
    private val sharedData: SharedDataRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed class DashboardState {
        object Idle : DashboardState()
        object CameraReady : DashboardState()
        object Processing : DashboardState()
        data class Approved(val detectionId: Long) : DashboardState()
        data class Rejected(val detectionId: Long) : DashboardState()
        data class NavigateToDetails(val detectionId: Long) : DashboardState()
        data class Error(val message: String) : DashboardState()
    }

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val state: StateFlow<DashboardState> = _state

    private var currentSpecification: Specification? = null

    // Access shared state directly
    val markerType = sharedData.markerType
    val currentInspectionId = sharedData.currentInspectionId

    init {
        loadCurrentSpecification()
    }

    private fun loadCurrentSpecification() {
        viewModelScope.launch {
            repository.getLatestSpecification().collect { spec ->
                currentSpecification = spec
                spec?.id?.let { sharedData.setSelectedSpecification(it) }
            }
        }
    }

    fun startInspection() {
        viewModelScope.launch {
            try {
                val inspectionId = repository.startInspection()
                sharedData.setCurrentInspection(inspectionId)
                _state.value = DashboardState.CameraReady
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Error starting inspection: ${e.message}")
            }
        }
    }

    fun onFrameCaptured(bitmap: Bitmap) {
        _state.value = DashboardState.Processing
        sharedData.setProcessing(true)

        viewModelScope.launch {
            val frameId = "insp_${System.currentTimeMillis()}"
            // Save bitmap to file for later review in Details
            saveBitmapToFile(context, bitmap, frameId)

            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)

            try {
                val currentMarkerType = markerType.value
                val detections = imageProcessor.processImage(mat, currentMarkerType)

                // Validate against specification
                val specification = currentSpecification
                val validationResult = if (specification != null) {
                    validateAgainstSpecification(detections, specification)
                } else {
                    createDefaultValidationResult(detections)
                }

                // Save to database with current frameId
                val detectionIds = saveDetectionsToDatabase(detections, validationResult, frameId)

                // Update shared state
                sharedData.setLastDetectionResults(detections)

                // Update UI state
                if (detections.isEmpty()) {
                    // Si no hay detecciones, vamos a detalles con un estado de "inspección vacía"
                    _state.value = DashboardState.Rejected(detectionIds.firstOrNull() ?: 0)
                } else {
                    updateStateBasedOnValidation(validationResult, detectionIds)
                }

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error processing frame", e)
                _state.value = DashboardState.Error(e.message ?: "Unknown error")
            } finally {
                mat.release()
                bitmap.recycle()
                sharedData.setProcessing(false)
            }
        }
    }

    private fun validateAgainstSpecification(
        detections: List<DetectionItem>,
        specification: Specification
    ): ValidationResult {
        val violations = mutableListOf<String>()
        var overallStatus = DetectionStatus.OK

        // Validate hole count
        val holesCount = detections.count { it.type == DetectionType.HOLE }
        if (holesCount != specification.expectedHoleCount) {
            violations.add("Hole count mismatch: expected ${specification.expectedHoleCount}, found $holesCount")
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        // Validate scratches
        val scratchesCount = detections.count { it.type == DetectionType.SCRATCH }
        if (scratchesCount > specification.maxAllowedScratches) {
            violations.add("Too many scratches: allowed ${specification.maxAllowedScratches}, found $scratchesCount")
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        return ValidationResult(
            isValid = violations.isEmpty(),
            violations = violations,
            overallStatus = overallStatus
        )
    }

    private fun createDefaultValidationResult(detections: List<DetectionItem>): ValidationResult {
        val hasCriticalDefects = detections.any { it.type == DetectionType.SCRATCH }
        return ValidationResult(
            isValid = !hasCriticalDefects,
            violations = if (hasCriticalDefects) listOf("Critical defects detected") else emptyList(),
            overallStatus = if (hasCriticalDefects) DetectionStatus.NOT_ACCEPTED else DetectionStatus.OK
        )
    }

    private suspend fun saveDetectionsToDatabase(
        detections: List<DetectionItem>,
        validationResult: ValidationResult,
        frameId: String
    ): List<Long> {
        val inspectionId = currentInspectionId.value ?: repository.startInspection().also {
            sharedData.setCurrentInspection(it)
        }

        val detectionIds = mutableListOf<Long>()

        if (detections.isEmpty()) {
            // Insert a placeholder for empty inspection
            val emptyDetection = DetectionItem(
                inspectionId = inspectionId,
                frameId = frameId,
                type = DetectionType.UNKNOWN,
                boundingBox = com.example.celestic.models.geometry.BoundingBox(0f, 0f, 0f, 0f),
                confidence = 0f,
                status = DetectionStatus.NOT_ACCEPTED,
                timestamp = System.currentTimeMillis(),
                notes = "No se detectó ningún elemento a inspeccionar."
            )
            val id = repository.insertDetection(emptyDetection)
            detectionIds.add(id)
        } else {
            detections.forEach { detection ->
                val validatedDetection = detection.copy(
                    inspectionId = inspectionId,
                    frameId = frameId,
                    status = validationResult.overallStatus
                )
                val id = repository.insertDetection(validatedDetection)
                detectionIds.add(id)
            }
        }

        return detectionIds
    }

    private fun updateStateBasedOnValidation(validationResult: ValidationResult, detectionIds: List<Long>) {
        when (validationResult.overallStatus) {
            DetectionStatus.OK -> {
                _state.value = DashboardState.Approved(detectionIds.firstOrNull() ?: 0)
            }

            DetectionStatus.WARNING -> {
                _state.value = DashboardState.NavigateToDetails(detectionIds.firstOrNull() ?: 0)
            }

            DetectionStatus.NOT_ACCEPTED -> {
                _state.value = DashboardState.Rejected(detectionIds.firstOrNull() ?: 0)
            }
        }
    }

    suspend fun getDetectionById(id: Long): DetectionItem? {
        return repository.getDetectionById(id)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val violations: List<String>,
        val overallStatus: DetectionStatus
    )

    fun startNewInspection() {
        sharedData.clearCurrentInspection()
        _state.value = DashboardState.CameraReady
    }

    fun resetState() {
        sharedData.clearCurrentInspection()
        _state.value = DashboardState.Idle
    }
}
