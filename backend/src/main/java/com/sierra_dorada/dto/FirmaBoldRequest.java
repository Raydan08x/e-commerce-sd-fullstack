package com.sierra_dorada.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FirmaBoldRequest(
    @NotNull Integer pedidoId,
    @NotBlank
    @Size(max = 60)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El identificador de orden no es válido")
    String orderId
) { }
