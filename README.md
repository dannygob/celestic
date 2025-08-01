🧠 General Purpose
Celestic is a native Android application in Kotlin + Jetpack Compose, designed to automate the visual inspection of industrial components using computer vision and artificial intelligence (AI) techniques. Its modular and scalable design allows it to be adapted to different industrial sectors. Currently focused on metal parts, its evolution to more complex environments is planned.

🔧 Current and Future Features
🔍 Visual detection and classification of physical objects with enriched queries to internal databases and external sources.
📐 Technical identification of characteristics in metal parts: 2D dimensions, holes, countersinks, alodine halos, etc.
🚦 Intelligent filtering of invalid objects based on their type, presence/absence, and inspection context.
📏 Blueprint-style technical drawing generated with detections, measurements, and statuses.
🧾 Report generation (PDF/Word/JSON) including processed blueprint images.
📷 Precise camera calibration using Charuco patterns.
🧿 Visual tracking using ArUco markers (default), with the future option to use AprilTag as a configurable alternative.
🧾 Inspection traceability via QR code or barcode scanning, linking each inspection 1:1 to a batch or production order.
🧭 (Future - Placeholder) 3D extrapolation using supervised rotation and multiple captures (future planning).
🚗 (Future) Scaling up to large bodies or structures, applying segmentation and imperfection detection (bumps, dents).
🛠️ Full offline mode guaranteed for inspections in environments without connectivity, running .tflite models directly on the device.

🧩 Base Technologies
📱 Android (Kotlin) + Jetpack Compose
🧠 Mobile AI: TensorFlow Lite / PyTorch Mobile
🎥 Image: OpenCV + CameraX
🐍 Python (for custom model training)
💾 Persistence: Room, Flow

🧩 Architecture: MVVM + data layer
🔐 Authentication: Firebase Auth (basic access control)
🧩 Main System Components
Component Description
CameraView.kt Video capture + real-time preprocessing
FrameAnalyzer.kt Analysis of each frame: edges, AI, measurements
DetectionItem.kt Per-piece data model, with serializable results
DetailsScreen.kt Per-piece interactive view with state and measurements
CalibrationManager.kt Calibration using Charuco, persistence in JSON
QRScanner.kt QR/barcode scanning for 1:1 traceability
ReportGenerator.kt Blueprint-style PDF/Word report generator
ReportRequestDialog.kt UI to generate reports based on user preference
StatusScreen.kt General dashboard with metrics, logs, and system status
AppNavigation.kt + MainActivity.kt Main navigation and screens

📊 Technical Summary by Functionality
Number Function Status Technology
1️⃣ Live Image Analysis ✅ CameraX + OpenCV
2️⃣ Physical Object Classifier ✅ TensorFlow Lite (.tflite)
3️⃣ Edge Detection ✅ Canny, Sobel, Contours
4️⃣ Technical Part Classification ✅ AI trained in Python
5️⃣ Body Inspection 🔲 Future Multi-capture + Segmentation
6️⃣ 2D drawing generation ✅ Calibrated canvas
7️⃣ Stateful part view ✅ Compose UI + traffic light colors
8️⃣ Charuco calibration ✅ OpenCV ArUco, JSON export
9️⃣ ArUco + AprilTag (alternative) ✅ Configurable JNI + option in settings
🔟 Code scanning ✅ ML Kit / Pyzbar
🧩 Inspection persistence ✅ Room DB + JSON export
📄 Technical-visual reports ✅ PDF / Word + blueprint image
🔐 Access control ✅ Firebase Authentication
📶 Offline mode ✅ 100% local operation
🧪 Unit tests ✅ JUnit + MockK
✨ UI animations ✅ Compose + Shimmer
🧱 Future Modularization 🔲 Designed as a Plug-in System

🧱 Construction Checklist (Updated)
1. Project Structure
MainActivity.kt, AppNavigation.kt
Folders ui/, data/, model/, theme/, utils/
XML Resources (colors.xml, strings.xml, etc.)

2. Data Model + Persistence
Entities: DetectionItem, ReportEntry, BoundingBox, CameraCalibrationData
1:1 Relationship between DetectionItem and BatchQR
Room Database (DetectionDao, DetectionRepository)
External Files: calibration.json, traceability.json

3. Image Analysis
Capture: CameraView.kt, CameraUtils.kt
Processing: detectEdges(), detectMarkers(), classifyImageAI(), extractDimensionsFromContours()
Calibration: generateCalibrationMatrix(), loadCalibrationFromJson()

