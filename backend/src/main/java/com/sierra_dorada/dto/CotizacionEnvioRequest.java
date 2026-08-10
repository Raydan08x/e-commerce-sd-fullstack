package com.sierra_dorada.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CotizacionEnvioRequest(
    @NotBlank(message = "El código DANE de destino es obligatorio")
    @Pattern(regexp = "^\\d{5,12}$", message = "El código DANE no es válido")
    String destinoCodigo,

    @NotEmpty(message = "Debes incluir al menos un producto")
    List<@Valid DetallePedidoRequest> detalles
) { }
