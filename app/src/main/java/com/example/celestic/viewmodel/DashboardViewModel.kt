package com.example.celestic.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
import com.example.celestic.manager.CalibrationManager
import com.example.celestic.manager.ImageClassifier
import com.example.celestic.models.ClassificationResult
import com.example.celestic.models.DashboardState
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.FaceDetectionResult
import com.example.celestic.models.FrameAnalysisResult
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import com.example.celestic.opencv.FrameAnalyzer
import com.example.celestic.ui.scanner.QRScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DetectionRepository,
    private val calibrationManager: CalibrationManager,
    private val arucoManager: ArUcoManager,
    private val aprilTagManager: AprilTagManager,
    private val qrScanner: QRScanner,
    private val frameAnalyzer: FrameAnalyzer,
    private val sharedViewModel: SharedViewModel,
    private val imageClassifier: ImageClassifier,
    private val specificationValidator: com.example.celestic.utils.SpecificationValidator
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val state: StateFlow<DashboardState> = _state

    private var currentInspectionId: Long? = null

    // Cache de especificación
    private var currentSpecification: com.example.celestic.models.Specification? = null
    private var loadedFeatures: List<com.example.celestic.models.SpecificationFeature> = emptyList()
    private val featureMatcher = com.example.celestic.validation.FeatureMatcher()

    init {
        // Cargar última especificación o crear una por defecto
        viewModelScope.launch {
            repository.getLatestSpecification().collect { spec ->
                if (spec == null) {
                    // Si no hay especificación, creamos una por defecto (Plano Base)
                    // para evitar tener que crearla manualmente en una pantalla nueva por ahora.
                    val defaultSpec = com.example.celestic.models.Specification(
                        name = "Plano Estándar",
                        sheetType = "Genérico",
                        minWidthMm = 95.0, maxWidthMm = 105.0,
                        minHeightMm = 45.0, maxHeightMm = 55.0,
                        expectedHoleCount = 4,
                        holeMinDiameterMm = 4.5, holeMaxDiameterMm = 5.5,
                        holeTolerance = 0.5,
                        holeNominalDiameterMm = 5.0,
                        expectedCountersinkCount = 0,
                        countersinkMinDiameterMm = 6.5, countersinkMaxDiameterMm = 8.5,
                        maxAllowedScratches = 0,
                        maxScratchLengthMm = 2.0,
                        maxAllowedDeformations = 0,
                        requireAlodineHalo = false,
                        minAlodineUniformity = 0.0
                    )
                    repository.insertSpecification(defaultSpec)
                } else {
                    currentSpecification = spec
                    loadFeatures(spec.id)
                }
            }
        }
    }

    private fun loadFeatures(specId: Long) {
        viewModelScope.launch {
            repository.getAllFeaturesBySpecification(specId).collect {
                loadedFeatures = it
            }
        }
    }

    fun startInspection() {
        viewModelScope.launch {
            try {
                currentInspectionId = repository.startInspection()
                _state.value = DashboardState.CameraReady
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Error al iniciar inspección: ${e.message}")
            }
        }
    }

    fun onFrameCaptured(bitmap: Bitmap) {
        _state.value = DashboardState.Processing

        viewModelScope.launch {
            try {
                // 1. Detección de Dimensiones (y ROI)
                val face = detectFaceWithOpenCV(bitmap)

                // 2. Clasificación Visual (IA)
                val classification = classifyWithTensorFlowLite(
                    roi = face.roiBitmap,
                    faceLabel = face.faceLabel
                )

                // 3. Análisis Métrico y de Características
                val analysis = analyzeWithFrameAnalyzer(bitmap)

                // 4. Validación contra Especificaciones
                var overallStatus = classification.status
                var validationNotes = classification.type

                currentSpecification?.let { spec ->
                    // Validar Dimensiones Exteriores (si aplica y si tenemos escala)
                    // (Aquí asumo que 1 pixel = 1 mm si no hay calibración, o necesitas lógica de escala)
                    // TODO: Usar calibrationManager.getScaleFactor() correctamente

                    // Validar Agujeros
                    // Extraemos agujeros de analysis (ahora FrameAnalysisResult debe tener info o lo inferimos)
                    // Nota: updated FrameAnalyzer returns 'holes' in AnalysisResult, but analyzeWithFrameAnalyzer returns FrameAnalysisResult DTO.
                    // Validation logic should happen inside analyzeWithFrameAnalyzer or here?
                    // Here is better for logic centralization.
                }

                val detectionId = saveResultsToRoom(
                    face = face,
                    classification = classification,
                    analysis = analysis
                )

                // Actualizar estado UI basado en resultado final
                // Si la clasificación IA dice OK, pero el validador métrico dice REJECT, gana REJECT.
                if (analysis.hasDeformations) {
                    overallStatus = DetectionStatus.NOT_ACCEPTED
                    validationNotes += " | Deformaciones detectadas"
                }

                when (overallStatus) {
                    DetectionStatus.OK -> {
                        _state.value = DashboardState.Approved(detectionId)
                    }
                    DetectionStatus.WARNING,
                    DetectionStatus.NOT_ACCEPTED -> {
                        _state.value = DashboardState.NavigateToDetails(detectionId)
                    }
                }

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error processing frame", e)
                _state.value = DashboardState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ... rest of the methods
    
    fun startNewInspection() {
        startInspection()
    }

    fun viewReport(detectionId: Long) {
        _state.value = DashboardState.NavigateToDetails(detectionId)
    }

    fun resetState() {
        _state.value = DashboardState.Idle
    }

    // -------------------------
    // FUNCIONES INTERNAS
    // -------------------------

    private fun detectFaceWithOpenCV(bitmap: Bitmap): FaceDetectionResult {
        // ... (Mismo código de antes)
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)

        val thresh = Mat()
        Imgproc.threshold(
            gray,
            thresh,
            100.0,
            255.0,
            Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU
        )

        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            thresh,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        val maxContour = contours.maxByOrNull { Imgproc.contourArea(it) }

        val rect = if (maxContour != null && Imgproc.contourArea(maxContour) > 1000) {
            Imgproc.boundingRect(maxContour)
        } else {
            org.opencv.core.Rect(0, 0, bitmap.width, bitmap.height)
        }

        val safeX = rect.x.coerceIn(0, bitmap.width - 1)
        val safeY = rect.y.coerceIn(0, bitmap.height - 1)
        val safeWidth = rect.width.coerceAtMost(bitmap.width - safeX)
        val safeHeight = rect.height.coerceAtMost(bitmap.height - safeY)

        val roi = Bitmap.createBitmap(bitmap, safeX, safeY, safeWidth, safeHeight)

        mat.release()
        gray.release()
        thresh.release()
        hierarchy.release()

        return FaceDetectionResult(
            roiBitmap = roi,
            faceLabel = "Pieza detectada",
            confidence = 1.0f,
            boundingBox = BoundingBox(
                left = safeX.toFloat(),
                top = safeY.toFloat(),
                right = (safeX + safeWidth).toFloat(),
                bottom = (safeY + safeHeight).toFloat()
            )
        )
    }

    private fun classifyWithTensorFlowLite(
        roi: Bitmap,
        faceLabel: String
    ): ClassificationResult {
        // ... (Mismo código de antes, usando ImageClassifier)
        val probabilities = imageClassifier.runInference(roi)
        val label = imageClassifier.mapPredictionToFeatureType(probabilities)
        val maxScore = probabilities.maxOrNull() ?: 0f

        val status = if (label.contains("Defecto") || label.contains("irregular")) {
            DetectionStatus.NOT_ACCEPTED
        } else if (label.contains("unknown") || label.contains("desconocida")) {
            DetectionStatus.WARNING
        } else {
            DetectionStatus.OK
        }

        return ClassificationResult(
            status = status,
            type = label,
            score = maxScore,
            probabilities = probabilities
        )
    }

    private fun analyzeWithFrameAnalyzer(bitmap: Bitmap): FrameAnalysisResult {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val markerType = sharedViewModel.markerType.value
        val result = frameAnalyzer.analyze(mat, markerType)

        // Estimar distancia y escala
        var estimatedScale = 0.264 // Fallback 96 DPI
        val referenceMarkerSizeMm = 50.0 // Ejemplo: Marcador de 50mm

        if (result.markers.isNotEmpty()) {
            val corners = result.markers[0].corners
            if (corners.total() >= 4) {
                val p1 = corners.get(0, 0)
                val p2 = corners.get(0, 1)
                val dx = p2[0] - p1[0]
                val dy = p2[1] - p1[1]
                val widthPx = kotlin.math.sqrt(dx * dx + dy * dy)

                val estimatedZ = calibrationManager.estimateDistance(widthPx, referenceMarkerSizeMm)
                estimatedScale = calibrationManager.getScaleFactor(estimatedZ)
            }
        } else {
            // Distancia fija fallback
            val fixedWorkDistanceMm = 300.0
            estimatedScale = calibrationManager.getScaleFactor(fixedWorkDistanceMm)
        }

        // Aquí conectamos con la validación de holes si tenemos especificación
        val spec = currentSpecification
        var holesOk = true
        var countersinksOk = true
        var scratchesOk = true

        // Listas para guardar IDs o referencias de elementos fallidos para pintarlos luego
        val failedHoles = java.util.HashSet<FrameAnalyzer.Hole>()
        val failedCountersinks =
            java.util.HashSet<FrameAnalyzer.Countersink>()
        val failedScratches = java.util.HashSet<FrameAnalyzer.Scratch>()

        // Listas para visualización avanzada (Ghost/Extras)
        val missingFeatures = ArrayList<com.example.celestic.models.SpecificationFeature>()
        val extraFeatures = ArrayList<com.example.celestic.validation.MatcherDetectedFeature>()

        spec?.let { s ->
            // Determinar origen de coordenadas (Marcador)
            var originX = 0.0
            var originY = 0.0
            if (result.markers.isNotEmpty()) {
                val c = result.markers[0].corners
                // Centro aproximado del marcador 0
                if (c.total() >= 4) {
                    val p0 = c.get(0, 0) // Top-Left
                    val p2 = c.get(0, 2) // Bottom-Right
                    if (p0 != null && p2 != null) {
                        originX = (p0[0] + p2[0]) / 2
                        originY = (p0[1] + p2[1]) / 2
                    }
                }
            }

            val featuresForFace = loadedFeatures.filter { it.face == result.orientation }

            if (featuresForFace.isNotEmpty()) {
                // --- DIGITAL TWIN VALIDATION (Coordenadas) ---

                // 1. Preparar Lista Detectada en MM
                val detectedList =
                    ArrayList<com.example.celestic.validation.MatcherDetectedFeature>()

                result.holes.forEach { hole ->
                    detectedList.add(
                        com.example.celestic.validation.MatcherDetectedFeature(
                            x = (hole.center.x - originX) * estimatedScale,
                            y = (hole.center.y - originY) * estimatedScale,
                            type = DetectionType.HOLE,
                            diameter = hole.radius * 2 * estimatedScale,
                            hasAlodine = hole.hasAlodine,
                            originalReference = hole
                        )
                    )
                }

                result.countersinks.forEach { cs ->
                    detectedList.add(
                        com.example.celestic.validation.MatcherDetectedFeature(
                            x = (cs.center.x - originX) * estimatedScale,
                            y = (cs.center.y - originY) * estimatedScale,
                            type = DetectionType.COUNTERSINK,
                            diameter = cs.outerRadius * 2 * estimatedScale,
                            hasAlodine = false,
                            originalReference = cs
                        )
                    )
                }

                // 2. Ejecutar Matching
                val matchResult = featureMatcher.matchFeatures(featuresForFace, detectedList)

                // 3. Validar Matches
                matchResult.matches.forEach { match ->
                    val detected = match.detected!! // Nunca es null en list de matches
                    val status =
                        specificationValidator.validateFeatureMatch(match.expected, detected)

                    if (status != DetectionStatus.OK) {
                        if (detected.originalReference is FrameAnalyzer.Hole)
                            failedHoles.add(detected.originalReference as FrameAnalyzer.Hole)
                        if (detected.originalReference is FrameAnalyzer.Countersink)
                            failedCountersinks.add(detected.originalReference as FrameAnalyzer.Countersink)
                    }
                }

                // 4. Registrar Missing y Extras
                missingFeatures.addAll(matchResult.missing)
                extraFeatures.addAll(matchResult.extras)

                // Update general status
                if (missingFeatures.isNotEmpty() || failedHoles.isNotEmpty() || failedCountersinks.isNotEmpty()) {
                    holesOk = false // Simplify global flag for reject
                }

            } else {
                // --- GLOBAL VALIDATION (Fallback: Conteo y Promedios) ---
                // Validar Agujeros y Alodine
                result.holes.forEach { hole ->
                    val diameterMm = hole.radius * 2 * estimatedScale
                    val dimensionBad =
                        specificationValidator.validateHole(diameterMm, s) != DetectionStatus.OK
                    val alodineBad = if (s.requireAlodineHalo) !hole.hasAlodine else false

                    if (dimensionBad || alodineBad) failedHoles.add(hole)
                }
                if (failedHoles.isNotEmpty()) holesOk = false

                // Validar Avellanados
                result.countersinks.forEach { cs ->
                    val diameterMm = cs.outerRadius * 2 * estimatedScale
                    if (specificationValidator.validateCountersink(
                            diameterMm,
                            s
                        ) != DetectionStatus.OK
                    ) {
                        failedCountersinks.add(cs)
                    }
                }
                if (failedCountersinks.isNotEmpty()) countersinksOk = false
            }

            // Validar Rayaduras (Siempre global por ahora)
            result.scratches.forEach { scratch ->
                val lengthMm = scratch.length * estimatedScale
                if (specificationValidator.validateScratch(lengthMm, s) != DetectionStatus.OK) {
                    failedScratches.add(scratch)
                }
            }
            if (failedScratches.isNotEmpty()) scratchesOk = false
        }

        val hasDefects = result.contours.any { Imgproc.contourArea(it) > 500 } ||
                !holesOk || !countersinksOk || !scratchesOk

        // Convertir Mat (con dibujos base) a Bitmap
        val annotatedBmp = Bitmap.createBitmap(
            result.annotatedMat.cols(),
            result.annotatedMat.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(result.annotatedMat, annotatedBmp)

        // POST-PROCESADO: Dibujar Semáforo (Verde/Rojo) sobre el Bitmap usando Canvas Android
        val canvas = android.graphics.Canvas(annotatedBmp)
        val paint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 5f
        }
        val paintGhost = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
            color = android.graphics.Color.BLUE
        }

        // Recalcular origins (quick copy from above scope needed if scale > 0)
        // Pero arriba spec podría ser null.
        // Mejor recalcular solo si needed.
        var originXDraw = 0.0
        var originYDraw = 0.0
        if (result.markers.isNotEmpty()) {
            val c = result.markers[0].corners
            if (c.total() >= 4) {
                val p0 = c.get(0, 0)
                val p2 = c.get(0, 2)
                if (p0 != null && p2 != null) {
                    originXDraw = (p0[0] + p2[0]) / 2; originYDraw = (p0[1] + p2[1]) / 2
                }
            }
        }

        // Pintar Agujeros (Sobrescribir lo de OpenCV con colores de validación)
        result.holes.forEach { hole ->
            paint.color =
                if (failedHoles.contains(hole)) android.graphics.Color.RED else android.graphics.Color.GREEN
            // Si es EXTRA (está en extraFeatures) pintarlo Amarillo?
            if (extraFeatures.any { it.originalReference == hole }) paint.color =
                android.graphics.Color.YELLOW

            canvas.drawCircle(
                hole.center.x.toFloat(),
                hole.center.y.toFloat(),
                hole.radius.toFloat(),
                paint
            )
        }

        // Pintar Avellanados
        result.countersinks.forEach { cs ->
            paint.color =
                if (failedCountersinks.contains(cs)) android.graphics.Color.RED else android.graphics.Color.GREEN
            canvas.drawCircle(
                cs.center.x.toFloat(),
                cs.center.y.toFloat(),
                cs.outerRadius.toFloat(),
                paint
            )
        }

        // Pintar Rayaduras
        result.scratches.forEach { scratch ->
            paint.color =
                if (failedScratches.contains(scratch)) android.graphics.Color.RED else android.graphics.Color.MAGENTA
            canvas.drawLine(
                scratch.startPoint.x.toFloat(),
                scratch.startPoint.y.toFloat(),
                scratch.endPoint.x.toFloat(),
                scratch.endPoint.y.toFloat(),
                paint
            )
        }

        // Pintar "Fantasmas" (Missing Features)
        if (estimatedScale > 0.001) {
            missingFeatures.forEach { missing ->
                // Map MM back to PX
                val px = (missing.positionX_mm / estimatedScale) + originXDraw
                val py = (missing.positionY_mm / estimatedScale) + originYDraw
                val r = (missing.diameter_mm / 2) / estimatedScale
                canvas.drawCircle(px.toFloat(), py.toFloat(), r.toFloat(), paintGhost)
            }
        }

        // Mapear resultado interno a DTO de ViewModel
        val analysis = FrameAnalysisResult(
            contoursCount = result.contours.size,
            markersDetected = result.markers.size,
            hasDeformations = hasDefects, // General flag for now
            holesCount = result.holes.size,
            countersinksCount = result.countersinks.size,
            scratchesCount = result.scratches.size,
            orientation = result.orientation,
            annotatedBitmap = annotatedBmp
        )

        mat.release()
        result.annotatedMat.release()

        return analysis
    }

    private suspend fun saveResultsToRoom(
        face: FaceDetectionResult,
        classification: ClassificationResult,
        analysis: FrameAnalysisResult
    ): Long {
        val inspectionId =
            currentInspectionId ?: repository.startInspection().also { currentInspectionId = it }
        val frameId = UUID.randomUUID().toString()

        // Guardar imagen anotada
        analysis.annotatedBitmap?.let { bmp ->
            repository.saveImage(bmp, frameId)
        }

        // Mapear etiqueta de texto a Enum DetectionType
        val typeEnum = when {
            classification.type.contains("Defecto") -> DetectionType.DEFECT
            classification.type.contains("Curvatura") -> DetectionType.BEND
            classification.type.contains("Pieza") -> DetectionType.HOLE
            else -> DetectionType.UNKNOWN
        }

        val item = DetectionItem(
            inspectionId = inspectionId,
            frameId = frameId,
            type = typeEnum,
            boundingBox = face.boundingBox,
            confidence = classification.score,
            status = classification.status,
            measurementMm = null,
            timestamp = System.currentTimeMillis(),
            notes = classification.type
        )

        return repository.insertDetection(item)
    }

    suspend fun getDetectionById(id: Long): DetectionItem? {
        return repository.getDetectionById(id)
    }
}