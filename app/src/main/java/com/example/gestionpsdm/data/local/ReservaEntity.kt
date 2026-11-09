
package com.example.gestionpsdm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservas")
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idUsuario: Int,
    val idEspacio: Int,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: String
)
