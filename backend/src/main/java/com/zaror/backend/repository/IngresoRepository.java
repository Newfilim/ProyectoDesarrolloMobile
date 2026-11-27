package com.zaror.backend.repository;

import com.zaror.backend.model.Ingreso;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IngresoRepository extends MongoRepository<Ingreso, ObjectId> {
    List<Ingreso> findAllByOrderByFechaDescIdAsc();
}
