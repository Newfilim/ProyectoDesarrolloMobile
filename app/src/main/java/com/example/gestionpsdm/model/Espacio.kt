package com.example.gestionpsdm.model

data class Espacio(
    val id: Int,
    val nombre: String,
    val capacidadMaxima: Int,
    val habilitado: Boolean
) {

    fun puedeRecibirPersonas(cantidad: Int): Boolean{
        if (!habilitado){
            return false
        }
        if (cantidad <= 0){
            return false
        }
        if (cantidad > capacidadMaxima){
            return false
        }
        return true
    }
}