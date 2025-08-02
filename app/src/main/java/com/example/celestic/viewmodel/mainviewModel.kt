package com.example.celestic.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.celestic.models.DetectionItem
import com.example.celestic.opencv.AlodineDetector
import com.example.celestic.opencv.HoleDetector
import org.opencv.core.Mat

class MainViewModel : ViewModel() {

    private val _holeItems = MutableLiveData<List<DetectionItem>>()
    val holeItems: LiveData<List<DetectionItem>> = _holeItems

    private val _alodineItems = MutableLiveData<List<DetectionItem>>()
    val alodineItems: LiveData<List<DetectionItem>> = _alodineItems

    private val _processedPath = MutableLiveData<String>()
    val processedPath: LiveData<String> = _processedPath

    fun analyzeFrame(frame: Mat) {
        val holeResult = HoleDetector.detectHoles(frame)
        val alodineResult = AlodineDetector.detect(holeResult.processedFrame, holeResult.items)

        _holeItems.value = holeResult.items
        _alodineItems.value = alodineResult.items
        _processedPath.value = saveFrameToCache(alodineResult.processedFrame)
    }

    private fun saveFrameToCache(mat: Mat): String {
        // Lógica de guardado aquí...
        return "ruta_fake.jpg"
    }
}