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
