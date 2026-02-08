# 🔹 Celestic – Intelligent Visual Inspection Android Project

**Status:** 🔄 In Active Development (70% Complete)  
**Last Updated:** 26 de Enero de 2026

---

# Celestic: AI-Powered Manufacturing Inspection

Sistema de inspección visual híbrido para Android que combina **Visión por Computador (OpenCV)** e *
*Inteligencia Artificial (TensorFlow Lite)** para validar piezas de manufactura aeronáutica con
precisión milimétrica.

## Características Clave (Estado Actual)

* **Visión Híbrida:** Medición geométrica precisa + Detección de defectos visuales (IA).
* **Multicapa:** Detecta Agujeros, Avellanados, Rayaduras y tratamientos químicos (Alodine).
* **Calibración Dinámica:** Conversión automática de Pixeles a Milímetros en tiempo real.
* **Blueprint Digital:** Visualización semafórica (Verde/Rojo) sobre la imagen real de la pieza.
* **Validación estricta:** Comparación contra especificaciones técnicas toleradas.

## Roadmap Técnico (Próximos Pasos)

* **Validación Posicional (Coordenadas X,Y):** Evolución hacia un "Gemelo Digital" que valida cada
  agujero en su posición exacta.
* **Segmentación de Defectos:** Uso de IA para dibujar el contorno exacto de daños superficiales (
  Púrpura).
* **Soporte Multicara:** Validación específica para Anverso/Reverso.es and external sources

- 📐 Identification of technical characteristics in metal parts: 2D dimensions, holes, countersinks,
  alodine halos, etc.
- 🚦 Intelligent filtering of invalid objects based on their type and context
- 🚗 Future scaling to complex bodies and structures to detect imperfections (bumps, dents, etc.)
- 🧭 3D extrapolation of views using supervised rotation of the part (planned)
- 📐 Precise camera calibration using ChArUco patterns
- 🧿 Tracking using ArUco markers and optional AprilTag
- 🧾 QR and barcode scanning to link inspections to batches or manufacturing orders
- 📄 Report generation upon request (PDF, Word, JSON, CSV) with inspection results, measurements, and
  alerts

---

## 🛠️ Core Technologies

- 📱 **Android (Kotlin)** + Jetpack Compose
- 🧠 **AI:** TensorFlow Lite (structure ready, integration pending)
- 🎥 **Image:** OpenCV 4.x + CameraX
- 🗄️ **Database:** Room + Flow
- 🧩 **Architecture:** MVVM + Repository Pattern
- 💉 **DI:** Dagger Hilt
- 🎨 **UI:** Material Design 3 with Dark/Light themes

---

## 🧩 System Components

### ✅ Fully Implemented

| Component                 | Description                                                  | Status     |
|---------------------------|--------------------------------------------------------------|------------|
| **CameraView.kt**         | Live capture + real-time preprocessing                       | ✅ Complete |
| **FrameAnalyzer.kt**      | Frame analysis with OpenCV (edges, contours, holes, markers) | ✅ Complete |
| **CalibrationManager.kt** | ChArUco calibration with JSON persistence                    | ✅ Complete |
| **ArUcoManager.kt**       | ArUco marker detection (DICT_6X6_250)                        | ✅ Complete |
| **AprilTagManager.kt**    | AprilTag detection (DICT_APRILTAG_36h11)                     | ✅ Complete |
| **DetectionItem.kt**      | @Entity model with Room database                             | ✅ Complete |
| **CelesticDatabase.kt**   | Room database with multiple entities                         | ✅ Complete |
| **NavigationGraph.kt**    | Jetpack Compose Navigation                                   | ✅ Complete |
| **ReportGenerator.kt**    | PDF/Word/JSON/CSV export                                     | ✅ Complete |

### 🔄 Partially Implemented

| Component                 | Description               | Status                  |
|---------------------------|---------------------------|-------------------------|
| **DashboardViewModel.kt** | Main inspection flow      | ⚠️ Has stub functions   |
| **ImageClassifier.kt**    | TensorFlow Lite inference | ⚠️ Structure only       |
| **QRScanner.kt**          | QR/Barcode scanning       | ⚠️ Basic implementation |
| **ImageProcessor.kt**     | Image processing pipeline | ⚠️ Returns empty list   |

