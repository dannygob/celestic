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
import java.nio.ByteBuffer // For ImageProxy conversion
import org.opencv.core.Mat // OpenCV Mat
import org.opencv.core.CvType // OpenCV CvType
import org.opencv.core.Core // For image rotation
import org.opencv.imgproc.Imgproc // OpenCV Imgproc
import org.opencv.imgcodecs.Imgcodecs // For saving Mat to file
import com.example.celestic.opencv.HoleDetector
import com.example.celestic.opencv.AlodineDetector // Import AlodineDetector
import com.example.celestic.opencv.AlodineDetectionResult // Import AlodineDetectionResult
import com.example.celestic.models.DetectionItem // For Parcelable DetectionItem
import com.example.celestic.opencv.HoleDetectionResult // Though items are extracted, good for context
import java.io.File // For file operations
import java.util.ArrayList // For parcelable list
// It's good practice to import R specifically from the app's package
import com.example.celestic.R
import com.example.celestic.utils.OpenCVInitializer

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    // Member variables to store latest detection results
    private var latestDetectionItemsForHole: ArrayList<DetectionItem>? = null
    private var latestDetectionItemsForAlodine: ArrayList<DetectionItem>? = null
    @Volatile private var latestProcessedImageFilePathForHole: String? = null // Will be used for combined image


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

            // Retrieve the latest results (thread-safely if getter is synchronized or values are volatile)
            val itemsToPass: ArrayList<DetectionItem>?
            val imagePathToPass: String?
            synchronized(this) { // Synchronize access to shared variables
                itemsToPass = this.latestDetectionItemsForHole
                imagePathToPass = this.latestProcessedImageFilePathForHole
            }

            if (imagePathToPass != null && itemsToPass != null) {
                intent.putExtra("PROCESSED_IMAGE_PATH", imagePathToPass)
                intent.putParcelableArrayListExtra("DETECTED_ITEMS", itemsToPass) // Changed key to "DETECTED_ITEMS"
                Log.d("MainActivity", "Passing image path: $imagePathToPass and ${itemsToPass?.size} items to DetailActivity.")
            } else {
                Log.d("MainActivity", "No processed image or detection items available to pass for HOLE_DETECTION.")
            }
            startActivity(intent)
        }

        val detectAlodineButton = findViewById<Button>(R.id.button_detect_alodine)
        detectAlodineButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SCREEN_TYPE", "ALODINE_DETECTION")

            var imagePath: String? = null
            var alodineItemsList: ArrayList<DetectionItem>? = null

            synchronized(this@MainActivity) {
                imagePath = this@MainActivity.latestProcessedImageFilePathForHole // Reusing this path
                // Create a new ArrayList for safety, or ensure DetailActivity can handle the original list type if not ArrayList
                this@MainActivity.latestDetectionItemsForAlodine?.let { items ->
                    alodineItemsList = ArrayList(items)
                }
            }

            if (imagePath != null) {
                intent.putExtra("PROCESSED_IMAGE_PATH", imagePath)
                Log.d("MainActivity", "Passing image path for Alodine: $imagePath")
            } else {
                Log.d("MainActivity", "No processed image path available for Alodine.")
            }

            // Pass alodine items, or an empty list if null
            if (alodineItemsList != null) {
                intent.putParcelableArrayListExtra("DETECTED_ALODINE_ITEMS", alodineItemsList)
                Log.d("MainActivity", "Passing ${alodineItemsList?.size} alodine items to DetailActivity.")
            } else {
                intent.putParcelableArrayListExtra("DETECTED_ALODINE_ITEMS", ArrayList<DetectionItem>())
                Log.d("MainActivity", "No alodine items detected, passing empty list to DetailActivity.")
            }

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
                        // Log.d("MainActivity", "ImageAnalysis: Frame received - Timestamp: ${imageProxy.imageInfo.timestamp}, Resolution: ${imageProxy.width}x${imageProxy.height}, Rotation: $rotationDegrees")

                        val tempMat = imageProxyToMat(imageProxy)

                        if (tempMat.empty()) {
                            Log.e("MainActivity", "ImageAnalysis: Converted Mat (tempMat) is empty.")
                            imageProxy.close() // Close proxy if mat conversion failed early
                            return@Analyzer
                        }

                        val rotatedMat: Mat
                        // val rotationDegrees = imageProxy.imageInfo.rotationDegrees // Already have this
                        if (rotationDegrees == 0) {
                            rotatedMat = tempMat // No rotation, use directly
                            Log.d("MainActivity", "ImageAnalysis: No rotation needed for Mat.")
                        } else {
                            rotatedMat = Mat() // Create new Mat for rotated image
                            when (rotationDegrees) {
                                90 -> Core.rotate(tempMat, rotatedMat, Core.ROTATE_90_CLOCKWISE)
                                180 -> Core.rotate(tempMat, rotatedMat, Core.ROTATE_180)
                                270 -> Core.rotate(tempMat, rotatedMat, Core.ROTATE_90_COUNTERCLOCKWISE)
                                else -> { // Should not happen if rotationDegrees is one of 0, 90, 180, 270
                                    Log.w("MainActivity", "ImageAnalysis: Unexpected rotationDegrees: $rotationDegrees. Using original mat.")
                                    rotatedMat = tempMat // Fallback to original if unexpected degree
                                }
                            }
                            Log.d("MainActivity", "ImageAnalysis: Mat rotated by $rotationDegrees degrees.")
                            if (rotatedMat !== tempMat) { // Check if tempMat was not assigned to rotatedMat (i.e. actual rotation happened)
                                tempMat.release() // Release the original tempMat as rotatedMat holds the data now
                            }
                        }

                        // Now, rotatedMat is the one to use. It will be modified by HoleDetector.
                        Log.d("MainActivity", "ImageAnalysis: Mat for hole detection - Size: ${rotatedMat.width()}x${rotatedMat.height()}")

                        val holeDetectionResult = HoleDetector.detectHoles(rotatedMat) // rotatedMat is modified by HoleDetector
                        val holeItems = ArrayList(holeDetectionResult.items)
                        // frameAfterHoleDetection is rotatedMat, which now has hole drawings
                        val frameAfterHoleDetection = holeDetectionResult.processedFrame
                        Log.d("MainActivity", "HoleDetection: Detected ${holeItems.size} holes.")
                        if (holeItems.isNotEmpty()) {
                             val firstHole = holeItems[0]
                             Log.d("MainActivity", "HoleDetection: First hole - PosX: ${firstHole.x}, PosY: ${firstHole.y}, Diameter: ${firstHole.diameter}")
                        }

                        // Now, detect alodine rings using the frame with holes already drawn
                        // AlodineDetector clones frameAfterHoleDetection, so frameAfterHoleDetection (rotatedMat) is not further modified by AlodineDetector
                        val alodineDetectionResult = AlodineDetector.detect(frameAfterHoleDetection, holeItems)
                        val alodineItems = ArrayList(alodineDetectionResult.items)
                        val finalProcessedFrame = alodineDetectionResult.processedFrame // This is a new Mat (clone) with alodine drawings

                        Log.d("MainActivity", "AlodineDetection: Detected ${alodineItems.size} alodine rings.")
                        if (alodineItems.isNotEmpty()) {
                            val firstAlodine = alodineItems[0]
                            Log.d("MainActivity", "AlodineDetection: First alodine - PosX: ${firstAlodine.x}, PosY: ${firstAlodine.y}, Diameter: ${firstAlodine.diameter}")
                        }

                        // Save the finalProcessedFrame to a temporary file
                        val tempFile = File(cacheDir, "processed_detection_img.jpg") // Generic name for combined image
                        Imgcodecs.imwrite(tempFile.absolutePath, finalProcessedFrame)

                        synchronized(this@MainActivity) {
                            this@MainActivity.latestDetectionItemsForHole = holeItems
                            this@MainActivity.latestDetectionItemsForAlodine = alodineItems
                            this@MainActivity.latestProcessedImageFilePathForHole = tempFile.absolutePath // Update path
                        }

                        // Release Mats:
                        // finalProcessedFrame is the Mat returned by AlodineDetector (a clone). It must be released.
                        finalProcessedFrame.release()
                        // rotatedMat (which is frameAfterHoleDetection) was processed by HoleDetector.
                        // It was either tempMat or a new Mat if rotation occurred.
                        // Its lifecycle management ensures it's released correctly:
                        // - if rotationDegrees == 0, rotatedMat is tempMat. tempMat is NOT released earlier. rotatedMat.release() handles it.
                        // - if rotation occurred, tempMat was released, rotatedMat is the new one. rotatedMat.release() handles it.
                        rotatedMat.release()

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

    private fun imageProxyToMat(image: ImageProxy): Mat {
        // Ensure image format is YUV_420_888
        if (image.format != android.graphics.ImageFormat.YUV_420_888) {
            Log.e("MainActivity", "Unsupported image format: ${image.format}. Expected YUV_420_888.")
            // Return an empty Mat or throw an exception
            return Mat()
        }

        val planes = image.planes
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y, V, U planes to nv21 byte array
        // Y plane first
        yBuffer.get(nv21, 0, ySize)
        // V plane next (NV21 expects V before U)
        vBuffer.get(nv21, ySize, vSize)
        // U plane last
        uBuffer.get(nv21, ySize + vSize, uSize)

        // Workaround for potential issues with direct NV21 put on certain devices or OpenCV versions
        // Create the YUV Mat with the correct dimensions for YUV_420_888 (NV21)
        // Height is 1.5 times the image height (height for Y, height/2 for UV interleaved)
        val yuvImage = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        yuvImage.put(0, 0, nv21)

        val bgrMat = Mat()
        Imgproc.cvtColor(yuvImage, bgrMat, Imgproc.COLOR_YUV2BGR_NV21)

        yuvImage.release() // Release intermediate Mat
        return bgrMat
    }
}
