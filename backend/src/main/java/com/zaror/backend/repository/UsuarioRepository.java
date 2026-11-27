package com.zaror.backend.repository;

import com.zaror.backend.model.*;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UsuarioRepository {
    private final MongoTemplate mongo;

    public Usuario buscar(ObjectId id) { return mongo.findById(id, Usuario.class); }

    public Usuario buscarPorCorreo(String correo) {
        return mongo.findOne(Query.query(Criteria.where("correo").is(correo)), Usuario.class);
    }

    public List<Usuario> listar() {
        return mongo.find(new Query().with(Sort.by("nombre", "_id")), Usuario.class);
    }

    public List<Usuario> buscarPorIds(Set<ObjectId> ids) {
        return mongo.find(Query.query(Criteria.where("_id").in(ids)), Usuario.class);
    }

    public Usuario insertar(Usuario usuario) { return mongo.insert(usuario); }

    public long contarAdministradoresActivos() {
        return mongo.count(Query.query(Criteria.where("rol").is(RolUsuario.ADMINISTRADOR).and("activo").is(true)), Usuario.class);
    }

    public Usuario editar(ObjectId id, String nombre, String correo) {
        return modificar(id, new Update().set("nombre", nombre).set("correo", correo));
    }

    public Usuario cambiarEstado(ObjectId id, boolean activo) {
        return modificar(id, new Update().set("activo", activo));
    }

    private Usuario modificar(ObjectId id, Update cambio) {
        return mongo.findAndModify(Query.query(Criteria.where("_id").is(id)), cambio,
                FindAndModifyOptions.options().returnNew(true), Usuario.class);
    }
}
