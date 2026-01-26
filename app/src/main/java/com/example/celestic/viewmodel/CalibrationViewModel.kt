package com.example.celestic.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.manager.CalibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import javax.inject.Inject

data class CalibrationState(
    val capturedFrames: Int = 0,
    val isCalibrating: Boolean = false,
    val rmsError: Double? = null,
    val lastCaptureSuccess: Boolean? = null,
    val calibrationDate: String? = null
)

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val calibrationManager: CalibrationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CalibrationState(
            calibrationDate = calibrationManager.calibrationDate
        )
    )
    val uiState: StateFlow<CalibrationState> = _uiState.asStateFlow()

    fun captureFrame(bitmap: Bitmap) {
        viewModelScope.launch {
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)

            val success = calibrationManager.addCalibrationFrame(mat)
            mat.release()

            _uiState.value = _uiState.value.copy(
                capturedFrames = _uiState.value.capturedFrames + if (success) 1 else 0,
                lastCaptureSuccess = success
            )
        }
    }

    fun runCalibration() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalibrating = true)

            val rms = calibrationManager.runCalibration()

            _uiState.value = _uiState.value.copy(
                isCalibrating = false,
                rmsError = rms,
                calibrationDate = calibrationManager.calibrationDate
            )
        }
    }

    fun reset() {
        calibrationManager.resetData()
        _uiState.value = CalibrationState(
            calibrationDate = calibrationManager.calibrationDate
        )
    }
}
