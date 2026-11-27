package com.zaror.backend.service;

import com.zaror.backend.dto.Entradas.*;
import com.zaror.backend.dto.Respuestas.UsuarioResponse;
import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.model.*;
import com.zaror.backend.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final Validaciones validar;
    private final BCryptPasswordEncoder bcrypt;
    private final ReentrantLock lockEscrituras;

    public UsuarioResponse usuario(ObjectId id) {
        return respuesta(exigirUsuario(id));
    }

    public List<UsuarioResponse> usuarios() {
        List<UsuarioResponse> lista = new ArrayList<>();
        for (Usuario usuario : usuarios.listar()) lista.add(respuesta(usuario));
        return lista;
    }

    public UsuarioResponse crear(ObjectId actor, AltaUsuario entrada) {
        String nombre = Validaciones.texto(entrada.getNombre(), 1, 100);
        String correo = validar.correo(entrada.getCorreo());
        if (!Validaciones.contrasenaValida(entrada.getContrasena())) {
            throw ErrorApi.invalido("La contraseña debe tener al menos 8 caracteres y como máximo 72 bytes UTF-8.");
        }
        Usuario usuario = new Usuario(new ObjectId(), nombre, correo, bcrypt.encode(entrada.getContrasena()), entrada.getRol(), true);
        lockEscrituras.lock();
        try {
            exigirActivo(actor);
            return respuesta(usuarios.insertar(usuario));
        } catch (DuplicateKeyException ex) {
            throw correoDuplicado();
        } finally {
            lockEscrituras.unlock();
        }
    }

    public UsuarioResponse editar(ObjectId actor, ObjectId id, EdicionUsuario entrada) {
        String nombre = Validaciones.texto(entrada.nombre(), 1, 100);
        String correo = validar.correo(entrada.correo());
        lockEscrituras.lock();
        try {
            exigirActivo(actor);
            Usuario usuario = usuarios.editar(id, nombre, correo);
            if (usuario == null) throw ErrorApi.noExiste();
            return respuesta(usuario);
        } catch (DuplicateKeyException ex) {
            throw correoDuplicado();
        } finally {
            lockEscrituras.unlock();
        }
    }

    public UsuarioResponse estadoUsuario(ObjectId actor, ObjectId id, boolean activo) {
        lockEscrituras.lock();
        try {
            exigirActivo(actor);
            Usuario usuario = exigirUsuario(id);
            if (usuario.isActivo() == activo) return respuesta(usuario);
            if (!activo && usuario.getRol() == RolUsuario.ADMINISTRADOR) {
                if (usuarios.contarAdministradoresActivos() <= 1) {
                    throw ErrorApi.conflicto("ULTIMO_ADMINISTRADOR", "Debe permanecer un administrador activo.");
                }
            }
            return respuesta(usuarios.cambiarEstado(id, activo));
        } finally {
            lockEscrituras.unlock();
        }
    }

    public void exigirActivo(ObjectId id) {
        Usuario usuario = usuarios.buscar(id);
        if (usuario == null || !usuario.isActivo()) throw ErrorApi.noAutenticado();
    }

    private static ErrorApi correoDuplicado() {
        return ErrorApi.conflicto("CORREO_DUPLICADO", "El correo ya está registrado.");
    }

    static UsuarioResponse respuesta(Usuario u) {
        return new UsuarioResponse(u.getId().toHexString(), u.getNombre(), u.getCorreo(), u.getRol(), u.isActivo());
    }

    private Usuario exigirUsuario(ObjectId id) {
        Usuario usuario = usuarios.buscar(id);
        if (usuario == null) throw ErrorApi.noExiste();
        return usuario;
    }
}
