🔹 Celestic – Intelligent Visual Inspection Android Project

🧠 General Purpose
Celestic is a native Android application in Kotlin + Jetpack Compose, designed to automate the
visual inspection of industrial components using computer vision and machine learning. Its evolution
includes:

- 🔍 Visual detection and classification of physical objects, with enriched queries via internal
  databases and external sources.
- 📐 Identification of technical characteristics in metal parts: 2D dimensions, holes, countersinks,
  alodine halos, etc.
- 🚦 Intelligent filtering of invalid objects based on their type and context.
- 🚗 Future scaling to complex bodies and structures to detect imperfections (bumps, dents, etc.).
- 🧭 3D extrapolation of views using supervised rotation of the part.
- 📐 Precise camera calibration using Charuco patterns.
- 🧿 Tracking using ArUco markers and optional AprilTag.
- 🧾 QR and barcode scanning to link inspections to batches or manufacturing orders.
- 📄 Report generation upon request (PDF or Word) with inspection results, measurements, and alerts
  for failed or critical parts.

🛠️ Core Technologies

- 📱 Android (Kotlin) + Jetpack Compose
- 🧠 AI: TensorFlow Lite / PyTorch Mobile
- 🎥 Image: OpenCV + CameraX
- 🐍 Python (custom model training)
- 🧩 MVVM Architecture + Data Layer (Room, Flow)

🧩 System Components

- CameraView.kt → Live capture + real-time preprocessing
- FrameAnalyzer.kt → Analysis of each frame with AI, edges, detection, measurements
- DetectionItem.kt → @Parcelize model with results for each part
- DetailsScreen.kt → Interactive view per part with measurements + status 🟢🟡🔴
- CalibrationManager.kt → Charuco handling and .json persistence
- QRScanner.kt → Code scanner with ML Kit
- ReportGenerator.kt → 🆕 New module to generate PDF/Word reports from data in Room
- ReportRequestDialog.kt → UI to allow the user to choose whether or not to generate the final
  report
- StatusScreen.kt → System overview with metrics and logs
- AppNavigation.kt + MainActivity.kt → Central navigation and screen loading

📊 Phased Goals (Technical Summary)
| No. | Feature | Status | Android Technical Description |
| 1️⃣ | Live Image Analysis | ✅ Completed | CameraX + OpenCV |
| 2️⃣ | Object Classifier | 🟡 In planning | .tflite + pre-tagging |
| 3️⃣ | Edge Detection | 🔲 Pending | Canny, Sobel, findContours |
| 4️⃣ | Technical classification | 🔲 Pending | AI trained in Python, converted to mobile |
| 5️⃣ | Car body inspection | 🔲 Future | Multi-capture + segmentation |
| 6️⃣ | 2D plan with measurements | 🔲 Pending | Canvas + calibrated scale |
| 7️⃣ | Dynamic display per part | 🔲 Pending | UI Compose + ID + state color |
| 8️⃣ | Charuco calibration | ✅ Completed | cv2.aruco, results in .json |
| 9️⃣ | ArUco + AprilTag | ✅ Implemented | Native JNI + persistence |
| 🔟 | Code scanning | ✅ Active | ML Kit or Android pyzbar |
| 🧩 | Inspection saved | 🔲 Pending | Room or local .json export |
| 📄 | Report generation | 🟡 In design | PDF/Word export on request |

🔹 Celestic – Proyecto Android de Inspección Visual Inteligente

🧠 Propósito General
Celestic es una aplicación Android nativa en Kotlin + Jetpack Compose, diseñada para automatizar la
inspección visual de componentes industriales, usando visión computacional y aprendizaje automático.
Su evolución contempla:

- 🔍 Detección y clasificación visual de objetos físicos, con consultas enriquecidas vía bases
  internas y fuentes externas.
- 📐 Identificación de características técnicas en piezas metálicas: dimensiones 2D, agujeros,
  avellanados, halos de alodine, etc.
- 🚦 Filtrado inteligente de objetos no válidos según su tipo y contexto.
- 🚗 Escalamiento futuro hacia carrocerías y estructuras complejas para detectar imperfecciones (
  golpes, abolladuras, etc.).
- 🧭 Extrapolación 3D de vistas mediante rotación supervisada de la pieza.
- 📐 Calibración precisa de la cámara usando patrones Charuco.
- 🧿 Seguimiento mediante marcadores ArUco y opcionalmente AprilTag.
- 🧾 Escaneo de códigos QR y barras para vincular inspecciones con lotes u órdenes de fabricación.
- 📄 Generación de reporte bajo solicitud (PDF o Word) con resultados de inspección, medidas y
  alertas por piezas falladas o críticas.

🛠️ Tecnologías Base

- 📱 Android (Kotlin) + Jetpack Compose
- 🧠 IA: TensorFlow Lite / PyTorch Mobile
- 🎥 Imagen: OpenCV + CameraX
- 🐍 Python (entrenamiento de modelos personalizados)
- 🧩 Arquitectura MVVM + Data Layer (Room, Flow)

🧩 Componentes del Sistema

- CameraView.kt → Captura en vivo + preprocesamiento en tiempo real
- FrameAnalyzer.kt → Análisis de cada frame con IA, bordes, detección, mediciones
- DetectionItem.kt → Modelo @Parcelize con resultados de cada pieza
- DetailsScreen.kt → Vista interactiva por pieza con medidas + estado 🟢🟡🔴
- CalibrationManager.kt → Manejo de Charuco y persistencia .json
- QRScanner.kt → Scanner de códigos con ML Kit
- ReportGenerator.kt → 🆕 Nuevo módulo para generar reportes PDF/Word desde los datos en Room
- ReportRequestDialog.kt → UI para permitir al usuario elegir generar o no el reporte final
- StatusScreen.kt → Vista general del sistema con métricas + logs
- AppNavigation.kt + MainActivity.kt → Navegación central y carga de pantallas

📊 Objetivos por Etapas (Resumen técnico)
| Nº | Función | Estado | Descripción técnica Android |
| 1️⃣ | Análisis de imagen en vivo | ✅ | CameraX + OpenCV |
| 2️⃣ | Clasificador de objetos | 🟡 En planificación | .tflite + preetiquetado |
| 3️⃣ | Detección de bordes | 🔲 Pendiente | Canny, Sobel, findContours |
| 4️⃣ | Clasificación técnica | 🔲 Pendiente | IA entrenada en Python, convertida a móvil |
| 5️⃣ | Inspección de carrocerías | 🔲 Futuro | Captura múltiple + segmentación |
| 6️⃣ | Plano 2D con medidas | 🔲 Pendiente | Canvas + escala calibrada |
| 7️⃣ | Pantalla dinámica por pieza | 🔲 Pendiente | UI Compose + ID + color de estado |
| 8️⃣ | Calibración Charuco | ✅ | cv2.aruco, resultados en .json |
| 9️⃣ | ArUco + AprilTag | 🔲 Pendiente | JNI nativo + persistencia |
| 🔟 | Escaneo de códigos | ✅ | ML Kit o pyzbar Android |
| 🧩 | Guardado de inspección | 🔲 Pendiente | Room o export .json local |
| 📄 | Generación de reporte | 🟡 En diseño | Exportador PDF/Word por solicitud | 




