package com.example.celestic.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FrameAnalyzer(private val listener: (Any) -> Unit) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        // Implement the frame analysis logic
        image.close()
    }
}
