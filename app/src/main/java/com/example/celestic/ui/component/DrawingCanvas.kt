package com.example.celestic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.celestic.models.calibration.DetectedFeature

@Composable
fun DrawingCanvas(features: List<DetectedFeature>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        features.forEach { feature ->
            drawCircle(
                color = Color.Red,
                center = Offset(feature.xCoord, feature.yCoord),
                radius = 5f,
                style = Stroke(width = 2f)
            )
        }
    }
}