4. Artificial Intelligence
Android: .tflite / .pt in assets/, ImageClassifier.kt
Python: train_model.py, data/train_images/, export for Android

5. User Interface
Screens: DashboardScreen.kt, CameraScreen.kt, DetailsScreen.kt, ReportRequestDialog.kt
Composables: FeatureCard.kt, StatusIndicator.kt, MeasurementOverlay.kt
Navigation: NavigationGraph.kt

6. Scanning + Markers
QRScanner.kt, decodeBarcode()
ArUcoManager.kt (active by default), AprilTagManager.kt (optional via JNI)
Configurable via settings

7. Technical Report
• ReportGenerator.kt: generatePdfFromDetections(), generateWordFromDetections()
• Blueprint images with measurements, features, and statuses
• Export to: /storage/emulated/0/Celestic/Reports/ReporteCelestic_Lote123.pdf
8. Visual Resources
• drawable/: icons, logo, Charuco pattern, markers, blueprint
• theme/: CelesticTheme.kt, Typography.kt, Shape.kt
________________________________________
🔜 Future Planning
Feature Status Observations
3D Extrapolation 🔲 Placeholder Techniques to be defined (multi-view, depth map, SLAM, etc.)
Body Inspection 🔲 Planning Segmentation, multi-image assembly
Modularization by sector 🔲 Planning Plugin-type structure by domain
Internal Audit and Traceability 🔲 Not started Add log events, changes, users
Advanced user management ❌ Not yet planned Differentiated roles: supervisor, technician, etc.


