package com.sierra_dorada.controller;

import com.sierra_dorada.model.MensajeContacto;
import com.sierra_dorada.repository.MensajeContactoRepository;
import com.sierra_dorada.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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

@RestController
@RequestMapping("/api/contacto")
public class ContactoController {
    private final MensajeContactoRepository mensajes;
    private final UsuarioRepository usuarios;

    public ContactoController(MensajeContactoRepository mensajes, UsuarioRepository usuarios) {
        this.mensajes = mensajes;
        this.usuarios = usuarios;
    }

    @PostMapping
    @SecurityRequirements
    public ResponseEntity<MensajeContacto> registrar(@Valid @RequestBody MensajeContacto mensaje,
                                                       Authentication autenticacion) {
        mensaje.setId(null);
        mensaje.setFechaCreacion(null);
        mensaje.setEstado("NUEVO");
        if (autenticacion != null) {
            usuarios.findByEmailIgnoreCase(autenticacion.getName()).ifPresent(mensaje::setUsuario);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(mensajes.save(mensaje));
    }

    @GetMapping
    public List<MensajeContacto> listar() { return mensajes.findAll(); }
}
