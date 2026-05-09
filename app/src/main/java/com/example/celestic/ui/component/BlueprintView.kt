package com.example.celestic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.celestic.models.calibration.DetectedFeature

/**
 * Renders a blueprint‑style view showing detected features on a grid.
 *
 * Each feature is drawn with:
 * - A highlighted anchor point
 * - A secondary inner circle
 * - A dimension line
 * - A measurement label (mm or inches)
 */
@Composable
fun BlueprintView(
    features: List<DetectedFeature>,
    useInches: Boolean = false
) {
    val textMeasurer = rememberTextMeasurer()

    // Blueprint color palette
    val blueprintGridColor = Color(0xFF1B263B).copy(alpha = 0.5f)
    val blueprintFeatureColor = Color(0xFF00B4D8)
    val textColor = Color.White.copy(alpha = 0.8f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        // Draw grid
        val step = 50f
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(
                color = blueprintGridColor,
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 0.5f
            )
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(
                color = blueprintGridColor,
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 0.5f
            )
        }

        // Draw detected features
        features.forEach { feature ->
            // Anchor point
            drawCircle(
                color = blueprintFeatureColor,
                center = Offset(feature.xCoord, feature.yCoord),
                radius = 8f,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = blueprintFeatureColor.copy(alpha = 0.3f),
                center = Offset(feature.xCoord, feature.yCoord),
                radius = 4f
            )

            // Decorative dimension line
            drawLine(
                color = blueprintFeatureColor.copy(alpha = 0.5f),
                start = Offset(feature.xCoord, feature.yCoord),
                end = Offset(feature.xCoord + 40, feature.yCoord - 40),
                strokeWidth = 1f
            )

            // Measurement label
            val unit = if (useInches) "in" else "mm"
            val dimension = if (useInches) {
                feature.measurements["diameter"]?.div(25.4f)
            } else {
                feature.measurements["diameter"]
            }

            dimension?.let {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${String.format(java.util.Locale.getDefault(), "%.2f", it)} $unit",
                    style = TextStyle(color = textColor, fontSize = 10.sp),
                    topLeft = Offset(feature.xCoord + 45, feature.yCoord - 55)
                )
            }
        }
    }
}
