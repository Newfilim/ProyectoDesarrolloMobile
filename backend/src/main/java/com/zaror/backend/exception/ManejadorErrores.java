package com.zaror.backend.exception;

import com.mongodb.MongoException;
import com.zaror.backend.dto.Respuestas.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

@RestControllerAdvice
public class ManejadorErrores {
    @ExceptionHandler(ErrorApi.class)
    public ResponseEntity<ErrorResponse> negocio(ErrorApi error) {
        return ResponseEntity.status(error.getEstado())
                .body(new ErrorResponse(error.getEstado().value(), error.getCodigo(), error.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
            ServletRequestBindingException.class, MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ErrorResponse> invalido() {
        return negocio(ErrorApi.invalido("Los datos de la solicitud no son válidos."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> grande() {
        return negocio(new ErrorApi(HttpStatus.PAYLOAD_TOO_LARGE, "ARCHIVO_GRANDE", "El archivo supera 10 MiB."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tipo() {
        return negocio(new ErrorApi(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "FORMATO_NO_ADMITIDO", "El formato no es admitido."));
    }

    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> inexistente() {
        return negocio(ErrorApi.noExiste());
    }

    @ExceptionHandler({DataAccessException.class, MongoException.class, org.springframework.transaction.TransactionException.class})
    public ResponseEntity<ErrorResponse> almacenamiento(Exception error) {
        DiagnosticoErrores.registrar("operación MongoDB", error);
        return negocio(ErrorApi.noDisponible());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception error) {
        DiagnosticoErrores.registrar("procesar solicitud HTTP", error);
        return negocio(new ErrorApi(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado."));
    }
}
