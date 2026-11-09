
package com.example.gestionpsdm.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    // Registrar un gasto
    @Insert
    suspend fun insertarGasto(gasto: GastoEntity)

    // Registrar varios gastos ficticios
    @Insert
    suspend fun insertarGastos(gastos: List<GastoEntity>)

    // Obtener todos los gastos
    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun obtenerGastos(): Flow<List<GastoEntity>>

    // Obtener el total de gastos
    @Query("SELECT COALESCE(SUM(monto), 0) FROM gastos")
    fun obtenerTotalGastos(): Flow<Long>

    // Agrupar los gastos por categoría
    @Query(
        """
        SELECT categoria, SUM(monto) AS total
        FROM gastos
        GROUP BY categoria
        ORDER BY total DESC
        """
    )
    fun obtenerGastosPorCategoria(): Flow<List<GastoPorCategoria>>

    // Contar los gastos almacenados
    @Query("SELECT COUNT(*) FROM gastos")
    suspend fun contarGastos(): Int
}

data class GastoPorCategoria(
    val categoria: String,
    val total: Long
)
