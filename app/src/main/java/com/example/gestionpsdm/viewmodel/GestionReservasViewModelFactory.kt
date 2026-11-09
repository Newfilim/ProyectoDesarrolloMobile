
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpsdm.repository.ReservasRoomRepository

class GestionReservasViewModelFactory(
    private val repository: ReservasRoomRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                GestionReservasViewModel::class.java
            )
        ) {
            return GestionReservasViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
