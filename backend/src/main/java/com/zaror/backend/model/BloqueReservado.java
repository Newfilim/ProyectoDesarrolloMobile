package com.zaror.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("bloques_reservados")
public class BloqueReservado {
    @Id private ObjectId id;
    private ObjectId reservaId;
    private ObjectId espacioId;
    private Instant inicio;
}
