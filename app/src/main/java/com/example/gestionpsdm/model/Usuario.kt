package com.example.gestionpsdm.model

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: RolUsuario,
    val activo: Boolean
) {

    fun puedeAdministrarEspacios(): Boolean {
        if (rol == RolUsuario.ADMINISTRADOR && activo) {
            return true
        }

        return false
    }

    fun puedeSolicitarReserva(): Boolean{
        if(rol == RolUsuario.RESIDENTE && activo){
            return true
        }
        return false
    }
}