package com.example.celestic.data.repository

import com.example.celestic.models.DetectionItem
import com.google.android.libraries.mapsplatform.transportation.consumer.model.MarkerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedDataRepository @Inject constructor() {
    // Inspection state
    private val _currentInspectionId = MutableStateFlow<Long?>(null)
    val currentInspectionId: StateFlow<Long?> = _currentInspectionId.asStateFlow()

    // Specification state
    private val _selectedSpecificationId = MutableStateFlow<Long?>(null)
    val selectedSpecificationId: StateFlow<Long?> = _selectedSpecificationId.asStateFlow()

    // Camera and marker settings
    private val _markerType = MutableStateFlow(MarkerType.ARUCO)
    val markerType: StateFlow<MarkerType> = _markerType.asStateFlow()

    private val _useInches = MutableStateFlow(false)
    val useInches: StateFlow<Boolean> = _useInches.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Processing state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Results state
    private val _lastDetectionResults = MutableStateFlow<List<DetectionItem>?>(null)
    val lastDetectionResults: StateFlow<List<DetectionItem>?> = _lastDetectionResults.asStateFlow()

    // Device info (static, no need for StateFlow)
    val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
    val hardwareInfo = "CPU: ${android.os.Build.HARDWARE} | API: ${android.os.Build.VERSION.SDK_INT}"

    // Methods
    fun setCurrentInspection(id: Long) {
        _currentInspectionId.value = id
    }

    fun setSelectedSpecification(id: Long) {
        _selectedSpecificationId.value = id
    }

    fun setMarkerType(type: MarkerType) {
        _markerType.value = type
    }

    fun setUseInches(useInches: Boolean) {
        _useInches.value = useInches
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    fun setProcessing(processing: Boolean) {
        _isProcessing.value = processing
    }

    fun setLastDetectionResults(results: List<DetectionItem>?) {
        _lastDetectionResults.value = results
    }

    fun clearCurrentInspection() {
        _currentInspectionId.value = null
        _lastDetectionResults.value = null
    }

    // Utility to get current state
    fun getCurrentState(): SharedAppState {
        return SharedAppState(
            currentInspectionId = _currentInspectionId.value,
            selectedSpecificationId = _selectedSpecificationId.value,
            markerType = _markerType.value,
            useInches = _useInches.value,
            isDarkMode = _isDarkMode.value,
            isProcessing = _isProcessing.value,
            lastDetectionResults = _lastDetectionResults.value
        )
    }
}

data class SharedAppState(
    val currentInspectionId: Long?,
    val selectedSpecificationId: Long?,
    val markerType: MarkerType,
    val useInches: Boolean,
    val isDarkMode: Boolean,
    val isProcessing: Boolean,
    val lastDetectionResults: List<DetectionItem>?
)
