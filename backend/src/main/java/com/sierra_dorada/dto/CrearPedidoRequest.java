package com.sierra_dorada.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearPedidoRequest(
    @NotBlank(message = "La dirección de envío es obligatoria")
    @Size(max = 500)
    String direccionEnvio,

    @NotBlank(message = "El código DANE de destino es obligatorio")
    @Pattern(regexp = "^\\d{5,12}$", message = "El código DANE no es válido")
    String destinoCodigo,

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    @Size(max = 150)
    String destinatarioNombre,

    @Size(max = 150)
    String destinatarioApellido,

    @NotBlank(message = "El correo del destinatario es obligatorio")
    @Email
    String destinatarioEmail,

    @NotBlank(message = "El teléfono del destinatario es obligatorio")
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "El teléfono no es válido")
    String destinatarioTelefono,

    @NotBlank(message = "Debes seleccionar una transportadora")
    String transportadoraId,

    Integer metodoPagoId,

    @Size(max = 2000)
    String notas,

    @NotEmpty(message = "El pedido debe contener productos")
    List<@Valid DetallePedidoRequest> detalles
) {
    public CotizacionEnvioRequest cotizacion() {
        return new CotizacionEnvioRequest(destinoCodigo, detalles);
    }
}
