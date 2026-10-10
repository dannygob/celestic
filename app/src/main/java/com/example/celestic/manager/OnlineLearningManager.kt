package com.example.celestic.manager

import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.LearnedSample
import com.example.celestic.models.enums.DetectionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Gestor de Reaprendizaje y Aprendizaje en Caliente en el dispositivo (On-Device Continual Learning).
 *
 * Permite al operario validar piezas en vivo como "muestras de referencia" (Golden Exemplars)
 * cuando la pieza está en perfecto estado, enriqueciendo dinámicamente el banco de tolerancia.
 *
 * Características clave:
 * 1. Aprendizaje incremental: Ingesta de métricas reales de piezas aprobadas.
 * 2. ROLLBACK: Si el operario aprobó por error o descuido una pieza defectuosa, puede
 *    revertir la última muestra agregada para no contaminar el modelo.
 * 3. RESET: Restablece todo el aprendizaje en caliente a cero en cualquier momento.
 * 4. Eliminación granular: Permite inspeccionar y borrar piezas individuales aprendidas.
 */
@Singleton
class OnlineLearningManager @Inject constructor(
    private val repository: DetectionRepository
) {

    data class AdaptiveToleranceProfile(
        val sampleCount: Int,
        val targetFeatureCount: Int,
        val meanHoleDiameterMm: Float,
        val holeDiameterStdDev: Float,
        val meanCircularityRatio: Float,
        val circularityStdDev: Float
    )

    fun getLearnedSamplesFlow(): Flow<List<LearnedSample>> =
        repository.getAllLearnedSamples()

    fun getSampleCountFlow(): Flow<Int> =
        repository.getLearnedSampleCount()

    /**
     * Aprende de una pieza aprobada en caliente.
     * Extrae características geométricas y de calidad y las persiste en la base de datos.
     */
    suspend fun learnApprovedPart(
        frameId: String,
        batchNumber: String,
        detections: List<DetectionItem>,
        notes: String = ""
    ): Long {
        val holes = detections.filter { it.type == DetectionType.HOLE }
        val avgDiam = if (holes.isNotEmpty()) {
            holes.mapNotNull { it.measurementMm }.average().toFloat()
        } else 0f

        val avgCircularity = if (holes.isNotEmpty()) {
            holes.map {
                val w = it.boundingBox.width()
                val h = it.boundingBox.height()
                if (h > 0) (w / h).coerceIn(0.5f, 2.0f) else 1.0f
            }.average().toFloat()
        } else 1.0f

        val sample = LearnedSample(
            frameId = frameId,
            batchNumber = batchNumber.ifBlank { "GENERAL" },
            featureCount = detections.size,
            avgHoleDiameterMm = avgDiam,
            avgCircularityRatio = avgCircularity,
            textureVariance = 10.0f,
            alodineColorRatio = 1.0f,
            operatorNotes = notes
        )

        return repository.insertLearnedSample(sample)
    }

    /**
     * ROLLBACK: Deshace y elimina la última muestra aprendida.
     * Retorna la muestra eliminada o null si no había ninguna.
     */
    suspend fun rollbackLastSample(): LearnedSample? {
        val latest = repository.getLatestLearnedSample() ?: return null
        repository.deleteLearnedSampleById(latest.id)
        return latest
    }

    /**
     * Elimina una muestra aprendida específica por su ID.
     */
    suspend fun deleteSampleById(id: Long) {
        repository.deleteLearnedSampleById(id)
    }

    /**
     * RESET: Reinicia completamente todo el banco de muestras aprendidas en caliente.
     */
    suspend fun resetAllLearning() {
        repository.deleteAllLearnedSamples()
    }

    /**
     * Calcula el perfil estadístico adaptativo en base a las muestras aprendidas aprobadas.
     */
    suspend fun getAdaptiveProfile(): AdaptiveToleranceProfile? {
        val samples = repository.getAllLearnedSamples().firstOrNull() ?: emptyList()
        if (samples.isEmpty()) return null

        val count = samples.size
        val avgCount = samples.map { it.featureCount }.average().toInt()
        val meanDiam = samples.map { it.avgHoleDiameterMm }.average().toFloat()
        val diamVariance = samples.map {
            val diff = it.avgHoleDiameterMm - meanDiam
            diff * diff
        }.average()
        val diamStdDev = sqrt(diamVariance).toFloat()

        val meanCirc = samples.map { it.avgCircularityRatio }.average().toFloat()
        val circVariance = samples.map {
            val diff = it.avgCircularityRatio - meanCirc
            diff * diff
        }.average()
        val circStdDev = sqrt(circVariance).toFloat()

        return AdaptiveToleranceProfile(
            sampleCount = count,
            targetFeatureCount = avgCount,
            meanHoleDiameterMm = meanDiam,
            holeDiameterStdDev = diamStdDev.coerceAtLeast(0.12f), // tolerancia mínima de seguridad
            meanCircularityRatio = meanCirc,
            circularityStdDev = circStdDev.coerceAtLeast(0.04f)
        )
    }

    /**
     * Evalúa si una nueva pieza cumple con el perfil aprendido en caliente.
     * Retorna Pair(scoreConformidad 0.0..1.0, estaDentroDeTolerancia).
     */
    suspend fun evaluateAgainstLearnedProfile(detections: List<DetectionItem>): Pair<Float, Boolean> {
        val profile = getAdaptiveProfile() ?: return Pair(1.0f, true)

        val holes = detections.filter { it.type == DetectionType.HOLE }
        val currentDiam = if (holes.isNotEmpty()) {
            holes.mapNotNull { it.measurementMm }.average().toFloat()
        } else profile.meanHoleDiameterMm

        val diamDiff = abs(currentDiam - profile.meanHoleDiameterMm)
        val diamZScore = if (profile.holeDiameterStdDev > 0) diamDiff / profile.holeDiameterStdDev else 0f

        // Criterio 3-sigma de tolerancia industrial
        val isCompliant = diamZScore <= 3.0f && abs(detections.size - profile.targetFeatureCount) <= 2
        val score = (1.0f - (diamZScore / 4.0f)).coerceIn(0f, 1f)

        return Pair(score, isCompliant)
    }
}
