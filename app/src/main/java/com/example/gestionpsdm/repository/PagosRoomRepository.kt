package com.example.gestionpsdm.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.gestionpsdm.data.local.PagoEntity
import com.example.gestionpsdm.data.local.ZarorDatabase
import com.example.gestionpsdm.model.EstadoPago
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class PagosRoomRepository(
    private val database: ZarorDatabase,
    private val context: Context
) {
    private val pagoDao = database.pagoDao()

    fun obtenerTodos(): Flow<List<PagoEntity>> = pagoDao.obtenerTodos()

    suspend fun guardarComprobante(idReserva: Int, origen: Uri): Boolean {
        val pago = pagoDao.buscar(idReserva) ?: return false
        if (pago.estado != "PENDIENTE" && pago.estado != "RECHAZADO") return false

        // Copiar a almacenamiento privado: las URI temporales de galería
        // pueden perder permisos al reiniciar la aplicación.
        val archivo = withContext(Dispatchers.IO) {
            val carpeta = File(context.filesDir, "comprobantes")
            carpeta.mkdirs()
            val destino = File(carpeta, "reserva_${idReserva}_${System.nanoTime()}.jpg")
            try {
                val entrada = context.contentResolver.openInputStream(origen)
                    ?: return@withContext null
                entrada.use { input ->
                    destino.outputStream().use { output -> input.copyTo(output) }
                }
                destino
            } catch (e: Exception) {
                destino.delete()
                null
            }
        } ?: return false

        val actualizado = try {
            database.withTransaction {
                // El DAO solo acepta PENDIENTE o RECHAZADO.
                pagoDao.adjuntarComprobante(idReserva, Uri.fromFile(archivo).toString()) == 1
            }
        } catch (e: Exception) {
            false
        }
        if (!actualizado) archivo.delete()
        return actualizado
    }

    suspend fun revisarComprobante(idReserva: Int, nuevoEstado: EstadoPago): Boolean {
        if (nuevoEstado != EstadoPago.CONFIRMADO && nuevoEstado != EstadoPago.RECHAZADO) {
            return false
        }
        return pagoDao.revisarComprobante(idReserva, nuevoEstado.name) == 1
    }
}
