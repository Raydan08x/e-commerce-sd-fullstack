package com.sierra_dorada.controller;

import com.sierra_dorada.dto.AuthResponse;
import com.sierra_dorada.dto.LoginRequest;
import com.sierra_dorada.dto.ReenviarVerificacionRequest;
import com.sierra_dorada.dto.RegistroPendienteResponse;
import com.sierra_dorada.dto.RegistroRequest;
import com.sierra_dorada.dto.VerificarCorreoRequest;
import com.sierra_dorada.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService servicio;

    public AuthController(AuthService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/api/auth/login")
    @SecurityRequirements
    public AuthResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return servicio.login(solicitud);
    }

    @PostMapping("/api/auth/registro")
    @SecurityRequirements
    public ResponseEntity<RegistroPendienteResponse> registro(
            @Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(servicio.registrar(solicitud));
    }

    @PostMapping("/api/auth/verificar-correo")
    @SecurityRequirements
    public AuthResponse verificarCorreo(@Valid @RequestBody VerificarCorreoRequest solicitud) {
        return servicio.verificarCorreo(solicitud.token());
    }

    @PostMapping("/api/auth/reenviar-verificacion")
    @SecurityRequirements
    public ResponseEntity<Void> reenviarVerificacion(
            @Valid @RequestBody ReenviarVerificacionRequest solicitud) {
        servicio.reenviarVerificacion(solicitud.email());
        return ResponseEntity.noContent().build();
    }
}
