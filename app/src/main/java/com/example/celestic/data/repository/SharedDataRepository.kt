// 1. Crea SharedDataRepository.kt
package com.example.celestic.data.repository

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedDataRepository @Inject constructor() {
    private val _currentInspectionId = MutableStateFlow<Long?>(null)
    val currentInspectionId: StateFlow<Long?> = _currentInspectionId.asStateFlow()

    private val _selectedSpecificationId = MutableStateFlow<Long?>(null)
    val selectedSpecificationId: StateFlow<Long?> = _selectedSpecificationId.asStateFlow()

    fun setCurrentInspection(id: Long) {
        _currentInspectionId.value = id
    }

    fun setSelectedSpecification(id: Long) {
        _selectedSpecificationId.value = id
    }
}

// 2. Inyecta en ambos ViewModels
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val sharedData: SharedDataRepository
) : ViewModel() {
    fun setInspection(id: Long) = sharedData.setCurrentInspection(id)
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DetectionRepository,
    private val sharedData: SharedDataRepository  // ← En lugar de SharedViewModel
) : ViewModel() {
    val currentInspectionId = sharedData.currentInspectionId
}