package com.sierra_dorada.controller;

import com.sierra_dorada.dto.FirmaBoldRequest;
import com.sierra_dorada.dto.FirmaBoldResponse;
import com.sierra_dorada.dto.ResultadoPagoBoldResponse;
import com.sierra_dorada.config.OpenApiConfig;
import com.sierra_dorada.model.Pago;
import com.sierra_dorada.repository.PagoRepository;
import com.sierra_dorada.service.PagoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {
    private final PagoRepository pagos;
    private final PagoService servicio;

    public PagoController(PagoRepository pagos, PagoService servicio) {
        this.pagos = pagos;
        this.servicio = servicio;
    }

    @GetMapping
    public List<Pago> listar(@RequestParam(required = false) Integer pedidoId) {
        return pedidoId == null ? pagos.findAll() : pagos.findByPedidoId(pedidoId);
    }

    @PostMapping("/bold/firma")
    public FirmaBoldResponse firmaBold(@Valid @RequestBody FirmaBoldRequest solicitud,
                                       Authentication autenticacion) {
        return servicio.firmar(
            solicitud, autenticacion.getName(), esAdmin(autenticacion));
    }

    @PostMapping("/bold/webhook")
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_FIRMA_BOLD)
    public ResponseEntity<Void> webhookBold(
        @RequestHeader("x-bold-signature") String firma,
        @RequestBody byte[] cuerpo) {
        servicio.procesarWebhook(firma, cuerpo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bold/ordenes/{referencia}/confirmacion")
    public ResultadoPagoBoldResponse confirmarRetorno(
            @PathVariable String referencia, Authentication autenticacion) {
        return servicio.confirmarRetorno(
            referencia, autenticacion.getName(), esAdmin(autenticacion));
    }

    private boolean esAdmin(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
            .anyMatch(autoridad -> "ROLE_ADMIN".equals(autoridad.getAuthority()));
    }
}
