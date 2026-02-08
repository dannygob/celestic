package com.example.celestic.validation

import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.enums.DetectionType
import kotlin.math.hypot

data class MatchedFeature(
    val expected: SpecificationFeature,
    val detected: MatcherDetectedFeature?, // Null if MISSING
    val distanceErrorMm: Double,
    val status: MatchStatus
)

enum class MatchStatus {
    MATCH_OK,           // Encontrado y en posición
    POSITION_ERROR,     // Encontrado pero lejos (aunque dentro del umbral de búsqueda amplia, quizás no se use si threshold es estricto)
    MISSING,            // No encontrado
    EXTRA               // Sobrante (sin Spec asociada)
}

data class MatcherDetectedFeature(
    val x: Double,
    val y: Double,
    val type: DetectionType,
    val diameter: Double,
    val hasAlodine: Boolean,
    val originalReference: Any? = null // Reference to original OpenCV object (Hole, Countersink)
)

data class MatchingResult(
    val matches: List<MatchedFeature>,  // Found couples
    val missing: List<SpecificationFeature>, // Expected but not found
    val extras: List<MatcherDetectedFeature> // Found but not expected
)

class FeatureMatcher {

    /**
     * Empareja características esperadas (Plano) con características detectadas (Cámara).
     * @param expected Lista de features definidas en SpecificationFeature (Coordenadas mm)
     * @param detected Lista de features detectadas por FrameAnalyzer (Coordenadas YA CONVERTIDAS a mm relativas al mismo origen)
     * @param maxDistanceToleranceMm Radio máximo de búsqueda para considerar que un punto es el mismo (ej. 5mm)
     */
    fun matchFeatures(
        expected: List<SpecificationFeature>,
        detected: List<MatcherDetectedFeature>,
        maxDistanceToleranceMm: Double = 15.0 // Tolerancia amplia para encontrar candidtos
    ): MatchingResult {
        val matches = ArrayList<MatchedFeature>()
        val missing = ArrayList<SpecificationFeature>()
        val extras = ArrayList<MatcherDetectedFeature>()

        // Copia mutable para ir consumiendo los detectados a medida que los asignamos
        val availableDetected = detected.toMutableList()

        // Algoritmo Greedy: Para cada esperado, buscar el más cercano del mismo tipo
        for (exp in expected) {
            val candidates = availableDetected.filter { it.type == exp.type }

            var bestMatch: MatcherDetectedFeature? = null
            var minDist = Double.MAX_VALUE

            for (det in candidates) {
                val dist = hypot(exp.positionX_mm - det.x, exp.positionY_mm - det.y)
                if (dist < minDist) {
                    minDist = dist
                    bestMatch = det
                }
            }

            if (bestMatch != null && minDist <= maxDistanceToleranceMm) {
                matches.add(MatchedFeature(exp, bestMatch, minDist, MatchStatus.MATCH_OK))
                availableDetected.remove(bestMatch)
            } else {
                missing.add(exp)
            }
        }

        // Los que sobran son extras (posibles falsos positivos o agujeros no planeados)
        extras.addAll(availableDetected)

        return MatchingResult(matches, missing, extras)
    }
}
