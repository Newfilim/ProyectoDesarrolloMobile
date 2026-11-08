
package com.example.gestionpsdm.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.gestionpsdm.model.Reserva
import com.example.gestionpsdm.model.EstadoReserva
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    fun agregarReserva(reserva: Reserva) {
        reservas.add(reserva)
    }

    private fun horaAMinutos(hora: String): Int {
        val partes = hora.split(":")
        val horas = partes[0].toInt()
        val minutos = partes[1].toInt()

        return horas * 60 + minutos
    }



    private fun hayConflicto(reservaNueva: Reserva): Boolean {

        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.forLanguageTag("es-CL")
        )

        formato.isLenient = false

        val inicioNuevo = formato.parse(
            "${reservaNueva.fecha} ${reservaNueva.horaInicio}"
        ) ?: return true

        var finNuevo = formato.parse(
            "${reservaNueva.fecha} ${reservaNueva.horaFin}"
        ) ?: return true

        // Si termina después de medianoche,
        // corresponde al día siguiente.
        if (!finNuevo.after(inicioNuevo)) {

            val calendario = Calendar.getInstance()
            calendario.time = finNuevo
            calendario.add(Calendar.DAY_OF_MONTH, 1)

            finNuevo = calendario.time
        }

        for (reservaExistente in reservas) {

            if (reservaExistente.id == reservaNueva.id) {
                continue
            }

            if (reservaExistente.estado != EstadoReserva.APROBADA) {
                continue
            }

            if (reservaExistente.idEspacio != reservaNueva.idEspacio) {
                continue
            }

            val inicioExistente = formato.parse(
                "${reservaExistente.fecha} ${reservaExistente.horaInicio}"
            ) ?: return true

            var finExistente = formato.parse(
                "${reservaExistente.fecha} ${reservaExistente.horaFin}"
            ) ?: return true

            if (!finExistente.after(inicioExistente)) {

                val calendario = Calendar.getInstance()
                calendario.time = finExistente
                calendario.add(Calendar.DAY_OF_MONTH, 1)

                finExistente = calendario.time
            }

            // Comprobar si ambos horarios se superponen.
            if (inicioNuevo.before(finExistente) &&
                inicioExistente.before(finNuevo)
            ) {
                return true
            }
        }

        return false
    }



    fun cambiarEstado(
        id: Int,
        nuevoEstado: EstadoReserva
    ): Boolean {

        for (i in reservas.indices) {

            val reservaActual = reservas[i]

            if (reservaActual.id == id) {

                // Solo se comprueba el horario al aprobar.
                if (nuevoEstado == EstadoReserva.APROBADA) {

                    if (hayConflicto(reservaActual)) {
                        return false
                    }
                }

                reservas[i] = reservaActual.copy(
                    estado = nuevoEstado
                )

                return true
            }
        }

        return false
    }
}
