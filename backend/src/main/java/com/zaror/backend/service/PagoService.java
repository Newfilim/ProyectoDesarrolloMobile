package com.zaror.backend.service;

import com.mongodb.MongoCommandException;
import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.exception.DiagnosticoErrores;
import com.zaror.backend.model.*;
import com.zaror.backend.repository.ReservaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PagoService {
    private final ReservaRepository reservas;
    private final ReservaService reservaService;
    private final VistasReservas vistas;
    private final ArchivosComprobantes archivos;

    public List<PagoResponse> listar() {
        return vistas.pagos(reservas.listarConComprobante());
    }

    public FileSystemResource descargar(ObjectId id, ObjectId propietario) {
        Reserva reserva = reservaService.exigirReserva(id);
        ReservaService.verificarPropietario(reserva, propietario);
        if (reserva.getPago() == null) throw ErrorApi.noExiste();
        return archivos.abrir(reserva.getPago().getComprobante());
    }

    public ReservaResponse subir(ObjectId id, ObjectId propietario, MultipartFile archivo) {
        Reserva reserva = reservaService.exigirReserva(id);
        ReservaService.verificarPropietario(reserva, propietario);
        Pago pago = reserva.getPago();
        if (reserva.getEstado() != EstadoReserva.APROBADA || pago == null
                || (pago.getEstado() != EstadoPago.PENDIENTE && pago.getEstado() != EstadoPago.RECHAZADO)) {
            throw ErrorApi.transicion();
        }
        String anterior = pago.getComprobante();
        String nuevo = archivos.guardar(archivo);
        Reserva actual;
        try {
            actual = reservas.adjuntarComprobante(id, propietario, pago.getEstado(), anterior, nuevo);
        } catch (RuntimeException error) {
            DiagnosticoErrores.registrar("actualizar referencia de comprobante", error);
            // Si la escritura es incierta, la referencia nueva puede haberse aplicado.
            actual = reservas.buscar(id);
            if (actual == null || actual.getPago() == null || !nuevo.equals(actual.getPago().getComprobante())) {
                if (esRechazoDefinitivo(error)) archivos.eliminar(nuevo);
                throw ErrorApi.noDisponible();
            }
        }
        if (actual == null) {
            archivos.eliminar(nuevo);
            throw ErrorApi.transicion();
        }
        if (anterior != null && !reservas.referenciaComprobante(anterior)) {
            archivos.eliminar(anterior);
        }
        return vistas.reserva(actual, true);
    }

    private static boolean esRechazoDefinitivo(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof MongoCommandException comando) {
                // Valor inválido, operación no autorizada o validación del documento.
                return comando.getCode() == 2 || comando.getCode() == 13 || comando.getCode() == 121;
            }
        }
        return false;
    }

    public PagoResponse revisar(ObjectId id, EstadoPago nuevo) {
        Reserva anterior = reservaService.exigirReserva(id);
        Pago pago = anterior.getPago();
        if (anterior.getEstado() != EstadoReserva.APROBADA || pago == null || pago.getEstado() != EstadoPago.EN_REVISION
                || pago.getComprobante() == null) throw ErrorApi.transicion();
        archivos.abrir(pago.getComprobante());
        Reserva actual = reservas.revisarComprobante(id, pago.getComprobante(), nuevo);
        if (actual == null) throw ErrorApi.transicion();
        return vistas.pagos(List.of(actual)).getFirst();
    }
}
