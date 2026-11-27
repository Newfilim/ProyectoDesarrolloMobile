package com.zaror.backend.repository;

import com.zaror.backend.model.BloqueReservado;
import java.util.List;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface BloqueReservadoRepository extends MongoRepository<BloqueReservado, ObjectId> {
    @Query("{ 'espacioId': ?0, 'inicio': { '$gte': ?1, '$lt': ?2 } }")
    List<BloqueReservado> buscarEntre(ObjectId espacioId, Instant inicio, Instant fin);
    void deleteByReservaId(ObjectId reservaId);
}
