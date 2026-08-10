package com.sierra_dorada.dto;

import com.sierra_dorada.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UsuarioActualizacionRequest(
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres")
    String nombres,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    String apellidos,

    LocalDate fechaNacimiento,

    @Size(max = 30, message = "El género no puede superar los 30 caracteres")
    String genero,

    String direccion,

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
    String email,

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    String telefono,

    Boolean activo,

    Rol rol,

    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    String contrasena
) {
}
