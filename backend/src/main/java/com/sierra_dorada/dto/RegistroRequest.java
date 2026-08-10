package com.sierra_dorada.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

public record RegistroRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    String apellidos,

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    LocalDate fechaNacimiento,

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 30, message = "El género no puede superar los 30 caracteres")
    String genero,

    @NotBlank(message = "La dirección es obligatoria")
    String direccion,

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos")
    String telefono,

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    String password,

    @AssertTrue(message = "Debes aceptar los términos y condiciones")
    Boolean aceptaTerminos,

    @AssertTrue(message = "Debes autorizar el tratamiento de datos personales")
    Boolean autorizaDatos,

    @NotNull(message = "Debes indicar si autorizas comunicaciones comerciales")
    Boolean autorizaComunicaciones
) {
}
