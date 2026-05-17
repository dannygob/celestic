package com.example.celestic.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import com.example.celestic.data.dao.CelesticDao
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.Inspection
import com.example.celestic.models.Specification
import com.example.celestic.models.SpecificationFeature
import com.example.celestic.models.calibration.CameraCalibrationData
import com.example.celestic.models.calibration.DetectedFeature
import com.example.celestic.models.enums.Orientation
import com.example.celestic.models.report.ReportConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionRepository @Inject constructor(
    private val dao: CelesticDao,
    @ApplicationContext private val context: Context
) {

    // ===== DETECTION ITEMS =====

    /** Inserts a detection item and returns its generated ID. */
    suspend fun insertDetection(item: DetectionItem): Long =
        dao.insertDetectionItem(item)

    /**
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? La interfaz de usuario actual no permite a los operarios borrar registros
     *   de defectos individuales del historial para mantener la inmutabilidad de los reportes.
     * - ¿Para qué se deja? Habilitará a los usuarios administradores borrar registros erróneos o falsos
     *   positivos del historial en el futuro.
     */
    suspend fun deleteDetection(item: DetectionItem) =
        dao.deleteDetectionItem(item)

    /** Retrieves all detection items ordered by timestamp. */
    fun getAllDetectionItems(): Flow<List<DetectionItem>> =
        dao.getAllDetectionItems()

    /** Retrieves unique batch numbers from the inspection history. */
    fun getUniqueBatches(): Flow<List<String>> =
        dao.getUniqueBatches()

    /**
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? La interfaz actual (como DetectionListScreen) muestra un historial plano
     *   de todas las detecciones de forma cronológica sin agruparlas por sesión de inspección física.
     * - ¿Para qué se deja? Para habilitar una futura vista Maestro-Detalle de "Sesiones de Inspección",
     *   permitiendo filtrar y mostrar únicamente los hallazgos de una sesión específica.
     */
    fun getDetectionItemsByInspection(inspectionId: Long): Flow<List<DetectionItem>> =
        dao.getDetectionItemsByInspection(inspectionId)

    /** Retrieves a detection item by its ID. */
    suspend fun getDetectionById(id: Long): DetectionItem? =
        dao.getDetectionItemById(id)

    // ===== DETECTED FEATURES =====

    /**
     * NOTA DE INGENIERÍA: Este bloque completo de persistencia de características se pospone ("para después").
     * - ¿Por qué se deja? La aplicación actualmente registra y consulta únicamente entidades de alto nivel
     *   (DetectionItem) en la base de datos local SQLite. No se requiere persistir las formas geométricas
     *   individuales o contornos detectados por OpenCV en tablas separadas.
     * - ¿Para qué se deja? Para permitir análisis avanzados y reconstrucción tridimensional de contornos
     *   en futuras integraciones con software CAD/CAM industrial.
     */
    suspend fun insertDetectedFeature(detection: DetectedFeature) {
        dao.insertDetectedFeature(detection)
    }

    /** Inserts a list of detected features. */
    suspend fun insertDetectedFeatures(detections: List<DetectedFeature>) {
        dao.insertDetectedFeatures(detections)
    }

    /** Retrieves all detected features ordered by timestamp. */
    fun getAllDetectedFeatures(): Flow<List<DetectedFeature>> =
        dao.getAllDetectedFeatures()

    /** Deletes all detected features from the database. */
    suspend fun clearDetectedFeatures() {
        dao.clearDetectedFeatures()
    }

    /** Retrieves all detected features associated with a detection item. */
    fun getFeaturesForDetection(detectionItemId: Long): Flow<List<DetectedFeature>> =
        dao.getFeaturesForDetection(detectionItemId)

    // ===== INSPECTIONS =====

    /**
     * Creates a new inspection with optional GPS coordinates.
     * Returns the generated inspection ID.
     */
    suspend fun startInspection(latitude: Double? = null, longitude: Double? = null): Long {
        val inspection = Inspection(
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )
        return dao.insertInspection(inspection)
    }

    /** Retrieves all inspections ordered by timestamp. */
    fun getAllInspections(): Flow<List<Inspection>> =
        dao.getAllInspections()

    /**
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? Actualmente no existe una vista detallada de los metadatos de una sesión
     *   de inspección individual (con coordenadas GPS u hora exacta) en la interfaz de usuario.
     * - ¿Para qué se deja? Para enriquecer la vista de auditoría en futuras fases del proyecto.
     */
    suspend fun getInspectionById(id: Long): Inspection? =
        dao.getInspectionById(id)

    // ===== SPECIFICATIONS =====

    /** Inserts a specification and returns its generated ID. */
    suspend fun insertSpecification(specification: Specification): Long {
        return dao.insertSpecification(specification)
    }

    /** Retrieves the most recently saved specification. */
    fun getLatestSpecification(): Flow<Specification?> =
        dao.getLatestSpecification()

    /**
     * NOTA DE INGENIERÍA: Este bloque de consulta de especificaciones se pospone ("para después").
     * - ¿Por qué se deja? Los planos patrón (Golden Samples) se cargan y validan estáticamente
     *   en memoria para la vista de análisis activo. No hay pantallas de gestión de catálogo de planos.
     * - ¿Para qué se deja? Para soportar un "Administrador de Especificaciones CAD/CAM" en la base de datos
     *   en una futura versión corporativa.
     */
    fun getAllSpecifications(): Flow<List<Specification>> =
        dao.getAllSpecifications()

    /** Retrieves a specification by its ID. */
    suspend fun getSpecificationById(id: Long): Specification? =
        dao.getSpecificationById(id)

    // ===== SPECIFICATION FEATURES =====

    /** Inserts a list of specification features. */
    suspend fun insertSpecificationFeatures(features: List<SpecificationFeature>) {
        dao.insertSpecificationFeatures(features)
    }

    /** Retrieves features for a specification filtered by face orientation. */
    fun getFeaturesBySpecificationAndFace(
        specId: Long,
        face: Orientation
    ): Flow<List<SpecificationFeature>> =
        dao.getFeaturesBySpecificationAndFace(specId, face)

    /**
     * NOTA DE INGENIERÍA: Este bloque de gestión de características de especificaciones se pospone ("para después").
     * - ¿Por qué se deja? Al igual que las especificaciones, las tolerancias de contornos
     *   están gestionadas en memoria estática por ahora.
     * - ¿Para qué se deja? Para permitir la creación, modificación y borrado dinámico de tolerancias de plano
     *   asociadas a especificaciones por parte de ingenieros de calidad en el futuro.
     */
    fun getAllFeaturesBySpecification(specId: Long): Flow<List<SpecificationFeature>> =
        dao.getAllFeaturesBySpecification(specId)

    /** Deletes all features linked to a specification. */
    suspend fun deleteFeaturesBySpecification(specId: Long) {
        dao.deleteFeaturesBySpecification(specId)
    }

    // ===== CAMERA CALIBRATION =====

    /** Inserts camera calibration data. */
    suspend fun insertCameraCalibrationData(cameraCalibrationData: CameraCalibrationData) {
        dao.insertCameraCalibrationData(cameraCalibrationData)
    }

    /** Retrieves the latest camera calibration data. */
    fun getCameraCalibrationData(): Flow<CameraCalibrationData?> =
        dao.getCameraCalibrationData()

    // ===== REPORT CONFIG =====

    /** Inserts a report configuration. */
    suspend fun insertReportConfig(reportConfig: ReportConfig) {
        dao.insertReportConfig(reportConfig)
    }

    /** Retrieves the latest report configuration. */
    fun getReportConfig(): Flow<ReportConfig?> =
        dao.getReportConfig()

    // ===== IMAGE MANAGEMENT =====

    /**
     * NOTA DE INGENIERÍA: Esta función se pospone ("para después").
     * - ¿Por qué se deja? La lógica de guardado y compresión de fotogramas capturados a JPG está
     *   codificada de forma inline o local en clases específicas de visualización (como CameraUtils y DashboardViewModel)
     *   para reducir acoplamiento con la capa de persistencia en SQLite.
     * - ¿Para qué se deja? Para centralizar y estandarizar toda la persistencia de archivos de imagen
     *   bajo un único punto de entrada unificado en futuras refactorizaciones del backend de la app.
     *
     * Saves a bitmap as a JPEG file inside the app's internal storage.
     * Returns the absolute file path or an empty string if saving fails.
     */
    fun saveImage(bitmap: Bitmap, filename: String): String {
        val dir = File(context.filesDir, "detection_images")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "$filename.jpg")

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // ===== TRANSACTIONAL OPERATIONS =====

    /**
     * NOTA DE INGENIERÍA: Esta transacción se pospone ("para después").
     * - ¿Por qué se deja? La inserción de detecciones se realiza en tiempo real a medida que
     *   se procesan los fotogramas de la cámara. No hay un flujo de "Guardar Inspección Completa"
     *   por lotes al finalizar el turno.
     * - ¿Para qué se deja? Para soportar flujos de trabajo "Offline-First", permitiendo guardar una sesión
     *   entera acumulada y sus detecciones en una única transacción atómica una vez restablecida la conexión.
     */
    suspend fun saveInspectionWithDetections(
        inspection: Inspection,
        detections: List<DetectionItem>
    ): Long {
        val inspectionId = dao.insertInspection(inspection)

        detections.forEach { detection ->
            val detectionWithInspection = detection.copy(inspectionId = inspectionId)
            dao.insertDetectionItem(detectionWithInspection)
        }

        return inspectionId
    }

    /**
     * Saves a specification and all its features.
     * Returns the generated specification ID.
     */
    suspend fun saveSpecificationWithFeatures(
        specification: Specification,
        features: List<SpecificationFeature>
    ): Long {
        val specId = dao.insertSpecification(specification)

        val featuresWithSpec = features.map { it.copy(specificationId = specId) }
        dao.insertSpecificationFeatures(featuresWithSpec)

        return specId
    }
}
