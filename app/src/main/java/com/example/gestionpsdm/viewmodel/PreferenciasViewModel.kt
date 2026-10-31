package com.example.gestionpsdm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpsdm.repository.PreferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PreferenciasViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = PreferenciasRepository(application)

    private val _soloHabilitados = MutableStateFlow(false)

    val soloHabilitados: StateFlow<Boolean> = _soloHabilitados

    init {
        viewModelScope.launch{
            repository.soloHabilitados.collect{
                valor ->
                _soloHabilitados.value = valor
            }
        }
    }

    fun cambiarSoloHabilitados(valor: Boolean){
        viewModelScope.launch{
            repository.guardarSoloHabilitados(valor)
            }
    }
}