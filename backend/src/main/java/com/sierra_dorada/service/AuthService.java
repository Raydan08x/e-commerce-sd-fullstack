package com.sierra_dorada.service;

import com.sierra_dorada.dto.AuthResponse;
import com.sierra_dorada.dto.LoginRequest;
import com.sierra_dorada.dto.RegistroPendienteResponse;
import com.sierra_dorada.dto.RegistroRequest;
import com.sierra_dorada.exception.CuentaNoVerificadaException;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.UsuarioRepository;
import com.sierra_dorada.security.JwtService;
import io.jsonwebtoken.JwtException;
import java.time.LocalDate;
import java.util.Locale;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/** Gestiona el registro, el inicio de sesión y la creación de tokens. */
@Service
public class AuthService {

    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";

    private final AuthenticationManager gestorAutenticacion;
    private final UsuarioRepository usuarios;
    private final UsuarioService servicioUsuarios;
    private final JwtService servicioJwt;
    private final CorreoVerificacionService correos;
    private final AdministradorPrincipalService administradorPrincipal;

    public AuthService(
            AuthenticationManager gestorAutenticacion,
            UsuarioRepository usuarios,
            UsuarioService servicioUsuarios,
            JwtService servicioJwt,
            CorreoVerificacionService correos,
            AdministradorPrincipalService administradorPrincipal) {
        this.gestorAutenticacion = gestorAutenticacion;
        this.usuarios = usuarios;
        this.servicioUsuarios = servicioUsuarios;
        this.servicioJwt = servicioJwt;
        this.correos = correos;
        this.administradorPrincipal = administradorPrincipal;
    }

    public AuthResponse login(LoginRequest solicitud) {
        String correo = normalizarCorreo(solicitud.usuario());
        usuarios.findByEmailIgnoreCase(correo)
            .filter(usuario -> !Boolean.TRUE.equals(usuario.getEmailVerificado()))
            .ifPresent(usuario -> {
                throw new CuentaNoVerificadaException(
                    "Debes confirmar tu correo antes de iniciar sesion");
            });

        try {
            gestorAutenticacion.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, solicitud.password()));
        } catch (AuthenticationException excepcion) {
            throw new BadCredentialsException(CREDENCIALES_INVALIDAS, excepcion);
        }

        Usuario usuario = usuarios.findByEmailIgnoreCase(correo)
                .orElseThrow(() -> new BadCredentialsException(CREDENCIALES_INVALIDAS));
        return crearRespuesta(usuario);
    }

    @Transactional
    public RegistroPendienteResponse registrar(RegistroRequest solicitud) {
        if (solicitud.fechaNacimiento().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Debes ser mayor de 18 años para crear una cuenta");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(solicitud.nombre());
        usuario.setApellidos(solicitud.apellidos());
        usuario.setFechaNacimiento(solicitud.fechaNacimiento());
        usuario.setGenero(solicitud.genero());
        usuario.setDireccion(solicitud.direccion());
        usuario.setTelefono(solicitud.telefono());
        usuario.setEmail(normalizarCorreo(solicitud.email()));
        usuario.setContrasena(solicitud.password());
        usuario.setAceptaTerminos(solicitud.aceptaTerminos());
        usuario.setAutorizaDatos(solicitud.autorizaDatos());
        usuario.setAutorizaComunicaciones(solicitud.autorizaComunicaciones());
        usuario.setEmailVerificado(false);
        usuario.setActivo(false);

        Usuario creado = servicioUsuarios.crear(usuario);
        correos.enviar(creado, servicioJwt.generarVerificacionEmail(creado));
        return new RegistroPendienteResponse(creado.getEmail(),
            "Revisa tu correo para confirmar y activar la cuenta");
    }

    @Transactional
    public AuthResponse verificarCorreo(String token) {
        String correo;
        try {
            correo = servicioJwt.obtenerEmailVerificacion(token);
        } catch (JwtException | IllegalArgumentException excepcion) {
            throw new IllegalArgumentException(
                "El enlace de confirmacion no es valido o ya vencio", excepcion);
        }

        Usuario usuario = usuarios.findByEmailIgnoreCase(correo)
            .orElseThrow(() -> new IllegalArgumentException(
                "El enlace de confirmacion no corresponde a una cuenta"));
        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new IllegalArgumentException("Este enlace de confirmacion ya fue utilizado");
        }
        usuario.setEmailVerificado(true);
        usuario.setActivo(true);
        administradorPrincipal.asignarRolSiCorresponde(usuario);
        return crearRespuesta(usuarios.save(usuario));
    }

    public void reenviarVerificacion(String email) {
        usuarios.findByEmailIgnoreCase(normalizarCorreo(email))
            .filter(usuario -> !Boolean.TRUE.equals(usuario.getEmailVerificado()))
            .ifPresent(usuario -> correos.enviar(
                usuario, servicioJwt.generarVerificacionEmail(usuario)));
    }

    private AuthResponse crearRespuesta(Usuario usuario) {
        return new AuthResponse(
                servicioJwt.generar(usuario),
                "Bearer",
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombres() + " " + usuario.getApellidos(),
                usuario.getRol().name().toLowerCase(Locale.ROOT));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
