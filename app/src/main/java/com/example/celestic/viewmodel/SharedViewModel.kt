package com.example.celestic.viewmodel

import androidx.lifecycle.ViewModel
import com.example.celestic.data.repository.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val sharedData: SharedDataRepository
) : ViewModel() {

    val useInches: StateFlow<Boolean> = sharedData.useInches
    val markerType: StateFlow<MarkerType> = sharedData.markerType
    val isDarkMode: StateFlow<Boolean> = sharedData.isDarkMode

    val deviceModel: String = sharedData.deviceModel
    val hardwareInfo: String = sharedData.hardwareInfo

    fun setUseInches(value: Boolean) {
        sharedData.setUseInches(value)
    }

    fun setMarkerType(type: MarkerType) {
        sharedData.setMarkerType(type)
    }

    fun setDarkMode(dark: Boolean) {
        sharedData.setDarkMode(dark)
    }
}
