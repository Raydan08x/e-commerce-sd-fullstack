package com.sierra_dorada.service;

import com.sierra_dorada.dto.FirmaBoldRequest;
import com.sierra_dorada.dto.FirmaBoldResponse;
import com.sierra_dorada.exception.ConflictoException;
import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.model.MetodoPago;
import com.sierra_dorada.model.Pago;
import com.sierra_dorada.model.Pedido;
import com.sierra_dorada.repository.MetodoPagoRepository;
import com.sierra_dorada.repository.PagoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class PagoService {
    private final PagoRepository pagos;
    private final MetodoPagoRepository metodos;
    private final PedidoService pedidos;
    private final String secretoBold;
    private final String moneda;
    private final ObjectMapper objectMapper;
    private final EnvioService envios;

    public PagoService(PagoRepository pagos, MetodoPagoRepository metodos, PedidoService pedidos,
                       @Value("${app.bold.secret-key}") String secretoBold,
                       @Value("${app.bold.currency}") String moneda,
                       ObjectMapper objectMapper, EnvioService envios) {
        this.pagos = pagos;
        this.metodos = metodos;
        this.pedidos = pedidos;
        this.secretoBold = secretoBold;
        this.moneda = moneda;
        this.objectMapper = objectMapper;
        this.envios = envios;
    }

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
            case "SALE_APPROVED" -> {
                if (!"Completado".equals(pago.getEstado())) {
                    if ("Cancelado".equals(pago.getPedido().getEstado())) {
                        throw new ConflictoException("El pedido asociado al pago está cancelado");
                    }
                    pago.setEstado("Completado");
                    pagos.save(pago);
                    Pedido pedido = pedidos.cambiarEstado(pago.getPedido().getId(), "Confirmado");
                    intentarGenerarGuia(pedido);
                }
            }
            case "SALE_REJECTED" -> {
                if ("Pendiente".equals(pago.getEstado())) {
                    pago.setEstado("Fallido");
                    pagos.save(pago);
                    pedidos.cambiarEstado(pago.getPedido().getId(), "Cancelado");
                }
            }
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
        int recibido = new java.math.BigDecimal(String.valueOf(amount.get("total"))).intValueExact();
        int esperado = pago.getMonto().intValueExact();
        if (recibido != esperado || !moneda.equals(String.valueOf(amount.get("currency")))) {
            throw new SecurityException("El monto o la moneda del webhook no coinciden");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapa(Object valor) {
        if (valor instanceof Map<?, ?> mapa) return (Map<String, Object>) mapa;
        throw new IllegalArgumentException("Payload Bold incompleto");
    }

    private void intentarGenerarGuia(Pedido pedido) {
        try {
            envios.generarGuia(pedido);
        } catch (RuntimeException excepcion) {
            // El pago queda confirmado. Un administrador puede reintentar la guía sin duplicar el cobro.
        }
    }

    @Transactional
    public FirmaBoldResponse firmar(FirmaBoldRequest solicitud, String email, boolean administrador) {
        if (!StringUtils.hasText(secretoBold)) {
            throw new IllegalStateException("Bold no está configurado en el servidor");
        }
        Pedido pedido = pedidos.obtenerAutorizado(
            solicitud.pedidoId(), email, administrador);
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
