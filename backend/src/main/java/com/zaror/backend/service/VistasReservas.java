package com.zaror.backend.service;

import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.model.*;
import com.zaror.backend.repository.UsuarioRepository;
import com.zaror.backend.repository.EspacioRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VistasReservas {
    private final UsuarioRepository usuariosRepository;
    private final EspacioRepository espaciosRepository;

    public ReservaResponse reserva(Reserva reserva, boolean comprobante) {
        return reservas(List.of(reserva), comprobante).getFirst();
    }

    public List<ReservaResponse> reservas(List<Reserva> reservas, boolean comprobante) {
        Set<ObjectId> usuarios = new HashSet<>();
        Set<ObjectId> espacios = new HashSet<>();
        for (Reserva reserva : reservas) {
            usuarios.add(reserva.getUsuarioId());
            espacios.add(reserva.getEspacioId());
        }
        Map<ObjectId, String> nombresUsuarios = new HashMap<>();
        Map<ObjectId, String> nombresEspacios = new HashMap<>();
        if (!usuarios.isEmpty()) {
            for (Usuario usuario : usuariosRepository.buscarPorIds(usuarios)) {
                nombresUsuarios.put(usuario.getId(), usuario.getNombre());
            }
            for (Espacio espacio : espaciosRepository.buscarPorIds(espacios)) {
                nombresEspacios.put(espacio.getId(), espacio.getNombre());
            }
        }

        List<ReservaResponse> resultado = new ArrayList<>();
        for (Reserva reserva : reservas) {
            String nombreUsuario = nombresUsuarios.get(reserva.getUsuarioId());
            String nombreEspacio = nombresEspacios.get(reserva.getEspacioId());
            if (nombreUsuario == null || nombreEspacio == null) throw new IllegalStateException("Referencia de reserva inválida.");
            Pago pago = reserva.getPago();
            PagoDetalle detalle = null;
            if (pago != null) {
                String url = null;
                if (comprobante && pago.getComprobante() != null) {
                    url = "/api/pagos/" + reserva.getId().toHexString() + "/comprobante";
                }
                detalle = new PagoDetalle(pago.getEstado(), url);
            }
            resultado.add(new ReservaResponse(reserva.getId().toHexString(), reserva.getUsuarioId().toHexString(),
                    nombreUsuario, reserva.getEspacioId().toHexString(), nombreEspacio,
                    reserva.getFechaOperativa(), reserva.getInicio(), reserva.getFin(), reserva.getEstado(), detalle));
        }
        return resultado;
    }

    public List<PagoResponse> pagos(List<Reserva> reservas) {
        List<PagoResponse> resultado = new ArrayList<>();
        for (ReservaResponse r : reservas(reservas, true)) {
            resultado.add(new PagoResponse(r.id(), r.usuarioId(), r.nombreUsuario(), r.espacioId(), r.nombreEspacio(),
                    r.fechaOperativa(), r.inicio(), r.fin(), r.estado(), r.pago().estado(), r.pago().comprobanteUrl()));
        }
        return resultado;
    }
}
