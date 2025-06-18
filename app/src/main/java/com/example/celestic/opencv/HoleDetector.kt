package com.example.celestic.opencv

import com.example.celestic.models.DetectionItem
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size // Added import for Size
import org.opencv.imgproc.Imgproc

// Data class for detection results
data class HoleDetectionResult(
    val items: List<DetectionItem>,
    val processedFrame: Mat // Frame with detections drawn
)

object HoleDetector {

    fun detectHoles(frame: Mat): HoleDetectionResult {
        val gray = Mat()
        // Assuming 'frame' is BGR. If from CameraX ImageAnalysis as YUV, it needs prior conversion.
        // For direct CameraX YUV_420_888 to Mat, a more complex conversion is needed.
        // Here, we assume 'frame' is already in a processable format like BGR.
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY) // Convert frame to grayscale

        // --- Gaussian Blur ---
        // Reduces noise to avoid false circle detection from minor details or texture.
        // Size(kernelWidth, kernelHeight): Kernel dimensions (must be positive and odd).
        //   Larger kernel = more blur. Common values: Size(3.0, 3.0), Size(5.0, 5.0), Size(9.0, 9.0).
        // sigmaX: Gaussian kernel standard deviation in X direction.
        // sigmaY: Gaussian kernel standard deviation in Y direction.
        //   Larger sigma = more blur. If sigmaY is zero, it is set to be the same as sigmaX.
        //   If both sigmas are zero, they are computed from kernelSize.
        // Using a 9x9 kernel and sigmaX=2, sigmaY=2. These values can be tuned.
        Imgproc.GaussianBlur(gray, gray, Size(9.0, 9.0), 2.0, 2.0)

        val circles = Mat()
        val detectedHoles = mutableListOf<DetectionItem>()

        // --- Hough Circle Transform ---
        // Detects circles in a grayscale image.
        Imgproc.HoughCircles(
            gray,                       // Input grayscale image (output from GaussianBlur).
            circles,                    // Output vector of found circles. Each circle is represented by 3 values: x, y, radius.
            Imgproc.HOUGH_GRADIENT,     // Detection method. CV_HOUGH_GRADIENT is currently the only implemented method.

            // dp: Inverse ratio of accumulator resolution to image resolution.
            //   Example: if dp=1, accumulator has the same resolution as input image.
            //   If dp=2, accumulator has half the width and height.
            //   Value of 1.0 is a common starting point.
            1.0,

            // minDist: Minimum distance between the centers of the detected circles.
            //   If the parameter is too small, multiple neighbor circles may be falsely detected in addition to a true one.
            //   If it is too large, some circles may be missed.
            //   Consider making this adaptive (e.g., based on image size or expected hole size) or a fixed pixel value.
            //   `gray.rows() / 8.0` makes it somewhat adaptive to image height.
            gray.rows() / 8.0,

            // param1: Higher threshold for the internal Canny edge detector (first stage of HOUGH_GRADIENT).
            //   Range: 0-255 typically. Higher values reduce noise but might miss faint edges of circles.
            //   A common starting range is 50-150.
            100.0,

            // param2: Accumulator threshold for the circle centers at the detection stage.
            //   The smaller it is, the more false circles may be detected.
            //   Circles, corresponding to the larger accumulator values, will be returned first.
            //   This is a critical parameter to tune for sensitivity vs. false positives. (Often 20-100)
            30.0,

            // minRadius: Minimum circle radius in pixels.
            //   Helps filter out very small noise that might be detected as circles.
            10,

            // maxRadius: Maximum circle radius in pixels.
            //   Helps filter out very large features or ignore areas outside expected hole size.
            //   Set to 0 if you do not want to limit the maximum size.
            50
        )

        if (circles.cols() > 0) {
            for (i in 0 until circles.cols()) {
                val data = circles.get(0, i)
                val center = Point(data[0], data[1])
                val radius = data[2].toInt()
                val diameter = radius * 2

                // Dibujar el agujero detectado
                Imgproc.circle(frame, center, radius, Scalar(0.0, 255.0, 0.0), 2)
                Imgproc.circle(frame, center, 3, Scalar(0.0, 0.0, 255.0), 2)

                // Agregar el agujero a la lista de detecciones
                detectedHoles.add(DetectionItem(type = "agujero", position = center, diameter = diameter))
            }
        }
        return HoleDetectionResult(detectedHoles, frame) // Return DetectionResult
    }
}
