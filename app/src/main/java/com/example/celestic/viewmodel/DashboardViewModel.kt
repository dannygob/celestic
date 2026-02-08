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

    private var currentInspectionId: Long? = null

    // Specification Cache
    private var currentSpecification: com.example.celestic.models.Specification? = null
    private var loadedFeatures: List<com.example.celestic.models.SpecificationFeature> = emptyList()
    private val featureMatcher = com.example.celestic.validation.FeatureMatcher()

    init {
        // Load latest specification or create a default one
        viewModelScope.launch {
            repository.getLatestSpecification().collect { spec ->
                if (spec == null) {
                    // If no specification exists, create a default one (Blueprint Base)
                    // to avoid manual creation in a new screen for now.
                    val defaultSpec = com.example.celestic.models.Specification(
                        name = "Standard Blueprint",
                        sheetType = "Generic",
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
                _state.value = DashboardState.Error("Error starting inspection: ${e.message}")
            }
        }
    }

    fun onFrameCaptured(bitmap: Bitmap) {
        _state.value = DashboardState.Processing

        viewModelScope.launch {
            try {
                // 1. Dimension Detection (and ROI)
                val face = detectFaceWithOpenCV(bitmap)

                // 2. Visual Classification (AI)
                val classification = classifyWithTensorFlowLite(
                    roi = face.roiBitmap,
                    faceLabel = face.faceLabel
                )

                // 3. Metric and Feature Analysis
                val analysis = analyzeWithFrameAnalyzer(bitmap)

                // 4. Validation against Specifications
                var overallStatus = classification.status
                var validationNotes = classification.type

                currentSpecification?.let { spec ->
                    // Validate Outer Dimensions (if applicable and if we have scale)
                    // (Assuming 1 pixel = 1 mm if no calibration, or need scale logic)
                    // TODO: Use calibrationManager.getScaleFactor() correctly

                    // Validate Holes
                    // Extract holes from analysis
                    // Note: validation logic happens inside analyzeWithFrameAnalyzer or here?
                    // Here is better for logic centralization.
                }

                val detectionId = saveResultsToRoom(
                    face = face,
                    classification = classification,
                    analysis = analysis
                )

                // Update UI state based on final result
                // If AI classification says OK, but metric validator says REJECT, REJECT wins.
                if (analysis.hasDeformations) {
                    overallStatus = DetectionStatus.NOT_ACCEPTED
                    validationNotes += " | Deformations detected"
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
                _state.value = DashboardState.Error(e.message ?: "Unknown error")
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
    // INTERNAL FUNCTIONS
    // -------------------------

    private fun detectFaceWithOpenCV(bitmap: Bitmap): FaceDetectionResult {
        // ... (Same code as before)
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
            faceLabel = "Part detected",
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
        // ... (Same code as before, using ImageClassifier)
        val probabilities = imageClassifier.runInference(roi)
        val label = imageClassifier.mapPredictionToFeatureType(probabilities)
        val maxScore = probabilities.maxOrNull() ?: 0f

        val status = if (label.contains("Defect") || label.contains("irregular")) {
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

        // Estimate distance and scale
        var estimatedScale = 0.264 // Fallback 96 DPI
        val referenceMarkerSizeMm = 50.0 // Example: 50mm Marker

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
            // Fixed distance fallback
            val fixedWorkDistanceMm = 300.0
            estimatedScale = calibrationManager.getScaleFactor(fixedWorkDistanceMm)
        }

        // Connect with validation if specification is available
        val spec = currentSpecification
        var holesOk = true
        var countersinksOk = true
        var scratchesOk = true

        // Lists to store IDs or references of failed elements for visual feedback
        val failedHoles = java.util.HashSet<FrameAnalyzer.Hole>()
        val failedCountersinks =
            java.util.HashSet<FrameAnalyzer.Countersink>()
        val failedScratches = java.util.HashSet<FrameAnalyzer.Scratch>()

        // Lists for advanced visualization (Ghost/Extras)
        val missingFeatures = ArrayList<com.example.celestic.models.SpecificationFeature>()
        val extraFeatures = ArrayList<com.example.celestic.validation.MatcherDetectedFeature>()

        spec?.let { s ->
            // Determine coordinate origin (Marker)
            var originX = 0.0
            var originY = 0.0
            if (result.markers.isNotEmpty()) {
                val c = result.markers[0].corners
                // Approximate center of marker 0
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
                // --- DIGITAL TWIN VALIDATION (Coordinates) ---

                // 1. Prepare Detected List in MM
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

                // 2. Execute Matching
                val matchResult = featureMatcher.matchFeatures(featuresForFace, detectedList)

                // 3. Validate Matches
                matchResult.matches.forEach { match ->
                    val detected = match.detected!! // Never null in matches list
                    val status =
                        specificationValidator.validateFeatureMatch(match.expected, detected)

                    if (status != DetectionStatus.OK) {
                        if (detected.originalReference is FrameAnalyzer.Hole)
                            failedHoles.add(detected.originalReference as FrameAnalyzer.Hole)
                        if (detected.originalReference is FrameAnalyzer.Countersink)
                            failedCountersinks.add(detected.originalReference as FrameAnalyzer.Countersink)
                    }
                }

                // 4. Register Missing and Extras
                missingFeatures.addAll(matchResult.missing)
                extraFeatures.addAll(matchResult.extras)

                // Update general status
                if (missingFeatures.isNotEmpty() || failedHoles.isNotEmpty() || failedCountersinks.isNotEmpty()) {
                    holesOk = false // Simplify global flag for reject
                }

            } else {
                // --- GLOBAL VALIDATION (Fallback: Count and Averages) ---
                // Validate Holes and Alodine
                result.holes.forEach { hole ->
                    val diameterMm = hole.radius * 2 * estimatedScale
                    val dimensionBad =
                        specificationValidator.validateHole(diameterMm, s) != DetectionStatus.OK
                    val alodineBad = if (s.requireAlodineHalo) !hole.hasAlodine else false

                    if (dimensionBad || alodineBad) failedHoles.add(hole)
                }
                if (failedHoles.isNotEmpty()) holesOk = false

                // Validate Countersinks
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

            // Validate Scratches (Always global for now)
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

        // Convert Mat (with base drawings) to Bitmap
        val annotatedBmp = Bitmap.createBitmap(
            result.annotatedMat.cols(),
            result.annotatedMat.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(result.annotatedMat, annotatedBmp)

        // POST-PROCESSING: Draw Traffic Light (Green/Red) on Bitmap using Android Canvas
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
        // Recalculated only if needed.
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

        // Paint Holes (Overwrite OpenCV with validation colors)
        result.holes.forEach { hole ->
            paint.color =
                if (failedHoles.contains(hole)) android.graphics.Color.RED else android.graphics.Color.GREEN
            // If it is EXTRA (in extraFeatures) paint it Yellow
            if (extraFeatures.any { it.originalReference == hole }) paint.color =
                android.graphics.Color.YELLOW

            canvas.drawCircle(
                hole.center.x.toFloat(),
                hole.center.y.toFloat(),
                hole.radius.toFloat(),
                paint
            )
        }

        // Paint Countersinks
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

        // Paint Scratches
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

        // Paint Ghosts (Missing Features)
        if (estimatedScale > 0.001) {
            missingFeatures.forEach { missing ->
                // Map MM back to PX
                val px = (missing.positionX_mm / estimatedScale) + originXDraw
                val py = (missing.positionY_mm / estimatedScale) + originYDraw
                val r = (missing.diameter_mm / 2) / estimatedScale
                canvas.drawCircle(px.toFloat(), py.toFloat(), r.toFloat(), paintGhost)
            }
        }

        // Map internal result to ViewModel DTO
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

        // Store annotated image
        analysis.annotatedBitmap?.let { bmp ->
            repository.saveImage(bmp, frameId)
        }

        // Map text label to DetectionType Enum
        val typeEnum = when {
            classification.type.contains("Defect") -> DetectionType.DEFECT
            classification.type.contains("Bend") -> DetectionType.BEND
            classification.type.contains("Part") -> DetectionType.HOLE
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