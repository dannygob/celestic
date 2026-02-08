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
 * Detector de objetos usando OpenCV DNN con YOLOv8-nano
 * Detecta: láminas, agujeros, avellanados, rayaduras, deformaciones, halo de alodine
 */
@Singleton
class DNNDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var net: Net? = null
    private val inputSize = Size(640.0, 640.0) // YOLOv8 input size
    private val confThreshold = 0.5f
    private val nmsThreshold = 0.4f

    // Clases que detecta el modelo
    enum class DetectionClass(val id: Int, val label: String) {
        SHEET(0, "lamina"),
        HOLE(1, "agujero"),
        COUNTERSINK(2, "avellanado"),
        SCRATCH(3, "rayadura"),
        DEFORMATION(4, "deformacion"),
        ALODINE_HALO(5, "halo_alodine")
    }



    private fun loadModel() {
        try {
            // Cargar modelo ONNX desde assets
            val modelPath = copyAssetToCache("models/yolov8n.onnx")
            net = Dnn.readNetFromONNX(modelPath)

            // Configurar backend (usar GPU si está disponible)
            net?.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV)
            net?.setPreferableTarget(Dnn.DNN_TARGET_CPU)

            Log.d(TAG, "Modelo DNN cargado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar modelo DNN", e)
        }
    }

    /**
     * Detecta objetos en la imagen usando el modelo DNN
     */
    fun detect(image: Mat): List<Detection> {
        if (net == null) {
            Log.w(TAG, "Modelo no cargado, intentando cargar...")
            loadModel()
            if (net == null) {
                Log.e(TAG, "No se pudo cargar el modelo")
                return emptyList()
            }
        }

        val detections = mutableListOf<Detection>()

        try {
            // 1. Preprocesar imagen
            val blob = Dnn.blobFromImage(
                image,
                1.0 / 255.0,
                inputSize,
                Scalar(0.0, 0.0, 0.0),
                true,
                false
            )

            // 2. Ejecutar inferencia
            net?.setInput(blob)
            val outputs = net?.forward() ?: run {
                blob.release()
                return emptyList()
            }

            // 3. Post-procesamiento (YOLOv8 format)
            val boxes = mutableListOf<Rect>()
            val confidences = mutableListOf<Float>()
            val classIds = mutableListOf<Int>()

            // Procesar salidas del modelo
            for (i in 0 until outputs.rows()) {
                val row = outputs.row(i)
                val scores = row.colRange(5, outputs.cols())
                val minMaxResult = Core.minMaxLoc(scores)
                val confidence = minMaxResult.maxVal.toFloat()

                if (confidence > confThreshold) {
                    val classId = minMaxResult.maxLoc.x.toInt()
                    val centerX = (row.get(0, 0)[0] * image.cols()).toInt()
                    val centerY = (row.get(0, 1)[0] * image.rows()).toInt()
                    val width = (row.get(0, 2)[0] * image.cols()).toInt()
                    val height = (row.get(0, 3)[0] * image.rows()).toInt()

                    val left = centerX - width / 2
                    val top = centerY - height / 2

                    boxes.add(Rect(left, top, width, height))
                    confidences.add(confidence)
                    classIds.add(classId)
                }
            }

            // 4. Non-Maximum Suppression
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

            Dnn.NMSBoxes(
                boxesMat,
                confidencesMat,
                confThreshold,
                nmsThreshold,
                indices
            )

            // 5. Crear objetos Detection
            indices.toArray().forEach { idx ->
                val className = DetectionClass.entries
                    .find { it.id == classIds[idx] }?.label ?: "unknown"

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

            blob.release()
            outputs.release()
            indices.release()

        } catch (e: Exception) {
            Log.e(TAG, "Error en detección DNN", e)
        }

        return detections
    }

    /**
     * Extrae la región de interés (ROI) de la imagen
     */
    private fun extractROI(image: Mat, rect: Rect): Mat {
        // Asegurar que el rectángulo está dentro de los límites
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
     * Copia un archivo de assets a cache para poder usarlo
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
                Log.e(TAG, "Error al copiar asset: $assetPath", e)
                throw e
            }
        }

        return cacheFile.absolutePath
    }

    /**
     * Libera recursos del detector
     */
    fun release() {
        net = null
        Log.d(TAG, "Recursos del detector liberados")
    }

    companion object {
        private const val TAG = "DNNDetector"
    }
}

/**
 * Clase de resultado de detección
 */
data class Detection(
    val classId: Int,
    val className: String,
    val confidence: Float,
    val boundingBox: Rect,
    val roi: Mat // Región de interés extraída
) {
    fun release() {
        roi.release()
    }
}