### ❌ Not Implemented

| Component                | Description             | Status                  |
|--------------------------|-------------------------|-------------------------|
| **StatusScreen.kt**      | System metrics and logs | ❌ Mentioned but missing |
| **AI Training Pipeline** | Python training scripts | ❌ Not in repository     |
| **Unit Tests**           | JUnit + MockK tests     | ❌ Not found             |
| **Firebase Auth**        | Authentication system   | ❓ Not verified          |

---

## 📊 Implementation Status by Feature

| No. | Feature                   | Status | Description                                             |
|-----|---------------------------|--------|---------------------------------------------------------|
| 1️⃣ | Live Image Analysis       | 🔄     | CameraX + OpenCV - Structure ready, integration pending |
| 2️⃣ | Object Classifier         | ⚠️     | ImageClassifier exists but not integrated               |
| 3️⃣ | Edge Detection            | ✅      | Canny, Sobel, findContours - Fully functional           |
| 4️⃣ | Technical Classification  | ⚠️     | AI structure ready, training pipeline missing           |
| 5️⃣ | Car Body Inspection       | 🔲     | Planned for future                                      |
| 6️⃣ | 2D Plan with Measurements | 🔄     | DrawingCanvas exists, needs integration                 |
| 7️⃣ | Dynamic Display per Part  | ✅      | Multiple screens with state colors                      |
| 8️⃣ | ChArUco Calibration       | ✅      | Fully functional with JSON persistence                  |
| 9️⃣ | ArUco + AprilTag          | ✅      | Both managers fully implemented                         |
| 🔟  | Code Scanning             | ⚠️     | QRScanner with OpenCV, not integrated                   |
| 🧩  | Inspection Saved          | ✅      | Room database with Inspection entity                    |
| 📄  | Report Generation         | 🔄     | Generators complete, UI integration partial             |
| 🧩  | Dependency Injection      | ✅      | Hilt fully configured                                   |
| 🐛  | Error Handling            | 🔄     | Sealed classes implemented, needs expansion             |
| 🧪  | Unit Tests                | ❌      | Not implemented                                         |
| ✨   | UI Improvements           | ✅      | Animations, Shimmer effects, themes                     |
| 🔐  | Authentication            | 🔄     | LoginScreen exists, Firebase not verified               |
| ⚙️  | Settings                  | ✅      | Complete settings screen                                |
| 📏  | 2D Drawing                | ✅      | DrawingCanvas, BlueprintView, MeasurementOverlay        |
| 🖼️ | Dynamic Display           | ✅      | Multiple detection screens                              |
| 💾  | Save Inspections          | ✅      | Full database support                                   |
| 🔬  | Image Processing          | ✅      | 5/7 techniques implemented                              |
| 📷  | Camera Calibration        | ✅      | Advanced calibration with ChArUco                       |

**Legend:**

- ✅ Complete and Functional
- 🔄 In Development / Partially Implemented
- ⚠️ Implemented but Not Integrated
- ❌ Not Implemented
- 🔲 Planned for Future
- ❓ Not Verified

---

## 📱 Screens and UI Components

### Screens (15 total)

#### ✅ Implemented

1. **LoginScreen.kt** - User authentication
2. **DashboardScreen.kt** - Main inspection dashboard
3. **CameraScreen.kt** - Live camera capture
4. **CameraView.kt** - Camera preview component
5. **DetailsScreen.kt** - Inspection details
6. **DetectionDetailsScreen.kt** - Detection-specific details
7. **DetectionListScreen.kt** - List of all detections
8. **CalibrationScreen.kt** - Camera calibration interface
9. **InspectionPreviewScreen.kt** - Preview before saving
10. **ReportsScreen.kt** - Report management
11. **ReportRequestDialog.kt** - Report generation dialog
12. **SettingsScreen.kt** - Application settings

#### ❌ Missing

13. **StatusScreen.kt** - System metrics (mentioned in docs but not implemented)

### UI Components (16 total)

#### ✅ Implemented

