package com.zaror.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("ingresos")
public class Ingreso {
    @Id private ObjectId id;
    private String descripcion;
    private long monto;
    private String fecha;
}
