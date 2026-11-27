package com.zaror.backend.controller;

import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.model.EstadoPago;
import com.zaror.backend.service.PagoService;
import com.zaror.backend.service.Validaciones;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/api/pagos")
@SecurityRequirement(name = "bearer")
@RequiredArgsConstructor
public class PagosController {
    private final PagoService pagos;

    @GetMapping
    public List<PagoResponse> listar() { return pagos.listar(); }

    @PostMapping(value = "/{reservaId}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReservaResponse subir(@AuthenticationPrincipal Jwt jwt, @PathVariable String reservaId,
                                 @RequestPart("archivo") MultipartFile archivo,
                                 MultipartHttpServletRequest solicitud) {
        if (solicitud.getMultiFileMap().size() != 1 || solicitud.getFiles("archivo").size() != 1
                || !solicitud.getParameterMap().isEmpty()) {
            throw ErrorApi.invalido("Envía únicamente un archivo en el campo archivo.");
        }
        return pagos.subir(Validaciones.id(reservaId), Validaciones.id(jwt.getSubject()), archivo);
    }

    @GetMapping(value = "/{reservaId}/comprobante", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<FileSystemResource> descargar(@PathVariable String reservaId, Principal principal) {
        FileSystemResource archivo = pagos.descargar(Validaciones.id(reservaId), ReservasController.propietario(principal));
        MediaType tipo = archivo.getFilename().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff").body(archivo);
    }

    @PatchMapping("/{reservaId}/confirmar")
    public PagoResponse confirmar(@PathVariable String reservaId, HttpServletRequest solicitud) throws IOException {
        Validaciones.sinCuerpo(solicitud);
        return pagos.revisar(Validaciones.id(reservaId), EstadoPago.CONFIRMADO);
    }

    @PatchMapping("/{reservaId}/rechazar")
    public PagoResponse rechazar(@PathVariable String reservaId, HttpServletRequest solicitud) throws IOException {
        Validaciones.sinCuerpo(solicitud);
        return pagos.revisar(Validaciones.id(reservaId), EstadoPago.RECHAZADO);
    }
}
