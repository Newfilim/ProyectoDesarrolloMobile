
package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpsdm.repository.ReservasRoomRepository

class ReservaViewModelFactory(
    private val repository: ReservasRoomRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ReservaViewModel::class.java)) {
            return ReservaViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
