package com.sierra_dorada.security;

import com.sierra_dorada.model.Usuario;
import io.jsonwebtoken.Jwts;
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

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMilisegundos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMilisegundos = expiracionMilisegundos;
    }

    public String generar(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
            .subject(usuario.getEmail())
            .claim("id", usuario.getId())
            .claim("rol", usuario.getRol().name())
            .issuedAt(ahora)
            .expiration(new Date(ahora.getTime() + expiracionMilisegundos))
            .signWith(clave)
            .compact();
    }

    public String obtenerEmail(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}
