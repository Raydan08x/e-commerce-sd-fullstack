package com.sierra_dorada.service;

import com.sierra_dorada.dto.FirmaBoldRequest;
import com.sierra_dorada.dto.FirmaBoldResponse;
import com.sierra_dorada.dto.ResultadoPagoBoldResponse;
import com.sierra_dorada.exception.ConflictoException;
import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.model.MetodoPago;
import com.sierra_dorada.model.Pago;
import com.sierra_dorada.model.Pedido;
import com.sierra_dorada.repository.MetodoPagoRepository;
import com.sierra_dorada.repository.PagoRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Service
public class PagoService {
    private static final Logger LOG = LoggerFactory.getLogger(PagoService.class);
    private final PagoRepository pagos;
    private final MetodoPagoRepository metodos;
    private final PedidoService pedidos;
    private final String secretoBold;
    private final String moneda;
    private final ObjectMapper objectMapper;
    private final EnvioService envios;
    private final BoldClient bold;
    private final NotificacionPedidoService notificaciones;
    private final String urlSeguimiento;

    public PagoService(PagoRepository pagos, MetodoPagoRepository metodos, PedidoService pedidos,
                       @Value("${app.bold.secret-key}") String secretoBold,
                       @Value("${app.bold.currency}") String moneda,
                       ObjectMapper objectMapper, EnvioService envios, BoldClient bold,
                       NotificacionPedidoService notificaciones,
                       @Value("${app.mipaquete.tracking-url:https://app.mipaquete.com/seguimiento-envio}")
                           String urlSeguimiento) {
        this.pagos = pagos;
        this.metodos = metodos;
        this.pedidos = pedidos;
        this.secretoBold = secretoBold;
        this.moneda = moneda;
        this.objectMapper = objectMapper;
        this.envios = envios;
        this.bold = bold;
        this.notificaciones = notificaciones;
        this.urlSeguimiento = urlSeguimiento;
    }

    @Transactional
    public void procesarWebhook(String firmaRecibida, byte[] cuerpo) {
        validarFirmaWebhook(firmaRecibida, cuerpo);
        @SuppressWarnings("unchecked")
        Map<String, Object> evento = objectMapper.readValue(cuerpo, Map.class);
        String tipo = String.valueOf(evento.get("type"));
        Map<String, Object> data = mapa(evento.get("data"));
        Map<String, Object> metadata = mapa(data.get("metadata"));
        String referencia = String.valueOf(metadata.get("reference"));

        Pago pago = pagos.findByTransaccionId(referencia)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado"));
        validarMontoWebhook(pago, data);

        switch (tipo) {
            case "SALE_APPROVED" -> confirmarPago(pago);
            case "SALE_REJECTED" -> rechazarPago(pago);
            case "VOID_APPROVED" -> {
                if ("Completado".equals(pago.getEstado())) {
                    pago.setEstado("Reembolsado");
                    pagos.save(pago);
                    pedidos.cambiarEstado(pago.getPedido().getId(), "Cancelado");
                }
            }
            case "VOID_REJECTED" -> { /* No cambia el estado confirmado. */ }
            default -> throw new IllegalArgumentException("Tipo de evento Bold no reconocido");
        }
    }

    @Transactional
    public ResultadoPagoBoldResponse confirmarRetorno(
            String referencia, String email, boolean administrador) {
        Pago pago = pagos.findByTransaccionId(referencia)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado"));
        pedidos.obtenerAutorizado(pago.getPedido().getId(), email, administrador);

        String estadoBold = estadoBoldLocal(pago);
        if (!esFinalLocal(pago)) {
            Map<String, Object> transaccion = bold.consultarTransaccion(referencia);
            validarReferenciaConsulta(referencia, transaccion);
            estadoBold = String.valueOf(
                transaccion.getOrDefault("payment_status", "NO_TRANSACTION_FOUND"));
            validarMontoConsulta(pago, transaccion);
            aplicarEstadoConsulta(pago, estadoBold);
        }
        return resultado(pago, estadoBold);
    }

