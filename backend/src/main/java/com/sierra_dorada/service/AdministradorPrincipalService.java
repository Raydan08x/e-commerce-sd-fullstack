package com.sierra_dorada.service;

import com.sierra_dorada.model.Rol;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantiene el rol del administrador principal sin crear ni almacenar credenciales.
 * La cuenta debe existir, estar activa y haber demostrado control del correo.
 */
@Service
public class AdministradorPrincipalService implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final String emailAdministrador;

    public AdministradorPrincipalService(
        UsuarioRepository usuarios,
        @Value("${app.admin.email}") String emailAdministrador
    ) {
        this.usuarios = usuarios;
        this.emailAdministrador = normalizar(emailAdministrador);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        usuarios.findByEmailIgnoreCase(emailAdministrador).ifPresent(usuario -> {
            if (asignarRolSiCorresponde(usuario)) {
                usuarios.save(usuario);
            }
        });
    }

    public boolean asignarRolSiCorresponde(Usuario usuario) {
        boolean identidadCorrecta = usuario.getEmail() != null
            && emailAdministrador.equals(normalizar(usuario.getEmail()));
        boolean cuentaVerificada = Boolean.TRUE.equals(usuario.getActivo())
            && Boolean.TRUE.equals(usuario.getEmailVerificado());
        if (identidadCorrecta && cuentaVerificada && usuario.getRol() != Rol.ADMIN) {
            usuario.setRol(Rol.ADMIN);
            return true;
        }
        return false;
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
