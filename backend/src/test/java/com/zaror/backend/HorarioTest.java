package com.zaror.backend;

import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.service.Horario;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HorarioTest {
    private final Horario horario = new Horario();

    @Test
    void bloquesOrdinariosYExtendidos() {
        assertEquals(14, horario.bloques(LocalDate.parse("2026-12-17")).size());
        for (String fecha : new String[]{"2026-12-18", "2026-12-19"}) {
            var bloques = horario.bloques(LocalDate.parse(fecha));
            assertEquals(18, bloques.size());
            var ultimo = bloques.getLast();
            assertEquals(2, ultimo.fin().atZone(ZoneId.of("America/Santiago")).getHour());
            assertEquals(3, horario.rango(LocalDate.parse(fecha), bloques.get(15).inicio(), ultimo.fin()).size());
            assertThrows(ErrorApi.class, () -> horario.rango(LocalDate.parse(fecha), ultimo.inicio().plusSeconds(1), ultimo.fin()));
        }
    }

    @Test
    void excluyeLimitesNoValidosEnCambiosDeHora() {
        for (String fecha : new String[]{"2026-04-04", "2026-09-05"}) {
            var bloques = horario.bloques(LocalDate.parse(fecha));
            assertTrue(bloques.size() < 18);
            for (var bloque : bloques) assertEquals(Duration.ofHours(1), Duration.between(bloque.inicio(), bloque.fin()));
        }
    }
}
