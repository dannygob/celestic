package com.example.celestic.traceability

import android.content.Context
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRScanner(private val context: Context) {

    fun scan(image: InputImage, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    onSuccess(barcode.rawValue ?: "")
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}
