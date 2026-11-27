package com.zaror.backend.repository;

import com.zaror.backend.model.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservaRepository {
    private final MongoTemplate mongo;

    public Reserva buscar(ObjectId id) { return mongo.findById(id, Reserva.class); }
    public void insertar(Reserva reserva) { mongo.insert(reserva); }

    public List<Reserva> listar(ObjectId propietario) {
        Query consulta = ordenadas();
        if (propietario != null) consulta.addCriteria(Criteria.where("usuarioId").is(propietario));
        return mongo.find(consulta, Reserva.class);
    }

    public List<Reserva> listarConComprobante() {
        return mongo.find(ordenadas().addCriteria(Criteria.where("pago.comprobante").ne(null)), Reserva.class);
    }

    public boolean referenciaComprobante(String nombre) {
        return mongo.exists(Query.query(Criteria.where("pago.comprobante").is(nombre)), Reserva.class);
    }

    public Reserva resolverPendiente(ObjectId id, boolean aprobar) {
        Criteria condicion = Criteria.where("estado").is(EstadoReserva.PENDIENTE).and("pago").is(null);
        Update cambio = new Update();
        if (aprobar) {
            cambio.set("estado", EstadoReserva.APROBADA).set("pago", new Pago(EstadoPago.PENDIENTE, null));
        } else {
            cambio.set("estado", EstadoReserva.RECHAZADA);
        }
        return modificar(id, condicion, cambio);
    }

    public Reserva rechazarPorImpago(ObjectId id) {
        Criteria condicion = Criteria.where("estado").is(EstadoReserva.APROBADA)
                .and("pago.estado").in(EstadoPago.PENDIENTE, EstadoPago.RECHAZADO);
        return modificar(id, condicion, new Update().set("estado", EstadoReserva.RECHAZADA)
                .set("pago.estado", EstadoPago.RECHAZADO));
    }

    public Reserva adjuntarComprobante(ObjectId id, ObjectId propietario, EstadoPago esperado,
                                       String anterior, String nuevo) {
        Criteria condicion = Criteria.where("usuarioId").is(propietario).and("estado").is(EstadoReserva.APROBADA)
                .and("pago.estado").is(esperado).and("pago.comprobante").is(anterior);
        return modificar(id, condicion, new Update().set("pago.estado", EstadoPago.EN_REVISION).set("pago.comprobante", nuevo));
    }

    public Reserva revisarComprobante(ObjectId id, String comprobante, EstadoPago nuevo) {
        Criteria condicion = Criteria.where("estado").is(EstadoReserva.APROBADA)
                .and("pago.estado").is(EstadoPago.EN_REVISION).and("pago.comprobante").is(comprobante);
        return modificar(id, condicion, new Update().set("pago.estado", nuevo));
    }

    private Reserva modificar(ObjectId id, Criteria condicion, Update cambio) {
        Query consulta = Query.query(Criteria.where("_id").is(id)).addCriteria(condicion);
        return mongo.findAndModify(consulta, cambio, FindAndModifyOptions.options().returnNew(true), Reserva.class);
    }

    private static Query ordenadas() {
        return new Query().with(Sort.by(Sort.Order.desc("inicio"), Sort.Order.asc("_id")));
    }
}
