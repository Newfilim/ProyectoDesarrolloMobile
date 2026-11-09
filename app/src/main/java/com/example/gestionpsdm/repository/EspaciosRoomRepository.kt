package com.example.gestionpsdm.repository

import com.example.gestionpsdm.data.local.EspacioDao
import com.example.gestionpsdm.data.local.toModel
import com.example.gestionpsdm.model.Espacio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EspaciosRoomRepository(private val espacioDao: EspacioDao) {


    fun obtenerEspacios(): Flow<List<Espacio>> {

        return espacioDao.obtenerEspacios().map { entidades ->

            val listaEspacios = mutableListOf<Espacio>()

            for (entidad in entidades) {

                val espacio = entidad.toModel()

                listaEspacios.add(espacio)
            }

            listaEspacios
        }
    }

    suspend fun cambiarEstado(
        idEspacio: Int,
        nuevoEstado: Boolean
    ){
        espacioDao.cambiarEstado(idEspacio, nuevoEstado)
    }

}