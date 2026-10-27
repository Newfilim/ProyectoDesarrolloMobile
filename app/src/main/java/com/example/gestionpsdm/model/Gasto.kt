package com.example.gestionpsdm.model

data class Gasto(
    val id: Int,
    val descripcion: String,
    val monto: Int,
    val fecha: String,
    val categoria: CategoriaGasto
) {
    fun esMontoValido(): Boolean{
        if(monto <= 0){
            return false
        }
        return true
    }

    fun esGastoDeMejora(): Boolean{
        if(categoria == CategoriaGasto.MEJORAS){
            return true
        }
        return false
    }

}