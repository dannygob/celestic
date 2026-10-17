# PLAN FASE 2: INSPECCIÓN AUTOMOTRIZ (CARROCERÍA, PIEZAS Y DAÑOS POR GOLPES/GRANIZO)
*TODO / Roadmap Técnico para Implementación Futura en Celestic*

---

## 1. Visión del Sistema
Evolucionar la plataforma Celestic de inspección metrológica de piezas aeronáuticas/industriales hacia la peritación y control de calidad automotriz. El sistema será capaz de:
1. Identificar automáticamente la pieza del vehículo encuadrada (capó, puerta delantera/trasera izq/der, techo, aleta, maletero, paragolpes).
2. Segmentar la silueta exacta de la chapa aislando reflejos y elementos de fondo.
3. Detectar y cuantificar deformaciones de superficie (abolladuras, micro-impactos de granizo, pliegues por choque).
4. Generar un informe técnico pericial (PDF/Excel) con mapa de calor de daños, conteo de impactos y recomendación de reparación (varillero PDR vs. chapa y pintura vs. sustitución).

---

## 2. Pipeline de Modelos y Arquitectura de IA

### A. Segmentación Semántica y Clasificación de Pieza
* **Modelo objetivo:** `car_parts_segmenter.tflite` basado en **YOLOv8x-Seg / YOLOv11-Seg** o **MobileNetV4 + DeepLabV3+**.
* **Clases:**
  * `hood` (capó)
  * `roof` (techo)
  * `door_front_left` / `door_front_right`
  * `door_rear_left` / `door_rear_right`
  * `fender_left` / `fender_right`
  * `trunk` (portón trasero)
  * `bumper_front` / `bumper_rear`
* **Salida requerida:** Polígono binario (Mask ROI) que acota la zona de análisis métrico a la chapa pura.

### B. Detección de Deformaciones y Granizo (*Hail Dent Detection*)
* **Modelo de Red Profunda:** Red convolucional / Transformer para detección de pequeñas anomalías superficiales entrenada sobre iluminación de deflectometría (líneas PDR o luz rasante).
* **Visión por Computador Híbrida (OpenCV):**
  * Análisis de gradiente de intensidad espectral (concavidades y convexidades generan pares reflectantes luz/sombra).
  * Extracción de curvatura local mediante operadores Laplaciano de Gaussiana (LoG) y gradientes Sobel direccionales.
  * Extensión del módulo actual `detectDeformationsWithOpticalFlow` para capturas en movimiento ligero alrededor de la carrocería.
* **Proyección de Rejilla Virtual (Deflectometría AR):**
  * Uso del subsistema de Realidad Aumentada / AprilTag de Celestic para superponer una trama de líneas paralelas sobre la pantalla. La curvatura de los reflejos cuantifica la profundidad en milímetros de la abolladura.

### C. Clasificación Pericial y Estimación de Daños
* **Variables calculadas:**
  * Número total de impactos por componente.
  * Densidad de impactos por $m^2$.
  * Diámetro medio y máximo de las abolladuras.
  * Proximidad a nervios y líneas de tensión estructural.
* **Dictamen:**
  * *Nivel 1 (Leve / Granizo Fino):* Reparación sin pintar (PDR / Varilleros).
  * *Nivel 2 (Medio):* Masillado, lijado y pintura.
  * *Nivel 3 (Grave / Estructural):* Deformación de nervio o pestaña; requiere sustitución del panel.

---

## 3. Hoja de Ruta de Implementación (TODOs)

- [ ] **TODO-AUTO-01:** Definición y recolección de dataset con etiquetado poligonal de piezas y catálogo de abolladuras.
- [ ] **TODO-AUTO-02:** Entrenamiento y cuantización a INT8 del modelo `car_parts_segmenter.tflite`.
- [ ] **TODO-AUTO-03:** Creación del módulo `AutomotiveAnalyzer.kt` en `ml/` integrando la máscara de segmentación con OpenCV.
- [ ] **TODO-AUTO-04:** Implementación de la vista de Deflectometría / Cuadrícula AR en la cámara para inspección de granizo en vivo.
- [ ] **TODO-AUTO-05:** Extensión de la base de datos Room (`CelesticDatabase`) con entidades de vehículo: `VehicleInspection`, `VehiclePart`, `DentAnomaly`.
- [ ] **TODO-AUTO-06:** Actualización de los reportes PDF periciales con el diagrama despiezado del vehículo y el mapa de calor de daños.
