package com.example.gestionpsdm.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.model.EstadoPago
import com.example.gestionpsdm.repository.PagosRoomRepository
import kotlinx.coroutines.launch

class ComprobantesViewModel(
    private val repository: PagosRoomRepository
) : ViewModel() {

    val comprobantesPorReserva = mutableStateMapOf<Int, Uri>()
    val estadosPorReserva = mutableStateMapOf<Int, EstadoPago>()

    init {
        viewModelScope.launch {
            repository.obtenerTodos().collect { pagos ->
                comprobantesPorReserva.clear()
                estadosPorReserva.clear()
                for (pago in pagos) {
                    estadosPorReserva[pago.idReserva] = EstadoPago.valueOf(pago.estado)
                    if (!pago.comprobante.isNullOrBlank()) {
                        comprobantesPorReserva[pago.idReserva] = Uri.parse(pago.comprobante)
                    }
                }
            }
        }
    }

    fun guardarComprobante(
        idReserva: Int,
        imagen: Uri,
        onResultado: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val resultado = try {
                repository.guardarComprobante(idReserva, imagen)
            } catch (e: Exception) {
                false
            }
            onResultado(resultado)
        }
    }

    fun actualizarEstadoPago(
        idReserva: Int,
        nuevoEstado: EstadoPago,
        onResultado: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val resultado = try {
                repository.revisarComprobante(idReserva, nuevoEstado)
            } catch (e: Exception) {
                false
            }
            onResultado(resultado)
        }
    }
}
