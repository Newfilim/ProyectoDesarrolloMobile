package com.zaror.backend.repository;

import com.zaror.backend.model.Gasto;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GastoRepository extends MongoRepository<Gasto, ObjectId> {
    List<Gasto> findAllByOrderByFechaDescIdAsc();
}
