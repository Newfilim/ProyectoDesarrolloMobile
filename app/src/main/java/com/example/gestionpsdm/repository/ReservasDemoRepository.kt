package com.example.gestionpsdm.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.gestionpsdm.model.Reserva
import com.example.gestionpsdm.model.EstadoReserva


object ReservasDemoRepository {

    val reservas = mutableStateListOf(
        Reserva(
            id = 1,
            idUsuario = 1,
            idEspacio = 1,
            fecha = "06/11/2026",
            horaInicio = "19:00",
            horaFin = "22:00",
            estado = EstadoReserva.PENDIENTE
        ),
        Reserva(
            id = 2,
            idUsuario = 1,
            idEspacio = 2,
            fecha = "07/11/2026",
            horaInicio = "10:00",
            horaFin = "14:00",
            estado = EstadoReserva.APROBADA
        )
    )

    fun agregarReserva(reserva : Reserva){
        reservas.add(reserva)
    }

    fun cambiarEstado(id: Int, nuevoEstado: EstadoReserva){
        for(i in reservas.indices) {
            if(reservas[i].id == id){
                val reservaActual = reservas[i]

                reservas[i] = reservaActual.copy(
                    estado = nuevoEstado

                )
                return
            }
        }
    }


}