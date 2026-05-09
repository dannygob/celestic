package com.example.celestic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.enums.Orientation
import com.example.celestic.opencv.ImageProcessorResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoldenSampleViewModel @Inject constructor(
    private val repository: DetectionRepository
) : ViewModel() {

    private val _blueprintName = MutableStateFlow("")
    val blueprintName: StateFlow<String> = _blueprintName.asStateFlow()

    private val _hasTwoFaces = MutableStateFlow(false)
    val hasTwoFaces: StateFlow<Boolean> = _hasTwoFaces.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _obverseFeatures = MutableStateFlow<List<SpecificationFeature>>(emptyList())
    val obverseFeatures: StateFlow<List<SpecificationFeature>> = _obverseFeatures.asStateFlow()

    private val _reverseFeatures = MutableStateFlow<List<SpecificationFeature>>(emptyList())
    val reverseFeatures: StateFlow<List<SpecificationFeature>> = _reverseFeatures.asStateFlow()

    fun updateName(name: String) {
        _blueprintName.value = name
    }

    fun toggleTwoFaces(hasTwo: Boolean) {
        _hasTwoFaces.value = hasTwo
        if (!hasTwo) {
            _reverseFeatures.value = emptyList()
        }
    }

    /**
     * Captures the current image processing result, extracting holes and countersinks.
     * Uses Option 2 (Relative Coordinates): It calculates the top-left most feature
     * and uses it as the (0,0) origin to make the system independent of where
     * the piece is placed on the table.
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
                specificationId = 0, // Room will assign this via Foreign Key later
                face = face,
                type = item.type,
                // We subtract minX and minY so the top-left hole is always (0,0)
                positionX_mm = (item.boundingBox.x - minX).toDouble(),
                positionY_mm = (item.boundingBox.y - minY).toDouble(),
                diameter_mm = item.measurementMm?.toDouble() ?: 10.0,
                tolerance_mm = 2.0, // Default 2mm tolerance for real factory floors
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

    fun saveBlueprint(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true

            // Create the Master Blueprint
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

            // Transactional Save
            repository.saveSpecificationWithFeatures(spec, allFeatures)

            _isSaving.value = false
            onSuccess()
        }
    }
}
