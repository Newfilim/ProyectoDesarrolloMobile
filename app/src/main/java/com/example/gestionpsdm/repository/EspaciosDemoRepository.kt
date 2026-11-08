
package com.example.gestionpsdm.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.gestionpsdm.model.Espacio

object EspaciosDemoRepository {

    val espacios = mutableStateListOf(
        Espacio(1, "Quincho", 25, true),
        Espacio(2, "Sala multiuso", 40, false),
        Espacio(3, "Piscina", 60, true)
    )

    fun cambiarEstadoEspacio(
        id: Int,
        habilitado: Boolean
    ): Boolean {

        for (i in espacios.indices) {

            if (espacios[i].id == id) {

                espacios[i] = espacios[i].copy(
                    habilitado = habilitado
                )

                return true
            }
        }

        return false
    }
}
