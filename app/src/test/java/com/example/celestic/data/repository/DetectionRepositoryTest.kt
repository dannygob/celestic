package com.example.celestic.data.repository

import app.cash.turbine.test
import com.example.celestic.data.dao.CelesticDao
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.Inspection
import com.example.celestic.models.calibration.CameraCalibrationData
import com.example.celestic.models.calibration.DetectedFeature
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import com.example.celestic.models.report.ReportConfig
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DetectionRepositoryTest {

    private lateinit var repository: DetectionRepository
    private lateinit var dao: CelesticDao

    @Before
    fun setUp() {
        dao = mockk()
        repository = DetectionRepository(dao)
    }

    @Test
    fun `getAll should return the list of detections from the dao`() = runTest {
        val detections = listOf(
            DetectionItem(
                id = 1,
                inspectionId = 1,
                frameId = "frame1",
                type = DetectionType.HOLE,
                boundingBox = BoundingBox(0f, 0f, 0f, 0f),
                confidence = 0.9f,
                status = DetectionStatus.OK,
                measurementMm = 10f,
                timestamp = 1234567890,
                linkedQrCode = "qr1",
                notes = "notes1"
            )
        )
        coEvery { dao.getAll() } returns flowOf(detections)

        repository.getAll().test {
            assertEquals(detections, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getFeaturesForDetection should return the list of features from the dao`() = runTest {
        val features = listOf(
            DetectedFeature(
                id = 1,
                detectionItemId = 1,
                featureType = "type",
                xCoord = 0f,
                yCoord = 0f,
                confidence = 0.9f,
                timestamp = 1234567890
            )
        )
        coEvery { dao.getFeaturesForDetection(1) } returns flowOf(features)

        repository.getFeaturesForDetection(1).test {
            assertEquals(features, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getCameraCalibrationData should return the camera calibration data from the dao`() = runTest {
        val cameraCalibrationData = CameraCalibrationData(
            id = 1,
            cameraMatrix = "matrix",
            distortionCoeffs = "coeffs",
            resolutionWidth = 1080,
            resolutionHeight = 1920,
            calibrationDate = "date"
        )
        coEvery { dao.getCameraCalibrationData() } returns flowOf(cameraCalibrationData)

        repository.getCameraCalibrationData().test {
            assertEquals(cameraCalibrationData, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getReportConfig should return the report config from the dao`() = runTest {
        val reportConfig = ReportConfig(
            id = 1,
            reportTitle = "title",
            includeMetadata = true,
            includeRawFeatures = true,
            exportFormat = "PDF",
            generationDate = "date",
            includeImages = true,
            outputFormat = "PDF",
            includeMeasurements = true
        )
        coEvery { dao.getReportConfig() } returns flowOf(reportConfig)

        repository.getReportConfig().test {
            assertEquals(reportConfig, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllInspections should return the list of inspections from the dao`() = runTest {
        val inspections = listOf(
            Inspection(
                id = 1,
                timestamp = 1234567890
            )
        )
        coEvery { dao.getAllInspections() } returns flowOf(inspections)

        repository.getAllInspections().test {
            assertEquals(inspections, awaitItem())
            awaitComplete()
        }
    }
}
