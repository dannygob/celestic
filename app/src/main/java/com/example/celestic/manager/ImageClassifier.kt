package com.example.celestic.manager

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
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
            // Simulación de resultados si no hay modelo
            // Retorna un array con probabilidades simuladas (mayormente OK)
            val dummyOutput = FloatArray(numClasses)
            dummyOutput[501] = 0.95f // Simular "Pieza sin defecto" (índice 501+)
            return dummyOutput
        }

        val inputBuffer = convertBitmapToByteBuffer(bitmap)
        val output = Array(1) { FloatArray(numClasses) }
        interpreter!!.run(inputBuffer, output)
        return output[0]
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer =
            ByteBuffer.allocateDirect(4 * inputImageSize * inputImageSize * numChannels)
        byteBuffer.order(ByteOrder.nativeOrder())

        val resizedBitmap = bitmap.scale(inputImageSize, inputImageSize)

        for (y in 0 until inputImageSize) {
            for (x in 0 until inputImageSize) {
                val pixel = resizedBitmap[x, y]
                val r = (pixel shr 16 and 0xFF) / 255f
                val g = (pixel shr 8 and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f

                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }

        return byteBuffer
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