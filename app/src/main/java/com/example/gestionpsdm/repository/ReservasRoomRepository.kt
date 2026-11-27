package com.example.gestionpsdm.repository

import androidx.room.withTransaction
import com.example.gestionpsdm.data.local.PagoEntity
import com.example.gestionpsdm.data.local.ReservaEntity
import com.example.gestionpsdm.data.local.ZarorDatabase
import com.example.gestionpsdm.data.local.toModel
import com.example.gestionpsdm.model.BloqueHorario
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.model.Reserva
import com.example.gestionpsdm.utils.HorariosReserva
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ReservasRoomRepository(
    private val database: ZarorDatabase
) {
    private val reservaDao = database.reservaDao()
    private val espacioDao = database.espacioDao()
    private val pagoDao = database.pagoDao()

    fun observarBloques(
        idEspacio: Int,
        fecha: String,
        reloj: Flow<Date>
    ): Flow<List<BloqueHorario>> {
        val bloques = HorariosReserva.bloques(fecha)
        return combine(
            reservaDao.observarVigentes(idEspacio),
            espacioDao.observarHabilitado(idEspacio),
            reloj
        ) { vigentes, habilitado, ahora ->
            bloques.map { bloque ->
                val disponible = habilitado == true &&
                    fechaOperativaVigente(fecha, ahora) &&
                    bloque.inicio.after(ahora) &&
                    !hayConflicto(bloque.inicio, bloque.fin, vigentes)
                bloque.copy(disponible = disponible)
            }
        }
    }

    private fun fechaOperativaVigente(fecha: String, ahora: Date): Boolean {
        val apertura = HorariosReserva.convertir(fecha, "08:00") ?: return false
        val aperturaHoy = HorariosReserva.convertir(HorariosReserva.fecha(ahora), "08:00")
            ?: return false
        return !apertura.before(aperturaHoy)
    }

    fun reservasPendientesDePago(): Flow<Set<Int>> {
        return pagoDao.observarReservasSinPago().map { ids -> ids.toSet() }
    }

    suspend fun cambiarEstado(idReserva: Int, nuevoEstado: EstadoReserva): Boolean {
        if (nuevoEstado != EstadoReserva.APROBADA && nuevoEstado != EstadoReserva.RECHAZADA) {
            return false
        }

        return database.withTransaction {
            val reserva = reservaDao.obtenerPorId(idReserva) ?: return@withTransaction false
            val pendiente = reserva.estado == EstadoReserva.PENDIENTE.name
            val rechazoPorImpago = reserva.estado == EstadoReserva.APROBADA.name &&
                nuevoEstado == EstadoReserva.RECHAZADA

            if (!pendiente && !rechazoPorImpago) return@withTransaction false

            if (nuevoEstado == EstadoReserva.APROBADA) {
                val inicio = HorariosReserva.convertir(reserva.fecha, reserva.horaInicio)
                    ?: return@withTransaction false
                val fin = HorariosReserva.termino(reserva.fecha, reserva.horaFin, inicio)
                    ?: return@withTransaction false
                val vigentes = reservaDao.obtenerVigentes(reserva.idEspacio)
                if (hayConflicto(inicio, fin, vigentes, idReserva)) return@withTransaction false
            }

            if (rechazoPorImpago) {
                // Las aprobaciones anteriores a la tabla de pagos pueden no tener un pago asociado.
                val pago = pagoDao.buscar(idReserva)
                if (pago != null && pagoDao.cerrarSinPago(idReserva) != 1) {
                    return@withTransaction false
                }
            }
            reservaDao.actualizarEstado(idReserva, nuevoEstado.name)

            if (nuevoEstado == EstadoReserva.APROBADA) {
                pagoDao.crearPendiente(PagoEntity(idReserva = idReserva))
            }
            true
        }
    }

    private fun hayConflicto(
        inicio: Date,
        fin: Date,
        vigentes: List<ReservaEntity>,
        excluirId: Int? = null
    ): Boolean {
        for (existente in vigentes) {
            if (existente.id == excluirId) continue
            val otroInicio = HorariosReserva.convertir(existente.fecha, existente.horaInicio)
                ?: return true
            val otroFin = HorariosReserva.termino(existente.fecha, existente.horaFin, otroInicio)
                ?: return true
            if (HorariosReserva.seSuperponen(inicio, fin, otroInicio, otroFin)) return true
        }
        return false
    }

    fun obtenerTodas(): Flow<List<Reserva>> {
        return reservaDao.obtenerTodas().map { entidades -> entidades.map { it.toModel() } }
    }

    fun obtenerPorUsuario(idUsuario: Int): Flow<List<Reserva>> {
        return reservaDao.obtenerPorUsuario(idUsuario).map { entidades -> entidades.map { it.toModel() } }
    }

    suspend fun registrarReserva(
        idUsuario: Int,
        idEspacio: Int,
        fecha: String,
        horaInicio: String,
        horaFin: String
    ): Boolean {
        if (idUsuario <= 0 || idEspacio <= 0) return false
        val inicio = HorariosReserva.convertir(fecha, horaInicio) ?: return false
        val fin = HorariosReserva.termino(fecha, horaFin, inicio) ?: return false
        if (!HorariosReserva.rangoPermitido(inicio, fin)) return false

        return database.withTransaction {
            if (espacioDao.estaHabilitado(idEspacio) != true) return@withTransaction false
            val vigentes = reservaDao.obtenerVigentes(idEspacio)
            if (!inicio.after(Date()) || hayConflicto(inicio, fin, vigentes)) {
                return@withTransaction false
            }

            reservaDao.insertar(
                ReservaEntity(
                    idUsuario = idUsuario,
                    idEspacio = idEspacio,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    estado = EstadoReserva.PENDIENTE.name
                )
            )
            true
        }
    }
}
