package com.example.celestic.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val plane = image.planes[0]
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
    yuvMat.put(0, 0, bytes)

    val rgbMat = Mat()
    Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21)

    val bmp = Bitmap.createBitmap(rgbMat.cols(), rgbMat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(rgbMat, bmp)

    yuvMat.release()
    rgbMat.release()

    return bmp
}
