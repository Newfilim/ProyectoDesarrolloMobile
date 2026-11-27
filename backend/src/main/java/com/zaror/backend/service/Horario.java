package com.zaror.backend.service;

import com.zaror.backend.config.Infraestructura;
import com.zaror.backend.exception.ErrorApi;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Horario {
    public record Bloque(Instant inicio, Instant fin) {}

    public List<Bloque> bloques(LocalDate fecha) {
        boolean extendido = fecha.getDayOfWeek() == DayOfWeek.FRIDAY || fecha.getDayOfWeek() == DayOfWeek.SATURDAY;
        int duracion = extendido ? 18 : 14;
        LocalDateTime apertura = fecha.atTime(8, 0);
        List<Bloque> bloques = new ArrayList<>();
        for (int hora = 0; hora < duracion; hora++) {
            LocalDateTime inicio = apertura.plusHours(hora);
            LocalDateTime fin = inicio.plusHours(1);
            var offsetsInicio = Infraestructura.SANTIAGO.getRules().getValidOffsets(inicio);
            var offsetsFin = Infraestructura.SANTIAGO.getRules().getValidOffsets(fin);
            if (offsetsInicio.size() != 1 || offsetsFin.size() != 1) continue;
            Instant desde = inicio.toInstant(offsetsInicio.getFirst());
            Instant hasta = fin.toInstant(offsetsFin.getFirst());
            if (Duration.between(desde, hasta).equals(Duration.ofHours(1))) bloques.add(new Bloque(desde, hasta));
        }
        return bloques;
    }

    public List<Bloque> rango(LocalDate fecha, Instant inicio, Instant fin) {
        List<Bloque> seleccion = new ArrayList<>();
        Instant siguiente = inicio;
        for (Bloque bloque : bloques(fecha)) {
            if (!bloque.inicio().equals(siguiente)) continue;
            seleccion.add(bloque);
            siguiente = bloque.fin();
            if (siguiente.equals(fin)) return seleccion;
        }
        throw ErrorApi.invalido("El rango debe contener bloques consecutivos de la jornada indicada.");
    }
}
