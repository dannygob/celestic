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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.celestic.utils.imageProxyToBitmap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Controller used to trigger a single image capture from the CameraPreview composable.
 */
class CameraCaptureController {
    val captureRequestFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    fun triggerCapture() {
        captureRequestFlow.tryEmit(Unit)
    }
}

val LocalCameraCaptureController = staticCompositionLocalOf { CameraCaptureController() }

/**
 * Camera preview composable using CameraX.
 *
 * Displays a live camera feed and captures a frame when requested.
 */
@Composable
@UiComposable
fun CameraPreview(
    onFrameCaptured: (Bitmap) -> Unit,
    controller: CameraCaptureController = LocalCameraCaptureController.current
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }

    // Initialize CameraX
    LaunchedEffect(lifecycleOwner) {
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
            Log.e("CameraPreview", "Error starting CameraX", e)
        }
    }

    // Listen for capture requests
    LaunchedEffect(controller) {
        controller.captureRequestFlow.collectLatest {
            Log.d("CameraPreview", "Capture requested")
            imageCapture?.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        Log.d("CameraPreview", "Capture success")
                        try {
                            val bitmap = imageProxyToBitmap(image)
                            image.close()
                            onFrameCaptured(bitmap)
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Error processing captured image", e)
                            image.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraPreview", "Error capturing image", exception)
                    }
                }
            )
        }
    }

    // UI Layer
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        PrecisionReticle()
    }
}

/**
 * Draws a precision reticle overlay for alignment and centering.
 */
@Composable
fun PrecisionReticle() {
    val paddingDp = 32.dp

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingDp)
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val color = Color.White.copy(alpha = 0.5f)
        val stroke = 2f

        // Central circle
        drawCircle(color, radius = 40f, center = center, style = Stroke(stroke))

        // Axis lines
        drawLine(color, Offset(center.x - 60f, center.y), Offset(center.x - 20f, center.y), stroke)
        drawLine(color, Offset(center.x + 60f, center.y), Offset(center.x + 20f, center.y), stroke)
        drawLine(color, Offset(center.x, center.y - 60f), Offset(center.x, center.y - 20f), stroke)
        drawLine(color, Offset(center.x, center.y + 60f), Offset(center.x, center.y + 20f), stroke)

        // Corner framing marks
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
