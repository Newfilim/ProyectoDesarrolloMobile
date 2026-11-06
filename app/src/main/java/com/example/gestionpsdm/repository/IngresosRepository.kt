
package com.example.gestionpsdm.repository

import androidx.compose.runtime.mutableStateListOf

data class Ingreso(
    val id: Int,
    val descripcion: String,
    val monto: Int,
    val fecha: String
)

object IngresosRepository {

    val ingresos = mutableStateListOf<Ingreso>()

    fun agregarIngreso(
        descripcion: String,
        monto: Int,
        fecha: String
    ): Boolean {

        if (descripcion.isBlank() || monto <= 0 || fecha.isBlank()) {
            return false
        }

        val ultimoId = ingresos.map { it.id }.maxOrNull() ?: 0

        val nuevoIngreso = Ingreso(
            id = ultimoId + 1,
            descripcion = descripcion.trim(),
            monto = monto,
            fecha = fecha
        )

        ingresos.add(nuevoIngreso)

        return true
    }

    fun obtenerTotalIngresos(): Long {
        return ingresos.sumOf { it.monto.toLong() }
    }
}