🧠 Propósito General 
Celestic es una aplicación Android nativa en Kotlin + Jetpack Compose, diseñada para automatizar la inspección visual de componentes industriales mediante técnicas de visión computacional e inteligencia artificial (IA). Su diseño modular y escalable permite su adaptación a distintos sectores industriales. Actualmente enfocada en piezas metálicas, se plantea su evolución hacia entornos más complejos.
🔧 Funcionalidades Actuales y Futuras
•	🔍 Detección y clasificación visual de objetos físicos con consulta enriquecida a bases internas y fuentes externas.
•	📐 Identificación técnica de características en piezas metálicas: dimensiones 2D, agujeros, avellanados, halos de alodine, etc.
•	🚦 Filtrado inteligente de objetos no válidos según su tipo, presencia/ausencia y contexto de inspección.
•	📏 Plano técnico estilo blueprint generado con detecciones, mediciones y estados.
•	🧾 Generación de reportes (PDF/Word/JSON) incluyendo imagen procesada tipo blueprint.
•	📷 Calibración precisa de la cámara usando patrones Charuco.
•	🧿 Seguimiento visual mediante marcadores ArUco (por defecto), con opción futura de usar AprilTag como alternativa configurable.
•	🧾 Trazabilidad de inspecciones vía escaneo de QR o códigos de barras, vinculando 1:1 cada inspección con un lote u orden de producción.
•	🧭 (Futuro - Placeholder) Extrapolación 3D mediante rotación supervisada y múltiples capturas (planificación futura).
•	🚗 (Futuro) Escalado hacia carrocerías o estructuras grandes, aplicando segmentación y detección de imperfecciones (golpes, abolladuras).
•	🛠️ Modo offline completo garantizado para inspecciones en entornos sin conectividad, ejecutando modelos .tflite directamente en el dispositivo.
________________________________________
🧩 Tecnologías Base
•	📱 Android (Kotlin) + Jetpack Compose
•	🧠 IA móvil: TensorFlow Lite / PyTorch Mobile
•	🎥 Imagen: OpenCV + CameraX
•	🐍 Python (para entrenamiento de modelos personalizados)
•	💾 Persistencia: Room, Flow
•	🧩 Arquitectura: MVVM + capa de datos
•	🔐 Autenticación: Firebase Auth (control básico de acceso)
________________________________________
🧩 Componentes Principales del Sistema
Componente	Descripción
CameraView.kt	Captura de video + preprocesamiento en tiempo real
FrameAnalyzer.kt	Análisis de cada frame: bordes, IA, mediciones
DetectionItem.kt	Modelo de datos por pieza, con resultados serializables
DetailsScreen.kt	Vista interactiva por pieza con estado 🟢🟡🔴 y medidas
CalibrationManager.kt	Calibración usando Charuco, persistencia en JSON
QRScanner.kt	Escaneo QR/barcode para trazabilidad 1:1
ReportGenerator.kt	Generador de reporte PDF/Word estilo blueprint
ReportRequestDialog.kt	UI para generar reporte según preferencia del usuario
StatusScreen.kt	Panel general con métricas, logs y estado del sistema
AppNavigation.kt + MainActivity.kt	Navegación principal y pantallas
________________________________________
📊 Resumen Técnico por Funcionalidad
Nº	Función	Estado	Tecnología
1️⃣	Análisis de imagen en vivo	✅	CameraX + OpenCV
2️⃣	Clasificador de objetos físicos	✅	TensorFlow Lite (.tflite)
3️⃣	Detección de bordes	✅	Canny, Sobel, Contornos
4️⃣	Clasificación técnica de piezas	✅	IA entrenada en Python
5️⃣	Inspección de carrocerías	🔲 Futuro	Multi-captura + segmentación
6️⃣	Generación de plano 2D	✅	Canvas calibrado
7️⃣	Vista por pieza con estado	✅	Compose UI + colores semafóricos
8️⃣	Calibración Charuco	✅	OpenCV ArUco, export JSON
9️⃣	ArUco + AprilTag (alternativos)	✅ Configurable	JNI + opción en ajustes
🔟	Escaneo de códigos	✅	ML Kit / Pyzbar
🧩	Persistencia de inspecciones	✅	Room DB + exportación JSON
📄	Reportes técnico-visuales	✅	PDF / Word + imagen blueprint
🔐	Control de acceso	✅	Firebase Authentication
📶	Modo Offline	✅	Operación 100% local
🧪	Tests Unitarios	✅	JUnit + MockK
✨	Animaciones UI	✅	Compose + shimmer
🧱	Modularización futura	🔲	Pensado como sistema plug-in
________________________________________
🧱 Checklist Constructivo 
1. Estructura del Proyecto
•	MainActivity.kt, AppNavigation.kt
•	Carpetas ui/, data/, model/, theme/, utils/
•	Recursos XML (colors.xml, strings.xml, etc.)
2. Modelo de Datos + Persistencia
•	Entidades: DetectionItem, ReportEntry, BoundingBox, CameraCalibrationData
•	Relación 1:1 entre DetectionItem y LoteQR
•	Base de datos Room (DetectionDao, DetectionRepository)
•	Archivos externos: calibration.json, trazabilidad.json
3. Análisis de Imagen
•	Captura: CameraView.kt, CameraUtils.kt
•	Procesamiento: detectEdges(), detectMarkers(), classifyImageAI(), extractDimensionsFromContours()
•	Calibración: generateCalibrationMatrix(), loadCalibrationFromJson()
4. Inteligencia Artificial
•	Android: .tflite / .pt en assets/, ImageClassifier.kt
•	Python: train_model.py, data/train_images/, exportación para Android
5. Interfaz de Usuario
•	Pantallas: DashboardScreen.kt, CameraScreen.kt, DetailsScreen.kt, ReportRequestDialog.kt
•	Composables: FeatureCard.kt, StatusIndicator.kt, MeasurementOverlay.kt
•	Navegación: NavigationGraph.kt
6. Escaneo + Marcadores
•	QRScanner.kt, decodeBarcode()
•	ArUcoManager.kt (activo por defecto), AprilTagManager.kt (opcional vía JNI)
•	Configurable desde ajustes
7. Reporte Técnico
•	ReportGenerator.kt: generatePdfFromDetections(), generateWordFromDetections()
•	Imágenes blueprint con medidas, características, estados
•	Exportación a: /storage/emulated/0/Celestic/Reports/ReporteCelestic_Lote123.pdf
8. Recursos Visuales
•	drawable/: iconos, logo, patrón Charuco, marcadores, blueprint
•	theme/: CelesticTheme.kt, Typography.kt, Shape.kt
________________________________________
🔜 Planificación Futura
Feature	Estado	Observaciones
Extrapolación 3D	🔲 Placeholder	Técnicas por definir (multi-view, depth map, SLAM, etc.)
Inspección de carrocerías	🔲 Planeado	Segmentación, ensamblaje multiimagen
Modularización por sector	🔲 Planeado	Estructura tipo plugin por dominio
Auditoría y trazabilidad interna	🔲 No iniciado	Agregar bitácora de eventos, cambios, usuarios
Gestión avanzada de usuarios	❌ No planeado aún	Roles diferenciados: supervisor, técnico, etc.
