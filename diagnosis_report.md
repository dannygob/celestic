# Diagnosis Report

## Class Declarations

### app/src/main/java/com/example/celestic/MainActivity.kt
- class MainActivity : ComponentActivity()

### app/src/main/java/com/example/celestic/models/DetectionItem.kt
- data class DetectionItem(...)

### app/src/main/java/com/example/celestic/opencv/CameraHandler.kt
- class CameraHandler(context: Context) : JavaCameraView(context, CAMERA_ID_BACK), CameraBridgeViewBase.CvCameraViewListener2

### app/src/main/java/com/example/celestic/opencv/Holedetector.kt
- object HoleDetector

### app/src/main/java/com/example/celestic/opencv/SteelSheetDetector.kt
- object SteelSheetDetector

### app/src/main/java/com/example/celestic/opencv/counterSinkDetector.kt
- object CountersinkDetector

### app/src/main/java/com/example/celestic/utils/OpenCVInitializer.kt
- object OpenCVInitializer

## Function Declarations

### app/src/main/java/com/example/celestic/MainActivity.kt
- override fun onCreate(savedInstanceState: Bundle?)

### app/src/main/java/com/example/celestic/navigation/AppNavigation.kt
- @Composable fun AppNavigation(...)

### app/src/main/java/com/example/celestic/opencv/CameraHandler.kt
- fun addFrameProcessor(processor: (Mat) -> Unit)
- override fun onCameraViewStarted(width: Int, height: Int)
- override fun onCameraViewStopped()
- override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat
- fun switchCamera(cameraId: Int)
- fun hasCameraPermission(context: Context): Boolean

### app/src/main/java/com/example/celestic/opencv/Holedetector.kt
- fun detectHoles(frame: Mat, gray: Mat): List<DetectionItem>

### app/src/main/java/com/example/celestic/opencv/SteelSheetDetector.kt
- fun detectSteelSheet(frame: Mat): DetectionItem?

### app/src/main/java/com/example/celestic/opencv/counterSinkDetector.kt
- fun classifyHoles(frame: Mat, holes: List<DetectionItem>): List<DetectionItem>
- private fun determineHoleType(center: Point, radius: Int, frame: Mat): String
- private fun getColorAtPoint(center: Point, frame: Mat): Scalar
- private fun isAnodized(color: Scalar): Boolean
- private fun isCountersink(center: Point, radius: Int, frame: Mat): Boolean

### app/src/main/java/com/example/celestic/ui/screens/CameraView.kt
- @Composable fun CameraView(navController: NavController)

### app/src/main/java/com/example/celestic/ui/screens/DetailsHoleScreen.kt
- @Composable fun DetailsHoleScreen(...)

### app/src/main/java/com/example/celestic/utils/OpenCVInitializer.kt
- fun initOpenCV(context: Context): Boolean

## Color Definitions

- Name: black, Hex: #FF000000
- Name: white, Hex: #FFFFFFFF
