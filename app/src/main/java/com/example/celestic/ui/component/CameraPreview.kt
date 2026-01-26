package com.example.celestic.ui.component

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val captureRequestFlow = MutableSharedFlow<Unit>()

fun triggerCameraCapture() {
    captureRequestFlow.tryEmit(Unit)
}

@Composable
fun CameraPreview(
    onFrameCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val imageCaptureConfig = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        imageCapture = imageCaptureConfig
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCaptureConfig
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error al iniciar CameraX", e)
        }
    }

    LaunchedEffect(Unit) {
        captureRequestFlow.collectLatest {
            imageCapture?.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val bitmap = image.toBitmap()
                        image.close()
                        onFrameCaptured(bitmap)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Error capturando imagen", exception)
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Mira de precisión (HUD overlay)
        PrecisionReticle()
    }
}

@Composable
fun PrecisionReticle() {
    val paddingDp = 32.dp
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(paddingDp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val color = Color.White.copy(alpha = 0.5f)
        val stroke = 2f

        // Círculo central
        drawCircle(color, radius = 40f, center = center, style = Stroke(stroke))

        // Líneas de ejes
        drawLine(color, Offset(center.x - 60f, center.y), Offset(center.x - 20f, center.y), stroke)
        drawLine(color, Offset(center.x + 60f, center.y), Offset(center.x + 20f, center.y), stroke)
        drawLine(color, Offset(center.x, center.y - 60f), Offset(center.x, center.y - 20f), stroke)
        drawLine(color, Offset(center.x, center.y + 60f), Offset(center.x, center.y + 20f), stroke)

        // Esquinas de encuadre
        val cornerSize = 40f
        // Top Left
        drawLine(color, Offset(0f, 0f), Offset(cornerSize, 0f), stroke)
        drawLine(color, Offset(0f, 0f), Offset(0f, cornerSize), stroke)
        // Top Right
        drawLine(color, Offset(w, 0f), Offset(w - cornerSize, 0f), stroke)
        drawLine(color, Offset(w, 0f), Offset(w, cornerSize), stroke)
        // Bottom Left
        drawLine(color, Offset(0f, h), Offset(cornerSize, h), stroke)
        drawLine(color, Offset(0f, h), Offset(0f, h - cornerSize), stroke)
        // Bottom Right
        drawLine(color, Offset(w, h), Offset(w - cornerSize, h), stroke)
        drawLine(color, Offset(w, h), Offset(w, h - cornerSize), stroke)
    }
}