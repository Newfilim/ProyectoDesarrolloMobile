package com.zaror.backend.config;

import com.zaror.backend.model.*;
import com.zaror.backend.exception.DiagnosticoErrores;
import com.zaror.backend.service.Validaciones;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class InicializacionMongo implements SmartInitializingSingleton {
    private static final Set<String> COLECCIONES = Set.of(
            "usuarios", "espacios", "reservas", "bloques_reservados", "ingresos", "gastos");

    private final MongoTemplate mongo;
    private final TransactionTemplate transacciones;
    private final Environment entorno;
    private final BCryptPasswordEncoder bcrypt;

    @Override
    public void afterSingletonsInstantiated() {
        try {
            inicializar();
        } catch (org.springframework.dao.DataAccessException | com.mongodb.MongoException
                 | org.springframework.transaction.TransactionException error) {
            DiagnosticoErrores.registrar("inicializar MongoDB", error);
            throw new IllegalStateException("No se pudo inicializar MongoDB para Zaror.");
        }
    }

    private void inicializar() {
        String base = mongo.getDb().getName();
        if (!base.equals("zaror_db") && !base.matches("zaror_test(?:_[a-zA-Z0-9_]+)?")) {
            throw new IllegalStateException("La base debe ser zaror_db o una base zaror_test aislada.");
        }
        Document hello = mongo.executeCommand(new Document("hello", 1));
        if (!"rs0".equals(hello.getString("setName")) || !Boolean.TRUE.equals(hello.getBoolean("isWritablePrimary"))) {
            throw new IllegalStateException("MongoDB debe tener un primario escribible del replica set rs0.");
        }
        if (mongo.getConverter() instanceof MappingMongoConverter converter) {
            converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        }
        boolean semilla = debeSembrar();
        String contrasena = entorno.getProperty("zaror.demo-password");
        if (semilla && !Validaciones.contrasenaValida(contrasena)) {
            throw new IllegalStateException("Configura ZAROR_DEMO_PASSWORD para inicializar la base dev.");
        }

        for (Class<?> tipo : List.of(Usuario.class, Espacio.class, Reserva.class, BloqueReservado.class,
                Ingreso.class, Gasto.class)) {
            if (!mongo.collectionExists(tipo)) mongo.createCollection(tipo);
        }
        indice(Usuario.class, "correo_unico", true, "correo");
        indice(Espacio.class, "nombre_unico", true, "nombre");
        indice(Reserva.class, "reserva_usuario", false, "usuarioId");
        indice(Reserva.class, "reserva_espacio_inicio", false, "espacioId", "inicio");
        indice(Reserva.class, "reserva_estado_pago", false, "estado", "pago.estado");
        indice(BloqueReservado.class, "bloque_unico", true, "espacioId", "inicio");
        indice(BloqueReservado.class, "bloque_reserva", false, "reservaId");
        indice(Ingreso.class, "ingreso_fecha", false, "fecha");
        indice(Gasto.class, "gasto_fecha", false, "fecha");

        transacciones.executeWithoutResult(estado -> {
            // Ejecutar un comando de escritura transaccional sobre un ID que no existe.
            ObjectId inexistente = new ObjectId();
            mongo.updateFirst(Query.query(Criteria.where("_id").is(inexistente)),
                    new Update().set("reservaId", inexistente), BloqueReservado.class);
            if (semilla) {
                if (hayDocumentos(COLECCIONES)) {
                    throw new IllegalStateException("La recuperación de semilla exige una base sin documentos.");
                }
                insertarSemilla(contrasena);
            }
        });
    }

    private boolean debeSembrar() {
        if (!entorno.acceptsProfiles(Profiles.of("dev"))) return false;
        Set<String> colecciones = mongo.getCollectionNames();
        if (colecciones.isEmpty()) return true;
        boolean autorizada = entorno.getProperty("zaror.inicializar-base-vacia", Boolean.class, false);
        if (!COLECCIONES.containsAll(colecciones)) {
            if (autorizada) throw new IllegalStateException("La recuperación exige únicamente colecciones de Zaror.");
            return false;
        }

        if (hayDocumentos(colecciones)) {
            if (autorizada) throw new IllegalStateException("La recuperación de semilla exige una base sin documentos.");
            return false;
        }
        if (!autorizada) {
            log.warn("Base Zaror con colecciones vacías. Para cargar datos dev, consulta CONFIGURACION_LOCAL.md.");
        }
        return autorizada;
    }

    private boolean hayDocumentos(Set<String> colecciones) {
        for (String coleccion : colecciones) {
            if (mongo.exists(new Query(), coleccion)) return true;
        }
        return false;
    }

    private void indice(Class<?> tipo, String nombre, boolean unico, String... campos) {
        Index indice = new Index().named(nombre);
        for (String campo : campos) indice.on(campo, Direction.ASC);
        if (unico) indice.unique();
        mongo.indexOps(tipo).createIndex(indice);
    }

    private void insertarSemilla(String contrasena) {
        mongo.insert(List.of(
                usuario(1, "Residente de prueba", "residente", RolUsuario.RESIDENTE, contrasena),
                usuario(2, "Administrador de prueba", "admin", RolUsuario.ADMINISTRADOR, contrasena),
                usuario(3, "Tesorería de prueba", "tesoreria", RolUsuario.TESORERIA, contrasena),
                usuario(4, "Directiva de prueba", "directiva", RolUsuario.DIRECTIVA, contrasena)), Usuario.class);
        mongo.insert(List.of(new Espacio(id(11), "Quincho", 25, true),
                new Espacio(id(12), "Sala multiuso", 40, false), new Espacio(id(13), "Piscina", 60, true)), Espacio.class);
        mongo.insert(List.of(new Ingreso(id(21), "Arriendo de quincho", 160000, "2026-11-02"),
                new Ingreso(id(22), "Arriendo de piscina", 100000, "2026-11-04"),
                new Ingreso(id(23), "Otros arriendos", 40000, "2026-11-06")), Ingreso.class);
        mongo.insert(List.of(
                gasto(31, "Pago de electricidad", 40000, CategoriaGasto.SERVICIOS_BASICOS),
                gasto(32, "Productos de limpieza", 25000, CategoriaGasto.LIMPIEZA),
                gasto(33, "Compra de insumos", 20000, CategoriaGasto.INSUMOS),
                gasto(34, "Reparación de instalaciones", 30000, CategoriaGasto.MANTENIMIENTO),
                gasto(35, "Mejoras del quincho", 35000, CategoriaGasto.MEJORAS),
                gasto(36, "Gastos administrativos", 10000, CategoriaGasto.OTROS)), Gasto.class);
    }

    private Usuario usuario(int id, String nombre, String cuenta, RolUsuario rol, String contrasena) {
        return new Usuario(id(id), nombre, cuenta + "@zaror.cl", bcrypt.encode(contrasena), rol, true);
    }

    private static Gasto gasto(int id, String nombre, long monto, CategoriaGasto categoria) {
        return new Gasto(id(id), nombre, monto, "2026-11-0" + (id - 30), categoria);
    }

    private static ObjectId id(int numero) { return new ObjectId("%024x".formatted(numero)); }
}
