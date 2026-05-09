package com.example.celestic.models

import org.opencv.core.Mat

/**
 * Represents a detected fiducial marker (ArUco, ChArUco, or AprilTag).
 *
 * Contains:
 * - The marker ID
 * - The detected corner coordinates stored in an OpenCV Mat
 */
data class FiducialMarker(
    val id: Int,

    /** Matrix containing the detected corner points of the marker. */
    val corners: Mat
)
