package com.zaror.backend.security;

import com.zaror.backend.model.Usuario;
import com.zaror.backend.exception.DiagnosticoErrores;
import com.zaror.backend.repository.UsuarioRepository;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Seguridad {
    private static final String R = "RESIDENTE";
    private static final String A = "ADMINISTRADOR";
    private static final String T = "TESORERIA";
    private static final String D = "DIRECTIVA";

    @Bean
    BCryptPasswordEncoder bcrypt() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain api(HttpSecurity http, UsuarioRepository usuarios, Environment entorno) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reglas -> {
                    if (entorno.acceptsProfiles(Profiles.of("dev"))) {
                        reglas.requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll();
                    }
                    reglas.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/auth/me", "/api/espacios", "/api/espacios/{id}")
                            .hasAnyRole(R, A, T, D)
                            .requestMatchers("/api/usuarios", "/api/usuarios/{id}", "/api/usuarios/{id}/estado").hasRole(A)
                            .requestMatchers(HttpMethod.PATCH, "/api/espacios/{id}/estado").hasRole(A)
                            .requestMatchers(HttpMethod.GET, "/api/espacios/{id}/disponibilidad").hasAnyRole(R, A)
                            .requestMatchers(HttpMethod.POST, "/api/reservas").hasRole(R)
                            .requestMatchers(HttpMethod.GET, "/api/reservas").hasRole(A)
                            .requestMatchers(HttpMethod.GET, "/api/reservas/mis-reservas").hasRole(R)
                            .requestMatchers(HttpMethod.GET, "/api/reservas/{id}").hasAnyRole(R, A)
                            .requestMatchers(HttpMethod.PATCH, "/api/reservas/{id}/aprobar", "/api/reservas/{id}/rechazar",
                                    "/api/reservas/{id}/rechazar-impago").hasRole(A)
                            .requestMatchers(HttpMethod.GET, "/api/pagos").hasRole(T)
                            .requestMatchers(HttpMethod.GET, "/api/pagos/{reservaId}/comprobante").hasAnyRole(R, T)
                            .requestMatchers(HttpMethod.POST, "/api/pagos/{reservaId}/comprobante").hasRole(R)
                            .requestMatchers(HttpMethod.PATCH, "/api/pagos/{reservaId}/confirmar", "/api/pagos/{reservaId}/rechazar").hasRole(T)
                            .requestMatchers(HttpMethod.GET, "/api/ingresos", "/api/gastos", "/api/finanzas/gastos-por-categoria").hasAnyRole(T, D)
                            .requestMatchers(HttpMethod.POST, "/api/ingresos", "/api/gastos").hasRole(T)
                            .requestMatchers(HttpMethod.GET, "/api/finanzas/resumen").hasAnyRole(R, T, D)
                            .anyRequest().denyAll();
                })
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> sinSesion(res, ex))
                        .accessDeniedHandler((req, res, ex) -> escribir(res, 403, "ACCESO_DENEGADO", "El rol no permite esta operación.")))
                .oauth2ResourceServer(o -> o.authenticationEntryPoint((req, res, ex) -> sinSesion(res, ex))
                        .jwt(j -> j.jwtAuthenticationConverter(jwt -> {
                    Usuario usuario;
                    try {
                        usuario = usuarios.buscar(new ObjectId(jwt.getSubject()));
                    } catch (org.springframework.dao.DataAccessException | com.mongodb.MongoException ex) {
                        DiagnosticoErrores.registrar("autenticar en MongoDB", ex);
                        throw new OAuth2AuthenticationException(new OAuth2Error("storage_unavailable"));
                    }
                    if (usuario == null || !usuario.isActivo()) {
                        throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"));
                    }
                    return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
                })));
        return http.build();
    }

    private static void sinSesion(HttpServletResponse res, Exception ex) throws IOException {
        if (ex instanceof OAuth2AuthenticationException error
                && "storage_unavailable".equals(error.getError().getErrorCode())) {
            escribir(res, 503, "DEPENDENCIA_NO_DISPONIBLE", "No se pudo completar la autenticación.");
            return;
        }
        res.setHeader("WWW-Authenticate", "Bearer");
        escribir(res, 401, "NO_AUTENTICADO", "La autenticación no es válida.");
    }

    private static void escribir(HttpServletResponse res, int estado, String codigo, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");
        res.getWriter().write("{\"estado\":" + estado + ",\"codigo\":\"" + codigo
                + "\",\"mensaje\":\"" + mensaje + "\"}");
    }
}
