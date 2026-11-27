package com.zaror.backend.service;

import com.zaror.backend.config.Infraestructura;
import com.zaror.backend.dto.Entradas.*;
import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.model.*;
import com.zaror.backend.repository.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinanzasService {
    private final IngresoRepository ingresos;
    private final GastoRepository gastos;
    private final TotalesFinancierosRepository totales;
    private final Clock reloj;

    public List<IngresoResponse> ingresos() {
        List<IngresoResponse> respuesta = new ArrayList<>();
        for (Ingreso i : ingresos.findAllByOrderByFechaDescIdAsc()) {
            respuesta.add(respuesta(i));
        }
        return respuesta;
    }

    public List<GastoResponse> gastos() {
        List<GastoResponse> respuesta = new ArrayList<>();
        for (Gasto g : gastos.findAllByOrderByFechaDescIdAsc()) {
            respuesta.add(respuesta(g));
        }
        return respuesta;
    }

    public IngresoResponse crearIngreso(NuevoIngreso entrada) {
        String descripcion = Validaciones.texto(entrada.descripcion(), 1, 250);
        validarFecha(entrada.fecha());
        return respuesta(ingresos.insert(new Ingreso(new ObjectId(), descripcion, entrada.monto(), entrada.fecha())));
    }

    public GastoResponse crearGasto(NuevoGasto entrada) {
        String descripcion = Validaciones.texto(entrada.descripcion(), 5, 250);
        validarFecha(entrada.fecha());
        return respuesta(gastos.insert(new Gasto(new ObjectId(), descripcion, entrada.monto(), entrada.fecha(), entrada.categoria())));
    }

    private void validarFecha(String fecha) {
        if (Validaciones.fecha(fecha).isAfter(LocalDate.ofInstant(reloj.instant(), Infraestructura.SANTIAGO))) {
            throw ErrorApi.invalido("La fecha contable no puede ser futura.");
        }
    }

    public ResumenFinancieroResponse resumen() {
        long ingresos = totales.ingresos();
        long gastos = totales.gastos();
        return new ResumenFinancieroResponse(ingresos, gastos, Math.subtractExact(ingresos, gastos));
    }

    public List<GastoPorCategoriaResponse> porCategoria() {
        Map<CategoriaGasto, Long> montos = totales.gastosPorCategoria();
        List<GastoPorCategoriaResponse> respuesta = new ArrayList<>();
        for (CategoriaGasto categoria : CategoriaGasto.values()) {
            if (montos.containsKey(categoria)) respuesta.add(new GastoPorCategoriaResponse(categoria, montos.get(categoria)));
        }
        return respuesta;
    }

    private static IngresoResponse respuesta(Ingreso i) {
        return new IngresoResponse(i.getId().toHexString(), i.getDescripcion(), i.getMonto(), i.getFecha());
    }

    private static GastoResponse respuesta(Gasto g) {
        return new GastoResponse(g.getId().toHexString(), g.getDescripcion(), g.getMonto(), g.getFecha(), g.getCategoria());
    }
}
