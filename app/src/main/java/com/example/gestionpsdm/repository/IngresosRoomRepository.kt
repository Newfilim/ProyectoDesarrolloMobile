
package com.example.gestionpsdm.repository

import com.example.gestionpsdm.data.local.IngresoDao
import com.example.gestionpsdm.data.local.IngresoEntity
import com.example.gestionpsdm.model.Ingreso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngresosRoomRepository(
    private val ingresoDao: IngresoDao
) {

    fun obtenerIngresos(): Flow<List<Ingreso>> {

        return ingresoDao.obtenerIngresos().map { entidades ->

            entidades.map { entidad ->
                Ingreso(
                    id = entidad.id,
                    descripcion = entidad.descripcion,
                    monto = entidad.monto,
                    fecha = entidad.fecha
                )
            }
        }
    }

    fun obtenerTotalIngresos(): Flow<Long> {
        return ingresoDao.obtenerTotalIngresos()
    }

    suspend fun agregarIngreso(
        descripcion: String,
        monto: Int,
        fecha: String
    ) {

        val nuevoIngreso = IngresoEntity(
            descripcion = descripcion,
            monto = monto,
            fecha = fecha
        )

        ingresoDao.insertarIngreso(nuevoIngreso)
    }
}
