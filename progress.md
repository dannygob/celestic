# Project Progress

This file tracks the progress of the Celestic project.

## Phases

| No. | Feature | Status | Android Technical Description |
|---|---|---|---|
| 1️⃣ | Live Image Analysis | ✅ Completed | CameraX + OpenCV |
| 2️⃣ | Object Classifier | 🟡 In planning | .tflite + pre-tagging |
| 3️⃣ | Edge Detection | 🔲 Pending | Canny, Sobel, findContours |
| 4️⃣ | Technical classification | 🔲 Pending | AI trained in Python, converted to mobile |
| 5️⃣ | Car body inspection | 🔲 Future | Multi-capture + segmentation |
| 6️⃣ | 2D plan with measurements | 🔲 Pending | Canvas + calibrated scale |
| 7️⃣ | Dynamic display per part | 🔲 Pending | UI Compose + ID + state color |
| 8️⃣ | Charuco calibration | ✅ Completed | cv2.aruco, results in .json |
| 9️⃣ | ArUco + AprilTag | 🔲 Pending | Native JNI + persistence |
| 🔟 | Code scanning | ✅ | ML Kit or pyzbar Android |
| 🧩 | Inspection saved | 🔲 Pending | Room or local .json export |
| 📄 | Report generation | 🟡 In design | PDF/Word export on request |

## Checklist

### 1. Basic Project Structure
- [X] MainActivity.kt
- [X] AppNavigation.kt
- [X] ui/ folder
- [X] model/ folder
- [X] data/ folder
- [X] theme/ folder
- [X] utils/ folder
- [X] colors.xml
- [X] strings.xml
- [X] dimens.xml
- [X] themes.xml
- [X] CelesticTheme.kt
- [X] Typography.kt
- [X] Shape.kt

### 2. Data Model + Persistence
- [X] DetectionItem.kt
- [X] DetectionStatus.kt
- [X] BoundingBox.kt
- [X] ReportEntry.kt
- [X] CameraCalibrationData.kt
- [X] DetectedFeature.kt
- [X] ReportConfig.kt
- [X] DetectionDao.kt
- [X] DetectionDatabase.kt
- [X] DetectionRepository.kt
- [X] calibration.json
- [X] traceability.json
- [X] config_report.json

### 3. Camera + Image Analysis Module
- [X] CameraView.kt
- [X] CameraUtils.kt
- [X] FrameAnalyzer.kt
- [X] CalibrationManager.kt
- [X] OpenCVInitializer.kt

### 4. Integrated Artificial Intelligence
- [X] ImageClassifier.kt

### 5. Interface and Screens
- [X] DashboardScreen.kt
- [X] CameraScreen.kt
- [X] DetailsScreen.kt
- [X] ReportRequestDialog.kt
- [X] InspectionPreviewScreen.kt
- [X] CalibrationScreen.kt
- [X] FeatureCard.kt
- [X] StatusIndicator.kt
- [X] MeasurementOverlay.kt
- [X] NavigationRoutes.kt
- [X] NavigationGraph.kt

### 6. QR / ArUco / AprilTag Traceability
- [X] QRScanner.kt
- [X] ArUcoManager.kt
- [X] AprilTagManager.kt

### 7. Inspection Report (PDF / Word / JSON)
- [X] ReportGenerator.kt

### 8. Visual Resources
- [X] charuco_pattern.png
- [X] logo_celestic.png
- [X] icon_inspection.png
- [X] icon_pdf.png, icon_word.png
- [X] status_green.png, status_yellow.png, status_red.png
- [X] graph_placeholder.png

### 9. Dependency Injection
- [X] Hilt implementation

### 10. Error Handling
- [X] Sealed class for UI states

### 11. Unit Tests
- [X] ViewModels tests

### 12. UI Improvements
- [X] Animations

### 13. Authentication
- [X] Firebase Authentication

### 14. Settings
- [X] Settings screen

### 15. 2D Drawing
- [X] Drawing canvas

### 16. Dynamic Display
- [X] Detection item card

### 17. Save Inspections
- [X] Inspection entity
