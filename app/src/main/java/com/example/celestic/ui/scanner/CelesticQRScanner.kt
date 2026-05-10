package com.example.celestic.ui.scanner

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.objdetect.QRCodeDetector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * QR scanner utility using OpenCV's QRCodeDetector.
 * Converts a Bitmap into a Mat and attempts to decode a QR code.
 */
@Singleton
class CelesticQRScanner @Inject constructor() {

    // Lazy initialization of the OpenCV QR detector
    private val detector by lazy { QRCodeDetector() }

    /**
     * Attempts to detect and decode a QR code from the given Bitmap.
     *
     * @param bitmap The image to scan.
     * @return The decoded QR content, or null if none is found.
     */
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

    /**
     * Placeholder for legacy scanning calls.
     * Currently unused but kept for compatibility.
     */
    fun startScanning() {
        // Legacy placeholder
    }
}
