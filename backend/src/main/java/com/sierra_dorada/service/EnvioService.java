package com.sierra_dorada.service;

import com.sierra_dorada.config.MiPaqueteProperties;
import com.sierra_dorada.dto.CotizacionEnvioRequest;
import com.sierra_dorada.dto.CrearPedidoRequest;
import com.sierra_dorada.dto.DetallePedidoRequest;
import com.sierra_dorada.dto.UbicacionResponse;
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
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnvioService {
    private static final int UNIDADES_POR_CAJA = 24;
    private static final int MAXIMO_UNIDADES_POR_PEDIDO = 240;
    private static final BigDecimal PESO_CAJA_24_KG = new BigDecimal("16.6");
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

    public List<UbicacionResponse> ubicaciones(String consulta) {
        String criterio = normalizar(consulta);
        if (criterio.length() < 2) {
            return List.of();
        }

        return cliente.obtenerUbicaciones().stream()
            .map(this::convertirUbicacion)
            .filter(ubicacion -> coincide(ubicacion, criterio))
            .sorted(Comparator
                .comparingInt((UbicacionResponse ubicacion) -> prioridad(ubicacion, criterio))
                .thenComparing(UbicacionResponse::nombre)
                .thenComparing(UbicacionResponse::departamento))
            .collect(Collectors.toMap(
                UbicacionResponse::codigo,
                Function.identity(),
                (primera, repetida) -> primera,
                LinkedHashMap::new))
            .values().stream()
            .limit(12)
            .toList();
    }

    private UbicacionResponse convertirUbicacion(Map<String, Object> ubicacion) {
        return new UbicacionResponse(
            texto(ubicacion.get("locationCode")),
            texto(ubicacion.get("locationName")),
            texto(ubicacion.get("departmentOrStateName")));
    }

    private boolean coincide(UbicacionResponse ubicacion, String criterio) {
        String codigo = normalizar(ubicacion.codigo());
        String nombreCompleto = normalizar(ubicacion.nombre() + " " + ubicacion.departamento());
        return codigo.startsWith(criterio)
            || java.util.Arrays.stream(criterio.split("\\s+"))
                .allMatch(nombreCompleto::contains);
    }

    private int prioridad(UbicacionResponse ubicacion, String criterio) {
        String nombre = normalizar(ubicacion.nombre());
        if (nombre.equals(criterio)) return 0;
        if (nombre.startsWith(criterio)) return 1;
        return 2;
    }

    private String normalizar(String texto) {
        if (!StringUtils.hasText(texto)) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .trim();
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor).trim();
    }

    public List<Map<String, Object>> cotizar(CotizacionEnvioRequest solicitud) {
        Paquete paquete = calcularPaquete(solicitud.detalles());
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("originCountryCode", propiedades.getOriginCountryCode());
        cuerpo.put("originLocationCode", propiedades.getOriginDaneCode());
        cuerpo.put("destinyCountryCode", "170");
        cuerpo.put("destinyLocationCode", solicitud.destinoCodigo());
        cuerpo.put("quantity", paquete.cantidadBultos());
        cuerpo.put("width", paquete.ancho());
        cuerpo.put("length", paquete.largo());
        cuerpo.put("height", paquete.alto());
        cuerpo.put("weight", paquete.pesoPorBulto());
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
        if (!propiedades.isCreateShipmentEnabled()) {
            envio.setEstado("PENDIENTE_ACTIVACION");
        }
        return envio;
    }

    public boolean generacionGuiasHabilitada() {
        return propiedades.isCreateShipmentEnabled();
    }

    @Transactional
    public Envio generarGuia(Pedido pedido) {
        if (!propiedades.isCreateShipmentEnabled()) {
            throw new ConflictoException(
                "La creación de envíos Mi Paquete está desactivada en este ambiente");
        }
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
        envio.setNumeroGuia(textoPrimero(respuesta,
            "guideNumber", "guide", "trackingNumber", "numeroGuia"));
        envio.setUrlGuia(textoPrimero(respuesta,
            "guideUrl", "urlGuide", "url", "urlGuia"));
        envio.setEstado("GENERADO");
        return envios.save(envio);
    }

    private String textoPrimero(Map<String, Object> respuesta, String... llaves) {
        for (String llave : llaves) {
            Object dato = respuesta.get(llave);
            if (dato != null && StringUtils.hasText(String.valueOf(dato))) {
                return String.valueOf(dato);
            }
        }
        return null;
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
        Paquete paquete = calcularPaquete(pedido);
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
            "height", paquete.alto(),
            "large", paquete.largo(),
            "productReference", "PEDIDO-" + pedido.getId(),
            "quantity", paquete.cantidadBultos(),
            "weight", paquete.pesoPorBulto(),
            "width", paquete.ancho()));
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
        Carga carga = new Carga();
        for (DetallePedidoRequest detalle : detalles) {
            Producto producto = productos.findById(detalle.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
            if (!Boolean.TRUE.equals(producto.getActivo()) || producto.getStock() < detalle.cantidad()) {
                throw new IllegalArgumentException("Producto inactivo o sin stock: " + producto.getNombre());
            }
            agregarProducto(carga, producto, detalle.cantidad());
            valor = valor.add(producto.getPrecio().multiply(BigDecimal.valueOf(detalle.cantidad())));
        }
        return especificacionPaquete(carga, valor);
    }

    private Paquete calcularPaquete(Pedido pedido) {
        Carga carga = new Carga();
        for (var detalle : pedido.getDetalles()) {
            agregarProducto(carga, detalle.getProducto(), detalle.getCantidad());
        }
        return especificacionPaquete(carga, pedido.getSubtotal());
    }

    private void agregarProducto(Carga carga, Producto producto, int cantidad) {
        try {
            if (tieneEmpaquePersonalizado(producto)) {
                validarEmpaquePersonalizado(producto);
                carga.pesoPersonalizado = carga.pesoPersonalizado.add(
                    producto.getPesoEnvioKg().multiply(BigDecimal.valueOf(cantidad)));
                long volumenUnidad = Math.multiplyExact(
                    Math.multiplyExact((long) producto.getAnchoEnvioCm(), producto.getLargoEnvioCm()),
                    producto.getAltoEnvioCm());
                carga.volumenPersonalizado = Math.addExact(carga.volumenPersonalizado,
                    Math.multiplyExact(volumenUnidad, cantidad));
                carga.anchoPersonalizado = Math.max(carga.anchoPersonalizado,
                    producto.getAnchoEnvioCm());
                carga.largoPersonalizado = Math.max(carga.largoPersonalizado,
                    producto.getLargoEnvioCm());
                carga.altoPersonalizado = Math.max(carga.altoPersonalizado,
                    producto.getAltoEnvioCm());
            } else {
                int unidadesDetalle = Math.multiplyExact(
                    producto.getUnidadesPorProducto(), cantidad);
                carga.unidadesBebida = Math.addExact(carga.unidadesBebida, unidadesDetalle);
            }
        } catch (ArithmeticException excepcion) {
            throw new IllegalArgumentException("La cantidad solicitada es demasiado grande");
        }
    }

    private boolean tieneEmpaquePersonalizado(Producto producto) {
        return producto.getPesoEnvioKg() != null
            || producto.getAnchoEnvioCm() != null
            || producto.getLargoEnvioCm() != null
            || producto.getAltoEnvioCm() != null;
    }

    private void validarEmpaquePersonalizado(Producto producto) {
        if (producto.getPesoEnvioKg() == null || producto.getAnchoEnvioCm() == null
            || producto.getLargoEnvioCm() == null || producto.getAltoEnvioCm() == null) {
            throw new IllegalArgumentException(
                "Completa peso, ancho, largo y alto de envío para: " + producto.getNombre());
        }
    }

    private Paquete especificacionPaquete(Carga carga, BigDecimal valorDeclarado) {
        if (carga.unidadesBebida < 1 && carga.volumenPersonalizado < 1) {
            throw new IllegalArgumentException("El envío debe contener al menos una unidad física");
        }
        if (carga.unidadesBebida > MAXIMO_UNIDADES_POR_PEDIDO) {
            throw new IllegalArgumentException(
                "El pedido supera el máximo de 240 unidades físicas por envío");
        }

        int cantidadBultos = Math.max(1,
            (carga.unidadesBebida + UNIDADES_POR_CAJA - 1) / UNIDADES_POR_CAJA);
        long volumenPersonalizadoProtegido = (long) Math.ceil(carga.volumenPersonalizado * 1.10d);

        while (cantidadBultos <= 10) {
            int unidadesPorBulto = carga.unidadesBebida == 0 ? 0
                : (carga.unidadesBebida + cantidadBultos - 1) / cantidadBultos;
            Dimensiones base = unidadesPorBulto == 0
                ? new Dimensiones(carga.anchoPersonalizado, carga.largoPersonalizado,
                    carga.altoPersonalizado)
                : dimensionesPara(unidadesPorBulto);
            int ancho = Math.max(base.ancho(), carga.anchoPersonalizado);
            int largo = Math.max(base.largo(), carga.largoPersonalizado);
            long volumenBase = unidadesPorBulto == 0 ? 0L
                : (long) base.ancho() * base.largo() * base.alto();
            long volumenPorBulto = volumenBase
                + dividirRedondeandoArriba(volumenPersonalizadoProtegido, cantidadBultos);
            int altoPorVolumen = (int) dividirRedondeandoArriba(volumenPorBulto,
                Math.max(1L, (long) ancho * largo));
            int alto = Math.max(Math.max(base.alto(), carga.altoPersonalizado), altoPorVolumen);

            BigDecimal pesoBebidas = unidadesPorBulto == 0 ? BigDecimal.ZERO
                : PESO_CAJA_24_KG.multiply(BigDecimal.valueOf(unidadesPorBulto))
                    .divide(BigDecimal.valueOf(UNIDADES_POR_CAJA), 6, RoundingMode.HALF_UP)
                    .add(pesoProteccion(unidadesPorBulto));
            BigDecimal pesoPersonalizadoPorBulto = carga.pesoPersonalizado
                .divide(BigDecimal.valueOf(cantidadBultos), 6, RoundingMode.CEILING);
            int peso = pesoBebidas.add(pesoPersonalizadoPorBulto)
                .setScale(0, RoundingMode.CEILING).intValueExact();

            if (alto <= 60 && peso <= 25) {
                return new Paquete(cantidadBultos, ancho, largo, alto, peso, valorDeclarado);
            }
            cantidadBultos++;
        }
        throw new IllegalArgumentException(
            "El pedido requiere más de 10 bultos; solicita una cotización mayorista");
    }

    private long dividirRedondeandoArriba(long dividendo, long divisor) {
        return dividendo == 0 ? 0 : 1 + (dividendo - 1) / divisor;
    }

    private Dimensiones dimensionesPara(int unidades) {
        if (unidades == 1) return new Dimensiones(10, 10, 25);
        if (unidades == 2) return new Dimensiones(10, 18, 25);
        if (unidades <= 4) return new Dimensiones(18, 18, 25);
        if (unidades <= 8) return new Dimensiones(18, 35, 25);
        if (unidades <= 12) return new Dimensiones(18, 51, 25);
        if (unidades <= 16) return new Dimensiones(35, 35, 25);
        if (unidades <= 23) return new Dimensiones(35, 51, 25);
        return new Dimensiones(29, 42, 27);
    }

    private BigDecimal pesoProteccion(int unidades) {
        if (unidades == 1) return new BigDecimal("0.20");
        if (unidades == 2) return new BigDecimal("0.25");
        if (unidades == 3) return new BigDecimal("0.30");
        if (unidades == 4) return new BigDecimal("0.35");
        if (unidades <= 8) return new BigDecimal("0.50");
        if (unidades <= 12) return new BigDecimal("0.70");
        if (unidades <= 16) return new BigDecimal("0.85");
        if (unidades <= 23) return new BigDecimal("1.10");
        return new BigDecimal("0.60");
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

    private record Dimensiones(int ancho, int largo, int alto) { }
    private record Paquete(int cantidadBultos, int ancho, int largo, int alto,
                           int pesoPorBulto, BigDecimal valorDeclarado) { }
    private static final class Carga {
        private int unidadesBebida;
        private BigDecimal pesoPersonalizado = BigDecimal.ZERO;
        private long volumenPersonalizado;
        private int anchoPersonalizado;
        private int largoPersonalizado;
        private int altoPersonalizado;
    }
    public record OpcionEnvio(String id, String nombre, BigDecimal costo) { }
}
