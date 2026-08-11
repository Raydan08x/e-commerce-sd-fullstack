package com.sierra_dorada.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ResultadoPagoBoldResponse(
    String referenciaPago,
    String estadoPago,
    boolean confirmado,
    String mensaje,
    ResumenPedido pedido
) {
    public record ResumenPedido(
        Integer id,
        String estado,
        LocalDateTime fechaPedido,
        BigDecimal subtotal,
        BigDecimal costoEnvio,
        BigDecimal total,
        String direccionEnvio,
        List<LineaPedido> productos,
        String estadoEnvio,
        Long codigoMiPaquete,
        String numeroGuia,
        String urlSeguimiento
    ) { }

    public record LineaPedido(
        String nombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
    ) { }
}