1. **FeatureCard.kt** - Display detected features
2. **StatusIndicator.kt** - Status visualization (🟢🟡🔴)
3. **MeasurementOverlay.kt** - Measurement display
4. **BlueprintView.kt** - Technical drawing view
5. **CameraPreview.kt** - Camera preview wrapper
6. **DashboardComponents.kt** - Dashboard UI elements
7. **DetectionItemCard.kt** - Detection card display
8. **DrawingCanvas.kt** - 2D drawing canvas
9. **ShimmerDetectionItemCard.kt** - Loading animation card

---

## 🗄️ Data Architecture

### Database Entities (Room)

1. **DetectionItem** - Main detection records
2. **DetectedFeature** - Individual features detected
3. **CameraCalibrationData** - Camera calibration parameters
4. **ReportConfig** - Report configuration
5. **Inspection** - Inspection sessions

### Data Models

- **DetectionStatus** (enum) - OK, WARNING, NOT_ACCEPTED
- **DetectionType** (enum) - HOLE, DEFORMATION, etc.
- **BoundingBox** - Geometric boundaries
- **ReportEntry** - Report data structure
- **TrazabilidadItem** - Traceability information
- **DetectionItemConTrazabilidad** - Detection with traceability

### Repository Pattern

- **DetectionRepository** - Single source of truth for all data operations
- **CelesticDao** - Room DAO with 16 database operations

---

## 🎥 Computer Vision Features

### ✅ Implemented in FrameAnalyzer

1. **Edge Detection**
    - Canny edge detection
    - Sobel operators
    - Contour finding with `findContours()`

2. **Hole Detection**
    - Hough Circle Transform
    - Configurable parameters for sensitivity

3. **Deformation Detection**
    - Contour approximation with `approxPolyDP()`
    - Polygon analysis for irregular shapes

4. **Adaptive Thresholding**
    - Gaussian adaptive thresholding
    - Configurable block size and constant

5. **Contour Filtering**
    - Area-based filtering
    - Minimum area threshold

6. **Optical Flow**
    - Lucas-Kanade pyramidal optical flow
    - Good features to track
    - Motion detection between frames

7. **Marker Detection**
    - ArUco markers (DICT_6X6_250)
    - AprilTag markers (DICT_APRILTAG_36h11)
    - Switchable via settings

### ⚠️ Partially Implemented

8. **Camera Calibration**
    - ✅ ChArUco board detection (5x7 grid)
    - ✅ Multi-frame calibration
    - ✅ Camera matrix calculation
    - ✅ Distortion coefficients
    - ✅ JSON persistence
    - ✅ Undistortion application

### ❌ Not Implemented

9. **Watershed Algorithm** - Planned
10. **Template Matching** - Planned

---

## 🧠 Artificial Intelligence

### Current Status: ⚠️ Structure Ready, Integration Pending

#### ✅ What's Implemented

- **ImageClassifier.kt**
    - TensorFlow Lite Interpreter initialization
    - `runInference(bitmap)` - Runs model inference
    - `convertBitmapToByteBuffer()` - Preprocessing
    - `mapPredictionToFeatureType()` - Post-processing

#### ❌ What's Missing

- TFLite model file (`mobilenet_v2.tflite`) - Not verified in assets
- Integration with main detection flow
- Python training pipeline
- Labeled dataset
- Model optimization for mobile

#### 🔄 Integration Points (Stubs)

The following functions in **DashboardViewModel** throw `NotImplementedError`:

```kotlin
private fun detectFaceWithOpenCV(bitmap: Bitmap): FaceDetectionResult
private fun classifyWithTensorFlowLite(roi: Bitmap, faceLabel: String): ClassificationResult
private fun analyzeWithFrameAnalyzer(bitmap: Bitmap): FrameAnalysisResult
private suspend fun saveResultsToRoom(...): Long
```

**Impact:** Main inspection flow is not functional yet.

---

## 📄 Report Generation

### ✅ Supported Formats

1. **PDF** - Using iText library
2. **Word (.docx)** - Using Apache POI
3. **JSON** - Using Gson
4. **CSV** - Custom implementation

### ✅ Implemented Functions

