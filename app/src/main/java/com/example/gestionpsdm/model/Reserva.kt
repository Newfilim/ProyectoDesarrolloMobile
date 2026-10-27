package com.example.gestionpsdm.model

data class Reserva(
    val id:Int,
    val idUsuario: Int,
    val idEspacio: Int,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: EstadoReserva
) {
    fun estaPendiente(): Boolean{
        if(estado == EstadoReserva.PENDIENTE){
            return true
        }
        return false
    }

}