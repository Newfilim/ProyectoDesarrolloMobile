package com.example.gestionpsdm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pagos")
data class PagoEntity(
    @PrimaryKey val idReserva: Int,
    val estado: String = "PENDIENTE",
    val comprobante: String? = null
)
