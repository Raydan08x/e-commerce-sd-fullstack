package com.sierra_dorada.service;

import com.sierra_dorada.config.MiPaqueteProperties;
import com.sierra_dorada.dto.CotizacionEnvioRequest;
import com.sierra_dorada.dto.CrearPedidoRequest;
import com.sierra_dorada.dto.DetallePedidoRequest;
import com.sierra_dorada.exception.ConflictoException;
import com.sierra_dorada.exception.IntegracionExternaException;
import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.model.Envio;
import com.sierra_dorada.model.EventoEnvio;
import com.sierra_dorada.model.Pedido;
import com.sierra_dorada.model.Producto;
import com.sierra_dorada.repository.EnvioRepository;
import com.sierra_dorada.repository.EventoEnvioRepository;
import com.sierra_dorada.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnvioService {
    private final MiPaqueteClient cliente;
    private final MiPaqueteProperties propiedades;
    private final ProductoRepository productos;
    private final EnvioRepository envios;
    private final EventoEnvioRepository eventos;
    private final ObjectMapper objectMapper;

    public EnvioService(MiPaqueteClient cliente, MiPaqueteProperties propiedades,
                        ProductoRepository productos, EnvioRepository envios,
                        EventoEnvioRepository eventos, ObjectMapper objectMapper) {
        this.cliente = cliente;
        this.propiedades = propiedades;
        this.productos = productos;
        this.envios = envios;
        this.eventos = eventos;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> ubicaciones(String codigo) {
        return cliente.obtenerUbicaciones(codigo);
    }

    public List<Map<String, Object>> cotizar(CotizacionEnvioRequest solicitud) {
        Paquete paquete = calcularPaquete(solicitud.detalles());
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("originCountryCode", propiedades.getOriginCountryCode());
        cuerpo.put("originLocationCode", propiedades.getOriginDaneCode());
        cuerpo.put("destinyCountryCode", "170");
        cuerpo.put("destinyLocationCode", solicitud.destinoCodigo());
        cuerpo.put("quantity", paquete.cantidad());
        cuerpo.put("width", 20);
        cuerpo.put("length", 20);
        cuerpo.put("height", 15);
        cuerpo.put("weight", paquete.peso());
        cuerpo.put("declaredValue", paquete.valorDeclarado());
        return cliente.cotizar(cuerpo);
    }

    public OpcionEnvio seleccionar(CotizacionEnvioRequest solicitud, String transportadoraId) {
        return cotizar(solicitud).stream()
            .filter(opcion -> transportadoraId.equals(String.valueOf(opcion.get("deliveryCompanyId"))))
            .findFirst()
            .map(opcion -> new OpcionEnvio(
                transportadoraId,
                String.valueOf(opcion.get("deliveryCompanyName")),
                decimal(opcion.get("shippingCost"))))
            .orElseThrow(() -> new IllegalArgumentException(
                "La transportadora seleccionada no está disponible para este destino"));
    }

    public Envio preparar(Pedido pedido, CrearPedidoRequest solicitud, OpcionEnvio opcion) {
        Envio envio = new Envio();
        envio.setPedido(pedido);
        envio.setCodigoDaneOrigen(propiedades.getOriginDaneCode());
        envio.setCodigoDaneDestino(solicitud.destinoCodigo());
        envio.setDestinatarioNombre(solicitud.destinatarioNombre());
        envio.setDestinatarioApellido(solicitud.destinatarioApellido());
        envio.setDestinatarioEmail(solicitud.destinatarioEmail());
        envio.setDestinatarioTelefono(solicitud.destinatarioTelefono());
        envio.setDireccionDestino(solicitud.direccionEnvio());
        envio.setTransportadoraId(opcion.id());
        envio.setTransportadoraNombre(opcion.nombre());
        envio.setCosto(opcion.costo());
        return envio;
    }

    @Transactional
    public Envio generarGuia(Pedido pedido) {
        validarConfiguracionRemitente();
        Envio envio = envios.findByPedidoId(pedido.getId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Envío no encontrado"));
        if (envio.getCodigoMiPaquete() != null) {
            throw new ConflictoException("La guía de este pedido ya fue generada");
        }

        Map<String, Object> respuesta = cliente.crearEnvio(cuerpoCreacion(pedido, envio));
        Object codigo = respuesta.get("mpCode");
        if (codigo == null) {
            throw new IllegalStateException("Mi Paquete no devolvió el código del envío");
        }
        envio.setCodigoMiPaquete(Long.valueOf(String.valueOf(codigo)));
        envio.setEstado("GENERADO");
        return envios.save(envio);
    }

    public Map<String, Object> tracking(Envio envio) {
        if (envio.getCodigoMiPaquete() == null) {
            throw new ConflictoException("La guía todavía no ha sido generada");
        }
        return cliente.obtenerTracking(envio.getCodigoMiPaquete());
    }

    public Envio obtenerPorPedido(Integer pedidoId) {
        return envios.findByPedidoId(pedidoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Envío no encontrado"));
    }

    @Transactional
    public void registrarWebhook(String secreto, Map<String, Object> payload) {
        validarSecretoWebhook(secreto);
        Object codigo = payload.get("mpCode");
        if (codigo == null) {
            throw new IllegalArgumentException("El webhook no contiene mpCode");
        }
        Envio envio = envios.findByCodigoMiPaquete(Long.valueOf(String.valueOf(codigo)))
            .orElseThrow(() -> new RecursoNoEncontradoException("Envío no encontrado"));
        String estado = String.valueOf(payload.getOrDefault("status", "ACTUALIZADO"));
        envio.setEstado(estado);
        envios.save(envio);

        EventoEnvio evento = new EventoEnvio();
        evento.setEnvio(envio);
        evento.setEstado(estado);
        evento.setDescripcion(String.valueOf(payload.getOrDefault("description", "")));
        evento.setPayload(objectMapper.writeValueAsString(payload));
        eventos.save(evento);
    }

    private Map<String, Object> cuerpoCreacion(Pedido pedido, Envio envio) {
        int cantidad = pedido.getDetalles().stream().mapToInt(d -> d.getCantidad()).sum();
        String descripcion = pedido.getDetalles().stream()
            .map(d -> d.getProducto().getNombre() + " x" + d.getCantidad())
            .reduce((a, b) -> a + ", " + b).orElse("Pedido Sierra Dorada");

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("adminTransactionData", Map.of("saleValue", pedido.getSubtotal()));
        cuerpo.put("channel", "Sierra Dorada Ecommerce");
        cuerpo.put("comments", pedido.getNotas() == null ? "" : pedido.getNotas());
        cuerpo.put("criteria", "price");
        cuerpo.put("deliveryCompany", envio.getTransportadoraId());
        cuerpo.put("description", descripcion);
        cuerpo.put("locate", Map.of(
            "destinyDaneCode", envio.getCodigoDaneDestino(),
            "originDaneCode", envio.getCodigoDaneOrigen(),
            "originCountryCode", propiedades.getOriginCountryCode(),
            "destinyCountryCode", "170"));
        cuerpo.put("paymentType", 101);
        cuerpo.put("productInformation", Map.of(
            "declaredValue", pedido.getSubtotal(),
            "forbiddenProduct", propiedades.isForbiddenProduct(),
            "height", 15,
            "large", 20,
            "productReference", "PEDIDO-" + pedido.getId(),
            "quantity", cantidad,
            "weight", Math.max(1, cantidad),
            "width", 20));
        cuerpo.put("receiver", Map.of(
            "cellPhone", limpiarTelefono(envio.getDestinatarioTelefono()),
            "destinationAddress", envio.getDireccionDestino(),
            "email", envio.getDestinatarioEmail(),
            "name", envio.getDestinatarioNombre(),
            "nit", ".",
            "nitType", ".",
            "prefix", "+57",
            "surname", valor(envio.getDestinatarioApellido())));
        cuerpo.put("requestPickup", String.valueOf(propiedades.isRequestPickup()));
        cuerpo.put("sender", Map.of(
            "cellPhone", limpiarTelefono(propiedades.getSenderPhone()),
            "email", propiedades.getSenderEmail(),
            "name", propiedades.getSenderName(),
            "nit", propiedades.getSenderDocument(),
            "nitType", propiedades.getSenderDocumentType(),
            "pickupAddress", propiedades.getSenderAddress(),
            "prefix", "+57",
            "surname", valor(propiedades.getSenderSurname())));
        cuerpo.put("user", propiedades.getUserId());
        return cuerpo;
    }

    private Paquete calcularPaquete(List<DetallePedidoRequest> detalles) {
        BigDecimal valor = BigDecimal.ZERO;
        int cantidad = 0;
        for (DetallePedidoRequest detalle : detalles) {
            Producto producto = productos.findById(detalle.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
            if (!Boolean.TRUE.equals(producto.getActivo()) || producto.getStock() < detalle.cantidad()) {
                throw new IllegalArgumentException("Producto inactivo o sin stock: " + producto.getNombre());
            }
            cantidad += detalle.cantidad();
            valor = valor.add(producto.getPrecio().multiply(BigDecimal.valueOf(detalle.cantidad())));
        }
        return new Paquete(cantidad, Math.max(1, cantidad), valor);
    }

    private void validarSecretoWebhook(String recibido) {
        String esperado = propiedades.getWebhookSecret();
        if (!StringUtils.hasText(esperado) || !StringUtils.hasText(recibido)
            || !MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Webhook no autorizado");
        }
    }

    private void validarConfiguracionRemitente() {
        if (!StringUtils.hasText(propiedades.getSenderName())
            || !StringUtils.hasText(propiedades.getSenderEmail())
            || !StringUtils.hasText(propiedades.getSenderPhone())
            || !StringUtils.hasText(propiedades.getSenderDocument())
            || !StringUtils.hasText(propiedades.getSenderAddress())
            || !StringUtils.hasText(propiedades.getUserId())) {
            throw new IntegracionExternaException(
                "Faltan datos del remitente de Mi Paquete en la configuración del servidor");
        }
    }

    private BigDecimal decimal(Object valor) {
        return new BigDecimal(String.valueOf(valor));
    }

    private String limpiarTelefono(String telefono) {
        return telefono == null ? "" : telefono.replaceAll("\\D", "");
    }

    private String valor(String texto) {
        return StringUtils.hasText(texto) ? texto : ".";
    }

    private record Paquete(int cantidad, int peso, BigDecimal valorDeclarado) { }
    public record OpcionEnvio(String id, String nombre, BigDecimal costo) { }
}
