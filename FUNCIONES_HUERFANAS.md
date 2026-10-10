# 📂 LISTADO DE CLASES Y FUNCIONES HUÉRFANAS O SIN USO ACTUAL

Este documento contiene la lista actualizada de los elementos del código de Celestic que, aunque
compilados y funcionales, actualmente no tienen referencias activas o consumidores en el flujo
principal de la aplicación.

---

## 🏗️ 1. CLASES DE IA / ML EN ESPERA DE MODELOS

Estas clases están preparadas para la fase avanzada de Inteligencia Artificial semántica, pero
permanecen huérfanas a la espera de que sus respectivos modelos sean entrenados y añadidos a
`assets/models/`:

* **`DNNDetector.kt`**
  ([Ver archivo](file:///c:/Users/compu/AndroidStudioProjects/celestic/app/src/main/java/com/example/celestic/ml/DNNDetector.kt)):
    * **Estado:** Huérfana. Diseñada para la detección de objetos por cajas con YOLOv8 (espera a
      `yolov8n.onnx`).
* **`DefectClassifier.kt`**
  ([Ver archivo](file:///c:/Users/compu/AndroidStudioProjects/celestic/app/src/main/java/com/example/celestic/ml/DefectClassifier.kt)):
    * **Estado:** Huérfana. Diseñada para categorizar fallas específicas recortadas en ROIs con
      MobileNetV3 (espera a `defect_classifier.tflite`).

---

## 🧩 2. COMPONENTES DE UI SIN USO

Componentes visuales presentes en la base de código pero desvinculados de las pantallas activas:

* **`DrawingCanvas.kt`**
  ([Ver archivo](file:///c:/Users/compu/AndroidStudioProjects/celestic/app/src/main/java/com/example/celestic/ui/component/DrawingCanvas.kt)):
    * **Estado:** Pospuesto (Para después).
    * **¿Por qué se deja?** (Nota de Ingeniería): Depende del modelo `DetectedFeature` (formas
      geométricas individuales en bruto detectadas por OpenCV). Estas características vectoriales
      detalladas no se persisten en base de datos para priorizar el rendimiento dinámico del
      procesado de la cámara.
    * **¿Para qué se deja?** (Nota de Ingeniería): Habilitará un lienzo táctil e interactivo en
      futuras fases del proyecto, permitiendo al operario pulsar e inspeccionar contornos de
      círculos o rayaduras específicas dibujadas sobre una captura fotográfica estática de la chapa.

* **`CameraView.kt`**
  ([Ver archivo](file:///c:/Users/compu/AndroidStudioProjects/celestic/app/src/main/java/com/example/celestic/ui/component/CameraView.kt)):
    * **Estado:** Pospuesto / Legacy.
    * **¿Por qué se deja?** (Nota de Ingeniería): Es una vista previa de cámara y procesado en
      tiempo real desarrollada de forma autónoma al principio. Sin embargo, en la arquitectura
      definitiva, la cámara en vivo de producción se integró directamente como `DashboardCameraView`
      en `DashboardScreen.kt` para sincronizarse de manera estrecha y eficiente con el controlador,
      la UI táctil y los flujos reactivos del dashboard.
    * **¿Para qué se deja?** (Nota de Ingeniería): Se conserva como una excelente referencia de
      implementación independiente y fallback para futuras pantallas de autodiagnóstico o soporte
      técnico.

---

## 🛠️ 3. UTILIDADES Y GESTORES (FUNCIONES PENDIENTES SIN LLAMADA)

Lógica y algoritmos internos que están declarados en sus respectivas clases pero que no son
invocados por ningún consumidor actual:

### `FrameAnalyzer.kt` (OpenCV)

* **`AlodineHalo` (data class)**:
    * **Estado:** Pospuesta / Concepto Reemplazado.
    * **¿Por qué se deja?** (Nota de Ingeniería): La evaluación de presencia del tratamiento químico
      de Alodine SÍ se ejecuta y calcula de forma activa en cada frame a través del algoritmo
      `checkAlodine`. Sin embargo, para mantener el modelo de datos limpio y unificado, el resultado
      se persiste directamente como un flag lógico (`hasAlodine: Boolean`) dentro del objeto `Hole`.
      Por ello, esta clase modelo específica queda sin referencias directas de uso.
    * **¿Para qué se deja?** (Nota de Ingeniería): Destinada a futuras ampliaciones del motor de
      metrología donde se requiera medir y almacenar de manera independiente la uniformidad del
      color, la excentricidad o el ancho real en milímetros de la aureola del tratamiento químico.

* **`detectDeformationsWithOpticalFlow`**:
    * **Estado:** Pospuesta (Para después).
    * **¿Por qué se deja?** (Nota de Ingeniería): El cálculo de Optical Flow (flujo óptico de
      Lucas-Kanade) es un proceso de visión artificial de alta carga computacional que corre cuadro
      por cuadro en el hilo de análisis. Actualmente no existe ningún visualizador, gráfica de
      vibración o indicador de deformación activa en la UI de producción. Mantenerlo encendido
      degrada innecesariamente el rendimiento de la cámara (FPS) del dispositivo móvil sin aportar
      valor práctico en esta fase.
    * **¿Para qué se deja?** (Nota de Ingeniería): Se conserva como cimiento algorítmico para una
      futura actualización del sistema destinada a pruebas de tolerancia dinámica estructural y
      detección de vibraciones/fatiga de materiales en chapas bajo carga física.

### `AprilTagManager.kt`

* **`generateVirtualTagForFeature`**:
    * **Estado:** Pospuesta (Para después).
    * **¿Por qué se deja?** (Nota de Ingeniería): La generación de etiquetas y planos virtuales 3D a
      través de códigos AprilTag requiere un motor de proyección espacial de Realidad Aumentada
      (como ARCore o Unity) integrado en el contenedor de cámara de Jetpack Compose, el cual no está
      contemplado en el MVP plano de esta aplicación industrial.
    * **¿Para qué se deja?** (Nota de Ingeniería): Se conserva para habilitar la superposición
      holográfica en Realidad Aumentada (AR) de planos CAD/CAM dorados (Golden Sample blueprints)
      directamente sobre las chapas metálicas físicas del operador en una fase de desarrollo futura.

### `SharedDataRepository.kt`

* **`getCurrentState()`**:
    * **Estado:** Pospuesta (Para después).
    * **¿Por qué se deja?** (Nota de Ingeniería): Los ViewModels y vistas Compose leen y observan el
      estado de la aplicación mediante flujos de datos observables reactivos (`StateFlow`). No es
      necesario hacer llamadas síncronas de captura de estado instantáneo (snapshots) en el flujo
      ordinario.
    * **¿Para qué se deja?** (Nota de Ingeniería): Diseñado para auditoría en caliente y volcados
      completos de telemetría (state telemetry dumps) en futuras interfaces de soporte técnico y
      diagnóstico.

### `BlueprintMatcher.kt`

* **`getBlueprintById(id)`** / **`getAllBlueprints()`**:
    * **Estado:** Pospuestas (Para después).
    * **¿Por qué se dejan?** (Nota de Ingeniería): El comparador de planos de tolerancia procesa la
      chapa actual directamente contrastando la plantilla devuelta por OpenCV, por lo que no
      requiere consultas individuales por identificador o listados masivos en el MVP de producción.
    * **¿Para qué se dejan?** (Nota de Ingeniería): Destinados a soportar una futura pantalla de
      catálogo estático donde el usuario pueda previsualizar o elegir manualmente un plano patrón en
      caso de fallo en la detección automática por cámara.

---

## 💾 4. CAPA DE DATOS (REPOSITORIO Y DAO)

Operaciones de persistencia en SQLite (Room) creadas con propósitos CRUD completos pero que no son
consumidas por la UI actual:

### `DetectionRepository.kt` / `CelesticDao.kt`

* **`saveImage(bitmap, filename)`**:
    * **Estado:** Pospuesta (Para después).
    * **¿Por qué se deja?** (Nota de Ingeniería): La compresión y el guardado de fotogramas JPG está
      implementada de manera local/inline en clases específicas de visualización (`CameraUtils` y
      `DashboardViewModel`) para independizar el guardado físico de archivos del repositorio de la
      base de datos Room.
    * **¿Para qué se deja?** (Nota de Ingeniería): Para unificar y estandarizar la persistencia y
      caché de ficheros binarios bajo una sola interfaz central en futuras refactorizaciones del
      backend.

* **`getDetectionItemsByInspection(inspectionId)`** / **`getInspectionById(id)`**:
    * **Estado:** Pospuestas (Para después).
    * **¿Por qué se dejan?** (Nota de Ingeniería): El historial de la interfaz de usuario
      (`DetectionListScreen`) recupera y lista un historial plano y cronológico global de todas las
      detecciones de la base de datos, sin agruparlas ni filtrarlas por sesión de inspección
      individual.
    * **¿Para qué se dejan?** (Nota de Ingeniería): Conservadas para soportar en el futuro una vista
      Maestro-Detalle de "Sesiones de Inspección", donde el operario pueda pulsar sobre una
      auditoría específica (con fecha, operador y coordenadas de GPS) y ver únicamente las
      detecciones de esa sesión.

* **`getAllSpecifications()`** / **`getSpecificationById(id)`** / **
  `getAllFeaturesBySpecification(specId)`** / **`deleteFeaturesBySpecification(specId)`**:
    * **Estado:** Pospuestas (Para después).
    * **¿Por qué se dejan?** (Nota de Ingeniería): Las dimensiones patrón esperadas (blueprints) de
      las piezas metálicas se cargan y comparan dinámicamente en memoria estática. El sistema actual
      de producción no tiene interfaces CRUD ni paneles de administrador para crear, editar o
      eliminar planos de tolerancia.
    * **¿Para qué se dejan?** (Nota de Ingeniería): Estructuradas como cimiento para un módulo
      "Gestor de Planos Técnicos y Tolerancias CAD", donde ingenieros de calidad puedan administrar
      el catálogo completo de piezas industriales de forma persistente.

* **`insertDetectedFeature`** / **`insertDetectedFeatures`** / **`getAllDetectedFeatures`** / **
  `clearDetectedFeatures`**:
    * **Estado:** Pospuestas (Para después).
    * **¿Por qué se dejan?** (Nota de Ingeniería): Celestic persiste y expone registros consolidados
      de hallazgos (`DetectionItem`). No se requiere guardar los contornos y geometrías en bruto
      detectados por OpenCV en la base de datos SQLite para el funcionamiento diario.
    * **¿Para qué se dejan?** (Nota de Ingeniería): Diseñadas para auditorías de alta fidelidad
      futuras, permitiendo reconstruir digitalmente las curvas y contornos medidos en milímetros de
      cada chapa inspeccionada en sistemas externos.

* **`saveInspectionWithDetections`**:
    * **Estado:** Pospuesta (Para después).
    * **¿Por qué se deja?** (Nota de Ingeniería): Las detecciones se guardan en tiempo real de forma
      atómica a medida que ocurren durante el análisis dinámico de la cámara. No hay un flujo de
      guardado por lotes (batch operations) al final de la sesión en el flujo principal.
    * **¿Para qué se deja?** (Nota de Ingeniería): Se conserva para habilitar un flujo
      "Offline-First", guardando localmente toda una jornada de inspección acumulada en una sola
      transacción segura una vez que el dispositivo recupere conectividad.

---

## 📊 5. VARIABLES ASIGNADAS Y PARÁMETROS SIN USO (OPTIMIZACIÓN Y RENDIMIENTO)

Declaraciones locales y parámetros que no tienen lectura activa pero que se mantienen con fines de
optimización o expansiones futuras:

### `FrameAnalyzer.kt` (OpenCV)

* **`thresholdedImage` (variable Mat)**:
    * **Estado:** Evaluada / Pre-asignada.
    * **¿Por qué se deja?** (Nota de Ingeniería): Pre-asignar contenedores `Mat` en el heap nativo
      de OpenCV antes de entrar al ciclo de análisis evita que la máquina virtual de Android tenga
      que recolectar y reconstruir memoria nativa de C++ repetidamente en cada frame a 30 FPS.
    * **¿Para qué se deja?** (Nota de Ingeniería): Optimiza el rendimiento, reduce el estrés de
      recolección de basura JNI y previene calentamiento térmico del dispositivo móvil. Sirve como
      anclaje estructural para futuros análisis de binarización adaptativa focalizada.

* **`contours` (parámetro en `detectScratches`)**:
    * **Estado:** Pospuesto (Para después).
    * **¿Por qué se deja?** (Nota de Ingeniería): Actualmente la detección probabilística de Hough
      opera de manera atómica sobre el mapa de bordes.
    * **¿Para qué se deja?** (Nota de Ingeniería): Reservado para implementar un filtro inteligente
      en el siguiente ciclo, descartando falsas alarmas de rayaduras que coincidan geométricamente
      con los contornos curvos de los agujeros.

### `DashboardViewModel.kt`

* **`detections` y `blueprint` (parámetros en `createBlueprintValidationResult`)**:
    * **Estado:** Pospuestos (Para después).
    * **¿Por qué se dejan?** (Nota de Ingeniería): La validación geométrica base se calcula delegada
      en la clase autónoma `BlueprintMatcher` antes de consolidar el resultado en el ViewModel.
    * **¿Para qué se dejan?** (Nota de Ingeniería): Reservados para evaluaciones de calidad
      complejas en fases posteriores, tales como realizar análisis térmicos cruzados o calcular
      estadísticas sobre las tolerancias exactas de los diámetros medidos.

### `SharedDataRepository.kt`

* **`deviceModel` y `hardwareInfo` (propiedades públicas)**:
    * **Estado:** Evaluadas / Exposición de diagnóstico.
    * **¿Por qué se dejan?** (Nota de Ingeniería): Son miembros expuestos a nivel de singleton que
      proporcionan detalles físicos de la máquina operaria de forma estática.
    * **¿Para qué se dejan?** (Nota de Ingeniería): Disponibles para cualquier exportador de
      reportes de auditoría, archivos PDF, exportaciones JSON de defectos o la pantalla de soporte
      de hardware `StatusScreen`.

### `Converters.kt` (Base de Datos)

* **`fromStringFloatMap` y `toStringFloatMap` (TypeConverters legacy)**:
    * **Estado:** Pospuestas / Retrocompatibilidad.
    * **¿Por qué se dejan?** (Nota de Ingeniería): Room requiere estos mapeadores en caso de
      restaurar bases de datos antiguas de inspecciones donde `DetectedFeature` guardaba mapas en
      lugar de objetos planos serializados.
    * **¿Para qué se dejan?** (Nota de Ingeniería): Garantiza que los dispositivos móviles de
      operarios con bases de datos heredadas no sufran cierres inesperados (crashes) ni pérdidas de
      datos al migrar de versión.
