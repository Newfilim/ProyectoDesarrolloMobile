package com.example.gestionpsdm.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EspacioDao {

    @Query("SELECT * FROM espacios ORDER BY id ASC")
    fun obtenerEspacios(): Flow<List<EspacioEntity>>

    @Query("UPDATE espacios SET habilitado = :nuevoEstado WHERE id = :idEspacio")
    suspend fun cambiarEstado(
        idEspacio: Int,
        nuevoEstado: Boolean
    )

    @Query(
        "SELECT habilitado FROM espacios WHERE id = :idEspacio LIMIT 1"
    )
    suspend fun estaHabilitado(idEspacio: Int): Boolean?
}