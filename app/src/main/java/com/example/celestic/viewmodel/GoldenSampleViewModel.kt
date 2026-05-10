package com.example.celestic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.enums.Orientation
import com.example.celestic.opencv.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import javax.inject.Inject

@HiltViewModel
class GoldenSampleViewModel @Inject constructor(
    private val repository: DetectionRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _blueprintName = MutableStateFlow("")
    val blueprintName: StateFlow<String> = _blueprintName.asStateFlow()

    private val _hasTwoFaces = MutableStateFlow(false)
    val hasTwoFaces: StateFlow<Boolean> = _hasTwoFaces.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _obverseFeatures = MutableStateFlow<List<SpecificationFeature>>(emptyList())
    val obverseFeatures: StateFlow<List<SpecificationFeature>> = _obverseFeatures.asStateFlow()

    private val _reverseFeatures = MutableStateFlow<List<SpecificationFeature>>(emptyStateList())
    val reverseFeatures: StateFlow<List<SpecificationFeature>> = _reverseFeatures.asStateFlow()

    /**
     * Updates the internal name of the blueprint being created.
     * 
     * @param name The new identification code for the master piece.
     */
    fun updateName(name: String) {
        _blueprintName.value = name
    }

    /**
     * Toggles the complexity mode of the piece.
     * If set to false, it clears any captured features for the reverse face.
     * 
     * @param hasTwo True if the piece requires both Obverse and Reverse scanning.
     */
    fun toggleTwoFaces(hasTwo: Boolean) {
        _hasTwoFaces.value = hasTwo
        if (!hasTwo) {
            _reverseFeatures.value = emptyList()
        }
    }

    /**
     * Processes a captured bitmap to extract features for a specific face.
     * 
     * @param bitmap The image captured from the camera.
     * @param face The orientation being scanned.
     */
    fun captureFrame(bitmap: android.graphics.Bitmap, face: Orientation) {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // We use null for markerType here as the Golden Sample is often the source of truth,
        // or we could pull it from SharedData if needed.
        val result = imageProcessor.processImage(mat, null)
        captureFace(result, face)

        mat.release()
    }

    /**
     * Captures the current image processing result and extracts structural features.
     * 
     * This function implements Option 2 (Relative Coordinates): 
     * 1. Filters detected items for structural markers (Holes and Countersinks).
     * 2. Calculates the top-left most feature to use as a local (0,0) anchor.
     * 3. Maps detections into SpecificationFeatures with coordinates relative to the anchor.
     * 4. Updates the state for either the Obverse or Reverse face.
     * 
     * @param result The latest vision pipeline output from FrameAnalyzer.
     * @param face The orientation being scanned (ANVERSO or REVERSO).
     */
    fun captureFace(result: ImageProcessorResult, face: Orientation) {
        val validItems = result.detectedItems.filter {
            it.type == DetectionType.HOLE || it.type == DetectionType.COUNTERSINK
        }

        if (validItems.isEmpty()) return

        // OPTION 2: Relative Coordinates (Smart Bounding Box Offset)
        val minX = validItems.minOf { it.boundingBox.x }
        val minY = validItems.minOf { it.boundingBox.y }

        val features = validItems.map { item ->
            SpecificationFeature(
                specificationId = 0, // Assigned during DB transaction
                face = face,
                type = item.type,
                // Coordinate normalization relative to the piece's anchor point
                positionX_mm = (item.boundingBox.x - minX).toDouble(),
                positionY_mm = (item.boundingBox.y - minY).toDouble(),
                diameter_mm = item.measurementMm?.toDouble() ?: 10.0,
                tolerance_mm = 2.0, // Industrial default tolerance
                requireAlodine = false,
                requireCountersink = item.type == DetectionType.COUNTERSINK
            )
        }

        if (face == Orientation.ANVERSO) {
            _obverseFeatures.value = features
        } else {
            _reverseFeatures.value = features
        }
    }

    /**
     * Persists the captured blueprint and its associated features to the database.
     * 
     * Creates a new Specification entry and maps all scanned features for 
     * both faces in a single atomic transaction.
     * 
     * @param onSuccess Callback triggered when the database operation is complete.
     */
    fun saveBlueprint(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true

            val spec = Specification(
                name = _blueprintName.value.ifBlank { "NEW_MASTER_BLUEPRINT" },
                sheetType = if (_hasTwoFaces.value) "2_CARAS" else "1_CARA",
                minWidthMm = 0.0, maxWidthMm = 2000.0,
                minHeightMm = 0.0, maxHeightMm = 2000.0,
                expectedHoleCount = _obverseFeatures.value.count { it.type == DetectionType.HOLE } +
                        _reverseFeatures.value.count { it.type == DetectionType.HOLE },
                holeMinDiameterMm = 0.0, holeMaxDiameterMm = 100.0,
                holeTolerance = 2.0, holeNominalDiameterMm = 10.0,
                expectedCountersinkCount = _obverseFeatures.value.count { it.type == DetectionType.COUNTERSINK } +
                        _reverseFeatures.value.count { it.type == DetectionType.COUNTERSINK },
                countersinkMinDiameterMm = 0.0, countersinkMaxDiameterMm = 100.0,
                maxAllowedScratches = 0, maxScratchLengthMm = 0.0, maxAllowedDeformations = 0,
                requireAlodineHalo = false, minAlodineUniformity = 0.0
            )

            val allFeatures = _obverseFeatures.value + _reverseFeatures.value
            repository.saveSpecificationWithFeatures(spec, allFeatures)

            _isSaving.value = false
            onSuccess()
        }
    }
}

private fun <T> emptyStateList(): List<T> = emptyList()