    private void aplicarEstadoConsulta(Pago pago, String estadoBold) {
        switch (estadoBold) {
            case "APPROVED" -> confirmarPago(pago);
            case "REJECTED", "FAILED" -> rechazarPago(pago);
            case "VOIDED" -> {
                if ("Completado".equals(pago.getEstado())) pago.setEstado("Reembolsado");
                else if ("Pendiente".equals(pago.getEstado())) pago.setEstado("Fallido");
                pagos.save(pago);
                pedidos.cambiarEstado(pago.getPedido().getId(), "Cancelado");
            }
            case "PROCESSING", "PENDING", "NO_TRANSACTION_FOUND" -> { /* Aún no es final. */ }
            default -> throw new IllegalArgumentException("Estado Bold no reconocido");
        }
    }

    private void confirmarPago(Pago pago) {
        if ("Completado".equals(pago.getEstado())) return;
        if ("Cancelado".equals(pago.getPedido().getEstado())) {
            throw new ConflictoException("El pedido asociado al pago está cancelado");
        }
        pago.setEstado("Completado");
        pagos.save(pago);
        Pedido pedido = pedidos.cambiarEstado(pago.getPedido().getId(), "Confirmado");
        intentarGenerarGuia(pedido);
        notificaciones.enviarConfirmacion(pedido);
    }

    private void rechazarPago(Pago pago) {
        if (!"Pendiente".equals(pago.getEstado())) return;
        pago.setEstado("Fallido");
        pagos.save(pago);
        pedidos.cambiarEstado(pago.getPedido().getId(), "Cancelado");
    }

