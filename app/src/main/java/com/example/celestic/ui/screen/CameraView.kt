package com.example.celestic.ui.screen


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.celestic.manager.CalibrationManager
import com.example.celestic.opencv.FrameAnalyzer
import com.example.celestic.utils.getCameraProvider
import com.example.celestic.utils.hasCameraPermission
import com.example.celestic.utils.OpenCVInitializer
import com.example.celestic.viewmodel.MainViewModel
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



@Composable
fun CameraView(
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val frameAnalyzer = remember { FrameAnalyzer(context, CalibrationManager(context)) }
    val qrScanner = remember { com.example.celestic.manager.QRScanner() }
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            Log.e("CameraView", "Permiso de cámara no concedido.")
        }
    }

    LaunchedEffect(Unit) {
        val success = OpenCVInitializer.initOpenCV(context)
        if (!success) Log.e("CameraView", "Error al inicializar OpenCV")
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    if (!permissionGranted) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permiso de cámara no concedido.")
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }.also { previewView ->
                    startCamera(ctx, previewView, cameraExecutor, viewModel, frameAnalyzer)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun startCamera(
    context: Context,
    previewView: PreviewView,
    cameraExecutor: ExecutorService,
    viewModel: MainViewModel,
    frameAnalyzer: FrameAnalyzer
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(224, 224)) // Tamaño del modelo
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    try {
                        val bitmap = imageProxyToBitmap(imageProxy)
                        val qrCode = qrScanner.detectQRCode(bitmap)
                        if (qrCode != null) {
                            Log.d("QRScanner", "QR Code detected: $qrCode")
                        }
                        val mat = Mat()
                        Utils.bitmapToMat(bitmap, mat)
                        val result = frameAnalyzer.analyze(mat)
                        // TODO: Do something with the result
                    } catch (e: Exception) {
                        Log.e("CameraView", "Error analyzing frame", e)
                    } finally {
                        imageProxy.close()
                    }
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("CameraView", "Error al iniciar cámara", e)
        }
    }, ContextCompat.getMainExecutor(context))
}