- `generatePdfFromDetections()`
- `generateWordFromDetections()`
- `exportJsonSummary()`
- `generateCsvFromDetections()`
- `filterDetectionsByStatus()`

### 🔄 Partial Implementation

- Report generation UI exists (ReportRequestDialog)
- Format selection needs completion
- Export location: Currently uses `getExternalFilesDir(null)`
- Planned: `/storage/emulated/0/Celestic/Reports/`

---

## 🧾 Traceability System

### ✅ QR/Barcode Scanning

- **QRScanner.kt** - OpenCV QRCodeDetector
- `startQrScan(bitmap)` - Scan from bitmap
- `decodeBarcode(mat)` - Decode QR/barcode

### ❌ Missing Integration

- Automatic linking to DetectionItem
- Traceability database (traceability.json)
- Visual connection in DetailsScreen
- Batch/order management

---

## 🏗️ Project Structure

```
celestic/
├── app/src/main/java/com/example/celestic/
│   ├── CelesticApp.kt                    # Application class with Hilt
│   ├── MainActivity.kt                   # Main activity
│   │
│   ├── data/
│   │   ├── dao/
│   │   │   └── CelesticDao.kt           # Room DAO (16 operations)
│   │   └── repository/
│   │       └── DetectionRepository.kt    # Repository pattern
│   │
│   ├── database/
│   │   ├── CelesticDatabase.kt          # Room database
│   │   └── converters/
│   │       └── Converters.kt            # Type converters
│   │
│   ├── di/
│   │   ├── DatabaseModule.kt            # Hilt database module
│   │   └── RepositoryModule.kt          # Hilt repository module
│   │
│   ├── manager/
│   │   ├── AprilTagManager.kt           # AprilTag detection
│   │   ├── ArUcoManager.kt              # ArUco detection
│   │   ├── CalibrationManager.kt        # Camera calibration
│   │   └── ImageClassifier.kt           # TFLite inference
│   │
│   ├── models/
│   │   ├── DetectionItem.kt
│   │   ├── DetectionItemConTrazabilidad.kt
│   │   ├── Inspection.kt
│   │   ├── TrazabilidadItem.kt
│   │   ├── calibration/
│   │   │   ├── CameraCalibrationData.kt
│   │   │   └── DetectedFeature.kt
│   │   ├── enums/
│   │   │   ├── DetectionStatus.kt
│   │   │   └── DetectionType.kt
│   │   ├── geometry/
│   │   │   └── BoundingBox.kt
│   │   └── report/
│   │       ├── ReportConfig.kt
│   │       └── ReportEntry.kt
│   │
│   ├── navigation/
│   │   ├── NavigationGraph.kt           # Compose navigation
│   │   └── NavigationRoutes.kt          # Route definitions
│   │
│   ├── opencv/
│   │   ├── FrameAnalyzer.kt             # Main CV analysis
│   │   └── ImageProcessor.kt            # ⚠️ Stub
│   │
│   ├── ui/
│   │   ├── component/                   # 9 reusable components
│   │   ├── preview/                     # Preview components
│   │   ├── scanner/
│   │   │   └── startQrScan.kt          # QR scanner
│   │   ├── screen/                      # 12 screens
│   │   └── theme/                       # Material Design 3 theme
│   │
│   ├── utils/
│   │   ├── CameraUtils.kt
│   │   ├── JsonLoader.kt
│   │   ├── LocalizedStrings.kt
│   │   ├── OpenCVInitializer.kt
│   │   ├── ReportGenerator.kt
│   │   ├── Result.kt
│   │   └── filterDetectionsByStatus.kt
│   │
│   └── viewmodel/
│       ├── CalibrationViewModel.kt      # Calibration state
│       ├── DashboardViewModel.kt        # ⚠️ Has stubs
│       ├── DetailsViewModel.kt
│       ├── MainViewModel.kt
│       └── SharedViewModel.kt           # Global settings
│
├── app/src/main/res/
│   ├── values/
│   │   ├── colors.xml
│   │   ├── strings.xml
│   │   ├── themes.xml
│   │   └── dimens.xml
│   └── values-zh/                       # Chinese localization
│
├── README.md                             # This file
├── README_ANALYSIS.md                    # Implementation analysis
├── ANALISIS_FUNCIONES.md                 # Function documentation
└── progress.md                           # Detailed progress tracking
```

