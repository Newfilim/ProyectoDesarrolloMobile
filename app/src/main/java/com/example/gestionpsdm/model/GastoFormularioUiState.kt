package com.example.gestionpsdm.model

data class GastoFormularioUiState(
    val descripcion: String = "",
    val monto: String = "",
    val fecha: String = "",
    val categoria: CategoriaGasto? = null,
    val formularioValido: Boolean = false
)