package com.example.celestic.opencv

import android.annotation.SuppressLint
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import com.example.celestic.models.DetectionItem
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class CameraHandler(context: Context) : JavaCameraView(context, CAMERA_ID_BACK), CameraBridgeViewBase.CvCameraViewListener2 {

    private var latestRgbaFrame: Mat? = null
    private var itemsToDraw: List<DetectionItem> = emptyList()
    private var frameProcessors: MutableList<(Mat) -> Unit> = mutableListOf() // Kept for now, but might be unused by current subtask
    var onPreviewTapped: ((x: Float, y: Float) -> Unit)? = null
    var currentFrameWidth: Int = 0
        private set // Make setter private if only updated internally
    var currentFrameHeight: Int = 0
        private set // Make setter private if only updated internally


    init {
        visibility = View.VISIBLE
        setCvCameraViewListener(this)
        enableFpsMeter = true // Mostrar el FPS
    }

    // Agrega un nuevo procesador de frames
    fun addFrameProcessor(processor: (Mat) -> Unit) {
        frameProcessors.add(processor)
    }

    // Method to set detection items to be drawn on the preview
    fun setDetectionItemsToDraw(items: List<DetectionItem>) {
        this.itemsToDraw = items
    }

    // Method to capture the current frame
    fun captureFrame(): Mat? {
        return latestRgbaFrame?.clone() // Return a clone to avoid modification issues
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        // Configuración inicial: aquí podríamos ajustar la resolución
        latestRgbaFrame = Mat()
        currentFrameWidth = width // Initial estimate, will be updated by onCameraFrame
        currentFrameHeight = height
    }

    override fun onCameraViewStopped() {
        // Limpieza de memoria si es necesario
        latestRgbaFrame?.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val rgba = inputFrame?.rgba() ?: return Mat() // Return empty Mat if input is null
        latestRgbaFrame = rgba // Store the latest frame
        currentFrameWidth = rgba.cols()
        currentFrameHeight = rgba.rows()

        // Draw detection items
        itemsToDraw.forEach { item ->
            item.position?.let { point ->
                val radius = item.diameter?.div(2) ?: 10 // Default radius if diameter is null
                val color = when (item.type) {
                    "lamina" -> Scalar(255.0, 0.0, 0.0, 255.0) // Blue for steel sheet (example)
                    "agujero" -> Scalar(0.0, 255.0, 0.0, 255.0) // Green for holes
                    "avellanado" -> Scalar(0.0, 0.0, 255.0, 255.0) // Red for countersink (example)
                    else -> Scalar(255.0, 255.0, 0.0, 255.0) // Yellow for others
                }
                Imgproc.circle(rgba, point, radius, color, 2)
                item.width?.let { width -> // For steel sheet, draw rectangle
                    item.height?.let { height ->
                        val tl = org.opencv.core.Point(point.x - width / 2, point.y - height / 2)
                        val br = org.opencv.core.Point(point.x + width / 2, point.y + height / 2)
                        Imgproc.rectangle(rgba, tl, br, color, 2)
                    }
                }
            }
        }

        // Optional: Process frames if any processors are added.
        // For this subtask, primary detection happens on button click, not continuously here.
        // frameProcessors.forEach { it.invoke(rgba.clone()) } // Pass a clone if processors modify the mat

        return rgba
    }

    // Método para alternar entre cámaras si el dispositivo tiene múltiples opciones
    fun switchCamera(cameraId: Int) {
        disableView()
        setCameraIndex(cameraId)
        enableView()
    }

    // Método para verificar permisos antes de activar la cámara
    fun hasCameraPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            onPreviewTapped?.invoke(event.x, event.y)
            // It's important to return true if you've handled the touch event,
            // or false if you haven't and want downstream listeners to handle it.
            // For a tap, typically you'd return true.
            // However, JavaCameraView's own onTouchEvent might have logic we don't want to fully bypass.
            // Let's call super.onTouchEvent for now and see its behavior.
            // If tap navigation works, this is fine. If not, we might need to return true.
            super.onTouchEvent(event)
            return true // Indicate we consumed the event for ACTION_UP
        }
        // Pass other events to the superclass
        return super.onTouchEvent(event)
    }
}