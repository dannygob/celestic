package com.example.celestic.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.R
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.data.repository.SharedDataRepository
import com.example.celestic.manager.CalibrationManager
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.enums.Orientation
import com.example.celestic.opencv.FrameAnalyzer
import com.example.celestic.opencv.ImageProcessor
import com.example.celestic.utils.saveBitmapToFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
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
    private val defectClassifier: com.example.celestic.ml.DefectClassifier,
    private val onlineLearningManager: com.example.celestic.manager.OnlineLearningManager,
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

    val isAiSlaveMode = MutableStateFlow(!defectClassifier.isRealModelLoaded())

    val learnedSamplesCount = onlineLearningManager.getSampleCountFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val learnedSamples = onlineLearningManager.getLearnedSamplesFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _onlineLearningMessage = MutableStateFlow<String?>(null)
    val onlineLearningMessage: StateFlow<String?> = _onlineLearningMessage.asStateFlow()

    // Cara activa de inspección: ANVERSO (Cara A) o REVERSO (Cara B)
    private val _currentInspectionFace = MutableStateFlow(Orientation.ANVERSO)
    val currentInspectionFace: StateFlow<Orientation> = _currentInspectionFace.asStateFlow()

    fun toggleInspectionFace() {
        _currentInspectionFace.value = if (_currentInspectionFace.value == Orientation.ANVERSO) {
            Orientation.REVERSO
        } else {
            Orientation.ANVERSO
        }
    }

    fun setInspectionFace(face: Orientation) {
        _currentInspectionFace.value = face
    }

    fun clearOnlineLearningMessage() {
        _onlineLearningMessage.value = null
    }

    private var lastCapturedFrameId: String = ""
    private var lastProcessedDetections: List<DetectionItem> = emptyList()

    private var currentSpecification: Specification? = null

    data class EditableEdge(
        val id: String,
        val edgeIndex: Int,
        val nominalMm: Double,
        val toleranceMm: Double
    )

    private val _editableEdges = MutableStateFlow<List<EditableEdge>>(emptyList())
    val editableEdges: StateFlow<List<EditableEdge>> = _editableEdges.asStateFlow()

    private val _blueprintNameDisplay = MutableStateFlow("Pieza Conformada Irregular")
    val blueprintNameDisplay: StateFlow<String> = _blueprintNameDisplay.asStateFlow()

    // Access shared state directly
    val markerType = sharedData.markerType
    val currentInspectionId = sharedData.currentInspectionId

    init {
        loadCurrentSpecification()
    }

    private fun loadCurrentSpecification() {
        viewModelScope.launch {
            repository.getLatestSpecification().collect { spec ->
                if (spec != null) {
                    currentSpecification = spec
                    _blueprintNameDisplay.value = spec.name
                    sharedData.setSelectedSpecification(spec.id)
                    loadEdgesForSpec(spec.id)
                } else {
                    bootstrapDefaultSpecification()
                }
            }
        }
    }

    private suspend fun loadEdgesForSpec(specId: Long) {
        val features = repository.getAllFeaturesBySpecification(specId).firstOrNull() ?: emptyList()
        val edgeFeatures = features.filter { it.type == DetectionType.EDGE }
            .distinctBy { it.label }
        if (edgeFeatures.size >= 8) {
            _editableEdges.value = edgeFeatures.mapIndexed { idx, feat ->
                val edgeIdx = feat.label.removePrefix("ARISTA_E").removePrefix("ARISTA_").toIntOrNull() ?: (idx + 1)
                EditableEdge(
                    id = "E$edgeIdx",
                    edgeIndex = edgeIdx,
                    nominalMm = feat.diameter_mm,
                    toleranceMm = feat.tolerance_mm
                )
            }.sortedBy { it.edgeIndex }
        } else {
            // Si la especificación tiene menos de 8 aristas o es de la versión anterior, ajustar todas al plano técnico
            adjustAllEdgesToBlueprint()
        }
    }

    fun adjustAllEdgesToBlueprint() {
        val bp = blueprintMatcher.getBlueprintById("PIEZA_IRREGULAR_001")
            ?: blueprintMatcher.getAllBlueprints().firstOrNull() ?: return

        _blueprintNameDisplay.value = bp.name
        val bpEdges = bp.expectedEdges.map {
            EditableEdge(
                id = it.id,
                edgeIndex = it.edgeIndex,
                nominalMm = it.lengthMm,
                toleranceMm = it.toleranceMm
            )
        }.sortedBy { it.edgeIndex }

        _editableEdges.value = bpEdges

        viewModelScope.launch {
            try {
                val spec = currentSpecification ?: repository.getLatestSpecification().firstOrNull()
                if (spec != null) {
                    val allFeatures = repository.getAllFeaturesBySpecification(spec.id).firstOrNull() ?: emptyList()
                    val nonEdgeFeatures = allFeatures.filter { it.type != DetectionType.EDGE }
                    val newEdgeFeatures = mutableListOf<SpecificationFeature>()
                    listOf(Orientation.ANVERSO, Orientation.REVERSO).forEach { face ->
                        bp.expectedEdges.forEach { edge ->
                            newEdgeFeatures.add(
                                SpecificationFeature(
                                    specificationId = spec.id,
                                    face = face,
                                    type = DetectionType.EDGE,
                                    positionX_mm = edge.startX,
                                    positionY_mm = edge.startY,
                                    diameter_mm = edge.lengthMm,
                                    tolerance_mm = edge.toleranceMm,
                                    label = "ARISTA_${edge.id}"
                                )
                            )
                        }
                    }
                    repository.deleteFeaturesBySpecification(spec.id)
                    repository.insertSpecificationFeatures(nonEdgeFeatures + newEdgeFeatures)
                    Log.d("DashboardViewModel", "Ajustadas exitosamente todas las ${bp.expectedEdges.size} aristas al plano técnico: ${bp.name}")
                } else {
                    bootstrapDefaultSpecification()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error al ajustar aristas al plano", e)
            }
        }
    }

    private fun loadDefaultEdgesFromBlueprint() {
        adjustAllEdgesToBlueprint()
    }

    private suspend fun bootstrapDefaultSpecification() {
        try {
            val bp = blueprintMatcher.getBlueprintById("PIEZA_IRREGULAR_001")
                ?: blueprintMatcher.getAllBlueprints().firstOrNull()
                ?: return

            val newSpec = Specification(
                name = bp.name,
                sheetType = "2_CARAS_ANVERSO_REVERSO",
                minWidthMm = 0.0,
                maxWidthMm = bp.dimensions.widthMm,
                minHeightMm = 0.0,
                maxHeightMm = bp.dimensions.heightMm,
                expectedHoleCount = bp.expectedHoleCount,
                holeMinDiameterMm = 5.8,
                holeMaxDiameterMm = 6.2,
                holeTolerance = bp.tolerances.holeDiameterToleranceMm,
                holeNominalDiameterMm = 6.0,
                expectedCountersinkCount = bp.expectedCountersinks.size,
                countersinkMinDiameterMm = 9.8,
                countersinkMaxDiameterMm = 10.2,
                maxAllowedScratches = bp.tolerances.maxAllowedScratches,
                maxScratchLengthMm = bp.tolerances.maxScratchLengthMm,
                maxAllowedDeformations = 1,
                requireAlodineHalo = bp.requiresAlodine,
                minAlodineUniformity = 0.8,
                linkedBlueprintId = bp.id,
                nominalThicknessMm = 0.0,
                thicknessToleranceMm = 0.0
            )

            val features = mutableListOf<SpecificationFeature>()
            listOf(Orientation.ANVERSO, Orientation.REVERSO).forEach { face ->
                bp.expectedEdges.forEach { edge ->
                    features.add(
                        SpecificationFeature(
                            specificationId = 0,
                            face = face,
                            type = DetectionType.EDGE,
                            positionX_mm = edge.startX,
                            positionY_mm = edge.startY,
                            diameter_mm = edge.lengthMm,
                            tolerance_mm = edge.toleranceMm,
                            label = "ARISTA_${edge.id}"
                        )
                    )
                }
                bp.expectedHoles.forEach { hole ->
                    features.add(
                        SpecificationFeature(
                            specificationId = 0,
                            face = face,
                            type = DetectionType.HOLE,
                            positionX_mm = hole.x,
                            positionY_mm = hole.y,
                            diameter_mm = hole.diameterMm,
                            tolerance_mm = hole.toleranceMm,
                            label = "TALADRO_${hole.id}"
                        )
                    )
                }
            }
            bp.expectedCountersinks.forEach { cs ->
                features.add(
                    SpecificationFeature(
                        specificationId = 0,
                        face = Orientation.ANVERSO,
                        type = DetectionType.COUNTERSINK,
                        positionX_mm = cs.x,
                        positionY_mm = cs.y,
                        diameter_mm = cs.outerDiameterMm,
                        tolerance_mm = cs.toleranceMm,
                        requireCountersink = true,
                        label = "AVELLANADO_${cs.id}"
                    )
                )
            }

            repository.saveSpecificationWithFeatures(newSpec, features)
            loadEdgesForSpec(newSpec.id)
            Log.d("DashboardViewModel", "Plano predeterminado inicializado con éxito: ${bp.name}")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error al inicializar especificación por defecto", e)
        }
    }

    fun updateEdgeCota(edgeIndex: Int, newNominal: Double, newTolerance: Double) {
        val list = _editableEdges.value.toMutableList()
        val idx = list.indexOfFirst { it.edgeIndex == edgeIndex }
        if (idx != -1) {
            list[idx] = list[idx].copy(nominalMm = newNominal, toleranceMm = newTolerance)
            _editableEdges.value = list
        }
    }

    fun saveAdjustedEdgesToBlueprint() {
        val spec = currentSpecification ?: return
        val currentEdges = _editableEdges.value
        viewModelScope.launch {
            try {
                val allFeatures = repository.getAllFeaturesBySpecification(spec.id).firstOrNull() ?: emptyList()
                val nonEdgeFeatures = allFeatures.filter { it.type != DetectionType.EDGE }
                val newEdgeFeatures = mutableListOf<SpecificationFeature>()
                listOf(Orientation.ANVERSO, Orientation.REVERSO).forEach { face ->
                    currentEdges.forEach { edge ->
                        newEdgeFeatures.add(
                            SpecificationFeature(
                                specificationId = spec.id,
                                face = face,
                                type = DetectionType.EDGE,
                                positionX_mm = 0.0,
                                positionY_mm = 0.0,
                                diameter_mm = edge.nominalMm,
                                tolerance_mm = edge.toleranceMm,
                                label = "ARISTA_${edge.id}"
                            )
                        )
                    }
                }
                repository.deleteFeaturesBySpecification(spec.id)
                repository.insertSpecificationFeatures(nonEdgeFeatures + newEdgeFeatures)
                Log.d("DashboardViewModel", "Cotas de aristas guardadas exitosamente: ${currentEdges.size}")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error al guardar aristas", e)
            }
        }
    }

    fun resetEdgesToBlueprint() {
        adjustAllEdgesToBlueprint()
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
                val orientation = if (processResult.orientation != Orientation.UNKNOWN) {
                    processResult.orientation
                } else {
                    _currentInspectionFace.value
                }
                _currentInspectionFace.value = orientation

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

                // 1.8 Run Deep Learning Defect Classifier on each detected feature's ROI
                val classifiedDetections = detections.map { detection ->
                    val bbox = detection.boundingBox
                    val left = bbox.left.toInt().coerceIn(0, bitmap.width - 1)
                    val top = bbox.top.toInt().coerceIn(0, bitmap.height - 1)
                    val width = bbox.width().toInt().coerceIn(1, bitmap.width - left)
                    val height = bbox.height().toInt().coerceIn(1, bitmap.height - top)
                    if (width > 8 && height > 8) {
                        try {
                            val roi = Bitmap.createBitmap(bitmap, left, top, width, height)
                            val dnnClass = when (detection.type) {
                                DetectionType.HOLE -> com.example.celestic.ml.DNNDetector.DetectionClass.HOLE
                                DetectionType.COUNTERSINK -> com.example.celestic.ml.DNNDetector.DetectionClass.COUNTERSINK
                                DetectionType.DEFECT -> com.example.celestic.ml.DNNDetector.DetectionClass.SCRATCH
                                DetectionType.BEND -> com.example.celestic.ml.DNNDetector.DetectionClass.DEFORMATION
                                else -> com.example.celestic.ml.DNNDetector.DetectionClass.SHEET
                            }
                            val classResult = defectClassifier.classify(roi, dnnClass)
                            roi.recycle()

                            val modeTag =
                                if (classResult.isSlaveMode) "[${context.getString(R.string.ai_tag_slave)}]" else "[${
                                    context.getString(R.string.ai_tag_tflite)
                                }]"
                            val localizedDefectLabel =
                                classResult.defectClass.getLocalizedLabel(context)
                            val updatedNotes =
                                "$modeTag $localizedDefectLabel (${(classResult.confidence * 100).toInt()}%) | ${detection.notes}"

                            val isDefective = classResult.defectClass == com.example.celestic.ml.DefectClassifier.DefectClass.HOLE_DEFECTIVE ||
                                classResult.defectClass == com.example.celestic.ml.DefectClassifier.DefectClass.COUNTERSINK_DEFECTIVE ||
                                classResult.defectClass == com.example.celestic.ml.DefectClassifier.DefectClass.SCRATCH_SEVERE ||
                                classResult.defectClass == com.example.celestic.ml.DefectClassifier.DefectClass.DEFORMATION_PRESENT ||
                                classResult.defectClass == com.example.celestic.ml.DefectClassifier.DefectClass.ALODINE_IRREGULAR

                            detection.copy(
                                notes = updatedNotes,
                                status = if (isDefective) DetectionStatus.NOT_ACCEPTED else detection.status
                            )
                        } catch (e: Exception) {
                            Log.w("DashboardViewModel", "Error running defectClassifier ROI", e)
                            detection
                        }
                    } else {
                        detection
                    }
                }
                detections.clear()
                detections.addAll(classifiedDetections)

                // 2. Validate against specification (Golden Sample DB + Blueprint Logic)
                val specification = currentSpecification
                val activeBlueprint = blueprintMatch?.blueprint
                    ?: blueprintMatcher.getBlueprintById(specification?.linkedBlueprintId ?: "PIEZA_IRREGULAR_001")
                    ?: blueprintMatcher.getAllBlueprints().firstOrNull()

                val bpValidation = if (activeBlueprint != null) {
                    blueprintMatcher.validateDetections(
                        detections.map { it.toMLDetection() },
                        activeBlueprint
                    )
                } else null

                val validationResult = if (specification != null) {
                    val expectedFeatures = repository.getFeaturesBySpecificationAndFace(
                        specification.id,
                        orientation
                    ).firstOrNull() ?: emptyList()

                    validateAgainstSpecification(
                        detections,
                        specification,
                        expectedFeatures,
                        bpValidation,
                        aiDefectAdded,
                        aiResultLabel
                    )
                } else if (activeBlueprint != null) {
                    // Use only blueprint if no manual spec is selected
                    createBlueprintValidationResult(
                        detections,
                        activeBlueprint,
                        bpValidation ?: com.example.celestic.ml.ValidationResult(true, emptyList()),
                        aiDefectAdded,
                        aiResultLabel
                    )
                } else {
                    createDefaultValidationResult(detections, aiDefectAdded, aiResultLabel)
                }

                // Save to database with current frameId
                val detectionIds = saveDetectionsToDatabase(detections, validationResult, frameId)

                lastCapturedFrameId = frameId
                lastProcessedDetections = detections.toList()

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

    private suspend fun validateAgainstSpecification(
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
            violations.add(context.getString(R.string.violation_semantic_ai, aiResultLabel))
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        // Evaluación de Reaprendizaje en Caliente (Perfil adaptativo)
        val (simScore, isCompliant) = onlineLearningManager.evaluateAgainstLearnedProfile(detections)
        if (!isCompliant) {
            violations.add(
                context.getString(
                    R.string.violation_relearning_dev,
                    (simScore * 100).toInt()
                )
            )
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        // If blueprint validation exists and failed, it takes priority on geometry
        if (bpValidation != null && !bpValidation.passed) {
            violations.addAll(bpValidation.issues)
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        // 1. Validar aristas y bordes de la pieza irregular con sus cotas y tolerancias del plano
        val detectedEdges = detections.filter { it.type == DetectionType.EDGE }
        val activeReferenceEdges = _editableEdges.value.ifEmpty {
            val bp = blueprintMatcher.getBlueprintById(specification.linkedBlueprintId ?: "PIEZA_IRREGULAR_001")
                ?: blueprintMatcher.getAllBlueprints().firstOrNull()
            bp?.expectedEdges?.map { EditableEdge(it.id, it.edgeIndex, it.lengthMm, it.toleranceMm) } ?: emptyList()
        }

        if (activeReferenceEdges.isNotEmpty()) {
            if (detectedEdges.size < activeReferenceEdges.size) {
                violations.add("Aristas incompletas en contorno: detectadas ${detectedEdges.size}, esperadas en plano ${activeReferenceEdges.size}")
                overallStatus = DetectionStatus.NOT_ACCEPTED
            }

            activeReferenceEdges.forEach { refEdge ->
                val matched = detectedEdges.firstOrNull { 
                    it.frameId.contains("E${refEdge.edgeIndex}") ||
                    it.frameId.equals("ARISTA_${refEdge.id}", ignoreCase = true)
                }

                if (matched != null && matched.measurementMm != null) {
                    val measured = matched.measurementMm.toDouble()
                    val delta = kotlin.math.abs(measured - refEdge.nominalMm)
                    if (delta > refEdge.toleranceMm) {
                        violations.add(
                            "Arista ${refEdge.id} fuera de tolerancia: ${"%.2f".format(measured)} mm (Plano: ${"%.2f".format(refEdge.nominalMm)} ±${"%.2f".format(refEdge.toleranceMm)} mm, Δ: ${"%.2f".format(delta)} mm)"
                        )
                        overallStatus = DetectionStatus.NOT_ACCEPTED
                    }
                } else if (matched == null) {
                    violations.add("Falta arista ${refEdge.id} del plano (${refEdge.nominalMm} mm)")
                    overallStatus = DetectionStatus.NOT_ACCEPTED
                }
            }
        }
        // NOTA: El espesor se omite como criterio de inspección por requerimiento técnico (espesor irrelevante).

        // 3. Validar Avellanados según la cara activa (Anverso vs Reverso)
        val detectedCountersinks = detections.count { it.type == DetectionType.COUNTERSINK }
        if (_currentInspectionFace.value == Orientation.ANVERSO) {
            val expectedCs = if (expectedFeatures.isNotEmpty()) {
                expectedFeatures.count { it.type == DetectionType.COUNTERSINK }
            } else specification.expectedCountersinkCount
            if (expectedCs > 0 && detectedCountersinks != expectedCs) {
                violations.add("Falta avellanado en Anverso: esperados $expectedCs, detectados $detectedCountersinks")
                overallStatus = DetectionStatus.NOT_ACCEPTED
            }
        } else if (_currentInspectionFace.value == Orientation.REVERSO) {
            // En el Reverso no debe haber avellanados cónicos frontales
            if (detectedCountersinks > 0) {
                violations.add("Anomalía en Reverso: se detectaron $detectedCountersinks avellanados cónicos en cara posterior")
                overallStatus = DetectionStatus.NOT_ACCEPTED
            }
        }

        // 4. Validar deformaciones perimetrales / alabeo
        val deformationsCount = detections.count { it.type == DetectionType.BEND }
        if (deformationsCount > specification.maxAllowedDeformations) {
            violations.add("Deformaciones perimetrales excesivas: detectadas $deformationsCount (máx permitido: ${specification.maxAllowedDeformations})")
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        if (expectedFeatures.isNotEmpty()) {
            // Validate specific features for this face (Golden Sample DB)
            val expectedHoles = expectedFeatures.count { it.type == DetectionType.HOLE }
            val detectedHoles = detections.count { it.type == DetectionType.HOLE }
            if (expectedHoles > 0 && detectedHoles != expectedHoles) {
                violations.add(
                    context.getString(
                        R.string.violation_hole_count_mismatch,
                        expectedHoles,
                        detectedHoles
                    )
                )
                overallStatus = DetectionStatus.NOT_ACCEPTED
            }
        } else {
            // Fallback to general specification
            val holesCount = detections.count { it.type == DetectionType.HOLE }
            if (holesCount != specification.expectedHoleCount) {
                violations.add(
                    context.getString(
                        R.string.violation_hole_count_mismatch,
                        specification.expectedHoleCount,
                        holesCount
                    )
                )
                overallStatus = DetectionStatus.NOT_ACCEPTED
            }
        }

        // Validate scratches (Surface Quality)
        val scratchesCount = detections.count { it.type == DetectionType.SCRATCH }
        if (scratchesCount > specification.maxAllowedScratches) {
            violations.add(
                context.getString(
                    R.string.violation_too_many_scratches,
                    specification.maxAllowedScratches,
                    scratchesCount
                )
            )
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
        val edgeIdx = if (this.type == DetectionType.EDGE) {
            this.frameId.removePrefix("ARISTA_E").removePrefix("ARISTA_").toIntOrNull() ?: 0
        } else 0
        return com.example.celestic.ml.Detection(
            classId = edgeIdx,
            className = when (this.type) {
                DetectionType.HOLE -> "agujero"
                DetectionType.COUNTERSINK -> "avellanado"
                DetectionType.SCRATCH -> "arañazo"
                DetectionType.EDGE -> "arista"
                DetectionType.DIMENSION -> "dimension"
                else -> "unknown"
            },
            confidence = this.confidence,
            boundingBox = org.opencv.core.Rect(
                this.boundingBox.left.toInt(),
                this.boundingBox.top.toInt(),
                (this.boundingBox.right - this.boundingBox.left).toInt(),
                (this.boundingBox.bottom - this.boundingBox.top).toInt()
            ),
            roi = Mat(),
            measurementMm = this.measurementMm?.toDouble() ?: 0.0,
            frameId = this.frameId
        )
    }

    private suspend fun createDefaultValidationResult(
        detections: List<DetectionItem>,
        aiDefectAdded: Boolean = false,
        aiResultLabel: String = ""
    ): ValidationResult {
        val violations = mutableListOf<String>()
        val hasCriticalDefects = detections.any { it.type == DetectionType.SCRATCH }
        var overallStatus =
            if (hasCriticalDefects) DetectionStatus.NOT_ACCEPTED else DetectionStatus.OK

        if (hasCriticalDefects) {
            violations.add(context.getString(R.string.violation_critical_defects))
        }

        if (aiDefectAdded) {
            violations.add(context.getString(R.string.violation_semantic_ai, aiResultLabel))
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        // Evaluación de Reaprendizaje en Caliente (Perfil adaptativo)
        val (simScore, isCompliant) = onlineLearningManager.evaluateAgainstLearnedProfile(detections)
        if (!isCompliant) {
            violations.add(
                context.getString(
                    R.string.violation_relearning_dev,
                    (simScore * 100).toInt()
                )
            )
            overallStatus = DetectionStatus.NOT_ACCEPTED
        }

        // 4. Validar aristas de la pieza irregular contra las cotas del plano
        val detectedEdges = detections.filter { it.type == DetectionType.EDGE }
        val referenceEdges = _editableEdges.value.ifEmpty {
            val bp = blueprintMatcher.getBlueprintById("PIEZA_IRREGULAR_001")
                ?: blueprintMatcher.getAllBlueprints().firstOrNull()
            bp?.expectedEdges?.map { EditableEdge(it.id, it.edgeIndex, it.lengthMm, it.toleranceMm) } ?: emptyList()
        }

        if (referenceEdges.isNotEmpty()) {
            referenceEdges.forEach { refEdge ->
                val matched = detectedEdges.firstOrNull { 
                    it.frameId.contains("E${refEdge.edgeIndex}") ||
                    it.frameId.equals("ARISTA_${refEdge.id}", ignoreCase = true)
                }

                if (matched != null && matched.measurementMm != null) {
                    val measured = matched.measurementMm.toDouble()
                    val delta = kotlin.math.abs(measured - refEdge.nominalMm)
                    if (delta > refEdge.toleranceMm) {
                        violations.add(
                            "Arista ${refEdge.id} fuera de tolerancia: ${"%.2f".format(measured)} mm (Plano: ${"%.2f".format(refEdge.nominalMm)} ±${"%.2f".format(refEdge.toleranceMm)} mm, Δ: ${"%.2f".format(delta)} mm)"
                        )
                        overallStatus = DetectionStatus.NOT_ACCEPTED
                    }
                } else if (matched == null) {
                    violations.add("Falta arista ${refEdge.id} del plano (${refEdge.nominalMm} mm)")
                    overallStatus = DetectionStatus.NOT_ACCEPTED
                }
            }
        }

        return ValidationResult(
            isValid = !hasCriticalDefects && !aiDefectAdded && isCompliant && violations.isEmpty(),
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

        // Leer el Albarán/Lote activo asignado manualmente desde el Dashboard.
        // Si no hay QR escaneado, el operario puede asignar un lote manual desde el
        // botón "LOTE" en la barra superior del Dashboard. Ese valor se persiste en
        // SharedPreferences y aquí lo recuperamos para vincularlo a cada detección,
        // de modo que la pantalla de Reportes pueda agrupar correctamente por lote
        // incluso cuando la pieza no tiene código QR ni código de barras físico.
        val prefs =
            context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
        val activeBatch = prefs.getString("current_albaran", null)
            ?.takeIf { it.isNotBlank() && it != "GENERAL" }

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
                notes = context.getString(R.string.note_no_elements_detected),
                linkedQrCode = activeBatch
            )
            val id = repository.insertDetection(emptyDetection)
            detectionIds.add(id)
        } else {
            detections.forEach { detection ->
                // Si la detección ya trae un QR escaneado en campo, tiene prioridad.
                // En caso contrario usamos el lote asignado manualmente en el Dashboard.
                val batchToLink = detection.linkedQrCode?.takeIf { it.isNotBlank() } ?: activeBatch
                val validatedDetection = detection.copy(
                    inspectionId = inspectionId,
                    frameId = frameId,
                    status = validationResult.overallStatus,
                    linkedQrCode = batchToLink
                )
                val id = repository.insertDetection(validatedDetection)
                detectionIds.add(id)

                // Persistir DetectedFeature vinculada para representación técnica en BlueprintView
                val isDefect = validatedDetection.status == DetectionStatus.NOT_ACCEPTED
                val widthPx = kotlin.math.abs(validatedDetection.boundingBox.right - validatedDetection.boundingBox.left)
                val heightPx = kotlin.math.abs(validatedDetection.boundingBox.bottom - validatedDetection.boundingBox.top)
                val estimatedMm = validatedDetection.measurementMm ?: (widthPx.coerceAtLeast(6f) * 0.1f)
                var nominalMm = when (validatedDetection.type) {
                    DetectionType.THICKNESS -> 2.0f
                    DetectionType.DIMENSION -> if (validatedDetection.frameId.contains("PITCH")) 80.0f else 200.0f
                    DetectionType.COUNTERSINK -> 10.0f
                    DetectionType.SCRATCH -> 0.0f
                    DetectionType.BEND -> 0.0f
                    else -> 6.0f
                }
                var toleranceMm = when (validatedDetection.type) {
                    DetectionType.THICKNESS -> 0.15f
                    DetectionType.DIMENSION -> 1.0f
                    DetectionType.COUNTERSINK -> 0.15f
                    DetectionType.SCRATCH -> 5.0f
                    DetectionType.BEND -> 0.5f
                    else -> 0.10f
                }

                if (validatedDetection.type == DetectionType.EDGE) {
                    val edgeIndex = validatedDetection.frameId.removePrefix("ARISTA_E").removePrefix("ARISTA_").toIntOrNull()
                    val matchedFromEditable = _editableEdges.value.firstOrNull { 
                        it.edgeIndex == edgeIndex || validatedDetection.frameId.contains(it.id) 
                    }
                    val bp = blueprintMatcher.getBlueprintById(currentSpecification?.linkedBlueprintId ?: "PIEZA_IRREGULAR_001")
                        ?: blueprintMatcher.getAllBlueprints().firstOrNull()
                    val matchedFromBp = bp?.expectedEdges?.firstOrNull {
                        it.edgeIndex == edgeIndex || validatedDetection.frameId.contains(it.id)
                    }

                    if (matchedFromEditable != null) {
                        nominalMm = matchedFromEditable.nominalMm.toFloat()
                        toleranceMm = matchedFromEditable.toleranceMm.toFloat()
                    } else if (matchedFromBp != null) {
                        nominalMm = matchedFromBp.lengthMm.toFloat()
                        toleranceMm = matchedFromBp.toleranceMm.toFloat()
                    } else {
                        nominalMm = estimatedMm
                        toleranceMm = 0.8f
                    }
                }

                val deviation = kotlin.math.abs(estimatedMm - nominalMm)
                val isOutOfTolerance = deviation > toleranceMm
                val isDefectFinal = isDefect || isOutOfTolerance

                val featureMeasurements = mutableMapOf<String, Float>(
                    "diameter" to estimatedMm,
                    "actual" to estimatedMm,
                    "measured" to estimatedMm,
                    "nominal" to nominalMm,
                    "tolerance" to toleranceMm,
                    "error_mm" to deviation,
                    "is_defect" to (if (isDefectFinal) 1.0f else 0.0f)
                )

                val feature = com.example.celestic.models.calibration.DetectedFeature(
                    detectionItemId = id,
                    featureType = validatedDetection.type.name.lowercase(),
                    xCoord = (validatedDetection.boundingBox.left + validatedDetection.boundingBox.right) / 2f,
                    yCoord = (validatedDetection.boundingBox.top + validatedDetection.boundingBox.bottom) / 2f,
                    confidence = validatedDetection.confidence,
                    timestamp = validatedDetection.timestamp,
                    measurements = featureMeasurements
                )
                repository.insertDetectedFeature(feature)
            }
        }

        return detectionIds
    }

    private fun updateStateBasedOnValidation(validationResult: ValidationResult, detectionIds: List<Long>) {
        when (validationResult.overallStatus) {
            DetectionStatus.OK -> {
                triggerHapticFeedback(true)
                _state.value = DashboardState.Approved(detectionIds.firstOrNull() ?: 0)
            }

            DetectionStatus.WARNING -> {
                triggerHapticFeedback(false)
                _state.value = DashboardState.NavigateToDetails(detectionIds.firstOrNull() ?: 0)
            }

            DetectionStatus.NOT_ACCEPTED -> {
                triggerHapticFeedback(false)
                _state.value = DashboardState.Rejected(detectionIds.firstOrNull() ?: 0)
            }
        }
    }

    private fun triggerHapticFeedback(isApproved: Boolean) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    if (isApproved) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(80, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        val timings = longArrayOf(0, 100, 80, 120)
                        vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, -1))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isApproved) 80 else 250)
                }
            }
        } catch (_: Exception) {}
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

    // ===== ACCIONES DE REAPRENDIZAJE EN CALIENTE =====

    fun learnCurrentPiece(notes: String = "") {
        viewModelScope.launch {
            val detections = lastProcessedDetections
            if (detections.isEmpty()) {
                _onlineLearningMessage.value = context.getString(R.string.no_detections_to_learn)
                return@launch
            }
            val prefs = context.getSharedPreferences("celestic_prefs", Context.MODE_PRIVATE)
            val batch = prefs.getString("current_albaran", "GENERAL") ?: "GENERAL"
            val id = onlineLearningManager.learnApprovedPart(
                frameId = lastCapturedFrameId.ifBlank { "manual_${System.currentTimeMillis()}" },
                batchNumber = batch,
                detections = detections,
                notes = notes
            )
            _onlineLearningMessage.value =
                context.getString(R.string.sample_learned_batch_toast, id, batch)
        }
    }

    fun rollbackLastSample() {
        viewModelScope.launch {
            val sample = onlineLearningManager.rollbackLastSample()
            if (sample != null) {
                _onlineLearningMessage.value =
                    context.getString(R.string.rollback_success_toast, sample.id)
            } else {
                _onlineLearningMessage.value = context.getString(R.string.no_previous_samples_undo)
            }
        }
    }

    fun deleteLearnedSample(id: Long) {
        viewModelScope.launch {
            onlineLearningManager.deleteSampleById(id)
            _onlineLearningMessage.value =
                context.getString(R.string.sample_deleted_memory_toast, id)
        }
    }

    fun resetAllLearning() {
        viewModelScope.launch {
            onlineLearningManager.resetAllLearning()
            _onlineLearningMessage.value = context.getString(R.string.learning_memory_reset_toast)
        }
    }
}
