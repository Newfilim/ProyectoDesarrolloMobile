
package com.example.gestionpsdm.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {

    @Insert
    suspend fun insertar(reserva: ReservaEntity): Long

    @Query("SELECT * FROM reservas ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<ReservaEntity>>

    @Query(
        "SELECT * FROM reservas " +
                "WHERE idUsuario = :idUsuario ORDER BY id DESC"
    )
    fun obtenerPorUsuario(
        idUsuario: Int
    ): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): ReservaEntity?

    @Query("UPDATE reservas SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(
        id: Int,
        nuevoEstado: String
    ): Int

    @Query("SELECT * FROM reservas WHERE idEspacio = :idEspacio AND estado IN ('PENDIENTE', 'APROBADA')")
    suspend fun obtenerVigentes(idEspacio: Int): List<ReservaEntity>

    @Query("SELECT * FROM reservas WHERE idEspacio = :idEspacio AND estado IN ('PENDIENTE', 'APROBADA')")
    fun observarVigentes(idEspacio: Int): Flow<List<ReservaEntity>>
}
