
package com.example.gestionpsdm.repository

import androidx.room.withTransaction
import com.example.gestionpsdm.data.local.PagoEntity
import com.example.gestionpsdm.data.local.ReservaEntity
import com.example.gestionpsdm.data.local.ZarorDatabase
import com.example.gestionpsdm.data.local.toModel
import com.example.gestionpsdm.model.Reserva
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.gestionpsdm.model.EstadoReserva
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
class ReservasRoomRepository(
    private val database: ZarorDatabase
) {

    private val reservaDao = database.reservaDao()
    private val espacioDao = database.espacioDao()

    suspend fun cambiarEstado(
        idReserva: Int,
        nuevoEstado: EstadoReserva
    ): Boolean {

        if (
            nuevoEstado != EstadoReserva.APROBADA &&
            nuevoEstado != EstadoReserva.RECHAZADA
        ) {
            return false
        }

        return database.withTransaction {

            val reserva = reservaDao.obtenerPorId(idReserva)
                ?: return@withTransaction false

            if (reserva.estado != EstadoReserva.PENDIENTE.name) {
                return@withTransaction false
            }

            if (nuevoEstado == EstadoReserva.APROBADA) {

                val aprobadas = reservaDao.obtenerAprobadas()

                for (existente in aprobadas) {

                    if (existente.idEspacio != reserva.idEspacio) {
                        continue
                    }

                    if (hayConflicto(reserva, existente)) {
                        return@withTransaction false
                    }
                }
            }

            val filasActualizadas = reservaDao.actualizarEstado(
                idReserva,
                nuevoEstado.name
            )

            if (filasActualizadas != 1) {
                return@withTransaction false
            }
            if (nuevoEstado == EstadoReserva.APROBADA) {
                database.pagoDao().crearPendiente(PagoEntity(idReserva = idReserva))
            }
            true
        }
    }

    private fun convertirInicio(
        fecha: String,
        hora: String
    ): Date? {

        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.forLanguageTag("es-CL")
        )

        formato.isLenient = false

        return try {
            formato.parse("$fecha $hora")
        } catch (e: Exception) {
            null
        }
    }

    private fun convertirFin(
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Date? {

        val inicio = convertirInicio(fecha, horaInicio)
            ?: return null

        val fin = convertirInicio(fecha, horaFin)
            ?: return null

        if (fin.after(inicio)) {
            return fin
        }

        val calendario = Calendar.getInstance()
        calendario.time = fin
        calendario.add(Calendar.DAY_OF_MONTH, 1)

        return calendario.time
    }

    private fun hayConflicto(
        nueva: ReservaEntity,
        existente: ReservaEntity
    ): Boolean {

        val inicioNuevo = convertirInicio(
            nueva.fecha,
            nueva.horaInicio
        ) ?: return true

        val finNuevo = convertirFin(
            nueva.fecha,
            nueva.horaInicio,
            nueva.horaFin
        ) ?: return true

        val inicioExistente = convertirInicio(
            existente.fecha,
            existente.horaInicio
        ) ?: return true

        val finExistente = convertirFin(
            existente.fecha,
            existente.horaInicio,
            existente.horaFin
        ) ?: return true

        return inicioNuevo.before(finExistente) &&
                inicioExistente.before(finNuevo)
    }


    fun obtenerTodas(): Flow<List<Reserva>> {
        return reservaDao.obtenerTodas().map { entidades ->
            val lista = mutableListOf<Reserva>()

            for (entidad in entidades) {
                lista.add(entidad.toModel())
            }

            lista
        }
    }

    fun obtenerPorUsuario(idUsuario: Int): Flow<List<Reserva>> {
        return reservaDao.obtenerPorUsuario(idUsuario).map { entidades ->
            val lista = mutableListOf<Reserva>()

            for (entidad in entidades) {
                lista.add(entidad.toModel())
            }

            lista
        }
    }

    suspend fun registrarReserva(
        idUsuario: Int,
        idEspacio: Int,
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean {

        if (idUsuario <= 0 || idEspacio <= 0) {
            return false
        }

        return database.withTransaction {

            val habilitado = espacioDao.estaHabilitado(idEspacio)

            if (habilitado != true) {
                return@withTransaction false
            }

            val nuevaReserva = ReservaEntity(
                idUsuario = idUsuario,
                idEspacio = idEspacio,
                fecha = fecha,
                horaInicio = horaInicio,
                horaFin = horaFin,
                estado = "PENDIENTE"
            )

            reservaDao.insertar(nuevaReserva)
            true
        }
    }
}
