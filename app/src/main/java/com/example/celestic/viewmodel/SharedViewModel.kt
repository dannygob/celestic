package com.example.celestic.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    private val _useInches = MutableStateFlow(false)
    val useInches: StateFlow<Boolean> = _useInches

    fun setUseInches(useInches: Boolean) {
        _useInches.value = useInches
    }
}
