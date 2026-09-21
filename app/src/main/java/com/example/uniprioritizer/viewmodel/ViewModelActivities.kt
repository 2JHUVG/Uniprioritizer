package com.example.uniprioritizer.viewmodel

import androidx.lifecycle.ViewModel
import com.example.uniprioritizer.data.actividadesMock
import com.example.uniprioritizer.model.Actividad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ViewModelActivities : ViewModel() {
    private val _actividades = MutableStateFlow(actividadesMock)
    val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

    fun agregar(nueva: Actividad) = _actividades.update { lista ->
        lista + nueva.copy(id = (lista.maxOfOrNull { it.id } ?: 0) + 1)
    }

    fun marcarCompletada(id: Int) = _actividades.update { lista ->
        lista.map { if (it.id == id) it.copy(completada = true) else it }
    }
}