---

## 🚨 Known Issues and Limitations

### Critical Issues

1. **Main Detection Flow Not Functional**
    - DashboardViewModel has 4 stub functions
    - Detection → Analysis → Save pipeline incomplete
    - **Priority:** HIGH

2. **AI Integration Incomplete**
    - ImageClassifier not connected to main flow
    - TFLite model not verified
    - Training pipeline missing
    - **Priority:** HIGH

3. **ImageProcessor Empty**
    - `processImage()` returns empty list
    - **Priority:** MEDIUM

### Non-Critical Issues

4. **QR Traceability Not Integrated**
    - QRScanner exists but standalone
    - No database linkage
    - **Priority:** MEDIUM

5. **Report UI Incomplete**
    - Generators work but UI needs completion
    - Format selection not implemented
    - **Priority:** LOW

6. **No Unit Tests**
    - Zero test coverage
    - **Priority:** MEDIUM

7. **StatusScreen Missing**
    - Mentioned in documentation but doesn't exist
    - **Priority:** LOW

---

## 🎯 Roadmap

### Phase 1: Core Functionality (Current - Q1 2026)

- [x] Project structure and architecture
- [x] Database and data models
- [x] Camera calibration
- [x] Basic OpenCV analysis
- [x] Marker detection
- [ ] Complete main detection flow ⬅️ **IN PROGRESS**
- [ ] AI integration
- [ ] Unit tests

### Phase 2: Production Ready (Q2 2026)

- [ ] Complete QR traceability
- [ ] Finish report generation UI
- [ ] Add comprehensive error handling
- [ ] Implement StatusScreen
- [ ] Performance optimization
- [ ] User documentation

### Phase 3: Advanced Features (Q3 2026)

- [ ] 3D extrapolation
- [ ] Car body inspection
- [ ] Watershed algorithm
- [ ] Template matching
- [ ] Multi-language support expansion

### Phase 4: Platform Expansion (Q4 2026)

- [ ] ML Kit integration for multiplatform
- [ ] Modular architecture for different industries
- [ ] Cloud sync capabilities
- [ ] Advanced analytics dashboard

---

## 📚 Documentation

- **README.md** (this file) - Project overview and status
- **README_ANALYSIS.md** - Detailed comparison of documentation vs implementation
- **ANALISIS_FUNCIONES.md** - Complete function-by-function analysis
- **progress.md** - Detailed progress tracking with percentages

---

## 🔧 Setup and Installation

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 34
- OpenCV 4.x for Android
- Gradle 8.x

### Dependencies

```gradle
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.compose.ui:ui:1.5.4")
implementation("androidx.compose.material3:material3:1.1.2")

// OpenCV
implementation(project(":opencv"))

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// Hilt
implementation("com.google.dagger:hilt-android:2.48")
kapt("com.google.dagger:hilt-compiler:2.48")

// TensorFlow Lite
implementation("org.tensorflow:tensorflow-lite:2.14.0")

// Report Generation
implementation("com.itextpdf:itext7-core:7.2.5")
implementation("org.apache.poi:poi-ooxml:5.2.3")

// JSON
implementation("com.google.code.gson:gson:2.10.1")
```

### Build and Run

```bash
# Clone the repository
git clone [repository-url]

# Open in Android Studio
# Sync Gradle
# Run on device or emulator
```

---

## 👥 Team and Credentials

### Development Team

- Architecture: MVVM + Clean Architecture
- UI/UX: Material Design 3
- Computer Vision: OpenCV 4.x
- Machine Learning: TensorFlow Lite

### Test Credentials

- **Usuario:** admin@celestic.com
- **Clave:** celestic_dev

---

## 📄 License

[Specify license here]

---

## 📞 Contact and Support

[Contact information]

---

## 🙏 Acknowledgments

- OpenCV community
- TensorFlow team
- Android Jetpack Compose team
- Material Design team

---

**Project Status:** 🔄 Active Development (70% Complete)  
**Last Updated:** 26 de Enero de 2026  
**Version:** 0.7.0-alpha
