package com.sierra_dorada.controller;

import com.sierra_dorada.dto.PerfilUsuarioResponse;
import com.sierra_dorada.service.UsuarioService;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final UsuarioService usuarios;

    public PerfilController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public PerfilUsuarioResponse obtener(Principal principal) {
        return usuarios.obtenerPerfil(principal.getName());
    }
}
