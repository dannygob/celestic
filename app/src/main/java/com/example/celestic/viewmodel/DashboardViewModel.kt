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
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.opencv.FrameAnalyzer
import com.example.celestic.opencv.ImageProcessor
import com.example.celestic.utils.saveBitmapToFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    private val blueprintMatcher: com.example.celestic.ml.BlueprintMatcher,
    private val sharedData: SharedDataRepository,
    private val imageClassifier: com.example.celestic.manager.ImageClassifier,
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
                val inspectionId = repository.startInspection(
                    latitude = sharedData.latitude.value,
                    longitude = sharedData.longitude.value
                )
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
                val processResult = imageProcessor.processImage(mat, currentMarkerType)
                val detections = processResult.detections.toMutableList()
                val orientation = processResult.orientation

                // 1. Automatic Blueprint Identification (Vision Artificial)
                val blueprintMatch = blueprintMatcher.matchBlueprint(mat)

                // 1.5 Run AI Semantic Classification using MobileNet V2
                val aiPredictions = imageClassifier.runInference(bitmap)
                val aiResultLabel = imageClassifier.mapPredictionToFeatureType(aiPredictions)
                val maxAiConfidence = aiPredictions.maxOrNull() ?: 0f

                var aiDefectAdded = false
                if (aiResultLabel == "Defecto superficial" || aiResultLabel == "Curvatura irregular") {
                    val aiDefectType =
                        if (aiResultLabel == "Defecto superficial") DetectionType.DEFECT else DetectionType.BEND
                    val aiDetectionItem = DetectionItem(
                        inspectionId = 0,
                        frameId = frameId,
                        type = aiDefectType,
                        boundingBox = com.example.celestic.models.geometry.BoundingBox(
                            0f,
                            0f,
                            bitmap.width.toFloat(),
                            bitmap.height.toFloat()
                        ),
                        confidence = maxAiConfidence,
                        status = DetectionStatus.NOT_ACCEPTED,
                        timestamp = System.currentTimeMillis(),
                        notes = "IA SEMÁNTICA: $aiResultLabel detectado (Confianza: ${(maxAiConfidence * 100).toInt()}%)"
                    )
                    detections.add(aiDetectionItem)
                    aiDefectAdded = true
                }

                // 2. Validate against specification (Golden Sample DB + Blueprint Logic)
                val specification = currentSpecification
                val validationResult = if (specification != null) {
                    val expectedFeatures = repository.getFeaturesBySpecificationAndFace(
                        specification.id,
                        orientation
                    ).firstOrNull() ?: emptyList()

                    // If we have a blueprint match that matches our spec, we use its advanced validation
                    if (blueprintMatch != null && blueprintMatch.blueprint.id == specification.linkedBlueprintId) {
                        val bpValidation = blueprintMatcher.validateDetections(
                            detections.map { it.toMLDetection() },
                            blueprintMatch.blueprint
                        )
                        // Merge validation results
                        validateAgainstSpecification(
                            detections,
                            specification,
                            expectedFeatures,
                            bpValidation,
                            aiDefectAdded,
                            aiResultLabel
                        )
                    } else {
                        validateAgainstSpecification(
                            detections,
                            specification,
                            expectedFeatures,
                            null,
                            aiDefectAdded,
                            aiResultLabel
                        )
                    }
                } else if (blueprintMatch != null) {
                    // Use only blueprint if no manual spec is selected
                    val bpValidation = blueprintMatcher.validateDetections(
                        detections.map { it.toMLDetection() },
                        blueprintMatch.blueprint
                    )
                    createBlueprintValidationResult(
                        detections,
                        blueprintMatch.blueprint,
                        bpValidation,
                        aiDefectAdded,
                        aiResultLabel
                    )
                } else {
                    createDefaultValidationResult(detections, aiDefectAdded, aiResultLabel)
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

                // Limpieza de memoria: Solo guardamos imágenes de las piezas que fallaron.
                // Si la pieza fue APROBADA (OK), borramos la foto pesada y dejamos solo los datos en la base de datos (blueprint).
                if (validationResult.overallStatus == DetectionStatus.OK) {
                    try {
                        val file = java.io.File(context.filesDir, "detection_images/$frameId.jpg")
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        Log.e("DashboardViewModel", "Error al borrar imagen pesada aprobada", e)
                    }
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
        specification: Specification,
        expectedFeatures: List<SpecificationFeature>,
        bpValidation: com.example.celestic.ml.ValidationResult? = null,
        aiDefectAdded: Boolean = false,
        aiResultLabel: String = ""
    ): ValidationResult {
        val violations = mutableListOf<String>()
        var overallStatus = DetectionStatus.OK

        if (aiDefectAdded) {
            violations.add("IA SEMÁNTICA: $aiResultLabel detectado en la pieza.")
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        // If blueprint validation exists and failed, it takes priority on geometry
        if (bpValidation != null && !bpValidation.passed) {
            violations.addAll(bpValidation.issues)
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        if (expectedFeatures.isNotEmpty()) {
            // Validate specific features for this face (Golden Sample DB)
            val expectedHoles = expectedFeatures.count { it.type == DetectionType.HOLE }
            val detectedHoles = detections.count { it.type == DetectionType.HOLE }
            if (expectedHoles > 0 && detectedHoles != expectedHoles) {
                violations.add("Hole count mismatch (Golden Sample): expected $expectedHoles, found $detectedHoles")
                overallStatus = DetectionStatus.NOT_ACCEPTED
            }
        } else {
            // Fallback to general specification
            val holesCount = detections.count { it.type == DetectionType.HOLE }
            if (holesCount != specification.expectedHoleCount) {
                violations.add("Hole count mismatch: expected ${specification.expectedHoleCount}, found $holesCount")
                overallStatus = DetectionStatus.NOT_ACCEPTED
            }
        }

        // Validate scratches (Surface Quality)
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

    /**
     * ==================================================================================
     * ⚠️ NOTA DE INGENIERÍA - PARÁMETROS RESERVADOS PARA VALIDACIÓN FUTURA
     * ==================================================================================
     * ¿POR QUÉ APARECEN SIN USO?
     * Los parámetros 'detections' y 'blueprint' no se leen en esta implementación puesto que
     * la validación geométrica base se delega por completo a la clase 'BlueprintMatcher' y se 
     * recibe ya precalculada en 'bpValidation'.
     * 
     * ¿PARA QUÉ SE DEJAN?
     * Se mantienen reservados para la siguiente fase, donde el ViewModel ejecutará controles de 
     * calidad secundarios no geométricos (por ejemplo, comparar umbrales específicos de tolerancia 
     * térmica o registrar estadísticas del lote contrastando 'detections' con 'blueprint') antes de 
     * dictaminar el 'ValidationResult' final.
     * ==================================================================================
     */
    private fun createBlueprintValidationResult(
        detections: List<DetectionItem>,
        blueprint: com.example.celestic.models.Blueprint,
        bpValidation: com.example.celestic.ml.ValidationResult,
        aiDefectAdded: Boolean = false,
        aiResultLabel: String = ""
    ): ValidationResult {
        val violations = mutableListOf<String>()
        violations.addAll(bpValidation.issues)
        var overallStatus =
            if (bpValidation.passed) DetectionStatus.OK else DetectionStatus.NOT_ACCEPTED

        if (aiDefectAdded) {
            violations.add("IA SEMÁNTICA: $aiResultLabel detectado en la pieza.")
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        return ValidationResult(
            isValid = bpValidation.passed && !aiDefectAdded,
            violations = violations,
            overallStatus = overallStatus
        )
    }

    private fun DetectionItem.toMLDetection(): com.example.celestic.ml.Detection {
        return com.example.celestic.ml.Detection(
            classId = 0, // Not critical for matcher
            className = when (this.type) {
                DetectionType.HOLE -> "agujero"
                DetectionType.COUNTERSINK -> "avellanado"
                DetectionType.SCRATCH -> "arañazo"
                else -> "unknown"
            },
            confidence = this.confidence,
            boundingBox = org.opencv.core.Rect(
                this.boundingBox.left.toInt(),
                this.boundingBox.top.toInt(),
                (this.boundingBox.right - this.boundingBox.left).toInt(),
                (this.boundingBox.bottom - this.boundingBox.top).toInt()
            ),
            roi = Mat() // Matcher doesn't need ROI
        )
    }

    private fun createDefaultValidationResult(
        detections: List<DetectionItem>,
        aiDefectAdded: Boolean = false,
        aiResultLabel: String = ""
    ): ValidationResult {
        val violations = mutableListOf<String>()
        val hasCriticalDefects = detections.any { it.type == DetectionType.SCRATCH }
        var overallStatus =
            if (hasCriticalDefects) DetectionStatus.NOT_ACCEPTED else DetectionStatus.OK

        if (hasCriticalDefects) {
            violations.add("Critical defects detected")
        }

        if (aiDefectAdded) {
            violations.add("IA SEMÁNTICA: $aiResultLabel detectado en la pieza.")
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        return ValidationResult(
            isValid = !hasCriticalDefects && !aiDefectAdded,
            violations = violations,
            overallStatus = overallStatus
        )
    }

    private suspend fun saveDetectionsToDatabase(
        detections: List<DetectionItem>,
        validationResult: ValidationResult,
        frameId: String
    ): List<Long> {
        val inspectionId = currentInspectionId.value ?: repository.startInspection(
            latitude = sharedData.latitude.value,
            longitude = sharedData.longitude.value
        ).also {
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
