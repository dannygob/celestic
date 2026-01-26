# Project Progress

This file tracks the progress of the Celestic project.

## Phases

| No. | Feature | Status | Android Technical Description |
|---|---|---|---|
| 1️⃣ | Live Image Analysis |  | CameraX + OpenCV |
| 2️⃣ | Object Classifier |  | .tflite + pre-tagging |
| 3️⃣ | Edge Detection |  | Canny, Sobel, findContours |
| 4️⃣ | Technical classification |  | AI trained in Python, converted to mobile |
| 5️⃣ | Car body inspection | 🔲 Future | Multi-capture + segmentation |
| 6️⃣ | 2D plan with measurements | | Canvas + calibrated scale |
| 7️⃣ | Dynamic display per part | | UI Compose + ID + state color |
| 8️⃣ | Charuco calibration |  | cv2.aruco, results in .json |
| 9️⃣ | ArUco + AprilTag | | Native JNI + persistence |
| 🔟 | Code scanning || ML Kit or Android pyzbar |
| 🧩 | Inspection saved | | Room or local .json export |
| 📄 | Report generation | | PDF/Word export on request |
| 🧩 | Dependency Injection |  | Hilt |
| 🐛 | Error Handling || Sealed class for UI states |
| 🧪 | Unit Tests | | JUnit and MockK |
| ✨ | UI Improvements | | Animations and Shimmer effect |
| 🔐 | Authentication || Firebase Authentication |
| ⚙️ | Settings |  | Settings screen |
| 📏 | 2D Drawing |  | 2D drawing with measurements |
| 🖼️ | Dynamic Display | | Dynamic display by part |
| 💾 | Save Inspections | | Save inspections to database |
| 🔬 | Image Processing |  | Advanced image processing techniques |
| 📷 | Camera Calibration |  | Advanced camera calibration techniques |

## Checklist

### 1. Basic Project Structure
- [ ] MainActivity.kt
- [ ] AppNavigation.kt
- [ ] ui/ folder
- [ ] model/ folder
- [ ] data/ folder
- [ ] theme/ folder
- [ ] utils/ folder
- [ ] colors.xml
- [ ] strings.xml
- [ ] dimens.xml
- [ ] themes.xml
- [ ] CelesticTheme.kt
- [ ] Typography.kt
- [ ] Shape.kt

### 2. Data Model + Persistence
- [ ] DetectionItem.kt
- [ ] DetectionStatus.kt
- [ ] BoundingBox.kt
- [ ] ReportEntry.kt
- [ ] CameraCalibrationData.kt
- [ ] DetectedFeature.kt
- [ ] ReportConfig.kt
- [ ] DetectionDao.kt
- [ ] DetectionDatabase.kt
- [ ] DetectionRepository.kt
- [ ] calibration.json
- [ ] traceability.json
- [ ] config_report.json

### 3. Camera + Image Analysis Module
- [ ] CameraView.kt
- [ ] CameraUtils.kt
- [ ] FrameAnalyzer.kt
- [ ] CalibrationManager.kt
- [ ] OpenCVInitializer.kt

### 4. Integrated Artificial Intelligence
- [ ] ImageClassifier.kt

### 5. Interface and Screens
- [ ] DashboardScreen.kt
- [ ] CameraScreen.kt
- [ ] DetailsScreen.kt
- [ ] ReportRequestDialog.kt
- [ ] InspectionPreviewScreen.kt
- [ ] CalibrationScreen.kt
- [ ] FeatureCard.kt
- [ ] StatusIndicator.kt
- [ ] MeasurementOverlay.kt
- [ ] NavigationRoutes.kt
- [ ] NavigationGraph.kt

### 6. QR / ArUco / AprilTag Traceability
- [ ] QRScanner.kt
- [ ] ArUcoManager.kt
- [ ] AprilTagManager.kt
 
### 7. Inspection Report (PDF / Word / JSON)
- [ ] ReportGenerator.kt

### 8. Visual Resources
- [ ] charuco_pattern.png
- [ ] logo_celestic.png
- [ ] icon_inspection.png
- [ ] icon_pdf.png, icon_word.png
- [ ] status_green.png, status_yellow.png, status_red.png
- [ ] graph_placeholder.png

### 9. Dependency Injection
- [ ] Hilt implementation

### 10. Error Handling
- [ ] Sealed class for UI states

### 11. Unit Tests
- [ ] ViewModels tests

### 12. UI Improvements
- [ ] Animations

### 13. Authentication
- [ ] Firebase Authentication

### 14. Settings
- [ ] Settings screen

### 15. 2D Drawing
- [ ] Drawing canvas

### 16. Dynamic Display
- [ ] Detection item card

### 17. Save Inspections
- [ ] Inspection entity

### 18. Unit Tests for Repositories
- [ ] DetectionRepository test

### 19. UI Improvements
- [ ] Shimmer effect

### 20. Image Processing
- [ ] Hough Circle Transform
- [ ] Contour Approximation
- [ ] Adaptive Thresholding
- [ ] Contour Filtering
- [ ] Watershed Algorithm
- [ ] Template Matching
- [ ] Optical Flow

### 21. Camera Calibration
- [ ] Sub-pixel corner detection
- [ ] Calibration with multiple images

### 22. Unit Tests for Business Logic
- [ ] FrameAnalyzer test

### 23. Report Generation
- [ ] CSV support

## Post-production
- Create a base application to add modules.
- Develop modules for other domains like automotive, aeronautics, etc.
