package com.zaror.backend;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mongodb.MongoException;
import com.zaror.backend.exception.DiagnosticoErrores;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosticoErroresTest {
    @Test
    void registraMetadatosSinIncluirMensajesNiTrazaCompletaDeLaExcepcion() {
        Logger logger = (Logger) LoggerFactory.getLogger(DiagnosticoErrores.class);
        ListAppender<ILoggingEvent> captura = new ListAppender<>();
        captura.start();
        logger.addAppender(captura);
        try {
            DiagnosticoErrores.registrar("actualizar MongoDB", new IllegalStateException("mensaje-sintetico-privado",
                    new MongoException(2, "contenido-sintetico-privado")));

            ILoggingEvent evento = captura.list.getFirst();
            String mensaje = evento.getFormattedMessage();
            assertTrue(mensaje.contains("actualizar MongoDB"));
            assertTrue(mensaje.contains("MongoException"));
            assertTrue(mensaje.contains("codigoMongo=2"));
            assertFalse(mensaje.contains("mensaje-sintetico-privado"));
            assertFalse(mensaje.contains("contenido-sintetico-privado"));
            assertNull(evento.getThrowableProxy());
        } finally {
            logger.detachAppender(captura);
            captura.stop();
        }
    }
}
