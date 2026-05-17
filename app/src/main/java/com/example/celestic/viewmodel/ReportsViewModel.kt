package com.example.celestic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.DetectionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for generating batch and individual inspection reports.
 * 
 * Bridges the gap between inspection history and document generation by
 * grouping detections by batch number (QR code).
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: DetectionRepository
) : ViewModel() {

    // Unique batch identifiers found in the database
    val batches: StateFlow<List<String>> = repository.getUniqueBatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All detections to show statistics in the report module
    private val _allDetections = MutableStateFlow<List<DetectionItem>>(emptyList())
    val allDetections: StateFlow<List<DetectionItem>> = _allDetections.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllDetectionItems().collect {
                _allDetections.value = it
            }
        }
    }

    /**
     * Calculates the total number of items detected for a specific batch.
     */
    fun getBatchCount(batchCode: String): Int {
        return _allDetections.value.count { it.linkedQrCode == batchCode }
    }
}
