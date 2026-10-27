package com.example.gestionpsdm.model

data class Pago(
    val id:Int,
    val idReserva: Int,
    val monto: Int,
    val fechaLimite: String,
    val estado: EstadoPago,
    val comprobante: String? = null
) {
    fun esMontoValido(): Boolean{
        if(monto <= 0){
            return false
        }
      return true
    }

    fun estaConfirmado(): Boolean{
        if(estado == EstadoPago.CONFIRMADO){
            return true
        }
        return false
    }

}