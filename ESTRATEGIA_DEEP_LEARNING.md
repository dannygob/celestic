# 🧠 ESTRATEGIA DE DEEP LEARNING PARA DETECCIÓN DE FALLAS EN ANDROID

**Fecha:** 21 de Febrero de 2026  
**Objetivo:** Sistema de inspección visual híbrido optimizado para manufactura aeronáutica.

---

## 🎯 ENFOQUE RECOMENDADO: HÍBRIDO (OpenCV + DNN)

### Estrategia Óptima para Android

Se utiliza una arquitectura de procesamiento en cascada que maximiza la eficiencia de la batería y la velocidad de
ejecución:

1. **Detección Rápida (OpenCV DNN)**: Localiza la lámina y sus características principales.
2. **Clasificación de Precisión (TFLite)**: Analiza el estado de calidad de cada característica detectada.
3. **Validación Geométrica (Planos)**: Contrasta los hallazgos contra especificaciones técnicas.

---

## 🏗️ ARQUITECTURA DETALLADA DE COMPONENTES

A continuación, se describe la función y el flujo de cada archivo clave en el pipeline de Deep Learning.

### 1. DNNDetector.kt

* **Descripción**: Actúa como el primer filtro de inteligencia. Utiliza una red neuronal YOLOv8-nano cargada a través
  del módulo DNN de OpenCV para realizar detecciones multiclase en tiempo real sobre el flujo de video.
* **Genera**: Una lista de hallazgos que incluye la clase (lámina, agujero, avellanado, etc.), el nivel de confianza y
  las coordenadas exactas (`BoundingBox`). Además, genera recortes de imagen (`ROI`) para cada detección.
* **Necesita**: El modelo `yolov8n.onnx` en assets, la librería nativa de OpenCV y un frame de cámara procesado.
* **Mejoras**: Implementar cuantificación de modelos a 16 bits para reducir el tamaño a la mitad sin perder precisión
  notable.

### 2. DefectClassifier.kt

* **Descripción**: Es el especialista en calidad. Recibe los recortes individuales (`ROI`) generados por el detector y
  aplica un modelo de clasificación profunda basado en MobileNetV3 para determinar el estado de salud de la pieza.
* **Genera**: Una etiqueta específica de estado (ej: `AGUJERO_OK`, `RAYADURA_SEVERA`, `ALODINE_IRREGULAR`) y un puntaje
  de precisión.
* **Necesita**: El modelo `defect_classifier.tflite`, el delegado de aceleración por GPU y bitmaps normalizados.
* **Mejoras**: Añadir un sistema de "Thresholding Dinámico" que solicite intervención humana cuando la confianza sea
  baja para alimentar un ciclo de mejora continua.

### 3. BlueprintMatcher.kt

* **Descripción**: Gestiona el contexto técnico. Utiliza técnicas de visión por computador para alinear la imagen de la
  cámara con el "Gemelo Digital" (plano) de la pieza, permitiendo validar no solo que el objeto sea correcto, sino que
  esté en la posición correcta.
* **Genera**: Un mapeo de orientación (identificando si se ve el anverso o reverso) y un informe de validación
  geométrica que detecta faltantes o desplazamientos.
* **Necesita**: Archivos maestros JSON de especificaciones y sus correspondientes imágenes patrón (templates).
* **Mejoras**: Evolucionar hacia descriptores de características (SIFT/ORB) para permitir el reconocimiento de piezas en
  condiciones de iluminación extrema o ángulos muy inclinados.

### 4. Blueprint.kt y Modelos de Datos

* **Descripción**: Define el lenguaje común de la inspección. Estructura de forma rígida y tipada los requerimientos de
  cada pieza: diámetros, tolerancias de posición, tipos de tratamientos químicos y límites de daño permitidos.
* **Genera**: El marco de referencia contra el cual se comparan todas las inferencias de la IA.
* **Necesita**: Librería de serialización Gson para transformar archivos planos en objetos inteligentes de Kotlin.
* **Mejoras**: Integrar soporte para coordenadas GPS o códigos de lote para vincular cada "plano" a una unidad física
  específica en la cadena de suministro.

### 5. DashboardViewModel.kt

* **Descripción**: Orquestador central del flujo. Implementa la máquina de estados de la inspección: captura la imagen,
  coordina la secuencia de inferencias (Detección -> Clasificación -> Validación) y maneja la persistencia en la base de
  datos Room.
* **Genera**: Estados de interfaz reactivos (Aprobado, Rechazado, Advertencia) y notifica al usuario en tiempo real
  sobre los resultados de la inspección.
* **Necesita**: Inyección de dependencias de todos los gestores de ML y acceso al repositorio de datos local.
* **Mejoras**: Implementar pre-carga predictiva de modelos basada en el escaneo inicial de códigos QR para reducir el
  tiempo de inicio de la inspección.

### 6. train_defect_classifier.py (Script Externo)

* **Descripción**: Pipeline de entrenamiento basado en Python. Utiliza Transfer Learning para especializar un modelo
  base de Google en la detección de defectos específicos de la industria (rayaduras, halos de alodine, deformaciones).
* **Genera**: El modelo optimizado y comprimido en formato TensorFlow Lite para su uso en dispositivos móviles.
* **Necesita**: Dataset de imágenes etiquetadas, Python 3.x y el framework TensorFlow/Keras.
* **Mejoras**: Implementar técnicas de "Data Augmentation" específicas para metales (simulación de reflejos y brillos)
  para mejorar la robustez del modelo.

---

## 📊 ESTRATEGIA DE INTEGRACIÓN DE DATOS

### Gemelos Digitales (JSON Blueprints)

* **Función**: Archivos maestros que definen la calidad.
* **Estructura**: ID de pieza, dimensiones físicas, mapa de coordenadas de agujeros y tabla de tolerancias.
* **Impacto**: Permite que la aplicación sea universal; para agregar una nueva pieza a la línea de ensamblaje, solo se
  necesita cargar su JSON correspondiente sin cambiar el código fuente de la app.

---

## 🎯 RESUMEN DE VENTAJAS

1. **Independencia**: Todo el procesamiento ocurre en el dispositivo (Edge AI), permitiendo inspecciones en entornos
   industriales sin Wi-Fi.
2. **Ligereza**: El conjunto total de modelos y configuraciones ocupa menos de 15 MB.
3. **Auditabilidad**: Cada decisión de la IA se contrasta con un plano técnico, eliminando la "caja negra" y permitiendo
   justificar legalmente cada aprobación o rechazo.
