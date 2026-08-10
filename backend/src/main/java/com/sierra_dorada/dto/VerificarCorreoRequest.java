package com.sierra_dorada.dto;

import jakarta.validation.constraints.NotBlank;

public record VerificarCorreoRequest(
    @NotBlank(message = "El token de confirmacion es obligatorio") String token
) {
}
