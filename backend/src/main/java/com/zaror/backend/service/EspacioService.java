package com.zaror.backend.service;

import com.zaror.backend.dto.Respuestas.EspacioResponse;
import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.model.Espacio;
import com.zaror.backend.repository.EspacioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EspacioService {
    private final EspacioRepository espacios;
    private final UsuarioService usuarios;
    private final ReentrantLock lockEscrituras;

    public EspacioResponse espacio(ObjectId id) { return respuesta(exigirEspacio(id)); }

    public List<EspacioResponse> espacios() {
        List<EspacioResponse> lista = new ArrayList<>();
        for (Espacio espacio : espacios.listar()) lista.add(respuesta(espacio));
        return lista;
    }

    public EspacioResponse estadoEspacio(ObjectId actor, ObjectId id, boolean habilitado) {
        lockEscrituras.lock();
        try {
            usuarios.exigirActivo(actor);
            Espacio espacio = espacios.cambiarEstado(id, habilitado);
            if (espacio == null) throw ErrorApi.noExiste();
            return respuesta(espacio);
        } finally {
            lockEscrituras.unlock();
        }
    }

    private static EspacioResponse respuesta(Espacio e) {
        return new EspacioResponse(e.getId().toHexString(), e.getNombre(), e.getCapacidadMaxima(), e.isHabilitado());
    }
    Espacio exigirEspacio(ObjectId id) {
        Espacio espacio = espacios.buscar(id);
        if (espacio == null) throw ErrorApi.noExiste();
        return espacio;
    }
}
