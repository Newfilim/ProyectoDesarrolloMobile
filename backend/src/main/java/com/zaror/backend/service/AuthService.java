package com.zaror.backend.service;

import com.zaror.backend.dto.Entradas.Login;
import com.zaror.backend.dto.Respuestas.LoginResponse;
import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.model.Usuario;
import com.zaror.backend.repository.UsuarioRepository;
import com.zaror.backend.security.JwtTokens;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarios;
    private final Validaciones validar;
    private final BCryptPasswordEncoder bcrypt;
    private final JwtEncoder encoder;
    private final Clock reloj;

    public LoginResponse login(Login entrada) {
        String correo;
        try {
            correo = validar.correo(entrada.getCorreo());
        } catch (ErrorApi error) {
            throw ErrorApi.noAutenticado();
        }
        if (!Validaciones.contrasenaValida(entrada.getContrasena())) throw ErrorApi.noAutenticado();
        Usuario usuario = usuarios.buscarPorCorreo(correo);
        if (usuario == null || !usuario.isActivo() || !bcrypt.matches(entrada.getContrasena(), usuario.getContrasenaHash())) {
            throw ErrorApi.noAutenticado();
        }
        return new LoginResponse(JwtTokens.emitir(usuario, encoder, reloj), "Bearer", JwtTokens.SEGUNDOS, UsuarioService.respuesta(usuario));
    }
}
