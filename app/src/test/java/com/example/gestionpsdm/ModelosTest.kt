package com.example.gestionpsdm

import com.example.gestionpsdm.model.Espacio
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.model.Reserva
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ModelosTest {

    @Test
    fun probarEspacio() {

        val quincho = Espacio(
            id = 1,
            nombre = "Quincho",
            capacidadMaxima = 25,
            habilitado = true
        )

        assertTrue(quincho.puedeRecibirPersonas(20))

        assertFalse(quincho.puedeRecibirPersonas(30))

        assertTrue(quincho.puedeRecibirPersonas(25))

        assertFalse(quincho.puedeRecibirPersonas(0))

        val quinchoDeshabilitado= Espacio(
            id = 2,
            nombre = "Quincho antiguo",
            capacidadMaxima = 25,
            habilitado = false
        )

        assertFalse(quinchoDeshabilitado.puedeRecibirPersonas(10))
    }

    @Test
    fun probarReserva(){

        val reserva1 = Reserva(
        id = 1,
            idUsuario = 1,
            idEspacio = 2,
            fecha = "2026-11-05",
            horaInicio = "15:00",
            horaFin = "19:00",
            estado = EstadoReserva.PENDIENTE
        )
        assertTrue(reserva1.estaPendiente())

        val reserva2 = Reserva(
            id = 2,
            idUsuario = 2,
            idEspacio = 3,
            fecha = "2026-11-05",
            horaInicio = "15:00",
            horaFin = "19:00",
            estado = EstadoReserva.APROBADA
        )
        assertFalse(reserva2.estaPendiente())

    }
}