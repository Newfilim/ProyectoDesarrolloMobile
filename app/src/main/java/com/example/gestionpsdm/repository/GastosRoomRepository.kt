
package com.example.gestionpsdm.repository

import com.example.gestionpsdm.data.local.GastoDao
import com.example.gestionpsdm.data.local.GastoEntity
import com.example.gestionpsdm.data.local.GastoPorCategoria
import com.example.gestionpsdm.data.local.toModel
import com.example.gestionpsdm.model.CategoriaGasto
import com.example.gestionpsdm.model.Gasto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GastosRoomRepository(
    private val gastoDao: GastoDao
) {

    // Observar todos los gastos almacenados en Room
    fun obtenerGastos(): Flow<List<Gasto>> {

        return gastoDao.obtenerGastos().map { entidades ->

            entidades.map { entidad ->
                entidad.toModel()
            }
        }
    }

    // Observar el total de gastos
    fun obtenerTotalGastos(): Flow<Long> {
        return gastoDao.obtenerTotalGastos()
    }

    // Obtener totales agrupados por categoría
    fun obtenerGastosPorCategoria():
            Flow<List<GastoPorCategoria>> {

        return gastoDao.obtenerGastosPorCategoria()
    }

    // Guardar un gasto nuevo en SQLite
    suspend fun agregarGasto(
        descripcion: String,
        monto: Int,
        fecha: String,
        categoria: CategoriaGasto
    ) {

        val nuevoGasto = GastoEntity(
            descripcion = descripcion,
            monto = monto,
            fecha = fecha,
            categoria = categoria.name
        )

        gastoDao.insertarGasto(nuevoGasto)
    }
}
