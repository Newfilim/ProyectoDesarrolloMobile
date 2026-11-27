package com.zaror.backend.repository;

import com.zaror.backend.model.Espacio;
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
public class EspacioRepository {
    private final MongoTemplate mongo;

    public Espacio buscar(ObjectId id) { return mongo.findById(id, Espacio.class); }

    public List<Espacio> listar() {
        return mongo.find(new Query().with(Sort.by("nombre", "_id")), Espacio.class);
    }

    public List<Espacio> buscarPorIds(Set<ObjectId> ids) {
        return mongo.find(Query.query(Criteria.where("_id").in(ids)), Espacio.class);
    }

    public Espacio cambiarEstado(ObjectId id, boolean habilitado) {
        return mongo.findAndModify(Query.query(Criteria.where("_id").is(id)), new Update().set("habilitado", habilitado),
                FindAndModifyOptions.options().returnNew(true), Espacio.class);
    }
}
