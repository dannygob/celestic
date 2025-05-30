# Application Navigation Flow Explanation

This document outlines the navigation flow and implementation within the Celestic application, focusing on how users move between different screens and how data is managed during these transitions.

## 1. Core Navigation Setup

The core navigation is managed using Jetpack Compose Navigation within `MainActivity.kt`.

*   **`NavHost` Setup**:
    A `NavHost` is conditionally set up in `MainActivity.kt` after camera permissions are granted. The `rememberNavController()` function creates a `NavController` instance that manages navigation state and operations.
    ```kotlin
    // In MainActivity.kt
    val navController = rememberNavController()
    // ... (permission handling) ...
    if (cameraPermissionGranted) {
        NavHost(navController = navController, startDestination = "camera") {
            // ... routes defined here ...
        }
    }
    ```

*   **Defined Routes**:
    Two main routes are defined:
    1.  `"camera"`: This is the start destination of the `NavHost`.
    2.  `"detailsHole/{index}"`: This route takes an `index` argument, which is the integer index of a detected item in a list.

*   **Associated Composables**:
    *   The `"camera"` route is associated with the `CameraView` composable:
        ```kotlin
        composable("camera") {
            CameraView(
                navController = navController,
                detectionItems = detectionItemsState, // From MainActivity's state
                onDetectionResult = { newDetections ->
                    detectionItemsState = newDetections
                }
            )
        }
        ```
    *   The `"detailsHole/{index}"` route is associated with the `DetailsHoleScreen` composable. The `index` is extracted from the route arguments:
        ```kotlin
        composable("detailsHole/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            DetailsHoleScreen(
                index = index,
                detectionItems = detectionItemsState, // From MainActivity's state
                navController = navController
            )
        }
        ```

## 2. Initial Flow & Permissions

Upon application startup, `MainActivity.kt` first handles camera permissions before displaying any primary UI that depends on the camera.

*   A `mutableStateOf(false)` named `cameraPermissionGranted` tracks the permission status.
*   An `ActivityResultLauncher` (`requestPermissionLauncher`) is registered to request `Manifest.permission.CAMERA`.
*   A `LaunchedEffect` checks the current permission status:
    *   If permission is already granted, `cameraPermissionGranted` is set to `true`.
    *   If not granted, `requestPermissionLauncher.launch()` is called.
*   The UI conditionally displays:
    *   If `cameraPermissionGranted` is `true`, the `NavHost` (and thus `CameraView`) is displayed.
    *   If `false`, a message and a button to re-request permission are shown.

## 3. Navigation from `CameraView` to `DetailsHoleScreen`

This navigation occurs when a user taps on a detected hole overlay in the `CameraView`.

*   **"Capture and Detect" Button**:
    *   Located in `CameraView.kt`, this button initiates the image capture and detection process.
    *   It calls `cameraHandler.captureFrame()` to get a `Mat` object.
    *   It then uses `SteelSheetDetector` and `HoleDetector` to find items in the frame.
    *   The results (a `List<DetectionItem>`) are passed back to `MainActivity` via the `onDetectionResult` callback, updating `detectionItemsState`.
    *   The `detectionItemsState` is passed to `CameraHandler` via `setDetectionItemsToDraw`, which then draws overlays on the camera preview.

*   **Interactive Hole Overlays**:
    *   `CameraHandler.kt` is equipped with an `onTouchEvent` listener. When a tap (`MotionEvent.ACTION_UP`) occurs on the camera preview surface, it invokes a callback, `onPreviewTapped((x, y) -> Unit)`.
    *   `CameraView.kt` sets this callback on its `CameraHandler` instance.

*   **Coordinate Transformation**:
    *   Inside the `onPreviewTapped` callback in `CameraView.kt`, the raw touch coordinates (View coordinates) are transformed to Mat (camera frame) coordinates.
    *   This is approximated using the dimensions of the `CameraHandler` view and the dimensions of the camera frame (`currentFrameWidth`, `currentFrameHeight` provided by `CameraHandler`):
        ```kotlin
        val viewWidth = cameraHandler.width.toFloat()
        val viewHeight = cameraHandler.height.toFloat()
        val matWidth = cameraHandler.currentFrameWidth.toFloat()
        val matHeight = cameraHandler.currentFrameHeight.toFloat()

        val scaleX = matWidth / viewWidth
        val scaleY = matHeight / viewHeight
        val transformedX = rawX * scaleX
        val transformedY = rawY * scaleY
        ```
    *   This transformation is crucial for mapping the tap location to the coordinate system used by the detection items.

*   **Navigation Logic**:
    *   After transforming coordinates, `CameraView.kt` iterates through the `detectionItems` list (received from `MainActivity`).
    *   For each `DetectionItem` of type `"agujero"`:
        *   It checks if the `transformedX, transformedY` point lies within the circular area of the hole (using its `position` and `diameter`).
        *   If a match is found, `navController.navigate("detailsHole/$index")` is called, where `index` is the index of the tapped hole in the `detectionItems` list.

## 4. Navigation from `DetailsHoleScreen` back to `CameraView`

Returning from the details screen is straightforward:

*   **"Back to Camera" Button**:
    *   `DetailsHoleScreen.kt` features a "Back to Camera" `Button`, typically placed in a `FloatingActionButton` within its `Scaffold`.
*   **`popBackStack()`**:
    *   The button's `onClick` action calls `navController.popBackStack()`.
    *   This action pops the current destination (`DetailsHoleScreen`) off the navigation back stack, returning the user to the previous screen, which in this flow is `CameraView`.

## 5. Data Passing

Data is passed between composables primarily through parameters and navigation arguments.

*   **`detectionItemsState` Management**:
    *   `MainActivity.kt` holds the primary list of detected items as a Compose `State`:
        ```kotlin
        private var detectionItemsState by mutableStateOf<List<DetectionItem>>(emptyList())
        ```
    *   This state is passed as a parameter to `CameraView`. `CameraView` uses it to display overlays (via `CameraHandler`) and to identify tapped holes.
    *   The `onDetectionResult` lambda provided to `CameraView` allows it to update this state in `MainActivity` when new detections occur.
    *   `detectionItemsState` is also passed as a parameter to `DetailsHoleScreen` so it can display information for the selected item.

*   **`index` as Navigation Argument**:
    *   When navigating from `CameraView` to `DetailsHoleScreen`, the `index` of the tapped hole within the `detectionItemsState` list is passed as part of the route string:
        ```kotlin
        navController.navigate("detailsHole/$index")
        ```
    *   `DetailsHoleScreen` then retrieves this `index` from its `backStackEntry.arguments` to access the correct `DetectionItem` from the `detectionItems` list it receives.

This navigation structure allows for a clear separation of concerns while enabling data flow and user transitions between the camera interaction view and the details display view.
