package com.example.celestic.utils

import android.content.Context
import android.util.Log
import org.opencv.android.OpenCVLoader

object OpenCVInitializer {

    fun initOpenCV(context: Context): Boolean {
        return if (OpenCVLoader.initLocal()) {
            Log.d("OpenCV", "OpenCV initialized successfully")
            true
        } else {
            Log.e("OpenCV", "OpenCV initialization failed")
            false
        }
    }
}
