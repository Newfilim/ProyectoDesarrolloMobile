package com.example.gestionpsdm.data.local

import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.model.Reserva

fun ReservaEntity.toModel(): Reserva {
    return Reserva(
        id = id,
        idUsuario = idUsuario,
        idEspacio = idEspacio,
        fecha = fecha,
        horaInicio = horaInicio,
        horaFin = horaFin,
        estado = EstadoReserva.valueOf(estado)
    )
}

fun Reserva.toEntity(): ReservaEntity {
    return ReservaEntity(
        id = id,
        idUsuario = idUsuario,
        idEspacio = idEspacio,
        fecha = fecha,
        horaInicio = horaInicio,
        horaFin = horaFin,
        estado = estado.name
    )
}