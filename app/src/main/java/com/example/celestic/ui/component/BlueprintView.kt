package com.example.celestic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.example.celestic.models.calibration.DetectedFeature

@Composable
fun BlueprintView(features: List<DetectedFeature>) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue.copy(alpha = 0.1f))
    ) {
        features.forEach { feature ->
            drawCircle(
                color = Color.White,
                center = Offset(feature.xCoord, feature.yCoord),
                radius = 5f,
                style = Stroke(width = 2f)
            )
        }
    }
}

@Preview
@Composable
fun BlueprintViewPreview() {
    BlueprintView(
        features = listOf(
            DetectedFeature(
                id = 1,
                detectionItemId = 1,
                featureType = "type",
                xCoord = 100f,
                yCoord = 100f,
                confidence = 0.9f,
                timestamp = 1234567890
            ),
            DetectedFeature(
                id = 2,
                detectionItemId = 1,
                featureType = "type",
                xCoord = 200f,
                yCoord = 200f,
                confidence = 0.9f,
                timestamp = 1234567890
            )
        )
    )
}
