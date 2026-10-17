package com.example.celestic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.enums.Orientation
import com.example.celestic.opencv.ImageProcessor
import com.example.celestic.opencv.ImageProcessorResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Mat
import javax.inject.Inject

@HiltViewModel
class GoldenSampleViewModel @Inject constructor(
    private val repository: DetectionRepository,
    private val imageProcessor: ImageProcessor,
    private val blueprintMatcher: com.example.celestic.ml.BlueprintMatcher
) : ViewModel() {

    private val _blueprintName = MutableStateFlow("")
    val blueprintName: StateFlow<String> = _blueprintName.asStateFlow()

    private val _hasTwoFaces = MutableStateFlow(true)
    val hasTwoFaces: StateFlow<Boolean> = _hasTwoFaces.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Espesor de referencia por calibre Vernier (ej. 2.0 mm)
    private val _vernierThicknessMm = MutableStateFlow("2.0")
    val vernierThicknessMm: StateFlow<String> = _vernierThicknessMm.asStateFlow()

    private val _obverseFeatures = MutableStateFlow<List<SpecificationFeature>>(emptyList())
    val obverseFeatures: StateFlow<List<SpecificationFeature>> = _obverseFeatures.asStateFlow()

    private val _reverseFeatures = MutableStateFlow<List<SpecificationFeature>>(emptyList())
    val reverseFeatures: StateFlow<List<SpecificationFeature>> = _reverseFeatures.asStateFlow()

    // Resumen dimensional capturado
    private val _obverseDimensionsSummary = MutableStateFlow("Sin captura")
    val obverseDimensionsSummary: StateFlow<String> = _obverseDimensionsSummary.asStateFlow()

    private val _reverseDimensionsSummary = MutableStateFlow("Sin captura")
    val reverseDimensionsSummary: StateFlow<String> = _reverseDimensionsSummary.asStateFlow()

    fun updateName(name: String) {
        _blueprintName.value = name
    }

    fun updateVernierThickness(thickness: String) {
        _vernierThicknessMm.value = thickness
    }

    fun toggleTwoFaces(hasTwo: Boolean) {
        _hasTwoFaces.value = hasTwo
        if (!hasTwo) {
            _reverseFeatures.value = emptyList()
            _reverseDimensionsSummary.value = "Desactivado"
        }
    }

    fun updateFeatureNominal(face: Orientation, index: Int, newNominal: Double, newTolerance: Double) {
        val list = if (face == Orientation.ANVERSO) _obverseFeatures.value.toMutableList() else _reverseFeatures.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(diameter_mm = newNominal, tolerance_mm = newTolerance)
            if (face == Orientation.ANVERSO) _obverseFeatures.value = list else _reverseFeatures.value = list
        }
    }

    fun applyBlueprintNominals(face: Orientation) {
        val bp = blueprintMatcher.getBlueprintById("PIEZA_IRREGULAR_001")
            ?: blueprintMatcher.getAllBlueprints().firstOrNull() ?: return
        val list = (if (face == Orientation.ANVERSO) _obverseFeatures.value else _reverseFeatures.value).toMutableList()

        bp.expectedEdges.forEach { bpEdge ->
            val existingIdx = list.indexOfFirst {
                it.type == DetectionType.EDGE && (it.label == "ARISTA_${bpEdge.id}" || it.label.contains("E${bpEdge.edgeIndex}"))
            }
            if (existingIdx != -1) {
                list[existingIdx] = list[existingIdx].copy(
                    diameter_mm = bpEdge.lengthMm,
                    tolerance_mm = bpEdge.toleranceMm
                )
            } else {
                list.add(
                    SpecificationFeature(
                        specificationId = 0,
                        face = face,
                        type = DetectionType.EDGE,
                        positionX_mm = bpEdge.startX,
                        positionY_mm = bpEdge.startY,
                        diameter_mm = bpEdge.lengthMm,
                        tolerance_mm = bpEdge.toleranceMm,
                        label = "ARISTA_${bpEdge.id}"
                    )
                )
            }
        }
        if (face == Orientation.ANVERSO) _obverseFeatures.value = list else _reverseFeatures.value = list
    }

    fun captureFrame(bitmap: android.graphics.Bitmap, face: Orientation) {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val result = imageProcessor.processImage(mat, null)
        captureFace(result, face)

        mat.release()
    }

    /**
     * Captura integral de todas las dimensiones de la pieza (Anverso o Reverso):
     * - Dimensiones exteriores de la chapa (Ancho x Alto x Perímetro).
     * - Espesor de lámina (calibre Vernier 2.0 mm).
     * - Taladros (diámetros, coordenadas relativas, márgenes a bordes).
     * - Avellanados cónicos (específicos de Anverso).
     * - Distancias entre centros de taladros (Pitch).
     * - Deformaciones perimetrales tolerables.
     */
    fun captureFace(result: ImageProcessorResult, face: Orientation) {
        val features = mutableListOf<SpecificationFeature>()

        val bp = blueprintMatcher.getBlueprintById("PIEZA_IRREGULAR_001")
            ?: blueprintMatcher.getAllBlueprints().firstOrNull()

        // 1. Extraer cada arista y borde de la pieza irregular ajustada al plano técnico (No es una pieza cuadrada)
        val edgeItems = result.detections.filter { it.type == DetectionType.EDGE }
        if (edgeItems.isNotEmpty()) {
            edgeItems.forEach { edge ->
                val edgeIndex = edge.frameId.removePrefix("ARISTA_E").removePrefix("ARISTA_").toIntOrNull() ?: 1
                val bpEdge = bp?.expectedEdges?.firstOrNull { it.edgeIndex == edgeIndex || edge.frameId.contains(it.id) }
                val nominalLength = bpEdge?.lengthMm ?: (edge.measurementMm?.toDouble() ?: 0.0)
                val tolerance = bpEdge?.toleranceMm ?: 0.8

                features.add(
                    SpecificationFeature(
                        specificationId = 0,
                        face = face,
                        type = DetectionType.EDGE,
                        positionX_mm = edge.boundingBox.left.toDouble(),
                        positionY_mm = edge.boundingBox.top.toDouble(),
                        diameter_mm = nominalLength,
                        tolerance_mm = tolerance,
                        label = "ARISTA_E$edgeIndex"
                    )
                )
            }
        } else if (bp != null) {
            bp.expectedEdges.forEach { bpEdge ->
                features.add(
                    SpecificationFeature(
                        specificationId = 0,
                        face = face,
                        type = DetectionType.EDGE,
                        positionX_mm = bpEdge.startX,
                        positionY_mm = bpEdge.startY,
                        diameter_mm = bpEdge.lengthMm,
                        tolerance_mm = bpEdge.toleranceMm,
                        label = "ARISTA_${bpEdge.id}"
                    )
                )
            }
        }

        // Cota general del contorno poligonal (perímetro)
        val polyItem = result.detections.firstOrNull { it.type == DetectionType.DIMENSION && it.frameId == "POLIGONO_PIEZA" }
        val perimMm = polyItem?.measurementMm?.toDouble() ?: 0.0
        if (perimMm > 0) {
            features.add(
                SpecificationFeature(
                    specificationId = 0,
                    face = face,
                    type = DetectionType.DIMENSION,
                    positionX_mm = 0.0,
                    positionY_mm = 0.0,
                    diameter_mm = perimMm,
                    tolerance_mm = 2.0,
                    label = "PERIMETRO_POLIGONO"
                )
            )
        }

        // NOTA: Espesor omitido según especificación técnica del usuario (irrelevante para la pieza).

        // 2. Puntos estructurales: Agujeros y Avellanados
        val holeAndCs = result.detections.filter {
            it.type == DetectionType.HOLE || it.type == DetectionType.COUNTERSINK
        }

        val minX = if (holeAndCs.isNotEmpty()) holeAndCs.minOf { it.boundingBox.left } else 0f
        val minY = if (holeAndCs.isNotEmpty()) holeAndCs.minOf { it.boundingBox.top } else 0f

        holeAndCs.forEachIndexed { idx, item ->
            val isCs = item.type == DetectionType.COUNTERSINK
            features.add(
                SpecificationFeature(
                    specificationId = 0,
                    face = face,
                    type = item.type,
                    positionX_mm = (item.boundingBox.left - minX).toDouble(),
                    positionY_mm = (item.boundingBox.top - minY).toDouble(),
                    diameter_mm = item.measurementMm?.toDouble() ?: if (isCs) 10.0 else 6.0,
                    tolerance_mm = if (isCs) 0.15 else 0.10,
                    requireAlodine = item.notes.contains("Alodine", ignoreCase = true),
                    requireCountersink = isCs,
                    label = if (isCs) "AVELLANADO_CS${idx + 1}" else "TALADRO_H${idx + 1}"
                )
            )
        }

        // 4. Distancias entre centros (Pitch)
        result.detections.filter { it.type == DetectionType.DIMENSION && it.frameId.startsWith("PITCH") }
            .forEach { pitchItem ->
                pitchItem.measurementMm?.toDouble()?.let { pitchMm ->
                    features.add(
                        SpecificationFeature(
                            specificationId = 0,
                            face = face,
                            type = DetectionType.DIMENSION,
                            positionX_mm = 0.0,
                            positionY_mm = 0.0,
                            diameter_mm = pitchMm,
                            tolerance_mm = 0.20,
                            label = pitchItem.frameId
                        )
                    )
                }
            }

        val holeCount = features.count { it.type == DetectionType.HOLE }
        val csCount = features.count { it.type == DetectionType.COUNTERSINK }
        val edgeCount = features.count { it.type == DetectionType.EDGE }
        val summary = "Pieza irregular: $edgeCount aristas/bordes | Perímetro: ${"%.1f".format(perimMm)} mm | $holeCount taladros, $csCount avellanados"

        if (face == Orientation.ANVERSO) {
            _obverseFeatures.value = features
            _obverseDimensionsSummary.value = summary
        } else {
            _reverseFeatures.value = features
            _reverseDimensionsSummary.value = summary
        }
    }

    fun saveBlueprint(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true

            val spec = Specification(
                name = _blueprintName.value.ifBlank { "PIEZA_PATRON_IRREGULAR" },
                sheetType = if (_hasTwoFaces.value) "2_CARAS_ANVERSO_REVERSO" else "1_CARA_ANVERSO",
                minWidthMm = 0.0,
                maxWidthMm = 1000.0,
                minHeightMm = 0.0,
                maxHeightMm = 1000.0,
                expectedHoleCount = _obverseFeatures.value.count { it.type == DetectionType.HOLE } +
                        _reverseFeatures.value.count { it.type == DetectionType.HOLE },
                holeMinDiameterMm = 5.8,
                holeMaxDiameterMm = 6.2,
                holeTolerance = 0.10,
                holeNominalDiameterMm = 6.0,
                expectedCountersinkCount = _obverseFeatures.value.count { it.type == DetectionType.COUNTERSINK } +
                        _reverseFeatures.value.count { it.type == DetectionType.COUNTERSINK },
                countersinkMinDiameterMm = 9.8,
                countersinkMaxDiameterMm = 10.2,
                maxAllowedScratches = 1,
                maxScratchLengthMm = 5.0,
                maxAllowedDeformations = 1,
                requireAlodineHalo = _obverseFeatures.value.any { it.requireAlodine },
                minAlodineUniformity = 0.8,
                nominalThicknessMm = 0.0,
                thicknessToleranceMm = 0.0
            )

            val allFeatures = _obverseFeatures.value + _reverseFeatures.value
            repository.saveSpecificationWithFeatures(spec, allFeatures)

            _isSaving.value = false
            onSuccess()
        }
    }
}
