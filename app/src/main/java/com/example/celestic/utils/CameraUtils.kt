package com.example.celestic.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.common.util.concurrent.ListenableFuture
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream

fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val plane = image.planes[0]
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
    yuvMat.put(0, 0, bytes)

    val rgbMat = Mat()
    Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21)

    val bmp = createBitmap(rgbMat.cols(), rgbMat.rows())
    Utils.matToBitmap(rgbMat, bmp)

    yuvMat.release()
    rgbMat.release()

    return bmp
}

fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

fun hasRequiredPermissions(context: Context): Boolean {
    val required = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        required.add(Manifest.permission.BLUETOOTH_CONNECT)
        required.add(Manifest.permission.BLUETOOTH_SCAN)
    }

    return required.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun getCameraProvider(context: Context): ListenableFuture<ProcessCameraProvider> {
    return ProcessCameraProvider.getInstance(context)
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
    val dir = File(context.filesDir, "detection_images")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "$fileName.jpg")
    return try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file
    } catch (e: Exception) {
        Log.e("CameraUtils", "Error saving bitmap $fileName", e)
        null
    }
}
