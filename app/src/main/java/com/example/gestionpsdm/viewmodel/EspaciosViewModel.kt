
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.model.Espacio
import com.example.gestionpsdm.repository.EspaciosRoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EspaciosViewModel(
    private val repository: EspaciosRoomRepository
) : ViewModel() {

    val espacios: StateFlow<List<Espacio>> =
        repository.obtenerEspacios().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun cambiarEstado(
        idEspacio: Int,
        nuevoEstado: Boolean
    ){
        viewModelScope.launch {
            repository.cambiarEstado(
                idEspacio, nuevoEstado
            )
        }
    }
}
