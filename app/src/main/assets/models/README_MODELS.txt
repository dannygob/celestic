================================================================================
GUÍA DE ARQUITECTURA DE IA Y APRENDIZAJE EN CALIENTE — CELESTIC QUALITY INSPECTION
================================================================================

La aplicación Celestic adopta un paradigma moderno de Visión Artificial Híbrida:
1. MODO ESCLAVO INTELIGENTE: Algoritmos de visión por computador (OpenCV) y
   heurísticas de alta precisión geométrica (análisis de contornos, ajuste de elipses,
   varianza de Laplaciano para rugosidad, análisis de espacio cromático HSV para Alodine).
2. REAPRENDIZAJE / APRENDIZAJE EN CALIENTE (ON-DEVICE CONTINUAL LEARNING):
   ¡No requiere generar datasets sintéticos ni entrenamientos offline complejos!
   El operario valida piezas conformes en tiempo real desde la cámara. El sistema extrae
   automáticamente la distribución geométrica (media, desviación típica 3-sigma, circularidad)
   y calibra en vivo el umbral de aceptación.
   - Dispone de ROLLBACK: Reversión inmediata de la última muestra si se introdujo por descuido.
   - Dispone de RESET: Restablecimiento completo de la memoria a cero.
   - Dispone de AUDITORÍA GRANULAR: Consulta y eliminación de muestras individuales.

--------------------------------------------------------------------------------
1. CLASIFICADOR DE DEFECTOS OPCIONAL (DefectClassifier.kt)
--------------------------------------------------------------------------------
Archivo opcional : defect_classifier.tflite
Directorio       : /app/src/main/assets/models/
Arquitectura     : MobileNetV3-Small o MobileNetV3-Large (transfer learning)
Entrada          : Float32 / UINT8 [1, 224, 224, 3] (RGB normalizado)
Salida           : Vector Softmax [1, 12] con las siguientes clases:
   0: agujero_ok
   1: agujero_defectuoso
   2: avellanado_ok
   3: avellanado_defectuoso
   4: sin_rayadura
   5: rayadura_leve
   6: rayadura_severa
   7: sin_deformacion
   8: deformado
   9: alodine_ok
   10: alodine_ausente
   11: alodine_irregular

--------------------------------------------------------------------------------
2. DETECTOR MULTICLASE YOLO OPCIONAL (DNNDetector.kt)
--------------------------------------------------------------------------------
Archivo opcional : yolov8n_celestic.onnx
Arquitectura     : YOLOv8-nano exportado a ONNX (opset 12 o superior)
Entrada          : [1, 3, 640, 640] o [1, 640, 640, 3]
Salida           : Cajas de detección (BoundingBox + Score + Class)

--------------------------------------------------------------------------------
3. FLUJO DE APRENDIZAJE RECOMENDADO
--------------------------------------------------------------------------------
- Utiliza el botón "APRENDER" en el Dashboard o tras una inspección aprobada.
- Con solo 3 a 5 piezas conformes, el sistema crea un perfil de tolerancia 3-sigma robusto.
- Si en el futuro dispones de un dataset físico etiquetado, puedes compilar un modelo
  TFLite usando 'train_defect_classifier.py' y colocarlo en este directorio.
================================================================================
