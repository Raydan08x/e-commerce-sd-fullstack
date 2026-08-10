package com.sierra_dorada.controller;

import com.sierra_dorada.exception.ConflictoException;
import com.sierra_dorada.model.SuscripcionNewsletter;
import com.sierra_dorada.repository.SuscripcionNewsletterRepository;
import com.sierra_dorada.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {
    private final SuscripcionNewsletterRepository suscripciones;
    private final UsuarioRepository usuarios;

    public NewsletterController(SuscripcionNewsletterRepository suscripciones,
                                UsuarioRepository usuarios) {
        this.suscripciones = suscripciones;
        this.usuarios = usuarios;
    }

    @PostMapping
    public ResponseEntity<SuscripcionNewsletter> registrar(
        @Valid @RequestBody SuscripcionNewsletter suscripcion,
        Authentication autenticacion) {
        String email = suscripcion.getEmail().trim().toLowerCase(Locale.ROOT);
        if (suscripciones.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Este correo ya está suscrito al newsletter");
        }
        suscripcion.setId(null);
        suscripcion.setEmail(email);
        suscripcion.setFechaSuscripcion(null);
        suscripcion.setActivo(true);
        if (autenticacion != null) {
            usuarios.findByEmailIgnoreCase(autenticacion.getName())
                .ifPresent(suscripcion::setUsuario);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(suscripciones.save(suscripcion));
    }

    @GetMapping
    public List<SuscripcionNewsletter> listar() { return suscripciones.findAll(); }
}
