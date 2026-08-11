package com.sierra_dorada.controller;

import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.model.Categoria;
import com.sierra_dorada.repository.CategoriaRepository;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaRepository repositorio;

    public CategoriaController(CategoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    @SecurityRequirements
    @Cacheable("categorias")
    public List<Categoria> listar() {
        return repositorio.findAll();
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Cacheable(value = "categorias", key = "#id")
    public Categoria obtener(@PathVariable Integer id) {
        return buscar(id);
    }

    @PostMapping
    @Caching(evict = {
        @CacheEvict(value = "categorias", allEntries = true),
        @CacheEvict(value = "productos", allEntries = true)
    })
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria categoria) {
        categoria.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repositorio.save(categoria));
    }

    @PutMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(value = "categorias", allEntries = true),
        @CacheEvict(value = "productos", allEntries = true)
    })
    public Categoria actualizar(@PathVariable Integer id, @Valid @RequestBody Categoria categoria) {
        buscar(id);
        categoria.setId(id);
        return repositorio.save(categoria);
    }

    @DeleteMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(value = "categorias", allEntries = true),
        @CacheEvict(value = "productos", allEntries = true)
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        repositorio.delete(buscar(id));
        return ResponseEntity.noContent().build();
    }

    private Categoria buscar(Integer id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }
}
