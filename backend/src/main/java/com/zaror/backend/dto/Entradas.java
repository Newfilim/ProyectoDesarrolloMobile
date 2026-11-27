package com.zaror.backend.dto;

import com.zaror.backend.model.CategoriaGasto;
import com.zaror.backend.model.RolUsuario;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class Entradas {
    private Entradas() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        @NotBlank private String correo;
        @NotBlank private String contrasena;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AltaUsuario {
        @NotBlank private String nombre;
        @NotBlank private String correo;
        @NotBlank private String contrasena;
        @NotNull private RolUsuario rol;
    }

    public record EdicionUsuario(@NotBlank String nombre, @NotBlank String correo) {}
    public record EstadoUsuario(@NotNull Boolean activo) {}
    public record EstadoEspacio(@NotNull Boolean habilitado) {}
    public record NuevaReserva(@NotBlank String espacioId, @NotBlank String fechaOperativa,
                               @NotBlank String inicio, @NotBlank String fin) {}
    public record NuevoIngreso(@NotBlank String descripcion, @NotNull @Positive Long monto, @NotBlank String fecha) {}
    public record NuevoGasto(@NotBlank String descripcion, @NotNull @Positive Long monto, @NotBlank String fecha,
                             @NotNull CategoriaGasto categoria) {}
}
