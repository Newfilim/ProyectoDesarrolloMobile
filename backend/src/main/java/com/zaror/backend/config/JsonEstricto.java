package com.zaror.backend.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

@Configuration
public class JsonEstricto {
    @Bean
    JsonMapperBuilderCustomizer evitarCoercionesDeTexto() {
        return builder -> {
            builder.withCoercionConfig(LogicalType.Textual, config -> {
                config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
                config.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
            });
            builder.withCoercionConfig(LogicalType.Enum,
                    config -> config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail));
        };
    }
}
