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

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: DetectionRepository,
    @field:ApplicationContext private val context: Context
) : ViewModel() {

    private val _traceabilityItem = MutableStateFlow<Result<TraceabilityItem?>>(Result.Loading)
    val traceabilityItem: StateFlow<Result<TraceabilityItem?>> = _traceabilityItem

    private val _features = MutableStateFlow<List<com.example.celestic.models.calibration.DetectedFeature>>(emptyList())
    val features: StateFlow<List<com.example.celestic.models.calibration.DetectedFeature>> = _features

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

    fun loadFeatures(detectionItemId: Long) {
        viewModelScope.launch {
            repository.getFeaturesForDetection(detectionItemId).collect {
                _features.value = it
            }
        }
    }
}
