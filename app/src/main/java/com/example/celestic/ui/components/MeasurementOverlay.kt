package com.example.celestic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.celestic.models.DetectionItem

@Composable
fun MeasurementOverlay(items: List<DetectionItem>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw measurements on the canvas
    }
}
