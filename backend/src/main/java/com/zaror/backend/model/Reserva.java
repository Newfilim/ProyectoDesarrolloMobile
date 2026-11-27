package com.zaror.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("reservas")
public class Reserva {
    @Id private ObjectId id;
    private ObjectId usuarioId;
    private ObjectId espacioId;
    private String fechaOperativa;
    private Instant inicio;
    private Instant fin;
    private EstadoReserva estado;
    @Field(write = Field.Write.ALWAYS) private Pago pago;
}
