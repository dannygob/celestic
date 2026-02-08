package com.example.celestic.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.enums.Orientation

@Entity(
    tableName = "specification_features",
    foreignKeys = [
        ForeignKey(
            entity = Specification::class,
            parentColumns = ["id"],
            childColumns = ["specificationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["specificationId"])  // ← ÍNDICE AÑADIDO
    ]
)
data class SpecificationFeature(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val specificationId: Long,
    val face: Orientation,        // ANVERSO o REVERSO
    val type: DetectionType,      // HOLE, COUNTERSINK, etc.

    // Coordenadas en milímetros relativas al origen (Marcador o Borde)
    val positionX_mm: Double,
    val positionY_mm: Double,

    // Dimensiones y Tolerancias
    val diameter_mm: Double,
    val tolerance_mm: Double = 0.5,

    // Requisitos de Cualidad
    val requireAlodine: Boolean = false,
    val requireCountersink: Boolean = false
)