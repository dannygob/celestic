package com.example.celestic.ui.scanner

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.objdetect.QRCodeDetector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CelesticQRScanner @Inject constructor() {

    private val detector by lazy { QRCodeDetector() }

    fun startQrScan(bitmap: Bitmap): String? {
        val mat = Mat()
        return try {
            Utils.bitmapToMat(bitmap, mat)
            val result = detector.detectAndDecode(mat)
            if (result.isNullOrBlank()) null else result
        } catch (e: Exception) {
            null
        } finally {
            mat.release()
        }
    }

    fun startScanning() {
        // Placeholder for legacy calls
    }
}
