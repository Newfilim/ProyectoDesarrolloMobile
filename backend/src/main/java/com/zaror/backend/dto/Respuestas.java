package com.zaror.backend.dto;

import com.zaror.backend.model.*;
import java.time.Instant;
import java.util.List;

public final class Respuestas {
    private Respuestas() {}

    public record UsuarioResponse(String id, String nombre, String correo, RolUsuario rol, boolean activo) {}
    public record LoginResponse(String token, String tipoToken, int expiraEn, UsuarioResponse usuario) {}
    public record EspacioResponse(String id, String nombre, int capacidadMaxima, boolean habilitado) {}
    public record BloqueHorarioResponse(Instant inicio, Instant fin, boolean disponible) {}
    public record DisponibilidadResponse(String espacioId, String fechaOperativa, boolean habilitado,
                                         List<BloqueHorarioResponse> bloques) {}
    public record PagoDetalle(EstadoPago estado, String comprobanteUrl) {}
    public record ReservaResponse(String id, String usuarioId, String nombreUsuario, String espacioId,
                                  String nombreEspacio, String fechaOperativa, Instant inicio, Instant fin,
                                  EstadoReserva estado, PagoDetalle pago) {}
    public record PagoResponse(String reservaId, String usuarioId, String nombreUsuario, String espacioId,
                               String nombreEspacio, String fechaOperativa, Instant inicio, Instant fin,
                               EstadoReserva estadoReserva, EstadoPago estadoPago, String comprobanteUrl) {}
    public record IngresoResponse(String id, String descripcion, long monto, String fecha) {}
    public record GastoResponse(String id, String descripcion, long monto, String fecha, CategoriaGasto categoria) {}
    public record ResumenFinancieroResponse(long ingresos, long gastos, long balance) {}
    public record GastoPorCategoriaResponse(CategoriaGasto categoria, long total) {}
    public record ErrorResponse(int estado, String codigo, String mensaje) {}
}
