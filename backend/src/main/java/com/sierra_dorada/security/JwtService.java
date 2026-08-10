package com.sierra_dorada.security;

import com.sierra_dorada.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey clave;
    private final long expiracionMilisegundos;
    private final long expiracionVerificacionMilisegundos;

    private static final String PROPOSITO = "proposito";
    private static final String ACCESO = "ACCESO";
    private static final String VERIFICAR_EMAIL = "VERIFICAR_EMAIL";

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMilisegundos,
                      @Value("${app.jwt.email-verification-expiration-ms}")
                      long expiracionVerificacionMilisegundos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMilisegundos = expiracionMilisegundos;
        this.expiracionVerificacionMilisegundos = expiracionVerificacionMilisegundos;
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
            .subject(usuario.getEmail())
            .claim("id", usuario.getId())
            .claim("rol", usuario.getRol().name())
            .claim(PROPOSITO, ACCESO)
            .issuedAt(ahora)
            .expiration(new Date(ahora.getTime() + expiracionMilisegundos))
            .signWith(clave)
            .compact();
    }

    public String obtenerEmail(String token) {
        Claims claims = claims(token);
        if (!ACCESO.equals(claims.get(PROPOSITO, String.class))) {
            throw new IllegalArgumentException("El token no es de acceso");
        }
        return claims.getSubject();
    }

    public String generarVerificacionEmail(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
            .subject(usuario.getEmail())
            .claim("id", usuario.getId())
            .claim(PROPOSITO, VERIFICAR_EMAIL)
            .issuedAt(ahora)
            .expiration(new Date(ahora.getTime() + expiracionVerificacionMilisegundos))
            .signWith(clave)
            .compact();
    }

    public String obtenerEmailVerificacion(String token) {
        Claims claims = claims(token);
        if (!VERIFICAR_EMAIL.equals(claims.get(PROPOSITO, String.class))) {
            throw new IllegalArgumentException("El enlace de confirmacion no es valido");
        }
        return claims.getSubject();
    }

    private Claims claims(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
