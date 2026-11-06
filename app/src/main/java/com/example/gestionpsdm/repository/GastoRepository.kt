
package com.example.gestionpsdm.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.gestionpsdm.model.Gasto

object GastosRepository {

    val gastos = mutableStateListOf<Gasto>()

    fun agregarGasto(gasto: Gasto) {
        gastos.add(gasto)
    }

    fun obtenerTotalGastos(): Long {
        return gastos.sumOf { it.monto.toLong() }
    }
}
