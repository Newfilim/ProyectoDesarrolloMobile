
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.model.Reserva
import com.example.gestionpsdm.repository.ReservasRoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GestionReservasViewModel(
    private val repository: ReservasRoomRepository
) : ViewModel() {

    val reservas: StateFlow<List<Reserva>> =
        repository.obtenerTodas().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun cambiarEstado(
        idReserva: Int,
        nuevoEstado: EstadoReserva,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val resultado = try {
                repository.cambiarEstado(idReserva, nuevoEstado)
            } catch (e: Exception) {
                false
            }

            onResultado(resultado)
        }
    }
}
