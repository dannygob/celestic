package com.example.celestic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.celestic.models.DetectionItem
import org.opencv.core.Mat

class MainViewModel : ViewModel() {

    private val _detectionItems = MutableLiveData<List<DetectionItem>>()
    val detectionItems: LiveData<List<DetectionItem>> = _detectionItems

    fun processFrame(frame: Mat) {
        // Lógica para procesar el frame y actualizar _detectionItems
    }
}