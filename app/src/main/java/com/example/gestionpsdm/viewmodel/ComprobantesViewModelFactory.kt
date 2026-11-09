package com.example.gestionpsdm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpsdm.repository.PagosRoomRepository

class ComprobantesViewModelFactory(
    private val repository: PagosRoomRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComprobantesViewModel::class.java)) {
            return ComprobantesViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
