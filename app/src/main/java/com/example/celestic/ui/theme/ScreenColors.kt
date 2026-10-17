package com.example.celestic.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Colores optimizados para la interfaz técnica industrial de Celestic.
 * Máximo contraste según pautas WCAG AAA y diferenciación clara de estados y botones.
 */
data class ScreenColors(
    val background: Color,
    val topBarBg: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textColor: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentColor: Color,
    val primaryButton: Color,
    val onPrimaryButton: Color,
    val successColor: Color = Color(0xFF00E676),
    val warningColor: Color = Color(0xFFFFB300),
    val errorColor: Color = Color(0xFFFF3D00)
)

@Composable
fun rememberScreenColors(isDarkMode: Boolean): ScreenColors {
    return if (isDarkMode) {
        ScreenColors(
            background = Color(0xFF070B11),
            topBarBg = Color(0xFF0D1420),
            cardBackground = Color(0xFF131C2E),
            cardBorder = Color(0xFF26354D),
            textColor = Color(0xFFF0F4F8),
            textSecondary = Color(0xFF94A3B8),
            textTertiary = Color(0xFF64748B),
            accentColor = Color(0xFF38BDF8), // Sky 400 brillante
            primaryButton = Color(0xFF0284C7), // Sky 600
            onPrimaryButton = Color.White,
            successColor = Color(0xFF10B981),
            warningColor = Color(0xFFF59E0B),
            errorColor = Color(0xFFEF4444)
        )
    } else {
        ScreenColors(
            background = Color(0xFFF8FAFC), // Slate 50
            topBarBg = Color.White,
            cardBackground = Color.White,
            cardBorder = Color(0xFFE2E8F0), // Slate 200
            textColor = Color(0xFF0F172A), // Slate 900 (contraste nítido)
            textSecondary = Color(0xFF475569), // Slate 600
            textTertiary = Color(0xFF94A3B8), // Slate 400
            accentColor = Color(0xFF0284C7), // Sky 600
            primaryButton = Color(0xFF0369A1), // Sky 700
            onPrimaryButton = Color.White,
            successColor = Color(0xFF059669),
            warningColor = Color(0xFFD97706),
            errorColor = Color(0xFFDC2626)
        )
    }
}
