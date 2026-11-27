package com.zaror.backend.exception;

import com.mongodb.MongoException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagnosticoErrores {
    private DiagnosticoErrores() {}

    public static void registrar(String operacion, Throwable error) {
        Throwable causa = error;
        Integer codigoMongo = null;
        while (causa != null) {
            if (causa instanceof MongoException mongo) codigoMongo = mongo.getCode();
            if (causa.getCause() == null || causa.getCause() == causa) break;
            causa = causa.getCause();
        }
        StackTraceElement[] traza = causa.getStackTrace();
        String origen = traza.length == 0 ? "desconocido" : traza[0].toString();
        // Algunos mensajes de drivers incluyen valores del documento o rutas privadas.
        log.error("Error al {}. Tipo={}, origen={}, codigoMongo={}",
                operacion, causa.getClass().getSimpleName(), origen, codigoMongo);
    }
}
