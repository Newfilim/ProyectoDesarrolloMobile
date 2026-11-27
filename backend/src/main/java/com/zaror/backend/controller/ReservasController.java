package com.zaror.backend.controller;

import com.zaror.backend.dto.Entradas.*;
import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.service.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class ReservasController {
    private final EspacioService espacios;
    private final ReservaService reservas;

    @GetMapping("/espacios")
    public List<EspacioResponse> espacios() { return espacios.espacios(); }

    @GetMapping("/espacios/{id}")
    public EspacioResponse espacio(@PathVariable String id) { return espacios.espacio(Validaciones.id(id)); }

    @PatchMapping("/espacios/{id}/estado")
    public EspacioResponse cambiarEspacio(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
                                         @Valid @RequestBody EstadoEspacio entrada) {
        return espacios.estadoEspacio(Validaciones.id(jwt.getSubject()), Validaciones.id(id), entrada.habilitado());
    }

    @GetMapping("/espacios/{id}/disponibilidad")
    public DisponibilidadResponse disponibilidad(@PathVariable String id, @RequestParam String fecha) {
        return reservas.disponibilidad(Validaciones.id(id), fecha);
    }

    @PostMapping("/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NuevaReserva entrada) {
        return reservas.crear(Validaciones.id(jwt.getSubject()), entrada);
    }

    @GetMapping("/reservas/mis-reservas")
    public List<ReservaResponse> propias(@AuthenticationPrincipal Jwt jwt) {
        return reservas.listar(Validaciones.id(jwt.getSubject()));
    }

    @GetMapping("/reservas")
    public List<ReservaResponse> listar() { return reservas.listar(null); }

    @GetMapping("/reservas/{id}")
    public ReservaResponse detalle(@PathVariable String id, java.security.Principal principal) {
        return reservas.detalle(Validaciones.id(id), propietario(principal));
    }

    @PatchMapping("/reservas/{id}/aprobar")
    public ReservaResponse aprobar(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
                                    HttpServletRequest solicitud) throws IOException {
        Validaciones.sinCuerpo(solicitud);
        return reservas.aprobar(Validaciones.id(jwt.getSubject()), Validaciones.id(id));
    }

    @PatchMapping("/reservas/{id}/rechazar")
    public ReservaResponse rechazar(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
                                    HttpServletRequest solicitud) throws IOException {
        Validaciones.sinCuerpo(solicitud);
        return reservas.rechazar(Validaciones.id(jwt.getSubject()), Validaciones.id(id), false);
    }

    @PatchMapping("/reservas/{id}/rechazar-impago")
    public ReservaResponse impago(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
                                  HttpServletRequest solicitud) throws IOException {
        Validaciones.sinCuerpo(solicitud);
        return reservas.rechazar(Validaciones.id(jwt.getSubject()), Validaciones.id(id), true);
    }

    static ObjectId propietario(java.security.Principal principal) {
        var autenticacion = (org.springframework.security.core.Authentication) principal;
        for (var permiso : autenticacion.getAuthorities()) {
            if (permiso.getAuthority().equals("ROLE_RESIDENTE")) return Validaciones.id(autenticacion.getName());
        }
        return null;
    }
}
