package com.example.celestic.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "specifications")
data class Specification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sheetType: String,

    // Dimensiones
    val minWidthMm: Double,
    val maxWidthMm: Double,
    val minHeightMm: Double,
    val maxHeightMm: Double,

    // Perforaciones
    val expectedHoleCount: Int,
    val holeMinDiameterMm: Double,
    val holeMaxDiameterMm: Double,
    val holeTolerance: Double,
    val holeNominalDiameterMm: Double,

    // Avellanados
    val expectedCountersinkCount: Int,
    val countersinkMinDiameterMm: Double,
    val countersinkMaxDiameterMm: Double,

    // Defectos
    val maxAllowedScratches: Int,
    val maxScratchLengthMm: Double,
    val maxAllowedDeformations: Int,

    // Alodine
    val requireAlodineHalo: Boolean,
    val minAlodineUniformity: Double
)
