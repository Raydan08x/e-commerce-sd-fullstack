package com.sierra_dorada.controller;

import com.sierra_dorada.dto.CrearPedidoRequest;
import com.sierra_dorada.dto.EstadoPedidoRequest;
import com.sierra_dorada.model.Pedido;
import com.sierra_dorada.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) Integer usuarioId,
                               Authentication autenticacion) {
        return servicio.listar(autenticacion.getName(), esAdmin(autenticacion), usuarioId);
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Integer id, Authentication autenticacion) {
        return servicio.obtenerAutorizado(id, autenticacion.getName(), esAdmin(autenticacion));
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody CrearPedidoRequest solicitud,
                                        Authentication autenticacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicio.crear(solicitud, autenticacion.getName()));
    }

    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Integer id,
        @Valid @RequestBody EstadoPedidoRequest solicitud) {
        return servicio.cambiarEstado(id, solicitud.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean esAdmin(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
            .anyMatch(autoridad -> "ROLE_ADMIN".equals(autoridad.getAuthority()));
    }
}
