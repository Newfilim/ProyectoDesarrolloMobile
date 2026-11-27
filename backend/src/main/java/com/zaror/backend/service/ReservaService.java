package com.zaror.backend.service;

import com.mongodb.MongoException;
import com.zaror.backend.config.Infraestructura;
import com.zaror.backend.dto.Entradas.NuevaReserva;
import com.zaror.backend.dto.Respuestas.*;
import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.exception.DiagnosticoErrores;
import com.zaror.backend.model.*;
import com.zaror.backend.repository.BloqueReservadoRepository;
import com.zaror.backend.repository.ReservaRepository;
import java.time.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class ReservaService {
    private final ReservaRepository reservas;
    private final BloqueReservadoRepository bloqueos;
    private final UsuarioService usuarios;
    private final EspacioService espacios;
    private final Horario horario;
    private final VistasReservas vistas;
    private final Clock reloj;
    private final ReentrantLock lockEscrituras;
    private final TransactionTemplate transacciones;

    public DisponibilidadResponse disponibilidad(ObjectId id, String fecha) {
        LocalDate dia = Validaciones.fecha(fecha);
        Espacio espacio = espacios.exigirEspacio(id);
        Instant ahora = reloj.instant();
        boolean vigente = espacio.isHabilitado() && !dia.isBefore(fechaLocal(ahora));
        List<Horario.Bloque> bloques = horario.bloques(dia);
        Set<Instant> ocupados = new HashSet<>();
        if (vigente && !bloques.isEmpty()) {
            for (BloqueReservado bloqueo : bloqueos.buscarEntre(
                    id, bloques.getFirst().inicio(), bloques.getLast().fin())) {
                ocupados.add(bloqueo.getInicio());
            }
        }
        List<BloqueHorarioResponse> respuesta = new ArrayList<>();
        for (Horario.Bloque bloque : bloques) {
            boolean disponible = vigente && bloque.inicio().isAfter(ahora) && !ocupados.contains(bloque.inicio());
            respuesta.add(new BloqueHorarioResponse(bloque.inicio(), bloque.fin(), disponible));
        }
        return new DisponibilidadResponse(id.toHexString(), fecha, espacio.isHabilitado(), respuesta);
    }

    public ReservaResponse crear(ObjectId usuario, NuevaReserva entrada) {
        ObjectId espacioId = Validaciones.id(entrada.espacioId());
        LocalDate fecha = Validaciones.fecha(entrada.fechaOperativa());
        Instant inicio = Validaciones.instante(entrada.inicio());
        Instant fin = Validaciones.instante(entrada.fin());
        List<Horario.Bloque> seleccion = horario.rango(fecha, inicio, fin);
        ObjectId id = new ObjectId();

        Reserva guardada;
        lockEscrituras.lock();
        try {
            guardada = transaccion(() -> {
                usuarios.exigirActivo(usuario);
                Espacio espacio = espacios.exigirEspacio(espacioId);
                if (!espacio.isHabilitado()) {
                    throw ErrorApi.conflicto("ESPACIO_DESHABILITADO", "El espacio no está habilitado.");
                }
                Instant ahora = reloj.instant();
                if (fecha.isBefore(fechaLocal(ahora)) || !inicio.isAfter(ahora)) {
                    throw ErrorApi.invalido("La jornada y la hora de inicio deben estar vigentes.");
                }

                Reserva nueva = new Reserva(id, usuario, espacioId, entrada.fechaOperativa(), inicio, fin,
                        EstadoReserva.PENDIENTE, null);
                reservas.insertar(nueva);
                for (Horario.Bloque bloque : seleccion) {
                    try {
                        bloqueos.insert(new BloqueReservado(new ObjectId(), id, espacioId, bloque.inicio()));
                    } catch (DuplicateKeyException error) {
                        throw ErrorApi.ocupado();
                    }
                }
                return nueva;
            });
        } finally {
            lockEscrituras.unlock();
        }
        return vistas.reserva(guardada, true);
    }

    public List<ReservaResponse> listar(ObjectId propietario) {
        return vistas.reservas(reservas.listar(propietario), propietario != null);
    }

    public ReservaResponse detalle(ObjectId id, ObjectId propietario) {
        Reserva reserva = exigirReserva(id);
        verificarPropietario(reserva, propietario);
        return vistas.reserva(reserva, propietario != null);
    }

    public ReservaResponse aprobar(ObjectId actor, ObjectId id) {
        return actualizar(actor, id, false, true);
    }

    public ReservaResponse rechazar(ObjectId actor, ObjectId id, boolean impago) {
        return actualizar(actor, id, impago, false);
    }

    private ReservaResponse actualizar(ObjectId actor, ObjectId id, boolean impago, boolean aprobar) {
        Reserva resultado;
        lockEscrituras.lock();
        try {
            resultado = transaccion(() -> {
                usuarios.exigirActivo(actor);
                Reserva previa = exigirReserva(id);
                if (impago && previa.getEstado() == EstadoReserva.APROBADA && previa.getPago() == null) {
                    throw new IllegalStateException("Reserva aprobada sin pago.");
                }
                Reserva actual = impago ? reservas.rechazarPorImpago(id) : reservas.resolverPendiente(id, aprobar);
                if (actual == null) throw ErrorApi.transicion();
                if (!aprobar) {
                    bloqueos.deleteByReservaId(id);
                }
                return actual;
            });
        } finally {
            lockEscrituras.unlock();
        }
        return vistas.reserva(resultado, false);
    }

    private <T> T transaccion(Supplier<T> trabajo) {
        for (int intento = 1; ; intento++) {
            try {
                return transacciones.execute(estado -> trabajo.get());
            } catch (RuntimeException error) {
                if (etiqueta(error, "UnknownTransactionCommitResult")) {
                    DiagnosticoErrores.registrar("resultado incierto del commit de reserva", error);
                    throw ErrorApi.noDisponible();
                }
                if (intento >= 3 || !etiqueta(error, "TransientTransactionError")) throw error;
            }
        }
    }

    private static boolean etiqueta(Throwable error, String etiqueta) {
        for (Throwable actual = error; actual != null; actual = actual.getCause()) {
            if (actual instanceof MongoException mongo && mongo.hasErrorLabel(etiqueta)) return true;
        }
        return false;
    }

    Reserva exigirReserva(ObjectId id) {
        Reserva reserva = reservas.buscar(id);
        if (reserva == null) throw ErrorApi.noExiste();
        return reserva;
    }

    public static void verificarPropietario(Reserva reserva, ObjectId propietario) {
        if (propietario != null && !propietario.equals(reserva.getUsuarioId())) throw ErrorApi.noExiste();
    }

    private static LocalDate fechaLocal(Instant instante) {
        return LocalDate.ofInstant(instante, Infraestructura.SANTIAGO);
    }
}
