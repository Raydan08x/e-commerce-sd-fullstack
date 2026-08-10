package com.sierra_dorada.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EstadoPedidoRequest(
    @NotBlank
    @Pattern(
        regexp = "Pendiente|Confirmado|En preparación|Enviado|Entregado|Cancelado",
        message = "El estado del pedido no es válido")
    String estado
) { }
