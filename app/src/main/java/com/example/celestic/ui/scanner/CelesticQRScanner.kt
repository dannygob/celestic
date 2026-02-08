package com.example.celestic.ui.scanner

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.objdetect.QRCodeDetector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CelesticQRScanner @Inject constructor() {

    fun startQrScan(bitmap: Bitmap): String? {
        return decodeBarcodeFromBitmap(bitmap)
    }

    fun startScanning() {
        // Placeholder for legacy calls
    }

    companion object {
        fun decodeBarcodeFromBitmap(bitmap: Bitmap): String? {
            val mat = Mat()
            return try {
                Utils.bitmapToMat(bitmap, mat)
                decodeBarcode(mat)
            } catch (e: Exception) {
                null
            } finally {
                mat.release()
            }
        }

        fun decodeBarcode(mat: Mat): String? {
            val detector = QRCodeDetector()
            val result = detector.detectAndDecode(mat)
            return if (result.isNullOrBlank()) null else result
        }
    }
}
