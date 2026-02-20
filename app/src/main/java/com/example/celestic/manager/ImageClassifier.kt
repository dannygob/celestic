package com.example.celestic.manager

import android.content.Context
import android.graphics.Bitmap
import com.example.celestic.utils.MLUtils
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ImageClassifier(context: Context) {

    private val modelFileName = "mobilenet_v2.tflite"
    private val inputImageSize = 224
    private val numChannels = 3
    private val numClasses = 1001

    private var interpreter: Interpreter? = null
    private val inputBuffer: ByteBuffer by lazy {
        ByteBuffer.allocateDirect(4 * inputImageSize * inputImageSize * numChannels).apply {
            order(ByteOrder.nativeOrder())
        }
    }

    init {
        try {
            val assetList = context.assets.list("")
            if (assetList?.contains(modelFileName) == true) {
                val assetFileDescriptor = context.assets.openFd(modelFileName)
                val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
                val fileChannel = inputStream.channel
                val startOffset = assetFileDescriptor.startOffset
                val declaredLength = assetFileDescriptor.declaredLength
                val modelBuffer =
                    fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
                interpreter = Interpreter(modelBuffer)
            } else {
                android.util.Log.w(
                    "ImageClassifier",
                    "Modelo $modelFileName no encontrado en assets. Usando modo simulación."
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageClassifier", "Error al inicializar modelo", e)
        }
    }

    fun runInference(bitmap: Bitmap): FloatArray {
        if (interpreter == null) {
            val dummyOutput = FloatArray(numClasses)
            dummyOutput[501] = 0.95f 
            return dummyOutput
        }

        convertBitmapToByteBuffer(bitmap)
        val output = Array(1) { FloatArray(numClasses) }
        interpreter!!.run(inputBuffer, output)
        return output[0]
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        MLUtils.fillBufferFromBitmap(bitmap, inputImageSize, inputBuffer, useImageNetNorm = false)
    }

    fun mapPredictionToFeatureType(predictions: FloatArray): String {
        // Nota: Esto asume un mapeo arbitrario de las clases de ImageNet a tus defectos.
        // Lo ideal es reentrenar el modelo para que las clases 0, 1, 2 sean Defecto, Curvatura, OK.
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