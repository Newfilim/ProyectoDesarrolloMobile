package com.example.gestionpsdm.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun crearPendiente(pago: PagoEntity): Long

    @Query("SELECT * FROM pagos ORDER BY idReserva DESC")
    fun obtenerTodos(): Flow<List<PagoEntity>>

    @Query("""
        SELECT reservas.id FROM reservas
        LEFT JOIN pagos ON pagos.idReserva = reservas.id
        WHERE reservas.estado = 'APROBADA'
        AND (pagos.estado IS NULL OR pagos.estado IN ('PENDIENTE', 'RECHAZADO'))
    """)
    fun observarReservasSinPago(): Flow<List<Int>>

    @Query("SELECT * FROM pagos WHERE idReserva = :idReserva LIMIT 1")
    suspend fun buscar(idReserva: Int): PagoEntity?

    @Query("""
        UPDATE pagos SET comprobante = :comprobante, estado = 'EN_REVISION'
        WHERE idReserva = :idReserva AND estado IN ('PENDIENTE', 'RECHAZADO')
        AND idReserva IN (SELECT id FROM reservas WHERE estado = 'APROBADA')
    """)
    suspend fun adjuntarComprobante(idReserva: Int, comprobante: String): Int

    @Query("""
        UPDATE pagos SET estado = :nuevoEstado
        WHERE idReserva = :idReserva AND estado = 'EN_REVISION'
        AND idReserva IN (SELECT id FROM reservas WHERE estado = 'APROBADA')
    """)
    suspend fun revisarComprobante(idReserva: Int, nuevoEstado: String): Int

    @Query("UPDATE pagos SET estado = 'RECHAZADO' WHERE idReserva = :idReserva AND estado IN ('PENDIENTE', 'RECHAZADO')")
    suspend fun cerrarSinPago(idReserva: Int): Int
}
