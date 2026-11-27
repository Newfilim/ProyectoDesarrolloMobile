package com.example.gestionpsdm

import com.example.gestionpsdm.model.ReservaFormularioUiState
import com.example.gestionpsdm.utils.HorariosReserva
import org.junit.Assert.*
import org.junit.Test

class HorariosReservaTest {
    @Test
    fun generaHorariosSegunElDia() {
        for (fecha in listOf("29/11/2026", "30/11/2026", "01/12/2026", "02/12/2026", "03/12/2026")) {
            val bloques = HorariosReserva.bloques(fecha)
            assertEquals(14, bloques.size)
            assertEquals("08:00", HorariosReserva.hora(bloques.first().inicio))
            assertEquals("22:00", HorariosReserva.hora(bloques.last().fin))
        }

        for (fecha in listOf("27/11/2026", "28/11/2026")) {
            val bloques = HorariosReserva.bloques(fecha)
            assertEquals(18, bloques.size)
            assertEquals("00:00", HorariosReserva.hora(bloques[16].inicio))
            assertEquals("02:00", HorariosReserva.hora(bloques.last().fin))
            assertNotEquals(fecha, HorariosReserva.fecha(bloques.last().inicio))
        }
    }

    @Test
    fun validaRangosDeUnaHoraYUsaElDiaRealAlCruzarMedianoche() {
        val fecha = "27/11/2026"
        val inicio = requireNotNull(HorariosReserva.convertir(fecha, "23:00"))
        val fin = requireNotNull(HorariosReserva.termino(fecha, "02:00", inicio))
        val medianoche = requireNotNull(HorariosReserva.convertir("28/11/2026", "00:00"))

        assertEquals("28/11/2026", HorariosReserva.fecha(fin))
        assertTrue(HorariosReserva.rangoPermitido(inicio, fin))
        assertTrue(HorariosReserva.rangoPermitido(medianoche, fin))
        assertTrue(HorariosReserva.seSuperponen(inicio, fin, medianoche, fin))
        val siguiente = requireNotNull(HorariosReserva.convertir("28/11/2026", "03:00"))
        assertFalse(HorariosReserva.seSuperponen(inicio, fin, fin, siguiente))
        assertFalse(HorariosReserva.rangoPermitido(inicio, siguiente))
        val finFraccionario = requireNotNull(HorariosReserva.termino(fecha, "23:30", inicio))
        assertFalse(HorariosReserva.rangoPermitido(inicio, finFraccionario))
    }

    @Test
    fun seleccionaSoloUnRangoContinuoDisponible() {
        val bloques = HorariosReserva.bloques("27/11/2026").mapIndexed { indice, bloque ->
            bloque.copy(disponible = indice != 3)
        }
        val formulario = ReservaFormularioUiState(bloques = bloques)

        assertSame(formulario, formulario.seleccionar(3))
        val uno = formulario.seleccionar(0)
        assertEquals(0..0, uno.seleccion)
        val tres = uno.seleccionar(2)
        assertEquals(0..2, tres.seleccion)
        assertTrue(tres.rangoDisponible)
        assertSame(tres, tres.seleccionar(3))
        assertSame(tres, tres.seleccionar(4))
        assertEquals(1..1, tres.seleccionar(1).seleccion)
        assertFalse(tres.copy(seleccion = null).rangoDisponible)
    }
}
