package com.sierra_dorada.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService servicioJwt;
    private final UserDetailsService servicioDetallesUsuario;

    public JwtAuthenticationFilter(JwtService servicioJwt, UserDetailsService servicioDetallesUsuario) {
        this.servicioJwt = servicioJwt;
        this.servicioDetallesUsuario = servicioDetallesUsuario;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta,
                                    FilterChain cadenaFiltros) throws ServletException, IOException {
        String encabezado = solicitud.getHeader("Authorization");
        boolean tieneToken = encabezado != null && encabezado.startsWith(PREFIJO_BEARER);
        boolean sinAutenticacion = SecurityContextHolder.getContext().getAuthentication() == null;

        if (tieneToken && sinAutenticacion) {
            try {
                String token = encabezado.substring(PREFIJO_BEARER.length());
                String email = servicioJwt.obtenerEmail(token);
                UserDetails usuario = servicioDetallesUsuario.loadUserByUsername(email);
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
            } catch (JwtException | IllegalArgumentException | AuthenticationException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        cadenaFiltros.doFilter(solicitud, respuesta);
    }
}
