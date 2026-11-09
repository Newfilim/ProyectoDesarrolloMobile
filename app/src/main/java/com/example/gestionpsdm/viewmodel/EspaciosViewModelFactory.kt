package com.example.gestionpsdm.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpsdm.repository.EspaciosRoomRepository


class EspaciosViewModelFactory(
    private val repository: EspaciosRoomRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if(modelClass.isAssignableFrom(EspaciosViewModel::class.java)){
            return EspaciosViewModel(
                repository = repository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido"
        )
    }
}
