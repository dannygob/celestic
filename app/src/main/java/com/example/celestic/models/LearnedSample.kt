package com.example.celestic.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Representa una muestra de pieza maestra aprendida en caliente por el operario.
 *
 * Utilizada por el motor de On-Device Continual Learning para adaptar dinámicamente
 * las tolerancias y la clasificación de calidad. Soporta Rollback y Reset.
 */
@Entity(tableName = "learned_samples")
@Parcelize
data class LearnedSample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Marca de tiempo exacta del aprendizaje. */
    val timestamp: Long = System.currentTimeMillis(),

    /** Identificador del frame / imagen asociada a la inspección. */
    val frameId: String,

    /** Número de lote o albarán en el que se validó la pieza. */
    val batchNumber: String,

    /** Cantidad de características detectadas (agujeros, avellanados, etc.). */
    val featureCount: Int,

    /** Diámetro promedio de agujeros en milímetros medido en la muestra. */
    val avgHoleDiameterMm: Float,

    /** Razón de circularidad promedio (1.0 = círculo perfecto). */
    val avgCircularityRatio: Float,

    /** Varianza de textura / rugosidad superficial observada. */
    val textureVariance: Float = 0f,

    /** Proporción de cobertura de halo Alodine observado (0.0 - 1.0). */
    val alodineColorRatio: Float = 1.0f,

    /** Notas opcionales introducidas por el operario. */
    val operatorNotes: String = ""
) : Parcelable
