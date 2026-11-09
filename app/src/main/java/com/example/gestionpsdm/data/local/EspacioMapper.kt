package com.example.gestionpsdm.data.local

import com.example.gestionpsdm.model.Espacio

fun Espacio.toEntity(): EspacioEntity {
    return EspacioEntity(
        id = id,
        nombre = nombre,
        capacidadMaxima = capacidadMaxima,
        habilitado = habilitado
    )
}
    fun EspacioEntity.toModel(): Espacio {
        return Espacio(
            id = id,
            nombre = nombre,
            capacidadMaxima = capacidadMaxima,
            habilitado = habilitado
        )
    }
