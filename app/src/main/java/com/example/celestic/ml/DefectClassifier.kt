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
 * Clasificador de defectos usando TensorFlow Lite
 * Clasifica cada ROI detectada en: OK, Defectuoso, Leve, Severo, etc.
 */
@Singleton
class DefectClassifier @Inject constructor(
    @field:ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    private val inputSize = 224 // MobileNetV3 input
    private val numChannels = 3

    // Clases de defectos
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

    private fun loadModel() {
        try {
            // Configurar opciones del intérprete
            val options = Interpreter.Options()

            // Intentar usar GPU delegate si está disponible
            val compatList = CompatibilityList()
            if (compatList.isDelegateSupportedOnThisDevice) {
                try {
                    val delegate = GpuDelegate()
                    gpuDelegate = delegate
                    options.addDelegate(delegate)
                    Log.d(TAG, "GPU delegate habilitado")
                } catch (e: Exception) {
                    Log.w(TAG, "GPU no disponible, usando CPU", e)
                }
            }

            // Configurar número de threads
            options.setNumThreads(4)

            // Cargar modelo
            val modelBuffer = loadModelFile()
            interpreter = Interpreter(modelBuffer, options)

            Log.d(TAG, "Modelo TFLite cargado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar modelo TFLite", e)
        }
    }

    private val inputBuffer: ByteBuffer by lazy {
        ByteBuffer.allocateDirect(4 * inputSize * inputSize * numChannels).apply {
            order(ByteOrder.nativeOrder())
        }
    }

    /**
     * Clasifica una imagen (ROI) según el tipo de detección
     */
    fun classify(
        bitmap: Bitmap,
        detectionType: DNNDetector.DetectionClass
    ): ClassificationResult {
        if (interpreter == null) {
            Log.w(TAG, "Modelo no cargado, intentando cargar...")
            loadModel()
            if (interpreter == null) {
                return ClassificationResult(
                    defectClass = DefectClass.HOLE_OK,
                    confidence = 0f,
                    error = "Modelo no cargado"
                )
            }
        }

        try {
            // 1. Preprocesar imagen
            preprocessImage(bitmap)

            // 2. Preparar output
            val outputArray = Array(1) { FloatArray(DefectClass.entries.size) }

            // 3. Ejecutar inferencia
            val startTime = System.currentTimeMillis()
            interpreter?.run(inputBuffer, outputArray)
            val inferenceTime = System.currentTimeMillis() - startTime

            // 4. Post-procesamiento
            val probabilities = outputArray[0]
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val maxConfidence = probabilities[maxIndex]

            // 5. Filtrar por tipo de detección
            val relevantClass = filterByDetectionType(maxIndex, detectionType)

            return ClassificationResult(
                defectClass = relevantClass,
                confidence = maxConfidence,
                probabilities = probabilities.toList(),
                inferenceTimeMs = inferenceTime
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error en clasificación", e)
            return ClassificationResult(
                defectClass = DefectClass.HOLE_OK,
                confidence = 0f,
                error = e.message
            )
        }
    }

    /**
     * Preprocesa la imagen para el modelo
     */
    private fun preprocessImage(bitmap: Bitmap) {
        MLUtils.fillBufferFromBitmap(bitmap, inputSize, inputBuffer, useImageNetNorm = true)
    }


    /**
     * Filtra el resultado de clasificación según el tipo de detección
     */
    private fun filterByDetectionType(
        classId: Int,
        detectionType: DNNDetector.DetectionClass
    ): DefectClass {
        // Mapear resultado según el tipo de detección
        return when (detectionType) {
            DNNDetector.DetectionClass.HOLE -> {
                when (classId) {
                    0, 1 -> DefectClass.entries[classId]
                    else -> DefectClass.HOLE_OK
                }
            }

            DNNDetector.DetectionClass.COUNTERSINK -> {
                when (classId) {
                    2, 3 -> DefectClass.entries[classId]
                    else -> DefectClass.COUNTERSINK_OK
                }
            }

            DNNDetector.DetectionClass.SCRATCH -> {
                when (classId) {
                    4, 5, 6 -> DefectClass.entries[classId]
                    else -> DefectClass.SCRATCH_NONE
                }
            }

            DNNDetector.DetectionClass.DEFORMATION -> {
                when (classId) {
                    7, 8 -> DefectClass.entries[classId]
                    else -> DefectClass.DEFORMATION_OK
                }
            }

            DNNDetector.DetectionClass.ALODINE_HALO -> {
                when (classId) {
                    9, 10, 11 -> DefectClass.entries[classId]
                    else -> DefectClass.ALODINE_OK
                }
            }

            else -> DefectClass.entries.getOrNull(classId) ?: DefectClass.HOLE_OK
        }
    }

    /**
     * Carga el archivo del modelo desde assets
     */
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Libera recursos del clasificador
     */
    fun release() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
        Log.d(TAG, "Recursos del clasificador liberados")
    }

    companion object {
        private const val TAG = "DefectClassifier"
        private const val MODEL_PATH = "models/defect_classifier.tflite"
    }
}

/**
 * Resultado de clasificación
 */
data class ClassificationResult(
    val defectClass: DefectClassifier.DefectClass,
    val confidence: Float,
    val probabilities: List<Float> = emptyList(),
    val inferenceTimeMs: Long = 0,
    val error: String? = null
)
