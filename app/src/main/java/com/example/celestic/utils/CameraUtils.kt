package com.example.celestic.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Mat

object CameraUtils {

    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        // Implement the conversion from ImageProxy to Bitmap
        return null
    }

    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }
}
