package com.example.gestionpsdm.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateMapOf
import com.example.gestionpsdm.model.EstadoPago


class ComprobantesViewModel : ViewModel() {

    val comprobantesPorReserva = mutableStateMapOf<Int, Uri>()

    val estadosPorReserva = mutableStateMapOf<Int, EstadoPago>()
    fun guardarComprobante(idReserva: Int, imagen: Uri) {

        val estadoActual = estadosPorReserva[idReserva]
            ?: EstadoPago.PENDIENTE

        if (estadoActual == EstadoPago.PENDIENTE || estadoActual == EstadoPago.RECHAZADO) {

            comprobantesPorReserva[idReserva] = imagen
            estadosPorReserva[idReserva] = EstadoPago.EN_REVISION

        }
    }

    fun actualizarEstadoPago(
        idReserva: Int,
        nuevoEstado: EstadoPago
    ){
        estadosPorReserva[idReserva] = nuevoEstado
    }

}

