package com.example.celestic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.celestic.R
import com.example.celestic.models.calibration.DetectedFeature
import java.util.Locale

/**
 * Renders a technical blueprint‑style view showing detected features on a coordinate grid.
 *
 * Implements full visual defect tagging:
 * - Approved elements (OK) are highlighted in neon green / cyan.
 * - Out-of-tolerance elements (DEFECT) jump out in high-contrast RED with:
 *     * Actual measured parameter (mm/in)
 *     * Nominal reference specification (mm/in)
 *     * Tolerance range (±Tol mm)
 *     * Deviation delta (Δ mm)
 */
@Composable
fun BlueprintView(
    features: List<DetectedFeature>,
    useInches: Boolean = false,
    faceOrientation: String = "OBVERSE",
    partName: String = "TECHNICAL SHEET"
) {
    val textMeasurer = rememberTextMeasurer()

    // Localized labels
    val strBlueprintPrefix = stringResource(R.string.blueprint_prefix)
    val strFaceLabel = stringResource(R.string.blueprint_face_label)
    val strScaleLabel = stringResource(R.string.blueprint_scale_label)
    val strNonConforming = stringResource(R.string.blueprint_non_conforming)
    val strOk = stringResource(R.string.blueprint_ok)
    val strActual = stringResource(R.string.blueprint_actual_label)
    val strNominal = stringResource(R.string.blueprint_nominal_label)
    val strDev = stringResource(R.string.blueprint_dev_label)
    val strOutOfTol = stringResource(R.string.blueprint_out_of_tol)

    // Technical Blueprint Palette
    val blueprintBg = Color(0xFF0A192F)
    val blueprintGridColor = Color(0xFF1E3A5F).copy(alpha = 0.45f)
    val blueprintBorderColor = Color(0xFF204E79)
    val okColor = Color(0xFF00E676)
    val defectColor = Color(0xFFFF1744)
    val textColor = Color.White.copy(alpha = 0.9f)
    val tagBgColor = Color(0xFF0D1B2A).copy(alpha = 0.85f)
    val unit = if (useInches) "in" else "mm"
    val unitScale = if (useInches) 1f / 25.4f else 1f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(blueprintBg)
    ) {
        // 1. Technical Coordinate Grid
        val step = 40f
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

        // 2. Blueprint Framing & Title Block
        drawRect(
            color = blueprintBorderColor,
            topLeft = Offset(8f, 8f),
            size = Size(size.width - 16f, size.height - 16f),
            style = Stroke(width = 1.5f)
        )

        // Title Block in Top-Left
        drawRoundRect(
            color = tagBgColor,
            topLeft = Offset(14f, 14f),
            size = Size(280f, 58f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = blueprintBorderColor,
            topLeft = Offset(14f, 14f),
            size = Size(280f, 58f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 1f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "$strBlueprintPrefix: $partName",
            style = TextStyle(color = Color(0xFF4FC3F7), fontSize = 10.sp, fontWeight = FontWeight.Bold),
            topLeft = Offset(20f, 18f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "$strFaceLabel: $faceOrientation | Geometría no cuadrada",
            style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium),
            topLeft = Offset(20f, 32f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "$strScaleLabel: 1:1 ($unit) | Cotas de aristas, bordes y taladros",
            style = TextStyle(color = Color.Gray, fontSize = 8.sp),
            topLeft = Offset(20f, 46f)
        )

        // 3. Render Features with Defect Callouts
        features.forEachIndexed { index, feature ->
            val isDefect = feature.measurements["is_defect"] == 1.0f ||
                    feature.confidence < 0.65f ||
                    feature.measurements["error_mm"]?.let { it > 0.2f } == true

            val primaryColor = if (isDefect) defectColor else okColor

            // Nominal and Measured Values
            val measuredMm = feature.measurements["diameter"]
                ?: feature.measurements["measured"]
                ?: feature.measurements["actual"]
                ?: 6.0f
            val nominalMm = feature.measurements["nominal"] ?: 6.0f
            val toleranceMm = feature.measurements["tolerance"] ?: 0.10f
            val deltaMm = measuredMm - nominalMm

            val displayMeasured = measuredMm * unitScale
            val displayNominal = nominalMm * unitScale
            val displayTolerance = toleranceMm * unitScale

            // Draw Feature Target Point
            val center = Offset(feature.xCoord, feature.yCoord)
            val outerRadius = if (isDefect) 12f else 8f

            // Pulsing / warning outer circle
            if (isDefect) {
                drawCircle(
                    color = defectColor.copy(alpha = 0.25f),
                    center = center,
                    radius = outerRadius + 8f
                )
            }

            drawCircle(
                color = primaryColor,
                center = center,
                radius = outerRadius,
                style = Stroke(width = if (isDefect) 2.5f else 1.5f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.4f),
                center = center,
                radius = 3.5f
            )

            // Leader Line
            val leaderOffset = if (isDefect) Offset(60f, -45f) else Offset(35f, -30f)
            val tagAnchor = center + leaderOffset

            drawLine(
                color = primaryColor.copy(alpha = 0.7f),
                start = center,
                end = tagAnchor,
                strokeWidth = if (isDefect) 1.5f else 1f
            )

            // Defect / Measurement Callout Box
            val boxWidth = if (isDefect) 185f else 115f
            val boxHeight = if (isDefect) 62f else 28f
            val boxTopLeft = Offset(
                x = (tagAnchor.x).coerceAtMost(size.width - boxWidth - 12f).coerceAtLeast(12f),
                y = (tagAnchor.y - boxHeight / 2).coerceAtMost(size.height - boxHeight - 12f).coerceAtLeast(12f)
            )

            // Background card for tag
            drawRoundRect(
                color = tagBgColor,
                topLeft = boxTopLeft,
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = if (isDefect) defectColor else blueprintBorderColor,
                topLeft = boxTopLeft,
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = if (isDefect) 1.5f else 0.8f)
            )

            // Feature Identifier
            val featureId = "F${index + 1}-${feature.featureType.uppercase(Locale.ROOT)}"

            if (isDefect) {
                // High-visibility Defect Badge
                drawText(
                    textMeasurer = textMeasurer,
                    text = "⚠️ $featureId [$strNonConforming]",
                    style = TextStyle(color = defectColor, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    topLeft = boxTopLeft + Offset(6f, 4f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$strActual: ${String.format(Locale.US, "%.2f", displayMeasured)} $unit",
                    style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                    topLeft = boxTopLeft + Offset(6f, 18f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$strNominal: ${String.format(Locale.US, "%.2f", displayNominal)} ±${String.format(Locale.US, "%.2f", displayTolerance)} $unit",
                    style = TextStyle(color = Color(0xFFFFB74D), fontSize = 8.sp),
                    topLeft = boxTopLeft + Offset(6f, 32f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$strDev: Δ ${if (deltaMm >= 0) "+" else ""}${String.format(Locale.US, "%.2f", deltaMm)} $unit ($strOutOfTol)",
                    style = TextStyle(color = defectColor, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    topLeft = boxTopLeft + Offset(6f, 46f)
                )
            } else {
                // OK Label
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$featureId ($strOk)",
                    style = TextStyle(color = okColor, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    topLeft = boxTopLeft + Offset(6f, 3f)
                )
                val measureText = if (feature.featureType.contains("edge") || feature.featureType.contains("dim")) {
                    "L: ${String.format(Locale.US, "%.2f", displayMeasured)} $unit"
                } else {
                    "Ø ${String.format(Locale.US, "%.2f", displayMeasured)} $unit"
                }
                drawText(
                    textMeasurer = textMeasurer,
                    text = measureText,
                    style = TextStyle(color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    topLeft = boxTopLeft + Offset(6f, 14f)
                )
            }
        }
    }
}
