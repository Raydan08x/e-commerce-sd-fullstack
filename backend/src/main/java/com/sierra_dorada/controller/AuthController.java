package com.sierra_dorada.controller;

import com.sierra_dorada.dto.AuthResponse;
import com.sierra_dorada.dto.LoginRequest;
import com.sierra_dorada.dto.RegistroRequest;
import com.sierra_dorada.service.AuthService;
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
    public AuthResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return servicio.login(solicitud);
    }

    @PostMapping("/api/auth/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(solicitud));
    }
}
