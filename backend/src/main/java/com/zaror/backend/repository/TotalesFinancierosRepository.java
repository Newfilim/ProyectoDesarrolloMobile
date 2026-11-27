package com.zaror.backend.repository;

import com.zaror.backend.model.CategoriaGasto;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TotalesFinancierosRepository {
    private final MongoTemplate mongo;

    public long ingresos() { return total("ingresos"); }
    public long gastos() { return total("gastos"); }

    private long total(String coleccion) {
        Document resultado = mongo.getCollection(coleccion).aggregate(List.of(suma(null))).first();
        return resultado == null ? 0 : total(resultado);
    }

    public Map<CategoriaGasto, Long> gastosPorCategoria() {
        Map<CategoriaGasto, Long> montos = new EnumMap<>(CategoriaGasto.class);
        for (Document documento : mongo.getCollection("gastos").aggregate(List.of(suma("$categoria")))) {
            montos.put(CategoriaGasto.valueOf(documento.getString("_id")), total(documento));
        }
        return montos;
    }

    private static Document suma(String agruparPor) {
        return new Document("$group", new Document("_id", agruparPor)
                .append("total", new Document("$sum", new Document("$toDecimal", "$monto"))));
    }

    private static long total(Document documento) {
        return documento.get("total", Decimal128.class).bigDecimalValue().longValueExact();
    }
}
