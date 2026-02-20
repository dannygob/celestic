package com.example.celestic.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Colores comunes utilizados en las pantallas de la aplicación para mantener consistencia.
 */
data class ScreenColors(
    val background: Color,
    val topBarBg: Color,
    val textColor: Color,
    val accentColor: Color
)

@Composable
fun rememberScreenColors(isDarkMode: Boolean): ScreenColors {
    return ScreenColors(
        background = if (isDarkMode) Color(0xFF0A0E14) else Color(0xFFF2F2F2),
        topBarBg = if (isDarkMode) Color.Black else Color.White,
        textColor = if (isDarkMode) Color.White else Color.Black,
        accentColor = if (isDarkMode) Color(0xFF4FC3F7) else Color(0xFF3366CC)
    )
}
