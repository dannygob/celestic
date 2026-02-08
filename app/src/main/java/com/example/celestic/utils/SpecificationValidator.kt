package com.example.celestic.utils

import com.example.celestic.models.Specification
import com.example.celestic.models.enums.DetectionStatus
import javax.inject.Inject
import kotlin.math.abs

class SpecificationValidator @Inject constructor() {

    fun validateHole(diameterMm: Double, spec: Specification): DetectionStatus {
        return when {
            diameterMm < spec.holeMinDiameterMm -> DetectionStatus.NOT_ACCEPTED
            diameterMm > spec.holeMaxDiameterMm -> DetectionStatus.NOT_ACCEPTED
            abs(diameterMm - spec.holeNominalDiameterMm) > spec.holeTolerance -> DetectionStatus.WARNING
            else -> DetectionStatus.OK
        }
    }

    fun validateCountersink(diameterMm: Double, spec: Specification): DetectionStatus {
        return when {
            diameterMm < spec.countersinkMinDiameterMm -> DetectionStatus.NOT_ACCEPTED
            diameterMm > spec.countersinkMaxDiameterMm -> DetectionStatus.NOT_ACCEPTED
            else -> DetectionStatus.OK
        }
    }

    fun validateScratch(lengthMm: Double, spec: Specification): DetectionStatus {
        return if (lengthMm > spec.maxScratchLengthMm) DetectionStatus.NOT_ACCEPTED else DetectionStatus.OK
    }

    fun validateDeformation(count: Int, spec: Specification): DetectionStatus {
        return if (count > spec.maxAllowedDeformations) DetectionStatus.NOT_ACCEPTED else DetectionStatus.OK
    }

    fun validateAlodine(uniformity: Double, spec: Specification): DetectionStatus {
        if (!spec.requireAlodineHalo) return DetectionStatus.OK
        return if (uniformity < spec.minAlodineUniformity) DetectionStatus.NOT_ACCEPTED else DetectionStatus.OK
    }

    fun validateSheetDimensions(
        width: Double,
        height: Double,
        spec: Specification
    ): DetectionStatus {
        if (width < spec.minWidthMm || width > spec.maxWidthMm) return DetectionStatus.NOT_ACCEPTED
        if (height < spec.minHeightMm || height > spec.maxHeightMm) return DetectionStatus.NOT_ACCEPTED
        return DetectionStatus.OK
    }

    // Granular Validation (Digital Twin)
    fun validateFeatureMatch(
        expected: com.example.celestic.models.SpecificationFeature,
        detected: com.example.celestic.validation.MatcherDetectedFeature
    ): DetectionStatus {
        // 1. Validate Dimension (Diameter)
        val diameterDiff = abs(detected.diameter - expected.diameter_mm)
        if (diameterDiff > expected.tolerance_mm) return DetectionStatus.NOT_ACCEPTED

        // 2. Validate Alodine
        if (expected.requireAlodine && !detected.hasAlodine) return DetectionStatus.NOT_ACCEPTED

        return DetectionStatus.OK
    }
}
