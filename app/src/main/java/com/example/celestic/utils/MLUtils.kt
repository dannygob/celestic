package com.example.celestic.utils

import android.graphics.Bitmap
import androidx.core.graphics.scale
import java.nio.ByteBuffer

/**
 * Utilidades para procesamiento de imágenes destinadas a modelos de Machine Learning.
 */
object MLUtils {

    /**
     * Preprocesa un Bitmap y llena un ByteBuffer con los datos normalizados.
     * 
     * @param bitmap Imagen original
     * @param targetSize Tamaño requerido por el modelo (ej: 224)
     * @param buffer Buffer de destino (debe estar ya inicializado)
     * @param useImageNetNorm Si es true, aplica media/std de ImageNet. Si es false, normaliza a [0, 1].
     */
    fun fillBufferFromBitmap(
        bitmap: Bitmap,
        targetSize: Int,
        buffer: ByteBuffer,
        useImageNetNorm: Boolean = false
    ) {
        buffer.rewind()

        // 1. Redimensionar si es necesario
        val resizedBitmap = if (bitmap.width == targetSize && bitmap.height == targetSize) {
            bitmap
        } else {
            bitmap.scale(targetSize, targetSize)
        }

        // 2. Extraer píxeles en lote
        val intValues = IntArray(targetSize * targetSize)
        resizedBitmap.getPixels(intValues, 0, targetSize, 0, 0, targetSize, targetSize)

        // 3. Normalizar y llenar buffer
        for (value in intValues) {
            val rRaw = (value shr 16 and 0xFF) / 255f
            val gRaw = (value shr 8 and 0xFF) / 255f
            val bRaw = (value and 0xFF) / 255f

            if (useImageNetNorm) {
                // Normalización ImageNet (mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
                buffer.putFloat((rRaw - 0.485f) / 0.229f)
                buffer.putFloat((gRaw - 0.456f) / 0.224f)
                buffer.putFloat((bRaw - 0.406f) / 0.225f)
            } else {
                buffer.putFloat(rRaw)
                buffer.putFloat(gRaw)
                buffer.putFloat(bRaw)
            }
        }

        // 4. Liberar si creamos una copia
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }
    }
}
