package com.example.celestic.viewmodel

import android.content.Context
import app.cash.turbine.test
import com.example.celestic.data.repository.DetectionRepository
import com.example.celestic.models.TrazabilidadItem
import com.example.celestic.utils.Result
import com.example.celestic.utils.cargarTrazabilidadDesdeJson
import io.mockk.coEvery
import io.mockk.mockk
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
        viewModel = DetailsViewModel(repository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTrazabilidad should emit Success when data is loaded successfully`() = runTest {
        val codigo = "123"
        val trazabilidadItem = TrazabilidadItem(codigo, "pieza", "operario", "fecha", "resultado")
        val lista = listOf(trazabilidadItem)

        coEvery { cargarTrazabilidadDesdeJson(context) } returns lista

        viewModel.trazabilidadItem.test {
            viewModel.loadTrazabilidad(codigo)
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Success(trazabilidadItem), awaitItem())
        }
    }

    @Test
    fun `loadTrazabilidad should emit Error when data loading fails`() = runTest {
        val codigo = "123"
        val exception = Exception("Error loading data")

        coEvery { cargarTrazabilidadDesdeJson(context) } throws exception

        viewModel.trazabilidadItem.test {
            viewModel.loadTrazabilidad(codigo)
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Error(exception), awaitItem())
        }
    }
}
