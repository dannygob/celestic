package com.example.celestic.opencv

import com.example.celestic.models.DetectionItem
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Core
import org.opencv.core.Rect
import org.opencv.core.CvType
import org.opencv.imgproc.Imgproc
// No need for java.util.ArrayList explicitly if using mutableListOf and List

// Data class to hold results from AlodineDetector
data class AlodineDetectionResult(
    val items: List<DetectionItem>,
    val processedFrame: Mat // Frame with alodine detections (and possibly prior hole detections) drawn
)

object AlodineDetector {

    /**
     * Detects alodine rings around previously detected holes.
     *
     * @param frame The input frame (Mat). This frame might already have hole detections drawn on it.
     *              The AlodineDetector will draw its own detections on this frame.
     * @param detectedHoles A list of DetectionItem objects representing previously found holes.
     * @return AlodineDetectionResult containing a list of alodine ring DetectionItems and the processed frame.
     */
    fun detect(frame: Mat, detectedHoles: List<DetectionItem>): AlodineDetectionResult {
        val alodineItems = mutableListOf<DetectionItem>()
        val workingFrame = frame.clone() // Work on a clone to draw detections

        // --- HSV Color Range for Light Grey Alodine ---
        // These values define the target color. Significant tuning is usually required.
        // Test with various lighting conditions and actual alodine samples.
        // Hue (0-180 for OpenCV): Greys have no specific hue, so the full range is often used.
        // Saturation (0-255 for OpenCV): Greys have very low saturation. Pure grey is S=0.
        // Value (0-255 for OpenCV): Defines brightness. Light greys will have high values.
        val lowerLightGrey = Scalar(
            0.0,   // Hue_MIN
            0.0,   // Saturation_MIN (very low for grey)
            150.0  // Value_MIN (brightness - fairly light)
        )
        val upperLightGrey = Scalar(
            180.0, // Hue_MAX (full range)
            45.0,  // Saturation_MAX (allowing for slight color tint and sensor variations)
            230.0  // Value_MAX (brightness - very light, but below pure white)
        )

        // --- Alodine Ring Geometry and Detection Thresholds ---
        // Defines the expected shape and appearance of the alodine ring relative to the detected hole.

        // ringMinRadiusFactor: Multiplier for hole radius to find the inner edge of the alodine ring.
        //   e.g., 1.0 means ring starts exactly at hole's edge, >1.0 means there's a gap.
        val ringMinRadiusFactor = 1.05

        // ringMaxRadiusFactor: Multiplier for hole radius to find the outer edge of the alodine ring.
        //   (ringMaxRadiusFactor - ringMinRadiusFactor) * holeRadius gives expected ring thickness.
        val ringMaxRadiusFactor = 1.4

        // ringAreaThresholdPercentage: Minimum percentage of pixels within the expected annular region
        //   that must match the 'light grey' color criteria to confirm an alodine ring.
        //   Value between 0.0 and 1.0. Higher values make detection stricter.
        val ringAreaThresholdPercentage = 0.3

        val hsvMat = Mat()
        Imgproc.cvtColor(workingFrame, hsvMat, Imgproc.COLOR_BGR2HSV) // Assuming workingFrame is BGR

        for (hole in detectedHoles) {
            if (hole.position == null || hole.diameter == null || hole.diameter <= 0) continue

            val holeRadius = hole.diameter / 2.0
            val holeCenterX = hole.position.x
            val holeCenterY = hole.position.y

            // Define ROI bounding box
            val roiOuterRadius = holeRadius * (ringMaxRadiusFactor + 0.2) // Add padding for ROI
            val roiX = Math.max(0.0, holeCenterX - roiOuterRadius).toInt()
            val roiY = Math.max(0.0, holeCenterY - roiOuterRadius).toInt()
            val roiWidth = Math.min(workingFrame.cols().toDouble() - roiX, roiOuterRadius * 2).toInt()
            val roiHeight = Math.min(workingFrame.rows().toDouble() - roiY, roiOuterRadius * 2).toInt()

            if (roiWidth <= 0 || roiHeight <= 0) continue

            val roi = Mat(hsvMat, Rect(roiX, roiY, roiWidth, roiHeight))

            // Color segmentation for light grey in ROI
            val colorMask = Mat()
            Core.inRange(roi, lowerLightGrey, upperLightGrey, colorMask)

            // Create an annular mask for the expected ring region within the ROI
            val annularMask = Mat.zeros(roi.size(), CvType.CV_8UC1)
            // Adjust hole center to ROI coordinates. Note: roiOuterRadius is half of roiWidth/Height if not clipped.
            // A more robust way for roiHoleCenterX/Y:
            val roiHoleCenterX = holeCenterX - roiX
            val roiHoleCenterY = holeCenterY - roiY


            val innerRingRadius = (holeRadius * ringMinRadiusFactor).toInt()
            val outerRingRadius = (holeRadius * ringMaxRadiusFactor).toInt()

            // Draw the outer circle of the annulus (filled)
            Imgproc.circle(annularMask, Point(roiHoleCenterX, roiHoleCenterY), outerRingRadius, Scalar(255.0), -1)
            // Carve out the inner circle (filled with black)
            Imgproc.circle(annularMask, Point(roiHoleCenterX, roiHoleCenterY), innerRingRadius, Scalar(0.0), -1)

            // Calculate total pixels in the annular region
            val totalAnnulusPixels = Core.countNonZero(annularMask)
            if (totalAnnulusPixels == 0) { // Avoid division by zero if radii are too small or equal
                roi.release()
                colorMask.release()
                annularMask.release()
                continue
            }

            // Combine color mask with annular mask
            val ringHighlight = Mat()
            Core.bitwise_and(colorMask, annularMask, ringHighlight)

            val detectedRingPixels = Core.countNonZero(ringHighlight)
            val ringCoverage = if (totalAnnulusPixels > 0) detectedRingPixels.toDouble() / totalAnnulusPixels else 0.0

            if (ringCoverage >= ringAreaThresholdPercentage) {
                // Alodine ring detected
                val alodineItem = DetectionItem(
                    type = "alodine_ring",
                    position = hole.position, // Positioned relative to the hole
                    diameter = (outerRingRadius * 2), // Diameter of the alodine ring's outer edge
                    x = hole.position.x, // Keep x, y for parcelization
                    y = hole.position.y
                )
                alodineItems.add(alodineItem)

                // Draw the detected alodine ring (e.g., a blue circle) on the workingFrame (original coordinates)
                Imgproc.circle(workingFrame, hole.position, outerRingRadius, Scalar(255.0, 0.0, 0.0), 2) // Blue
            }

            // Release intermediate Mats for this iteration
            roi.release()
            colorMask.release()
            annularMask.release()
            ringHighlight.release()
        }

        hsvMat.release()
        return AlodineDetectionResult(items = alodineItems, processedFrame = workingFrame)
    }
}
