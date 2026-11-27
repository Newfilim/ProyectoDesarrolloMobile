package com.example.gestionpsdm.model

import java.util.Date

data class BloqueHorario(
    val inicio: Date,
    val fin: Date,
    val disponible: Boolean = false
)
