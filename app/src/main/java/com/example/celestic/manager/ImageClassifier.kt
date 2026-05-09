package com.example.celestic.manager

import android.content.Context
import android.graphics.Bitmap
import com.example.celestic.utils.MLUtils
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * ImageClassifier loads a TensorFlow Lite model and performs inference
 * on input bitmaps to produce class probability predictions.
 *
 * This class is designed for lightweight on‑device inference.
 */
class ImageClassifier(context: Context) {

    // Name of the TFLite model stored in the assets folder
    private val modelFileName = "models/mobilenet_v2.tflite"

    // Expected input dimensions for the model
    private val inputImageSize = 224
    private val numChannels = 3
    private val numClasses = 1001

    // TensorFlow Lite interpreter instance
    private var interpreter: Interpreter? = null

    /**
     * Direct ByteBuffer used to store the normalized image data
     * before passing it to the TFLite interpreter.
     */
    private val inputBuffer: ByteBuffer by lazy {
        ByteBuffer.allocateDirect(4 * inputImageSize * inputImageSize * numChannels).apply {
            order(ByteOrder.nativeOrder())
        }
    }

    /**
     * Loads the TFLite model from assets and initializes the interpreter.
     */
    init {
        try {
            val assetFileDescriptor = context.assets.openFd(modelFileName)
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength

            // Memory‑map the model file for efficient loading
            val modelBuffer =
                fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            interpreter = Interpreter(modelBuffer)

        } catch (e: Exception) {
            android.util.Log.e(
                "ImageClassifier",
                "Error al inicializar modelo: $modelFileName",
                e
            )
        }
    }

    /**
     * Runs inference on the given bitmap and returns a probability array.
     *
     * If the interpreter failed to initialize, a dummy output is returned
     * to avoid crashing the application.
     */
    fun runInference(bitmap: Bitmap): FloatArray {
        if (interpreter == null) {
            val dummyOutput = FloatArray(numClasses)
            dummyOutput[501] = 0.95f // Simulated "safe" prediction
            return dummyOutput
        }

        convertBitmapToByteBuffer(bitmap)

        val output = Array(1) { FloatArray(numClasses) }
        interpreter!!.run(inputBuffer, output)

        return output[0]
    }

    /**
     * Converts a Bitmap into a normalized ByteBuffer compatible with the model.
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        MLUtils.fillBufferFromBitmap(
            bitmap,
            inputImageSize,
            inputBuffer,
            useImageNetNorm = false
        )
    }

    /**
     * Maps the predicted class index to a human‑readable label.
     *
     * NOTE:
     * This mapping is arbitrary and only for demonstration.
     * Ideally, the model should be retrained so that class indices
     * correspond directly to your defect categories.
     */
    fun mapPredictionToFeatureType(predictions: FloatArray): String {
        val maxIndex =
            predictions.indices.maxByOrNull { predictions[it] } ?: return "Clase desconocida"

        return when {
            maxIndex in 0..100 -> "Defecto superficial"
            maxIndex in 101..500 -> "Curvatura irregular"
            maxIndex >= 501 -> "Pieza sin defecto"
            else -> "Clase desconocida"
        }
    }
}
