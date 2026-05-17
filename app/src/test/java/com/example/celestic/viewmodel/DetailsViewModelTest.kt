package com.example.celestic.viewmodel

import android.content.Context
import app.cash.turbine.test
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.TraceabilityItem
import com.example.celestic.utils.JsonLoader
import com.example.celestic.utils.Result
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DetailsViewModel
    private lateinit var repository: DetectionRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        context = mockk()
        mockkObject(JsonLoader)
        viewModel = DetailsViewModel(repository, context)
    }

    @After
    fun tearDown() {
        unmockkObject(JsonLoader)
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTraceability should emit Success when data is loaded successfully`() = runTest {
        val codigo = "123"
        val traceabilityItem = TraceabilityItem(codigo, "pieza", "operario", "fecha", "resultado")
        val lista = listOf(traceabilityItem)

        coEvery { JsonLoader.loadTraceabilityFromJson(context) } returns lista

        viewModel.traceabilityItem.test {
            viewModel.loadTraceability(codigo)
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Success(traceabilityItem), awaitItem())
        }
    }

    @Test
    fun `loadTraceability should emit Error when data Loading fails`() = runTest {
        val codigo = "123"
        val exception = Exception("Error Loading data")

        coEvery { JsonLoader.loadTraceabilityFromJson(context) } throws exception

        viewModel.traceabilityItem.test {
            viewModel.loadTraceability(codigo)
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Error(exception), awaitItem())
        }
    }
}
