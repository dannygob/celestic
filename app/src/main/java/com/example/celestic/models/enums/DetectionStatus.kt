package com.example.celestic.models.enums

/**
 * Estado técnico de la detection basado en validation dimensional.
 * 🟢 OK: Dentro de tolerancia
 * 🟡 WARNING: Fuera de tolerancia pero no crític
 * 🔴 NOT_ACCEPTED: Invalid o defect crític
 */
enum class DetectionStatus {
    OK,           // 🟢 Válido
    WARNING,      // 🟡 Advertencia
    NOT_ACCEPTED  // 🔴 No aceptado
}