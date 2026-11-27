package com.example.gestionpsdm.model

data class ReservaFormularioUiState(
    val fecha: String? = null,
    val bloques: List<BloqueHorario> = emptyList(),
    val seleccion: IntRange? = null,
    val cargando: Boolean = true
) {
    val rangoDisponible: Boolean
        get() {
            val rango = seleccion ?: return false
            return disponible(rango)
        }

    fun seleccionar(indice: Int): ReservaFormularioUiState {
        if (bloques.getOrNull(indice)?.disponible != true) return this
        val actual = seleccion
        if (actual == null) return copy(seleccion = indice..indice)
        if (indice in actual) return copy(seleccion = indice..indice)

        val nuevo = minOf(actual.first, indice)..maxOf(actual.last, indice)
        if (disponible(nuevo)) return copy(seleccion = nuevo)
        return this
    }

    private fun disponible(rango: IntRange): Boolean {
        for (indice in rango) {
            val bloque = bloques.getOrNull(indice) ?: return false
            if (!bloque.disponible) return false
            if (indice > rango.first && bloques[indice - 1].fin != bloque.inicio) return false
        }
        return !rango.isEmpty()
    }
}
