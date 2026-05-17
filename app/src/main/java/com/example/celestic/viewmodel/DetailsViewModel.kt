package com.example.celestic.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.TraceabilityItem
import com.example.celestic.utils.JsonLoader
import com.example.celestic.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the inspection details screen.
 * 
 * Responsible for loading specific detection records, their associated features,
 * and performing traceability lookups via JSON. It also allows manual status 
 * overrides for quality control.
 */
@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: DetectionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _traceabilityItem = MutableStateFlow<Result<TraceabilityItem?>>(Result.Loading)
    val traceabilityItem: StateFlow<Result<TraceabilityItem?>> = _traceabilityItem

    private val _features = MutableStateFlow<List<com.example.celestic.models.calibration.DetectedFeature>>(emptyList())
    val features: StateFlow<List<com.example.celestic.models.calibration.DetectedFeature>> = _features

    private val _detectionItem = MutableStateFlow<com.example.celestic.models.DetectionItem?>(null)
    val detectionItem: StateFlow<com.example.celestic.models.DetectionItem?> = _detectionItem

    /**
     * Loads traceability data for a specific part code.
     * 
     * @param code The part code or QR code string.
     */
    fun loadTraceability(code: String) {
        viewModelScope.launch {
            try {
                val list = JsonLoader.loadTraceabilityFromJson(context)
                _traceabilityItem.value = Result.Success(JsonLoader.findByCode(code, list))
            } catch (e: Exception) {
                _traceabilityItem.value = Result.Error(e)
            }
        }
    }

    /**
     * Loads features (measurements) associated with a specific detection record.
     * 
     * @param detectionItemId The ID of the detection record.
     */
    fun loadFeatures(detectionItemId: Long) {
        viewModelScope.launch {
            repository.getFeaturesForDetection(detectionItemId).collect {
                _features.value = it
            }
        }
    }

    /**
     * Loads a complete detection record and its related data by its ID.
     * 
     * @param id The ID of the detection to load.
     */
    fun loadDetectionById(id: Long) {
        viewModelScope.launch {
            _traceabilityItem.value = Result.Loading
            _features.value = emptyList()
            
            val item = repository.getDetectionById(id)
            _detectionItem.value = item

            if (item != null) {
                loadFeatures(item.id)
                if (!item.linkedQrCode.isNullOrEmpty()) {
                    loadTraceability(item.linkedQrCode)
                } else {
                    _traceabilityItem.value = Result.Success(null)
                }
            } else {
                _traceabilityItem.value = Result.Success(null)
            }
        }
    }

    /**
     * Manually overrides the status of the current detection to "OK".
     * 
     * Used in industrial environments for false positive correction.
     * Updates the database and triggers local state refresh.
     */
    fun overrideStatusToOk() {
        val currentItem = _detectionItem.value ?: return
        viewModelScope.launch {
            val updatedItem = currentItem.copy(
                status = com.example.celestic.models.enums.DetectionStatus.OK,
                notes = "${currentItem.notes ?: ""} | (MANUAL OVERRIDE: APPROVED)"
            )
            repository.insertDetection(updatedItem)
            _detectionItem.value = updatedItem

            // Cleanup: Delete heavy raw image if we manually approve the part
            try {
                val file = java.io.File(
                    context.filesDir,
                    "detection_images/${currentItem.frameId}.jpg"
                )
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Silent fail for cleanup
            }
        }
    }

    /**
     * Deletes the current detection from the database and internal storage.
     * 
     * @param onDeleted Callback invoked after successful deletion.
     */
    fun deleteCurrentDetection(onDeleted: () -> Unit) {
        val currentItem = _detectionItem.value ?: return
        viewModelScope.launch {
            repository.deleteDetection(currentItem)

            // Delete associated file if it exists
            try {
                val file = java.io.File(
                    context.filesDir,
                    "detection_images/${currentItem.frameId}.jpg"
                )
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Silent fail for file cleanup
            }
            onDeleted()
        }
    }
}
