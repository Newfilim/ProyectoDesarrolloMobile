package com.zaror.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorApi extends RuntimeException {
    private final HttpStatus estado;
    private final String codigo;

    public ErrorApi(HttpStatus estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }

    public static ErrorApi invalido(String mensaje) {
        return new ErrorApi(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", mensaje);
    }

    public static ErrorApi noExiste() {
        return new ErrorApi(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "El recurso solicitado no existe.");
    }

    public static ErrorApi conflicto(String codigo, String mensaje) {
        return new ErrorApi(HttpStatus.CONFLICT, codigo, mensaje);
    }

    public static ErrorApi transicion() {
        return conflicto("TRANSICION_INVALIDA", "El estado actual no permite la operación.");
    }

    public static ErrorApi ocupado() {
        return conflicto("HORARIO_OCUPADO", "Uno o más horarios seleccionados ya no están disponibles.");
    }

    public static ErrorApi noAutenticado() {
        return new ErrorApi(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "La autenticación no es válida.");
    }

    public static ErrorApi noDisponible() {
        return new ErrorApi(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCIA_NO_DISPONIBLE",
                "No se pudo completar la operación con el almacenamiento.");
    }
}
