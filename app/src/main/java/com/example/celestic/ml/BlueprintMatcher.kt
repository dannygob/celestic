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
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * BlueprintMatcher handles reference blueprint matching using OpenCV.
 *
 * Responsibilities:
 * - Load blueprint definitions and template images from assets
 * - Perform template matching to identify which blueprint best matches an input image
 * - Detect sheet orientation (front/back)
 * - Validate detected features against expected blueprint geometry
 */
@Singleton
class BlueprintMatcher @Inject constructor(
    @field:ApplicationContext private val context: Context
) {

    /** Loaded blueprint definitions (metadata + expected geometry). */
    private val blueprints = mutableMapOf<String, Blueprint>()

    /** Loaded template images used for matching each blueprint. */
    private val templates = mutableMapOf<String, Mat>()

    init {
        loadBlueprints()
    }

    /**
     * Loads all blueprint JSON files and their corresponding template images
     * from the assets/blueprints/ directory.
     *
     * Each blueprint consists of:
     * - Metadata (ID, name, expected holes, tolerances, etc.)
     * - A template PNG used for template matching
     */
    private fun loadBlueprints() {
        try {
            val blueprintFiles = context.assets.list("blueprints") ?: emptyArray()

            blueprintFiles.filter { it.endsWith(".json") }.forEach { filename ->
                try {
                    // Load blueprint metadata
                    val json = context.assets.open("blueprints/$filename")
                        .bufferedReader().use { it.readText() }

                    val blueprint = Gson().fromJson(json, Blueprint::class.java)
                    blueprints[blueprint.id] = blueprint

                    // Load associated template image
                    val templateName = filename.replace(".json", "_template.png")

                    try {
                        val templatePath = copyAssetToCache("blueprints/$templateName")
                        val template = Imgcodecs.imread(templatePath)

                        if (!template.empty()) {
                            templates[blueprint.id] = template
                            Log.d(TAG, "Blueprint loaded: ${blueprint.name}")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Template not found for ${blueprint.name}", e)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error loading blueprint: $filename", e)
                }
            }

            Log.d(TAG, "Total blueprints loaded: ${blueprints.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading blueprints", e)
        }
    }

    /**
     * Attempts to find the blueprint that best matches the input image
     * using OpenCV template matching.
     *
     * @return BlueprintMatchResult if a match exceeds the confidence threshold,
     *         otherwise null.
     */
    fun matchBlueprint(image: Mat): BlueprintMatchResult? {
        if (blueprints.isEmpty()) {
            Log.w(TAG, "No blueprints loaded")
            return null
        }

        var bestMatch: BlueprintMatchResult? = null
        var bestScore = 0.0

        blueprints.forEach { (id, blueprint) ->
            val template = templates[id] ?: return@forEach

            val result = Mat()
            try {
                // Perform normalized cross‑correlation template matching
                Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED)

                val minMax = Core.minMaxLoc(result)
                val score = minMax.maxVal

                if (score > bestScore && score > MATCH_THRESHOLD) {
                    bestScore = score
                    bestMatch = BlueprintMatchResult(
                        blueprint = blueprint,
                        matchScore = score,
                        matchLocation = minMax.maxLoc,
                        orientation = detectOrientation(image, template)
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Template matching error for ${blueprint.name}", e)
            } finally {
                result.release()
            }
        }

        return bestMatch
    }

    /**
     * Detects sheet orientation (front/back) by comparing template matching scores
     * between the normal template and a 180° flipped version.
     */
    private fun detectOrientation(image: Mat, template: Mat): Orientation {
        val resultNormal = Mat()
        val templateFlipped = Mat()
        val resultFlipped = Mat()

        try {
            // Normal orientation
            Imgproc.matchTemplate(image, template, resultNormal, Imgproc.TM_CCOEFF_NORMED)
            val scoreNormal = Core.minMaxLoc(resultNormal).maxVal

            // Flipped orientation (180° rotation)
            Core.flip(template, templateFlipped, -1)
            Imgproc.matchTemplate(image, templateFlipped, resultFlipped, Imgproc.TM_CCOEFF_NORMED)
            val scoreFlipped = Core.minMaxLoc(resultFlipped).maxVal

            return if (scoreNormal > scoreFlipped) Orientation.ANVERSO else Orientation.REVERSO

        } catch (e: Exception) {
            Log.e(TAG, "Orientation detection error", e)
            return Orientation.UNKNOWN

        } finally {
            resultNormal.release()
            resultFlipped.release()
            templateFlipped.release()
        }
    }

    /**
     * Validates detected features against the expected blueprint geometry.
     *
     * Checks:
     * - Expected number of holes
     * - Expected hole positions (within tolerance)
     * - Expected countersinks
     */
    fun validateDetections(
        detections: List<Detection>,
        blueprint: Blueprint
    ): ValidationResult {

        val issues = mutableListOf<String>()

        // Validate hole count
        val holeCount = detections.count { it.className == "agujero" }
        if (holeCount != blueprint.expectedHoleCount) {
            issues.add(
                "Incorrect number of holes: expected ${blueprint.expectedHoleCount}, found $holeCount"
            )
        }

        // Validate hole positions
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
                    "Missing hole ${expectedHole.id} at position (${expectedHole.x}, ${expectedHole.y})"
                )
            }
        }

        // Validate countersinks
        val countersinkCount = detections.count { it.className == "avellanado" }
        if (countersinkCount != blueprint.expectedCountersinks.size) {
            issues.add(
                "Incorrect number of countersinks: expected ${blueprint.expectedCountersinks.size}, found $countersinkCount"
            )
        }

        return ValidationResult(
            passed = issues.isEmpty(),
            issues = issues
        )
    }

    /**
     * Computes Euclidean distance between two points.
     */
    private fun calculateDistance(p1: Point, p2: Point): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Copies an asset file to the app cache directory and returns its absolute path.
     */
    private fun copyAssetToCache(assetPath: String): String {
        val cacheFile = File(context.cacheDir, assetPath)
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
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? La validación de chapas realiza un cotejo directo en caliente contra el plano
     *   identificado por el analizador de OpenCV en una única pasada, sin necesidad de consultar especificaciones
     *   por ID individualmente desde otros módulos.
     * - ¿Para qué se deja? Habilitará la vista de catálogo y visualización estática de un plano patrón
     *   específico seleccionado por el usuario en futuras pantallas del sistema.
     *
     * Returns a blueprint by its ID.
     */
    fun getBlueprintById(id: String): Blueprint? = blueprints[id]

    /**
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? El matcher carga todos los planos en memoria y los coteja contra la chapa actual,
     *   pero no se expone el listado completo de planos a la interfaz en la fase de MVP.
     * - ¿Para qué se deja? Para permitir al operario seleccionar manualmente un plano patrón de una lista
     *   completa en caso de que falle la detección automática de plantilla en futuras versiones.
     *
     * Returns all loaded blueprints.
     */
    fun getAllBlueprints(): List<Blueprint> = blueprints.values.toList()

    /**
     * Releases OpenCV Mat resources and clears loaded data.
     */
    fun release() {
        templates.values.forEach { it.release() }
        templates.clear()
        blueprints.clear()
        Log.d(TAG, "Blueprint matcher resources released")
    }

    companion object {
        private const val TAG = "BlueprintMatcher"
        private const val MATCH_THRESHOLD = 0.7 // Confidence threshold for template matching
    }
}

/** Result of a blueprint matching operation. */
data class BlueprintMatchResult(
    val blueprint: Blueprint,
    val matchScore: Double,
    val matchLocation: Point,
    val orientation: Orientation
)

/** Sheet orientation result. */
enum class Orientation {
    ANVERSO,
    REVERSO,
    UNKNOWN
}

/** Result of blueprint validation. */
data class ValidationResult(
    val passed: Boolean,
    val issues: List<String>
)
