
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.repository.GastosRoomRepository
import com.example.gestionpsdm.repository.IngresosRoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.gestionpsdm.data.local.GastoPorCategoria
import com.example.gestionpsdm.repository.Ingreso
data class ResumenFinanciero(
    val ingresos: Long = 0L,
    val gastos: Long = 0L,
    val cargando: Boolean = true
) {
    val balance: Long
        get() = ingresos - gastos
}

class FinanzasViewModel(
    gastosRepository: GastosRoomRepository,
    ingresosRepository: IngresosRoomRepository
) : ViewModel() {

    val resumen: StateFlow<ResumenFinanciero> = combine(
        ingresosRepository.obtenerTotalIngresos(),
        gastosRepository.obtenerTotalGastos()
    ) { ingresos, gastos ->

        ResumenFinanciero(
            ingresos = ingresos,
            gastos = gastos,
            cargando = false
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResumenFinanciero()
    )


    val gastosPorCategoria: StateFlow<List<GastoPorCategoria>> =
        gastosRepository.obtenerGastosPorCategoria().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val ingresosRegistrados: StateFlow<List<Ingreso>> =
        ingresosRepository.obtenerIngresos().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


}
