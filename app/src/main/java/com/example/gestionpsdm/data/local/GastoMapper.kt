
package com.example.gestionpsdm.data.local

import com.example.gestionpsdm.model.CategoriaGasto
import com.example.gestionpsdm.model.Gasto

fun Gasto.toEntity(): GastoEntity {
    return GastoEntity(
        id = id,
        descripcion = descripcion,
        monto = monto,
        fecha = fecha,
        categoria = categoria.name
    )
}

fun GastoEntity.toModel(): Gasto {
    return Gasto(
        id = id,
        descripcion = descripcion,
        monto = monto,
        fecha = fecha,
        categoria = CategoriaGasto.valueOf(categoria)
    )
}
