package com.example.celestic.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celestic.models.TrazabilidadItem
import com.example.celestic.utils.buscarPorCodigo
import com.example.celestic.utils.cargarTrazabilidadDesdeJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(private val context: Context) : ViewModel() {

    private val _trazabilidadItem = MutableStateFlow<TrazabilidadItem?>(null)
    val trazabilidadItem: StateFlow<TrazabilidadItem?> = _trazabilidadItem

    fun loadTrazabilidad(codigo: String) {
        viewModelScope.launch {
            val lista = cargarTrazabilidadDesdeJson(context)
            _trazabilidadItem.value = buscarPorCodigo(codigo, lista)
        }
    }
}
