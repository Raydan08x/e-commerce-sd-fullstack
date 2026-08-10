package com.sierra_dorada.service;

import com.sierra_dorada.dto.UsuarioActualizacionRequest;
import com.sierra_dorada.exception.ConflictoException;
import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final String CORREO_REGISTRADO = "El email ya está registrado";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificadorContrasenas;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder codificadorContrasenas) {
        this.usuarios = usuarios;
        this.codificadorContrasenas = codificadorContrasenas;
    }

    public List<Usuario> listar() {
        return usuarios.findAll();
    }

    public Usuario obtener(Integer id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    public Usuario crear(Usuario usuario) {
        String correo = normalizarCorreo(usuario.getEmail());
        validarCorreoDisponible(correo, null);

        usuario.setId(null);
        usuario.setEmail(correo);
        usuario.setContrasena(codificadorContrasenas.encode(usuario.getContrasena()));
        return usuarios.save(usuario);
    }

    public Usuario actualizar(Integer id, UsuarioActualizacionRequest datos) {
        Usuario actual = obtener(id);
        String correo = normalizarCorreo(datos.email());
        validarCorreoDisponible(correo, id);

        actual.setNombres(datos.nombres());
        actual.setApellidos(datos.apellidos());
        actual.setFechaNacimiento(datos.fechaNacimiento());
        actual.setGenero(datos.genero());
        actual.setDireccion(datos.direccion());
        actual.setEmail(correo);
        actual.setTelefono(datos.telefono());

        if (datos.activo() != null) {
            actual.setActivo(datos.activo());
        }
        if (datos.rol() != null) {
            actual.setRol(datos.rol());
        }
        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            actual.setContrasena(codificadorContrasenas.encode(datos.contrasena()));
        }

        return usuarios.save(actual);
    }

    public void eliminar(Integer id) {
        usuarios.delete(obtener(id));
    }

    private void validarCorreoDisponible(String correo, Integer usuarioPermitidoId) {
        usuarios.findByEmailIgnoreCase(correo)
                .filter(usuario -> !Objects.equals(usuario.getId(), usuarioPermitidoId))
                .ifPresent(usuario -> {
                    throw new ConflictoException(CORREO_REGISTRADO);
                });
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
