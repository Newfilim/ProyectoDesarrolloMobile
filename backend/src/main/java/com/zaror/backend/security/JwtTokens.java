package com.zaror.backend.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.zaror.backend.model.Usuario;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtTokens {
    public static final String ISSUER = "zaror-backend";
    public static final String AUDIENCE = "zaror-android";
    public static final int SEGUNDOS = 28800;

    @Bean
    SecretKey claveJwt(@Value("${zaror.jwt-secret}") String texto) {
        try {
            byte[] bytes = Base64.getDecoder().decode(texto);
            if (bytes.length >= 32) return new SecretKeySpec(bytes, "HmacSHA256");
        } catch (IllegalArgumentException ignored) {
            // Informar el formato esperado sin mostrar la clave.
        }
        throw new IllegalStateException("JWT_SECRET debe ser Base64 de al menos 32 bytes.");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey clave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey clave, Clock reloj) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        JwtTimestampValidator tiempo = new JwtTimestampValidator(java.time.Duration.ZERO);
        tiempo.setClock(reloj);
        OAuth2TokenValidator<Jwt> datos = token -> {
            if (token.getExpiresAt() != null && token.getExpiresAt().isAfter(reloj.instant())
                    && token.getAudience() != null && token.getAudience().contains(AUDIENCE)
                    && token.getSubject() != null && org.bson.types.ObjectId.isValid(token.getSubject())) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(tiempo, new JwtIssuerValidator(ISSUER), datos));
        return decoder;
    }

    public static String emitir(Usuario usuario, JwtEncoder encoder, Clock reloj) {
        Instant ahora = reloj.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(usuario.getId().toHexString())
                .issuer(ISSUER).audience(List.of(AUDIENCE)).issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(SEGUNDOS)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
