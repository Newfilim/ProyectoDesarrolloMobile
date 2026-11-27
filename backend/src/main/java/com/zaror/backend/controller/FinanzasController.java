package com.zaror.backend.controller;

import com.zaror.backend.dto.Entradas.*;
import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.service.FinanzasService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class FinanzasController {
    private final FinanzasService finanzas;

    @GetMapping("/ingresos")
    public List<IngresoResponse> ingresos() { return finanzas.ingresos(); }

    @PostMapping("/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    public IngresoResponse registrarIngreso(@Valid @RequestBody NuevoIngreso entrada) {
        return finanzas.crearIngreso(entrada);
    }

    @GetMapping("/gastos")
    public List<GastoResponse> gastos() { return finanzas.gastos(); }

    @PostMapping("/gastos")
    @ResponseStatus(HttpStatus.CREATED)
    public GastoResponse registrarGasto(@Valid @RequestBody NuevoGasto entrada) {
        return finanzas.crearGasto(entrada);
    }

    @GetMapping("/finanzas/resumen")
    public ResumenFinancieroResponse resumen() { return finanzas.resumen(); }

    @GetMapping("/finanzas/gastos-por-categoria")
    public List<GastoPorCategoriaResponse> categorias() { return finanzas.porCategoria(); }
}
