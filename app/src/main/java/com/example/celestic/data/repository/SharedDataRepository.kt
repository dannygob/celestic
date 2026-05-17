package com.example.celestic.data.repository

import com.example.celestic.models.DetectionItem
import com.example.celestic.viewmodel.MarkerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedDataRepository @Inject constructor() {

    // ===== INSPECTION STATE =====

    /** Holds the ID of the currently active inspection. */
    private val _currentInspectionId = MutableStateFlow<Long?>(null)
    val currentInspectionId: StateFlow<Long?> = _currentInspectionId.asStateFlow()

    // ===== SPECIFICATION STATE =====

    /** Stores the ID of the currently selected specification. */
    private val _selectedSpecificationId = MutableStateFlow<Long?>(null)
    val selectedSpecificationId: StateFlow<Long?> = _selectedSpecificationId.asStateFlow()

    // ===== CAMERA & MARKER SETTINGS =====

    /** Defines the currently selected marker type (e.g., ARUCO). */
    private val _markerType = MutableStateFlow(MarkerType.ARUCO)
    val markerType: StateFlow<MarkerType> = _markerType.asStateFlow()

    /** Indicates whether measurements should be displayed in inches instead of millimeters. */
    private val _useInches = MutableStateFlow(false)
    val useInches: StateFlow<Boolean> = _useInches.asStateFlow()

    /** Tracks whether the app is currently using dark mode. */
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // ===== PROCESSING STATE =====

    /** Indicates whether the system is currently processing detection or analysis tasks. */
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /** Stores the last known latitude of the device. */
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    /** Stores the last known longitude of the device. */
    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    // ===== RESULTS STATE =====

    /** Holds the most recent detection results produced by the system. */
    private val _lastDetectionResults = MutableStateFlow<List<DetectionItem>?>(null)
    val lastDetectionResults: StateFlow<List<DetectionItem>?> = _lastDetectionResults.asStateFlow()

    // ===== DEVICE INFO (STATIC) =====

    /**
     * ==================================================================================
     * 📊 TELEMETRÍA PÚBLICA - PROPIEDADES EXPUESTAS PARA DIAGNÓSTICO
     * ==================================================================================
     * ¿POR QUÉ APARECEN SIN USO EN ESTE ARCHIVO?
     * Son propiedades públicas declaradas a nivel de miembro que no se leen internamente.
     * 
     * ¿PARA QUÉ SE DEJAN?
     * Están diseñadas para que cualquier componente de logs, exportadores de reportes PDF/JSON
     * o la vista de autodiagnóstico 'StatusScreen' consulte directamente la información del
     * hardware del dispositivo operario.
     * ==================================================================================
     */
    val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"

    /** Hardware summary including CPU and API level. */
    val hardwareInfo = "CPU: ${android.os.Build.HARDWARE} | API: ${android.os.Build.VERSION.SDK_INT}"

    // ===== STATE MUTATION METHODS =====

    /** Sets the currently active inspection ID. */
    fun setCurrentInspection(id: Long) {
        _currentInspectionId.value = id
    }

    /** Sets the currently selected specification ID. */
    fun setSelectedSpecification(id: Long) {
        _selectedSpecificationId.value = id
    }

    /** Updates the selected marker type. */
    fun setMarkerType(type: MarkerType) {
        _markerType.value = type
    }

    /** Enables or disables inch-based measurement mode. */
    fun setUseInches(useInches: Boolean) {
        _useInches.value = useInches
    }

    /** Toggles dark mode on or off. */
    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    /** Sets whether the system is currently processing data. */
    fun setProcessing(processing: Boolean) {
        _isProcessing.value = processing
    }

    /** Stores the latest detection results. */
    fun setLastDetectionResults(results: List<DetectionItem>?) {
        _lastDetectionResults.value = results
    }

    /** Updates the device's last known GPS location. */
    fun setLocation(lat: Double?, lon: Double?) {
        _latitude.value = lat
        _longitude.value = lon
    }

    /** Clears the current inspection and its associated results. */
    fun clearCurrentInspection() {
        _currentInspectionId.value = null
        _lastDetectionResults.value = null
    }

    // ===== STATE SNAPSHOT =====

    /**
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? Las pantallas y viewmodels de Jetpack Compose leen el estado reactivo
     *   de forma continua mediante flujos observables (StateFlow.collectAsState()). No se requiere
     *   realizar capturas de estado no reactivas (snapshots) en el flujo principal.
     * - ¿Para qué se deja? Para facilitar la depuración, exportación rápida de telemetría y diagnóstico
     *   de fallos de sincronización en caliente (hot state telemetry dumps) en futuras herramientas de soporte técnico.
     *
     * Returns a snapshot of the current shared application state.
     * Useful for debugging or exporting state.
     */
    fun getCurrentState(): SharedAppState {
        return SharedAppState(
            currentInspectionId = _currentInspectionId.value,
            selectedSpecificationId = _selectedSpecificationId.value,
            markerType = _markerType.value,
            useInches = _useInches.value,
            isDarkMode = _isDarkMode.value,
            isProcessing = _isProcessing.value,
            lastDetectionResults = _lastDetectionResults.value,
            latitude = _latitude.value,
            longitude = _longitude.value
        )
    }
}

/** Immutable snapshot of the shared application state. */
data class SharedAppState(
    val currentInspectionId: Long?,
    val selectedSpecificationId: Long?,
    val markerType: MarkerType,
    val useInches: Boolean,
    val isDarkMode: Boolean,
    val isProcessing: Boolean,
    val lastDetectionResults: List<DetectionItem>?,
    val latitude: Double?,
    val longitude: Double?
)
