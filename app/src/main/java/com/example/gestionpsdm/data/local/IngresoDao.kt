
package com.example.gestionpsdm.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {

    @Insert
    suspend fun insertarIngreso(ingreso: IngresoEntity)

    @Query("SELECT * FROM ingresos ORDER BY id DESC")
    fun obtenerIngresos(): Flow<List<IngresoEntity>>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM ingresos")
    fun obtenerTotalIngresos(): Flow<Long>
}
