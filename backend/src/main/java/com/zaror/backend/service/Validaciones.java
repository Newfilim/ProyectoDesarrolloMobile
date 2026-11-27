package com.zaror.backend.service;

import com.zaror.backend.exception.ErrorApi;
import jakarta.validation.constraints.Email;
import jakarta.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Validaciones {
    private final Validator validator;
    private record Correo(@Email String valor) {}

    public String correo(String dato) {
        String valor = texto(dato, 1, 254).toLowerCase(Locale.ROOT);
        if (valor.length() > 254 || !validator.validate(new Correo(valor)).isEmpty()) {
            throw ErrorApi.invalido("El correo no es válido.");
        }
        return valor;
    }

    public static boolean contrasenaValida(String dato) {
        return dato != null && !dato.isBlank() && dato.length() >= 8
                && dato.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public static String texto(String dato, int minimo, int maximo) {
        String valor = dato == null ? "" : dato.trim();
        if (valor.isBlank() || valor.length() < minimo || valor.length() > maximo) {
            throw ErrorApi.invalido("La longitud del texto no es válida.");
        }
        return valor;
    }

    public static ObjectId id(String valor) {
        if (valor == null || !ObjectId.isValid(valor)) throw ErrorApi.invalido("El identificador no es válido.");
        return new ObjectId(valor);
    }

    public static LocalDate fecha(String valor) {
        try {
            if (valor != null && valor.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return LocalDate.parse(valor);
        } catch (DateTimeParseException ignored) {
            // Responder con el mismo formato de error.
        }
        throw ErrorApi.invalido("La fecha debe ser válida y tener formato yyyy-MM-dd.");
    }

    public static Instant instante(String valor) {
        try {
            if (valor != null && valor.endsWith("Z")) return Instant.parse(valor);
        } catch (DateTimeParseException ignored) {
            // Responder con el mismo formato de error.
        }
        throw ErrorApi.invalido("El instante debe estar en formato ISO 8601 UTC.");
    }

    public static void sinCuerpo(HttpServletRequest solicitud) throws IOException {
        if (solicitud.getContentLengthLong() > 0 || solicitud.getInputStream().read() != -1) {
            throw ErrorApi.invalido("Esta operación no admite un cuerpo de solicitud.");
        }
    }
}
