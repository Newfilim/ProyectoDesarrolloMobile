
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpsdm.repository.GastosRoomRepository
import com.example.gestionpsdm.repository.IngresosRoomRepository

class FinanzasViewModelFactory(
    private val gastosRepository: GastosRoomRepository,
    private val ingresosRepository: IngresosRoomRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(FinanzasViewModel::class.java)) {

            return FinanzasViewModel(
                gastosRepository = gastosRepository,
                ingresosRepository = ingresosRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido"
        )
    }
}
