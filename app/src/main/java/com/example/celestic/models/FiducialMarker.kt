package com.example.celestic.models

import org.opencv.core.Mat

/**
 * Representa un marcador fiducial (ArUco o AprilTag) detectado.
 */
data class FiducialMarker(
    val id: Int,
    val corners: Mat // Mat de corners detectados
)
