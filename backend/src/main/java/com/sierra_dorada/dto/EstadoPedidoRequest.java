package com.sierra_dorada.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Cambio de estado operativo de un pedido")
public record EstadoPedidoRequest(
    @NotBlank
    @Pattern(
        regexp = "Pendiente|Confirmado|En preparación|Enviado|Entregado|Cancelado",
        message = "El estado del pedido no es válido")
    @Schema(
        description = "Nuevo estado del pedido",
        example = "En preparación",
        allowableValues = {
            "Pendiente", "Confirmado", "En preparación", "Enviado", "Entregado", "Cancelado"
        })
    String estado
) { }
