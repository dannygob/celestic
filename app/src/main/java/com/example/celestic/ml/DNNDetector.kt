package com.example.celestic.ml

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfRect2d
import org.opencv.core.Rect
import org.opencv.core.Rect2d
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DNNDetector performs object detection using OpenCV DNN with a YOLOv8‑nano model.
 *
 * Responsibilities:
 * - Load YOLOv8 ONNX model from assets
 * - Preprocess input images into DNN blobs
 * - Run forward inference using OpenCV DNN
 * - Apply post‑processing (confidence filtering + Non‑Maximum Suppression)
 * - Return structured detection results including ROI extraction
 *
 * The model detects:
 * - Sheet
 * - Hole
 * - Countersink
 * - Scratch
 * - Deformation
 * - Alodine halo
 */
@Singleton
class DNNDetector @Inject constructor(
    @field:ApplicationContext private val context: Context
) {

    private var net: Net? = null

    // YOLOv8 input resolution
    private val inputSize = Size(640.0, 640.0)

    // Confidence and NMS thresholds
    private val confThreshold = 0.5f
    private val nmsThreshold = 0.4f

    /**
     * Enumeration of all detection classes supported by the YOLO model.
     */
    enum class DetectionClass(val id: Int, val label: String) {
        SHEET(0, "sheet"),
        HOLE(1, "hole"),
        COUNTERSINK(2, "countersink"),
        SCRATCH(3, "scratch"),
        DEFORMATION(4, "deformation"),
        ALODINE_HALO(5, "alodine_halo")
    }

    /**
     * Loads the YOLOv8 ONNX model from assets and initializes the OpenCV DNN network.
     */
    private fun loadModel() {
        try {
            val modelPath = copyAssetToCache("models/yolov8n.onnx")
            net = Dnn.readNetFromONNX(modelPath)

            net?.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV)
            net?.setPreferableTarget(Dnn.DNN_TARGET_CPU)

            Log.d(TAG, "DNN model loaded successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load DNN model", e)
        }
    }

    /**
     * Runs object detection on the input image using YOLOv8.
     */
    fun detect(image: Mat): List<Detection> {
        if (net == null) {
            Log.w(TAG, "Model not loaded, attempting to load...")
            loadModel()
            if (net == null) {
                Log.e(TAG, "Model could not be loaded")
                return emptyList()
            }
        }

        val detections = mutableListOf<Detection>()

        try {
            // 1. Preprocess image into blob
            val blob = Dnn.blobFromImage(
                image,
                1.0 / 255.0,
                inputSize,
                Scalar(0.0, 0.0, 0.0),
                true,
                false
            )

            // 2. Run inference
            net?.setInput(blob)
            val outputs = net?.forward() ?: run {
                blob.release()
                return emptyList()
            }

            // 3. Parse YOLO output
            val boxes = mutableListOf<Rect>()
            val confidences = mutableListOf<Float>()
            val classIds = mutableListOf<Int>()

            for (i in 0 until outputs.rows()) {
                val row = outputs.row(i)
                val scores = row.colRange(5, outputs.cols())
                val minMax = Core.minMaxLoc(scores)
                val confidence = minMax.maxVal.toFloat()

                if (confidence > confThreshold) {
                    val classId = minMax.maxLoc.x.toInt()

                    val centerX = (row.get(0, 0)[0] * image.cols()).toInt()
                    val centerY = (row.get(0, 1)[0] * image.rows()).toInt()
                    val width = (row.get(0, 2)[0] * image.cols()).toInt()
                    val height = (row.get(0, 3)[0] * image.rows()).toInt()

                    boxes.add(Rect(centerX - width / 2, centerY - height / 2, width, height))
                    confidences.add(confidence)
                    classIds.add(classId)
                }

                row.release()
                scores.release()
            }

            // 4. Apply Non‑Maximum Suppression
            if (boxes.isNotEmpty()) {
                val indices = MatOfInt()
                val boxes2d = boxes.map {
                    Rect2d(
                        it.x.toDouble(),
                        it.y.toDouble(),
                        it.width.toDouble(),
                        it.height.toDouble()
                    )
                }
                val boxesMat = MatOfRect2d(*boxes2d.toTypedArray())
                val confidencesMat = MatOfFloat(*confidences.toFloatArray())

                Dnn.NMSBoxes(boxesMat, confidencesMat, confThreshold, nmsThreshold, indices)

                // 5. Build Detection objects
                indices.toArray().forEach { idx ->
                    val className = DetectionClass.entries.find { it.id == classIds[idx] }?.label ?: "unknown"

                    detections.add(
                        Detection(
                            classId = classIds[idx],
                            className = className,
                            confidence = confidences[idx],
                            boundingBox = boxes[idx],
                            roi = extractROI(image, boxes[idx])
                        )
                    )
                }

                indices.release()
                boxesMat.release()
                confidencesMat.release()
            }

            blob.release()
            outputs.release()

        } catch (e: Exception) {
            Log.e(TAG, "DNN detection error", e)
        }

        return detections
    }

    /**
     * Extracts a safe Region of Interest (ROI) from the image based on a bounding box.
     */
    private fun extractROI(image: Mat, rect: Rect): Mat {
        val safeRect = Rect(
            maxOf(0, rect.x),
            maxOf(0, rect.y),
            minOf(rect.width, image.cols() - maxOf(0, rect.x)),
            minOf(rect.height, image.rows() - maxOf(0, rect.y))
        )

        return if (safeRect.width > 0 && safeRect.height > 0) {
            Mat(image, safeRect).clone()
        } else {
            Mat()
        }
    }

    /**
     * Copies an asset file to the cache directory so OpenCV can load it.
     */
    private fun copyAssetToCache(assetPath: String): String {
        val cacheFile = java.io.File(context.cacheDir, assetPath)
        cacheFile.parentFile?.mkdirs()

        if (!cacheFile.exists()) {
            try {
                context.assets.open(assetPath).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to copy asset: $assetPath", e)
                throw e
            }
        }

        return cacheFile.absolutePath
    }

    /**
     * Releases DNN resources.
     */
    fun release() {
        net = null
        Log.d(TAG, "Detector resources released")
    }

    companion object {
        private const val TAG = "DNNDetector"
    }
}

/**
 * Detection result returned by the DNN detector.
 */
data class Detection(
    val classId: Int,
    val className: String,
    val confidence: Float,
    val boundingBox: Rect,
    val roi: Mat
) {
    /** Releases the ROI Mat to free memory. */
    fun release() {
        roi.release()
    }
}
