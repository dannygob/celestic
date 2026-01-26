package com.example.celestic.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class MarkerType {
    ARUCO,
    APRILTAG
}

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    private val _useInches = MutableStateFlow(false)
    val useInches: StateFlow<Boolean> = _useInches

    private val _markerType = MutableStateFlow(MarkerType.ARUCO)
    val markerType: StateFlow<MarkerType> = _markerType

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
    val hardwareInfo =
        "CPU: ${android.os.Build.HARDWARE} | API: ${android.os.Build.VERSION.SDK_INT}"

    fun setUseInches(useInches: Boolean) {
        _useInches.value = useInches
    }

    fun setMarkerType(markerType: MarkerType) {
        _markerType.value = markerType
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }
}
