package com.example.celestic.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.manager.AprilTagManager
import com.example.celestic.manager.ArUcoManager
import com.example.celestic.manager.CalibrationManager
import com.example.celestic.opencv.FrameAnalyzer
import com.example.celestic.ui.scanner.QRScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val repository: DetectionRepository,
    private val calibrationManager: CalibrationManager,
    private val arucoManager: ArUcoManager,
    private val aprilTagManager: AprilTagManager,
    private val qrScanner: QRScanner,
    private val frameAnalyzer: FrameAnalyzer,
    private val sharedViewModel: SharedViewModel
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val state: StateFlow<DashboardState> = _state

    fun startInspection() {
        _state.value = DashboardState.CameraReady
    }

    fun onFrameCaptured(bitmap: Bitmap) {
        _state.value = DashboardState.Processing

        viewModelScope.launch {
            try {
                val face = detectFaceWithOpenCV(bitmap)

                val classification = classifyWithTensorFlowLite(
                    roi = face.roiBitmap,
                    faceLabel = face.faceLabel
                )

                val analysis = analyzeWithFrameAnalyzer(bitmap)

                val detectionId = saveResultsToRoom(
                    face = face,
                    classification = classification,
                    analysis = analysis
                )

                when (classification.status) {
                    DetectionStatus.OK -> {
                        _state.value = DashboardState.Approved(detectionId)
                    }

                    DetectionStatus.WARNING,
                    DetectionStatus.NOT_ACCEPTED -> {
                        _state.value = DashboardState.NavigateToDetails(detectionId)
                    }
                }

            } catch (e: Exception) {
                _state.value = DashboardState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun startNewInspection() {
        _state.value = DashboardState.CameraReady
    }

    fun viewReport(detectionId: Long) {
        // La UI decide navegación
    }

    fun resetState() {
        _state.value = DashboardState.Idle
    }

    // -------------------------
    // FUNCIONES INTERNAS
    // -------------------------

    private fun detectFaceWithOpenCV(bitmap: Bitmap): FaceDetectionResult {
        throw NotImplementedError("detectFaceWithOpenCV no implementado aún")
    }

    private fun classifyWithTensorFlowLite(
        roi: Bitmap,
        faceLabel: String
    ): ClassificationResult {
        throw NotImplementedError("classifyWithTensorFlowLite no implementado aún")
    }

    private fun analyzeWithFrameAnalyzer(bitmap: Bitmap): FrameAnalysisResult {
        throw NotImplementedError("analyzeWithFrameAnalyzer no implementado aún")
    }

    private suspend fun saveResultsToRoom(
        face: FaceDetectionResult,
        classification: ClassificationResult,
        analysis: FrameAnalysisResult
    ): Long {
        throw NotImplementedError("saveResultsToRoom no implementado aún")
    }
}