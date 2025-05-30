package com.example.celestic.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.celestic.models.DetectionItem
import com.example.celestic.opencv.CameraHandler
import com.example.celestic.opencv.HoleDetector
import com.example.celestic.opencv.SteelSheetDetector
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

@Composable
fun CameraView(
    navController: NavController,
    detectionItems: List<DetectionItem>, // Current list of items to display
    onDetectionResult: (List<DetectionItem>) -> Unit // Callback to update detections
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraHandler = remember { CameraHandler(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> cameraHandler.enableView()
                Lifecycle.Event.ON_PAUSE -> cameraHandler.disableView()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cameraHandler.disableView() // Ensure camera is released
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                cameraHandler.apply {
                    onPreviewTapped = { rawX, rawY ->
                        // Attempt coordinate transformation
                        val viewWidth = cameraHandler.width.toFloat()
                        val viewHeight = cameraHandler.height.toFloat()

                        // Frame dimensions need to be known.
                        // Assuming CameraHandler's latestRgbaFrame is up-to-date or accessible.
                        // This part is tricky as latestRgbaFrame might not be perfectly synced
                        // or its dimensions might not be what's used for display scaling.
                        // For a robust solution, CameraHandler might need to expose its current display scale factors or transformed frame rect.
                        // Let's try to get frame dimensions from cameraHandler if possible,
                        // or assume they are what OpenCV reports (mFrameWidth, mFrameHeight are protected in JavaCameraView).
                        // A simpler approach: CameraHandler could expose its internal mFrameWidth and mFrameHeight.
                        // For now, let's assume cameraHandler.getFrameWidth() and getFrameHeight() give Mat dimensions.
                        // These are not standard public methods, so this is an assumption for now.
                        // If these are not available, we might need to modify CameraHandler to expose them.
                        // Let's proceed with a simplified logic and refine if needed.
                        // A practical way: The Mat returned by onCameraFrame has its dimensions.
                        // CameraHandler's captureFrame() returns a Mat. We could use its dimensions.

                        val currentFrameMat = cameraHandler.captureFrame() // Potentially expensive, just for dimensions
                        if (currentFrameMat == null || currentFrameMat.empty()) {
                            currentFrameMat?.release()
                            return@apply
                        }
                        val matWidth = currentFrameMat.width().toFloat()
                        val matHeight = currentFrameMat.height().toFloat()
                        currentFrameMat.release()

                        if (viewWidth == 0f || viewHeight == 0f || matWidth == 0f || matHeight == 0f) return@apply


                        // This scaling assumes the Mat is scaled to fit the view, preserving aspect ratio,
                        // and there might be letterboxing/pillarboxing.
                        // The actual displayed frame might be centered and scaled.
                        // A more accurate transformation would involve the matrix used by SurfaceView.
                        // This is a common simplification:
                        val scaleX = matWidth / viewWidth
                        val scaleY = matHeight / viewHeight

                        // This simple scaling might not be accurate if the aspect ratios differ significantly
                        // and the view uses FIT_CENTER or similar. The actual drawn area of the camera frame
                        // within the view needs to be considered.
                        // For now, let's assume the tap is relative to the view, and the detection items
                        // are in Mat coordinates.

                        val transformedX = rawX * scaleX
                        val transformedY = rawY * scaleY

                        detectionItems.forEachIndexed { index, item ->
                            if (item.type == "agujero" && item.position != null && item.diameter != null) {
                                val radius = item.diameter / 2.0
                                val dx = transformedX - item.position.x
                                val dy = transformedY - item.position.y
                                // Check if tap is within the circle of the hole
                                if ((dx * dx + dy * dy) < (radius * radius)) {
                                    navController.navigate("detailsHole/$index")
                                    return@apply // Found a hole, navigate and exit
                                }
                            }
                        }
                    }
                }
                cameraHandler // Return the handler
            },
            modifier = Modifier.weight(1f),
            update = { ch ->
                ch.setDetectionItemsToDraw(detectionItems)
            }
        )
        Button(
            onClick = {
                val capturedMat = cameraHandler.captureFrame()
                if (capturedMat != null && !capturedMat.empty()) {
                    val grayMat = Mat()
                    Imgproc.cvtColor(capturedMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

                    val allDetections = mutableListOf<DetectionItem>()

                    // 1. Detect Steel Sheet
                    val steelSheet = SteelSheetDetector.detectSteelSheet(capturedMat.clone()) // Use a clone for detection
                    steelSheet?.let {
                        allDetections.add(it)
                        // TODO: Optionally, create a sub-mat of the steel sheet area from grayMat for HoleDetector
                        // For now, HoleDetector runs on the full grayMat.
                    }

                    // 2. Detect Holes (even if no steel sheet, or refine to detect only within steel sheet bounds)
                    val detectedHoles = HoleDetector.detectHoles(grayMat.clone()) // Use a clone
                    allDetections.addAll(detectedHoles)

                    onDetectionResult(allDetections) // Update MainActivity's list

                    // Release Mats
                    capturedMat.release()
                    grayMat.release()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Capture and Detect")
        }
    }
}