    private void validarFirmaWebhook(String firmaRecibida, byte[] cuerpo) {
        if (!StringUtils.hasText(firmaRecibida) || !StringUtils.hasText(secretoBold)) {
            throw new SecurityException("Webhook Bold no autorizado");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretoBold.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String base64 = Base64.getEncoder().encodeToString(cuerpo);
            String esperada = HexFormat.of().formatHex(
                mac.doFinal(base64.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(
                esperada.getBytes(StandardCharsets.UTF_8),
                firmaRecibida.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
                throw new SecurityException("Firma de webhook Bold inválida");
            }
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException("HMAC-SHA256 no está disponible", excepcion);
        } catch (java.security.InvalidKeyException excepcion) {
            throw new IllegalStateException("La llave Bold no es válida", excepcion);
        }
    }

    private void validarMontoWebhook(Pago pago, Map<String, Object> data) {
        Map<String, Object> amount = mapa(data.get("amount"));
        int recibido = new BigDecimal(String.valueOf(amount.get("total"))).intValueExact();
        int esperado = pago.getMonto().intValueExact();
        if (recibido != esperado || !moneda.equals(String.valueOf(amount.get("currency")))) {
            throw new SecurityException("El monto o la moneda del webhook no coinciden");
        }
    }

    private void validarReferenciaConsulta(String referencia, Map<String, Object> transaccion) {
        Object recibida = transaccion.get("reference_id");
        if (recibida != null && !referencia.equals(String.valueOf(recibida))) {
            throw new SecurityException("La referencia devuelta por Bold no coincide");
        }
    }

    private void validarMontoConsulta(Pago pago, Map<String, Object> transaccion) {
        Object total = transaccion.get("total");
        if (total == null) return;
        BigDecimal recibido = new BigDecimal(String.valueOf(total));
        if (recibido.compareTo(pago.getMonto()) != 0) {
            throw new SecurityException("El monto devuelto por Bold no coincide con el pedido");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapa(Object valor) {
        if (valor instanceof Map<?, ?> mapa) return (Map<String, Object>) mapa;
        throw new IllegalArgumentException("Payload Bold incompleto");
    }

    private void intentarGenerarGuia(Pedido pedido) {
        if (!envios.generacionGuiasHabilitada()) {
            LOG.info("Pedido {} confirmado en modo de prueba; no se crea guía Mi Paquete",
                pedido.getId());
            return;
        }
        try {
            envios.generarGuia(pedido);
        } catch (RuntimeException excepcion) {
            // El pago queda confirmado. Un administrador puede reintentar la guía sin duplicar el cobro.
            LOG.warn("Pago confirmado, pero no fue posible generar la guía del pedido {}",
                pedido.getId(), excepcion);
        }
    }

    private boolean esFinalLocal(Pago pago) {
        return List.of("Completado", "Fallido", "Reembolsado").contains(pago.getEstado());
    }

    private String estadoBoldLocal(Pago pago) {
        return switch (pago.getEstado()) {
            case "Completado" -> "APPROVED";
            case "Fallido" -> "FAILED";
            case "Reembolsado" -> "VOIDED";
            default -> "NO_TRANSACTION_FOUND";
        };
    }

    private ResultadoPagoBoldResponse resultado(Pago pago, String estadoBold) {
        Pedido pedido = pago.getPedido();
        var envio = pedido.getEnvio();
        List<ResultadoPagoBoldResponse.LineaPedido> lineas = pedido.getDetalles().stream()
            .map(detalle -> new ResultadoPagoBoldResponse.LineaPedido(
                detalle.getProducto().getNombre(), detalle.getCantidad(),
                detalle.getPrecioUnitario(), detalle.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()))))
            .toList();
        var resumen = new ResultadoPagoBoldResponse.ResumenPedido(
            pedido.getId(), pedido.getEstado(), pedido.getFechaPedido(), pedido.getSubtotal(),
            pedido.getCostoEnvio(), pedido.getTotal(), pedido.getDireccionEnvio(), lineas,
            envio == null ? null : envio.getEstado(),
            envio == null ? null : envio.getCodigoMiPaquete(),
            envio == null ? null : envio.getNumeroGuia(), urlSeguimiento);
        boolean confirmado = "Completado".equals(pago.getEstado());
        return new ResultadoPagoBoldResponse(pago.getTransaccionId(), estadoBold, confirmado,
            mensajeResultado(pago), resumen);
    }

    private String mensajeResultado(Pago pago) {
        if ("Completado".equals(pago.getEstado())) {
            return envios.generacionGuiasHabilitada()
                ? "Compra realizada con éxito. Estamos preparando tu envío."
                : "Compra realizada con éxito. El envío queda pendiente hasta activar Mi Paquete.";
        }
        if ("Fallido".equals(pago.getEstado())) return "El pago no fue aprobado por Bold.";
        if ("Reembolsado".equals(pago.getEstado())) return "El pago fue anulado o reembolsado.";
        return "Bold todavía está procesando la transacción.";
    }

    @Transactional
    public FirmaBoldResponse firmar(FirmaBoldRequest solicitud, String email, boolean administrador) {
        if (!StringUtils.hasText(secretoBold)) {
            throw new IllegalStateException("Bold no está configurado en el servidor");
        }
        Pedido pedido = pedidos.obtenerAutorizado(solicitud.pedidoId(), email, administrador);
        if (!"Pendiente".equals(pedido.getEstado())) {
            throw new ConflictoException("Solo se pueden pagar pedidos pendientes");
        }
        if (pagos.existsByTransaccionId(solicitud.orderId())) {
            throw new ConflictoException("La orden de pago ya fue utilizada");
        }

        int monto;
        try {
            monto = pedido.getTotal().intValueExact();
        } catch (ArithmeticException excepcion) {
            throw new IllegalStateException("El total del pedido no es válido para Bold");
        }
        if (monto < 1000 || monto > 10_000_000) {
            throw new IllegalArgumentException("El monto está fuera del rango permitido");
        }

        MetodoPago metodo = pedido.getMetodoPago();
        if (metodo == null) {
            metodo = metodos.findByNombreIgnoreCase("Bold")
                .orElseThrow(() -> new RecursoNoEncontradoException(
                    "El método de pago Bold no está configurado"));
            pedido.setMetodoPago(metodo);
        }

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(metodo);
        pago.setMonto(pedido.getTotal());
        pago.setTransaccionId(solicitud.orderId());
        pagos.save(pago);

        String firma = sha256(solicitud.orderId() + monto + moneda + secretoBold);
        return new FirmaBoldResponse(firma, monto, moneda, pedido.getId());
    }

    private String sha256(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException("SHA-256 no está disponible", excepcion);
        }
    }
}
