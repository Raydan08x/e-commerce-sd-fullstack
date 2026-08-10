package com.sierra_dorada.controller;

import com.sierra_dorada.dto.CotizacionEnvioRequest;
import com.sierra_dorada.model.Envio;
import com.sierra_dorada.model.Pedido;
import com.sierra_dorada.service.EnvioService;
import com.sierra_dorada.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {
    private final EnvioService envios;
    private final PedidoService pedidos;

    public EnvioController(EnvioService envios, PedidoService pedidos) {
        this.envios = envios;
        this.pedidos = pedidos;
    }

    @GetMapping("/ubicaciones")
    public List<Map<String, Object>> ubicaciones(
        @RequestParam(required = false) String codigo) {
        return envios.ubicaciones(codigo);
    }

    @PostMapping("/cotizaciones")
    public List<Map<String, Object>> cotizar(
        @Valid @RequestBody CotizacionEnvioRequest solicitud) {
        return envios.cotizar(solicitud);
    }

    @PostMapping("/pedidos/{pedidoId}/guia")
    public ResponseEntity<Envio> generarGuia(@PathVariable Integer pedidoId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(envios.generarGuia(pedidos.obtener(pedidoId)));
    }

    @GetMapping("/pedidos/{pedidoId}/tracking")
    public Map<String, Object> tracking(@PathVariable Integer pedidoId,
                                        Authentication autenticacion) {
        Pedido pedido = pedidos.obtenerAutorizado(
            pedidoId, autenticacion.getName(), esAdmin(autenticacion));
        return envios.tracking(envios.obtenerPorPedido(pedido.getId()));
    }

    @PostMapping("/webhook/estados")
    public ResponseEntity<Void> webhook(
        @RequestHeader("X-Webhook-Secret") String secreto,
        @RequestBody Map<String, Object> payload) {
        envios.registrarWebhook(secreto, payload);
        return ResponseEntity.noContent().build();
    }

    private boolean esAdmin(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
            .anyMatch(autoridad -> "ROLE_ADMIN".equals(autoridad.getAuthority()));
    }
}
