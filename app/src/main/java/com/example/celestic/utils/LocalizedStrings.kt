package com.example.celestic.utils

import androidx.compose.runtime.compositionLocalOf

data class LocalizedStrings(
    val dashboardTitle: String = "Panel principal",
    val calibrationSection: String = "Calibración y configuración",
    val calibrationOpen: String = "Abrir calibración",
    val analysisModes: String = "Modos de inspección",
    val modeBodywork: String = "Carrocería",
    val modePrecision: String = "Precisión",
    val modeMetals: String = "Metal",
    val reportSection: String = "Reportes y historial",
    val reportGenerate: String = "Generar reporte",
    val viewHistory: String = "Ver historial",
    val languageSettingHint: String = "Idioma actual: Español",
    val calibrationOpening: String = "Navegando a calibración...",
    val dashboardOpenDetails: String = "Abriendo historial...",
    val toastOpenReportDialog: String = "Solicitando generación de reporte...",
    val modeBodyworkActivated: String = "Mod Carrocería seleccionado",
    val modePrecisionActivated: String = "Modo Precisión seleccionado",
    val celesticMode: String = "Modo Metal seleccionado",
)

val LocalLocalizedStrings = compositionLocalOf { LocalizedStrings() }