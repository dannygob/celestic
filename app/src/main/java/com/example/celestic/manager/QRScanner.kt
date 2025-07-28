package com.example.celestic.manager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor

class QRScanner(private val executor: Executor) {

    @SuppressLint("UnsafeOptInUsageError")
    fun startQrScan(
        context: Context,
        image: androidx.camera.core.ImageProxy,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(inputImage)
                .addOnSuccessListener(executor) { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            onSuccess(rawValue)
                            return@addOnSuccessListener
                        }
                    }
                }
                .addOnFailureListener(executor) { e ->
                    Log.e("QRScanner", "Error al escanear código QR", e)
                    onFailure(e)
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }
}
