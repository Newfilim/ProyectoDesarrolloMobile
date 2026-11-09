package com.example.gestionpsdm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "espacios")
data class EspacioEntity(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val capacidadMaxima: Int,
    val habilitado: Boolean
)