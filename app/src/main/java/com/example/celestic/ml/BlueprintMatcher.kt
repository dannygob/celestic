package com.example.celestic.ml

import android.content.Context
import android.util.Log
import com.example.celestic.models.Blueprint
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Matcher de planos de referencia
 * - Identifica el tipo de lámina usando template matching
 * - Detecta orientación (anverso/reverso)
 * - Valida posiciones y cantidades de características
 */
@Singleton
class BlueprintMatcher @Inject constructor(
    @field:ApplicationContext private val context: Context
) {
    private val blueprints = mutableMapOf<String, Blueprint>()
    private val templates = mutableMapOf<String, Mat>()

    init {
        loadBlueprints()
    }

    /**
     * Carga todos los planos desde assets/blueprints/
     */
    private fun loadBlueprints() {
        try {
            // Cargar todos los archivos JSON de planos
            val blueprintFiles = context.assets.list("blueprints") ?: emptyArray()

            blueprintFiles.filter { it.endsWith(".json") }.forEach { filename ->
                try {
                    val json = context.assets.open("blueprints/$filename")
                        .bufferedReader().use { it.readText() }

                    val blueprint = Gson().fromJson(json, Blueprint::class.java)
                    blueprints[blueprint.id] = blueprint

                    // Cargar template correspondiente si existe
                    val templateName = filename.replace(".json", "_template.png")
                    try {
                        val templatePath = copyAssetToCache("blueprints/$templateName")
                        val template = Imgcodecs.imread(templatePath)
                        if (!template.empty()) {
                            templates[blueprint.id] = template
                            Log.d(TAG, "Plano cargado: ${blueprint.name}")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Template no encontrado para ${blueprint.name}", e)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar plano: $filename", e)
                }
            }

            Log.d(TAG, "Total de planos cargados: ${blueprints.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar planos", e)
        }
    }

    /**
     * Encuentra el plano que mejor coincide con la imagen
     */
    fun matchBlueprint(image: Mat): BlueprintMatchResult? {
        if (blueprints.isEmpty()) {
            Log.w(TAG, "No hay planos cargados")
            return null
        }

        var bestMatch: BlueprintMatchResult? = null
        var bestScore = 0.0

        blueprints.forEach { (id, blueprint) ->
            val template = templates[id]
            if (template == null || template.empty()) {
                return@forEach
            }

            try {
                // Template matching
                val result = Mat()
                Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED)

                val minMaxResult = Core.minMaxLoc(result)
                val score = minMaxResult.maxVal

                if (score > bestScore && score > MATCH_THRESHOLD) {
                    bestScore = score
                    bestMatch = BlueprintMatchResult(
                        blueprint = blueprint,
                        matchScore = score,
                        matchLocation = minMaxResult.maxLoc,
                        orientation = detectOrientation(image, template)
                    )
                }

                result.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error en template matching para ${blueprint.name}", e)
            }
        }

        return bestMatch
    }

    /**
     * Detecta la orientación de la lámina (anverso/reverso)
     */
    private fun detectOrientation(image: Mat, template: Mat): Orientation {
        try {
            // Intentar match normal
            val resultNormal = Mat()
            Imgproc.matchTemplate(image, template, resultNormal, Imgproc.TM_CCOEFF_NORMED)
            val scoreNormal = Core.minMaxLoc(resultNormal).maxVal

            // Intentar match rotado 180°
            val templateFlipped = Mat()
            Core.flip(template, templateFlipped, -1) // Flip horizontal y vertical

            val resultFlipped = Mat()
            Imgproc.matchTemplate(image, templateFlipped, resultFlipped, Imgproc.TM_CCOEFF_NORMED)
            val scoreFlipped = Core.minMaxLoc(resultFlipped).maxVal

            resultNormal.release()
            resultFlipped.release()
            templateFlipped.release()

            return if (scoreNormal > scoreFlipped) {
                Orientation.ANVERSO
            } else {
                Orientation.REVERSO
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al detectar orientación", e)
            return Orientation.UNKNOWN
        }
    }

    /**
     * Valida las detecciones contra el plano de referencia
     */
    fun validateDetections(
        detections: List<Detection>,
        blueprint: Blueprint
    ): ValidationResult {
        val issues = mutableListOf<String>()

        // Validar cantidad de agujeros
        val holeCount = detections.count { it.className == "agujero" }
        if (holeCount != blueprint.expectedHoleCount) {
            issues.add(
                "Cantidad de agujeros incorrecta: " +
                        "esperado ${blueprint.expectedHoleCount}, encontrado $holeCount"
            )
        }

        // Validar posiciones de agujeros esperados
        blueprint.expectedHoles.forEach { expectedHole ->
            val found = detections.any { detection ->
                if (detection.className != "agujero") return@any false

                val centerX = detection.boundingBox.x + detection.boundingBox.width / 2.0
                val centerY = detection.boundingBox.y + detection.boundingBox.height / 2.0

                val distance = calculateDistance(
                    Point(centerX, centerY),
                    Point(expectedHole.x, expectedHole.y)
                )

                distance < blueprint.positionTolerance
            }

            if (!found) {
                issues.add(
                    "Agujero ${expectedHole.id} faltante en posición " +
                            "(${expectedHole.x}, ${expectedHole.y})"
                )
            }
        }

        // Validar avellanados si existen
        val countersinkCount = detections.count { it.className == "avellanado" }
        if (countersinkCount != blueprint.expectedCountersinks.size) {
            issues.add(
                "Cantidad de avellanados incorrecta: " +
                        "esperado ${blueprint.expectedCountersinks.size}, encontrado $countersinkCount"
            )
        }

        return ValidationResult(
            passed = issues.isEmpty(),
            issues = issues
        )
    }

    /**
     * Calcula la distancia euclidiana entre dos puntos
     */
    private fun calculateDistance(p1: Point, p2: Point): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Copia un archivo de assets a cache
     */
    private fun copyAssetToCache(assetPath: String): String {
        val cacheFile = java.io.File(context.cacheDir, assetPath)
        cacheFile.parentFile?.mkdirs()

        if (!cacheFile.exists()) {
            context.assets.open(assetPath).use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return cacheFile.absolutePath
    }

    /**
     * Obtiene un plano por su ID
     */
    fun getBlueprintById(id: String): Blueprint? = blueprints[id]

    /**
     * Obtiene todos los planos cargados
     */
    fun getAllBlueprints(): List<Blueprint> = blueprints.values.toList()

    /**
     * Libera recursos
     */
    fun release() {
        templates.values.forEach { it.release() }
        templates.clear()
        blueprints.clear()
        Log.d(TAG, "Recursos del matcher liberados")
    }

    companion object {
        private const val TAG = "BlueprintMatcher"
        private const val MATCH_THRESHOLD = 0.7 // Umbral de confianza para template matching
    }
}

/**
 * Resultado de matching con plano
 */
data class BlueprintMatchResult(
    val blueprint: Blueprint,
    val matchScore: Double,
    val matchLocation: Point,
    val orientation: Orientation
)

/**
 * Orientación de la lámina
 */
enum class Orientation {
    ANVERSO,
    REVERSO,
    UNKNOWN
}

/**
 * Resultado de validación
 */
data class ValidationResult(
    val passed: Boolean,
    val issues: List<String>
)
