package com.zaror.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Field;


@Getter
@NoArgsConstructor
@AllArgsConstructor

public class Pago {
    private EstadoPago estado;
    @Field(write = Field.Write.ALWAYS) private String comprobante;
}
