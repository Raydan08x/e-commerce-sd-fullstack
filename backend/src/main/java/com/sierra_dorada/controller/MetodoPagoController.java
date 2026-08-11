package com.sierra_dorada.controller;

import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.model.MetodoPago;
import com.sierra_dorada.repository.MetodoPagoRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {
    private final MetodoPagoRepository repositorio;

    public MetodoPagoController(MetodoPagoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    @SecurityRequirements
    public List<MetodoPago> listar(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return soloActivos ? repositorio.findByActivoTrue() : repositorio.findAll();
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    public MetodoPago obtener(@PathVariable Integer id) {
        return buscar(id);
    }

    @PostMapping
    public ResponseEntity<MetodoPago> crear(@Valid @RequestBody MetodoPago metodoPago) {
        metodoPago.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repositorio.save(metodoPago));
    }

    @PutMapping("/{id}")
    public MetodoPago actualizar(@PathVariable Integer id, @Valid @RequestBody MetodoPago metodoPago) {
        buscar(id);
        metodoPago.setId(id);
        return repositorio.save(metodoPago);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        repositorio.delete(buscar(id));
        return ResponseEntity.noContent().build();
    }

    private MetodoPago buscar(Integer id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Método de pago no encontrado"));
    }
}
