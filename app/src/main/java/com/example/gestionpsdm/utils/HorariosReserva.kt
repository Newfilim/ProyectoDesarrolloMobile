package com.example.gestionpsdm.utils

import com.example.gestionpsdm.model.BloqueHorario
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object HorariosReserva {
    private fun formato(patron: String) = SimpleDateFormat(patron, Locale.ROOT).apply {
        isLenient = false
    }

    fun fecha(instante: Date): String = formato("dd/MM/yyyy").format(instante)

    fun hora(instante: Date): String = formato("HH:mm").format(instante)

    fun convertir(fecha: String, hora: String): Date? {
        val texto = "$fecha $hora"
        val formato = formato("dd/MM/yyyy HH:mm")
        return try {
            val instante = formato.parse(texto)
            if (instante != null && formato.format(instante) == texto) instante else null
        } catch (e: java.text.ParseException) {
            null
        }
    }

    fun desplazarDia(instante: Date, dias: Int): Date {
        val calendario = Calendar.getInstance()
        calendario.time = instante
        calendario.add(Calendar.DAY_OF_MONTH, dias)
        return calendario.time
    }

    fun termino(fecha: String, horaFin: String, inicio: Date): Date? {
        val diaFin = if (horaFin <= hora(inicio)) fecha(desplazarDia(inicio, 1)) else fecha
        return convertir(diaFin, horaFin)
    }

    fun bloques(fechaOperativa: String): List<BloqueHorario> {
        val apertura = convertir(fechaOperativa, "08:00") ?: return emptyList()
        val calendario = Calendar.getInstance()
        calendario.time = apertura
        val dia = calendario.get(Calendar.DAY_OF_WEEK)
        val cierre = if (dia == Calendar.FRIDAY || dia == Calendar.SATURDAY) 26 else 22
        val fechaSiguiente = fecha(desplazarDia(apertura, 1))
        val bloques = mutableListOf<BloqueHorario>()

        for (hora in 8 until cierre) {
            val inicio = limite(fechaOperativa, fechaSiguiente, hora) ?: continue
            val fin = limite(fechaOperativa, fechaSiguiente, hora + 1) ?: continue
            if (fin.time - inicio.time == 3_600_000L) bloques.add(BloqueHorario(inicio, fin))
        }
        return bloques
    }

    private fun limite(fecha: String, fechaSiguiente: String, hora: Int): Date? {
        val dia = if (hora < 24) fecha else fechaSiguiente
        val textoHora = (hora % 24).toString().padStart(2, '0')
        return convertir(dia, "$textoHora:00")
    }

    fun rangoPermitido(inicio: Date, fin: Date): Boolean {
        // Una reserva que empieza después de medianoche pertenece al turno anterior.
        val diaOperativo = if (hora(inicio) < "08:00") desplazarDia(inicio, -1) else inicio
        var extremo = inicio
        for (bloque in bloques(fecha(diaOperativo))) {
            if (bloque.inicio == extremo) {
                extremo = bloque.fin
                if (extremo == fin) return true
            }
        }
        return false
    }

    fun seSuperponen(inicio: Date, fin: Date, otroInicio: Date, otroFin: Date): Boolean {
        return inicio.before(otroFin) && otroInicio.before(fin)
    }
}
