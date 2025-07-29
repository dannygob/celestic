package com.example.celestic.viewmodel

import app.cash.turbine.test
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.DetectionItem
import com.example.celestic.models.enums.DetectionStatus
import com.example.celestic.models.enums.DetectionType
import com.example.celestic.models.geometry.BoundingBox
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel
    private lateinit var repository: DetectionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `detections should emit the list of detections from the repository`() = runTest {
        val detections = listOf(
            DetectionItem(
                id = 1,
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
        coEvery { repository.getAll() } returns flowOf(detections)

        viewModel.detections.test {
            assertEquals(detections, awaitItem())
        }
    }
}
