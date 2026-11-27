package com.example.gestionpsdm

import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestionpsdm.data.local.GastoEntity
import com.example.gestionpsdm.data.local.IngresoEntity
import com.example.gestionpsdm.data.local.PagoEntity
import com.example.gestionpsdm.data.local.ReservaEntity
import com.example.gestionpsdm.data.local.ZarorDatabase
import com.example.gestionpsdm.model.EstadoPago
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.repository.PagosRoomRepository
import com.example.gestionpsdm.repository.ReservasRoomRepository
import com.example.gestionpsdm.repository.UsuariosDemoRepository
import com.example.gestionpsdm.utils.HorariosReserva
import com.example.gestionpsdm.viewmodel.GestionUsuariosViewModel
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisponibilidadReservasTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun bloqueaLiberaYConservaLaDisponibilidadAlReabrir() = runBlocking {
        val nombre = "reservas-prueba-${UUID.randomUUID()}"
        val fecha = fechaFutura(Calendar.FRIDAY)
        try {
            Room.databaseBuilder(context, ZarorDatabase::class.java, nombre).build().usar { db ->
                crearEspacios(db)
                val repositorio = ReservasRoomRepository(db)

                assertFalse(repositorio.registrarReserva(1, 3, fecha, "10:00", "11:00"))
                assertFalse(repositorio.registrarReserva(1, 1, "01/01/2000", "10:00", "11:00"))
                assertTrue(repositorio.registrarReserva(1, 1, fecha, "23:00", "02:00"))
                val reserva = repositorio.obtenerPorUsuario(1).first().single()
                assertEquals(EstadoReserva.PENDIENTE, reserva.estado)

                val viernes = requireNotNull(HorariosReserva.convertir(fecha, "08:00"))
                val sabado = HorariosReserva.fecha(HorariosReserva.desplazarDia(viernes, 1))
                assertFalse(repositorio.registrarReserva(2, 1, sabado, "00:00", "01:00"))
                assertTrue(repositorio.registrarReserva(2, 2, sabado, "00:00", "01:00"))
                assertTrue(repositorio.registrarReserva(2, 1, fecha, "22:00", "23:00"))

                assertTrue(repositorio.cambiarEstado(reserva.id, EstadoReserva.APROBADA))
                assertEquals("PENDIENTE", db.pagoDao().buscar(reserva.id)?.estado)
                assertEquals(1, db.pagoDao().adjuntarComprobante(reserva.id, "comprobante"))
                assertFalse(repositorio.cambiarEstado(reserva.id, EstadoReserva.RECHAZADA))
                assertEquals(1, db.pagoDao().revisarComprobante(reserva.id, "RECHAZADO"))
                assertEquals(1, db.pagoDao().adjuntarComprobante(reserva.id, "reenvio"))
                assertEquals(1, db.pagoDao().revisarComprobante(reserva.id, "RECHAZADO"))
                assertFalse(repositorio.registrarReserva(2, 1, sabado, "01:00", "02:00"))
            }

            Room.databaseBuilder(context, ZarorDatabase::class.java, nombre).build().usar { db ->
                val repositorio = ReservasRoomRepository(db)
                val reserva = repositorio.obtenerPorUsuario(1).first().single()
                assertEquals(EstadoReserva.APROBADA, reserva.estado)
                val disponibilidad = repositorio.observarBloques(1, fecha, flowOf(Date()))
                assertFalse(disponibilidad.first().last().disponible)

                assertTrue(repositorio.cambiarEstado(reserva.id, EstadoReserva.RECHAZADA))
                assertEquals(0, db.pagoDao().adjuntarComprobante(reserva.id, "otro"))
                assertTrue(disponibilidad.first().last().disponible)
                assertTrue(repositorio.registrarReserva(2, 1, fecha, "23:00", "02:00"))
                val pendiente = repositorio.obtenerPorUsuario(2).first().first()
                assertFalse(disponibilidad.first().last().disponible)
                db.reservaDao().actualizarEstado(pendiente.id, EstadoReserva.CANCELADA.name)
                assertTrue(disponibilidad.first().last().disponible)
            }
        } finally {
            context.deleteDatabase(nombre)
        }
    }

    @Test
    fun dosSolicitudesConcurrentesSoloGuardanUna() = runBlocking {
        Room.inMemoryDatabaseBuilder(context, ZarorDatabase::class.java).build().usar { db ->
            crearEspacios(db)
            val fecha = fechaFutura(Calendar.MONDAY)
            val repositorio = ReservasRoomRepository(db)
            val resultados = withTimeout(10_000) {
                coroutineScope {
                    val primera = async(Dispatchers.IO) {
                        repositorio.registrarReserva(1, 1, fecha, "10:00", "12:00")
                    }
                    val segunda = async(Dispatchers.IO) {
                        ReservasRoomRepository(db).registrarReserva(2, 1, fecha, "11:00", "13:00")
                    }
                    listOf(primera.await(), segunda.await())
                }
            }
            assertEquals(1, resultados.count { it })
            assertEquals(1, repositorio.obtenerTodas().first().size)
        }
    }

    @Test
    fun solicitudesAntiguasSuperpuestasRequierenResolverElConflicto() = runBlocking {
        Room.inMemoryDatabaseBuilder(context, ZarorDatabase::class.java).build().usar { db ->
            crearEspacios(db)
            val fecha = fechaFutura(Calendar.MONDAY)
            val dao = db.reservaDao()
            val primera = dao.insertar(ReservaEntity(0, 1, 1, fecha, "10:30", "11:30", "PENDIENTE")).toInt()
            val segunda = dao.insertar(ReservaEntity(0, 2, 1, fecha, "11:00", "12:00", "PENDIENTE")).toInt()
            val repositorio = ReservasRoomRepository(db)

            assertFalse(repositorio.cambiarEstado(primera, EstadoReserva.APROBADA))
            assertFalse(repositorio.cambiarEstado(segunda, EstadoReserva.APROBADA))
            assertNull(db.pagoDao().buscar(primera))
            assertTrue(repositorio.cambiarEstado(segunda, EstadoReserva.RECHAZADA))
            assertTrue(repositorio.cambiarEstado(primera, EstadoReserva.APROBADA))

            val antiguaSinPago = dao.insertar(
                ReservaEntity(0, 2, 1, fecha, "12:00", "13:00", "APROBADA")
            ).toInt()
            assertTrue(antiguaSinPago in repositorio.reservasPendientesDePago().first())
            assertTrue(repositorio.cambiarEstado(antiguaSinPago, EstadoReserva.RECHAZADA))
        }
    }

    @Test
    fun usuarioCreadoSeResuelvePorIdEnElMismoProceso() = runBlocking {
        val correo = "residente-${UUID.randomUUID()}@zaror.cl"
        val contrasena = "Prueba1234!"
        val nombre = "Residente creado en administración"
        assertTrue(GestionUsuariosViewModel().crearUsuario(nombre, correo, contrasena, RolUsuario.RESIDENTE))
        val usuario = requireNotNull(UsuariosDemoRepository.iniciarSesion(correo, contrasena))
        assertTrue(usuario.id > 4)

        Room.inMemoryDatabaseBuilder(context, ZarorDatabase::class.java).build().usar { db ->
            crearEspacios(db)
            val repositorio = ReservasRoomRepository(db)
            val fecha = fechaFutura(Calendar.MONDAY)
            assertTrue(repositorio.registrarReserva(usuario.id, 1, fecha, "10:00", "11:00"))
            val reserva = repositorio.obtenerTodas().first().single()
            assertEquals(usuario.id, reserva.idUsuario)
            assertEquals(nombre, UsuariosDemoRepository.obtenerUsuarios().find { it.id == reserva.idUsuario }?.nombre)
            assertTrue(repositorio.cambiarEstado(reserva.id, EstadoReserva.APROBADA))
        }
    }

    @Test
    fun impagoLiberaHorasSinPermitirPagosNiAlterarMovimientos() = runBlocking {
        Room.inMemoryDatabaseBuilder(context, ZarorDatabase::class.java).build().usar { db ->
            crearEspacios(db)
            val reservas = ReservasRoomRepository(db)
            val pagos = PagosRoomRepository(db, context)
            val fecha = fechaFutura(Calendar.MONDAY)
            db.ingresoDao().insertarIngreso(IngresoEntity(descripcion = "Arriendo", monto = 1000, fecha = fecha))
            db.gastoDao().insertarGasto(GastoEntity(descripcion = "Limpieza", monto = 500, fecha = fecha, categoria = "LIMPIEZA"))
            val ingresosIniciales = db.ingresoDao().obtenerIngresos().first()
            val gastosIniciales = db.gastoDao().obtenerGastos().first()

            assertTrue(reservas.registrarReserva(1, 1, fecha, "10:00", "11:00"))
            val impaga = reservas.obtenerPorUsuario(1).first().single()
            assertTrue(reservas.cambiarEstado(impaga.id, EstadoReserva.APROBADA))
            assertTrue(reservas.cambiarEstado(impaga.id, EstadoReserva.RECHAZADA))
            assertEquals(EstadoReserva.RECHAZADA, reservas.obtenerPorUsuario(1).first().single().estado)
            assertTrue(reservas.registrarReserva(2, 1, fecha, "10:00", "11:00"))

            val archivo = File.createTempFile("comprobante-test-", ".jpg", context.cacheDir)
            try {
                archivo.writeText("Comprobante de prueba")
                assertFalse(pagos.guardarComprobante(impaga.id, Uri.fromFile(archivo)))
                assertNull(db.pagoDao().buscar(impaga.id)?.comprobante)
            } finally {
                archivo.delete()
            }
            assertFalse(pagos.revisarComprobante(impaga.id, EstadoPago.CONFIRMADO))

            val nueva = reservas.obtenerPorUsuario(2).first().single()
            assertTrue(reservas.cambiarEstado(nueva.id, EstadoReserva.APROBADA))
            assertEquals(1, db.pagoDao().adjuntarComprobante(nueva.id, "comprobante-test"))
            assertTrue(pagos.revisarComprobante(nueva.id, EstadoPago.CONFIRMADO))
            assertFalse(reservas.cambiarEstado(nueva.id, EstadoReserva.RECHAZADA))
            assertFalse(nueva.id in reservas.reservasPendientesDePago().first())
            assertFalse(reservas.registrarReserva(3, 1, fecha, "10:00", "11:00"))

            val cancelada = db.reservaDao().insertar(
                ReservaEntity(0, 3, 1, fecha, "12:00", "13:00", EstadoReserva.APROBADA.name)
            ).toInt()
            db.pagoDao().crearPendiente(PagoEntity(idReserva = cancelada))
            assertEquals(1, db.pagoDao().adjuntarComprobante(cancelada, "comprobante-test"))
            db.reservaDao().actualizarEstado(cancelada, EstadoReserva.CANCELADA.name)
            assertFalse(pagos.revisarComprobante(cancelada, EstadoPago.CONFIRMADO))
            assertTrue(reservas.registrarReserva(3, 1, fecha, "12:00", "13:00"))

            assertEquals(ingresosIniciales, db.ingresoDao().obtenerIngresos().first())
            assertEquals(gastosIniciales, db.gastoDao().obtenerGastos().first())
        }
    }

    private inline fun ZarorDatabase.usar(bloque: (ZarorDatabase) -> Unit) {
        try {
            bloque(this)
        } finally {
            close()
        }
    }

    private fun crearEspacios(db: ZarorDatabase) {
        db.openHelper.writableDatabase.execSQL(
            "INSERT INTO espacios(id, nombre, capacidadMaxima, habilitado) VALUES " +
                "(1, 'Quincho', 25, 1), (2, 'Piscina', 60, 1), (3, 'Sala', 40, 0)"
        )
    }

    private fun fechaFutura(dia: Int): String {
        val calendario = Calendar.getInstance()
        do {
            calendario.add(Calendar.DAY_OF_MONTH, 1)
        } while (calendario.get(Calendar.DAY_OF_WEEK) != dia)
        return HorariosReserva.fecha(calendario.time)
    }
}
