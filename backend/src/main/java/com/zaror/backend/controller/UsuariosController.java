package com.zaror.backend.controller;

import com.zaror.backend.dto.Entradas.*;
import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.service.AuthService;
import com.zaror.backend.service.UsuarioService;
import com.zaror.backend.service.Validaciones;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsuariosController {
    private final UsuarioService servicio;
    private final AuthService auth;

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody Login entrada) { return auth.login(entrada); }

    @GetMapping("/auth/me")
    @SecurityRequirement(name = "bearer")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) { return servicio.usuario(Validaciones.id(jwt.getSubject())); }

    @GetMapping("/usuarios")
    @SecurityRequirement(name = "bearer")
    public List<UsuarioResponse> usuarios() { return servicio.usuarios(); }

    @GetMapping("/usuarios/{id}")
    @SecurityRequirement(name = "bearer")
    public UsuarioResponse usuario(@PathVariable String id) { return servicio.usuario(Validaciones.id(id)); }

    @PostMapping("/usuarios")
    @SecurityRequirement(name = "bearer")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AltaUsuario entrada) {
        return servicio.crear(Validaciones.id(jwt.getSubject()), entrada);
    }

    @PutMapping("/usuarios/{id}")
    @SecurityRequirement(name = "bearer")
    public UsuarioResponse editar(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
                                  @Valid @RequestBody EdicionUsuario entrada) {
        return servicio.editar(Validaciones.id(jwt.getSubject()), Validaciones.id(id), entrada);
    }

    @PatchMapping("/usuarios/{id}/estado")
    @SecurityRequirement(name = "bearer")
    public UsuarioResponse cambiarEstado(@AuthenticationPrincipal Jwt jwt, @PathVariable String id,
                                        @Valid @RequestBody EstadoUsuario entrada) {
        return servicio.estadoUsuario(Validaciones.id(jwt.getSubject()), Validaciones.id(id), entrada.activo());
    }
}
