package com.example.celestic.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.celestic.utils.MLUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DefectClassifier performs defect classification on detected ROIs using TensorFlow Lite.
 *
 * Responsibilities:
 * - Load a TFLite model (with optional GPU acceleration)
 * - Preprocess ROI bitmaps
 * - Run inference and obtain class probabilities
 * - Map raw model outputs to defect categories
 * - Filter predictions based on the detection type (hole, countersink, scratch, etc.)
 */
@Singleton
class DefectClassifier @Inject constructor(
    @field:ApplicationContext private val context: Context
) {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    private val inputSize = 224   // MobileNetV3 input resolution
    private val numChannels = 3   // RGB

    /**
     * Enumeration of all supported defect classes.
     * Each class has:
     * - an ID (matching the model output index)
     * - a label used internally
     */
    enum class DefectClass(val id: Int, val label: String) {
        HOLE_OK(0, "agujero_ok"),
        HOLE_DEFECTIVE(1, "agujero_defectuoso"),
        COUNTERSINK_OK(2, "avellanado_ok"),
        COUNTERSINK_DEFECTIVE(3, "avellanado_defectuoso"),
        SCRATCH_NONE(4, "sin_rayadura"),
        SCRATCH_MINOR(5, "rayadura_leve"),
        SCRATCH_SEVERE(6, "rayadura_severa"),
        DEFORMATION_OK(7, "sin_deformacion"),
        DEFORMATION_PRESENT(8, "deformado"),
        ALODINE_OK(9, "alodine_ok"),
        ALODINE_ABSENT(10, "alodine_ausente"),
        ALODINE_IRREGULAR(11, "alodine_irregular")
    }

    /**
     * Loads the TFLite model and configures the interpreter.
     * Attempts to enable GPU acceleration if supported.
     */
    private fun loadModel() {
        try {
            val options = Interpreter.Options()

            // Try enabling GPU delegate
            val compatList = CompatibilityList()
            if (compatList.isDelegateSupportedOnThisDevice) {
                try {
                    val delegate = GpuDelegate()
                    gpuDelegate = delegate
                    options.addDelegate(delegate)
                    Log.d(TAG, "GPU delegate enabled")
                } catch (e: Exception) {
                    Log.w(TAG, "GPU unavailable, falling back to CPU", e)
                }
            }

            // Configure CPU threads
            options.setNumThreads(4)

            // Load model file
            val modelBuffer = loadModelFile()
            interpreter = Interpreter(modelBuffer, options)

            Log.d(TAG, "TFLite model loaded successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading TFLite model", e)
        }
    }

    /**
     * Input buffer used to store normalized image data.
     */
    private val inputBuffer: ByteBuffer by lazy {
        ByteBuffer.allocateDirect(4 * inputSize * inputSize * numChannels).apply {
            order(ByteOrder.nativeOrder())
        }
    }

    /**
     * Classifies a bitmap ROI according to the detection type.
     *
     * Steps:
     * 1. Load model if needed
     * 2. Preprocess bitmap
     * 3. Run inference
     * 4. Extract highest‑confidence class
     * 5. Filter class based on detection type
     */
    fun classify(
        bitmap: Bitmap,
        detectionType: DNNDetector.DetectionClass
    ): ClassificationResult {

        if (interpreter == null) {
            Log.w(TAG, "Model not loaded, attempting to load...")
            loadModel()

            if (interpreter == null) {
                return ClassificationResult(
                    defectClass = DefectClass.HOLE_OK,
                    confidence = 0f,
                    error = "Model not loaded"
                )
            }
        }

        return try {
            // Preprocess image
            preprocessImage(bitmap)

            // Prepare output buffer
            val outputArray = Array(1) { FloatArray(DefectClass.entries.size) }

            // Run inference
            val startTime = System.currentTimeMillis()
            interpreter?.run(inputBuffer, outputArray)
            val inferenceTime = System.currentTimeMillis() - startTime

            // Extract probabilities
            val probabilities = outputArray[0]
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val maxConfidence = probabilities[maxIndex]

            // Filter class based on detection type
            val relevantClass = filterByDetectionType(maxIndex, detectionType)

            ClassificationResult(
                defectClass = relevantClass,
                confidence = maxConfidence,
                probabilities = probabilities.toList(),
                inferenceTimeMs = inferenceTime
            )

        } catch (e: Exception) {
            Log.e(TAG, "Classification error", e)
            ClassificationResult(
                defectClass = DefectClass.HOLE_OK,
                confidence = 0f,
                error = e.message
            )
        }
    }

    /**
     * Preprocesses the bitmap into a normalized ByteBuffer compatible with the model.
     */
    private fun preprocessImage(bitmap: Bitmap) {
        MLUtils.fillBufferFromBitmap(bitmap, inputSize, inputBuffer, useImageNetNorm = true)
    }

    /**
     * Maps the raw model output class ID to a valid defect class,
     * constrained by the detection type (hole, countersink, scratch, etc.).
     */
    private fun filterByDetectionType(
        classId: Int,
        detectionType: DNNDetector.DetectionClass
    ): DefectClass {

        return when (detectionType) {

            DNNDetector.DetectionClass.HOLE ->
                if (classId in 0..1) DefectClass.entries[classId] else DefectClass.HOLE_OK

            DNNDetector.DetectionClass.COUNTERSINK ->
                if (classId in 2..3) DefectClass.entries[classId] else DefectClass.COUNTERSINK_OK

            DNNDetector.DetectionClass.SCRATCH ->
                if (classId in 4..6) DefectClass.entries[classId] else DefectClass.SCRATCH_NONE

            DNNDetector.DetectionClass.DEFORMATION ->
                if (classId in 7..8) DefectClass.entries[classId] else DefectClass.DEFORMATION_OK

            DNNDetector.DetectionClass.ALODINE_HALO ->
                if (classId in 9..11) DefectClass.entries[classId] else DefectClass.ALODINE_OK

            else ->
                DefectClass.entries.getOrNull(classId) ?: DefectClass.HOLE_OK
        }
    }

    /**
     * Loads the TFLite model file from assets and memory‑maps it.
     */
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    /**
     * Releases interpreter and GPU delegate resources.
     */
    fun release() {
        interpreter?.close()
        interpreter = null

        gpuDelegate?.close()
        gpuDelegate = null

        Log.d(TAG, "Classifier resources released")
    }

    companion object {
        private const val TAG = "DefectClassifier"
        private const val MODEL_PATH = "models/defect_classifier.tflite"
    }
}

/**
 * Classification result returned by the classifier.
 */
data class ClassificationResult(
    val defectClass: DefectClassifier.DefectClass,
    val confidence: Float,
    val probabilities: List<Float> = emptyList(),
    val inferenceTimeMs: Long = 0,
    val error: String? = null
)
