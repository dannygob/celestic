package com.example.celestic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
// It's good practice to import R specifically from the app's package
import com.example.celestic.R
import com.example.celestic.utils.OpenCVInitializer // Added import for OpenCVInitializer

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContentView(R.layout.activity_main) // Use the XML layout

        // Check for camera permission
        checkCameraPermission()

        // OpenCV initialization is moved to startCamera()

        val detectHoleButton = findViewById<Button>(R.id.button_detect_hole)
        detectHoleButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "HOLE_DETECTION")
            startActivity(intent)
        }

        val detectAlodineButton = findViewById<Button>(R.id.button_detect_alodine)
        detectAlodineButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "ALODINE_DETECTION")
            startActivity(intent)
        }

        val detectCountersinkButton = findViewById<Button>(R.id.button_detect_countersink)
        detectCountersinkButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "COUNTERSINK_DETECTION")
            startActivity(intent)
        }

        val statusButton = findViewById<Button>(R.id.button_status)
        statusButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "STATUS")
            startActivity(intent)
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            // Permission has already been granted
            Log.d("MainActivity", "Camera permission already granted.")
            startCamera() // Call startCamera if permission is already granted
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted
                Log.d("MainActivity", "Camera permission granted by user.")
                startCamera() // Call startCamera if permission is granted by user
            } else {
                // Permission denied
                Log.d("MainActivity", "Camera permission denied by user.")
                // You could show a Toast or a Snackbar explaining why the permission is needed
            }
            return
        }
        // Handle other permissions if any
    }

    private fun startCamera() {
        // Initialize OpenCV
        // TODO: Ensure OpenCVInitializer is available or this will cause a compilation error.
        // Assuming OpenCVInitializer.initOpenCV is a static method in an accessible class.
        // If it's not, this part will need adjustment based on how OpenCV is actually initialized in this project.
        // Uncommenting the block as per current subtask instruction.
        if (!OpenCVInitializer.initOpenCV(this)) {
            Log.e("OpenCV", "Error initializing OpenCV.")
            Toast.makeText(this, "Error initializing OpenCV", Toast.LENGTH_LONG).show()
            // Optionally, return or disable camera features if OpenCV is critical
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully.")
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val previewView = findViewById<PreviewView>(R.id.camera_preview_view)
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                // Optionally, set target resolution, backpressure strategy, etc.
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        // Log frame information
                        Log.d("MainActivity", "ImageAnalysis: Frame received - Timestamp: ${imageProxy.imageInfo.timestamp}, Resolution: ${imageProxy.width}x${imageProxy.height}, Rotation: $rotationDegrees")

                        // IMPORTANT: Close the imageProxy to allow a new image to be delivered
                        imageProxy.close()
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis) // Added imageAnalysis

                Log.d("MainActivity", "CameraX preview and analysis started successfully.")

            } catch(exc: Exception) {
                Log.e("MainActivity", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this)) // Use main executor for UI updates
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
