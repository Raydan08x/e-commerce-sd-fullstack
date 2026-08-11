package com.sierra_dorada.service;

import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.exception.ConflictoException;
import com.sierra_dorada.model.Categoria;
import com.sierra_dorada.model.Producto;
import com.sierra_dorada.repository.CategoriaRepository;
import com.sierra_dorada.repository.ProductoRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @Cacheable(value = "productos",
        condition = "(#textoBusqueda == null || #textoBusqueda.isBlank()) && #categoriaId == null")
    public List<Producto> listar(boolean soloActivos, String textoBusqueda, Integer categoriaId) {
        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            return productos.findByNombreContainingIgnoreCase(textoBusqueda);
        }
        if (categoriaId != null) {
            return productos.findByCategoriaId(categoriaId);
        }
        return soloActivos ? productos.findByActivoTrue() : productos.findAll();
    }

    public Producto obtener(Integer id) {
        return productos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    @CacheEvict(value = "productos", allEntries = true)
    public Producto crear(Producto producto) {
        validarCodigoDisponible(producto.getCodigo(), null);
        producto.setId(null);
        return guardar(producto);
    }

    @CacheEvict(value = "productos", allEntries = true)
    public Producto actualizar(Integer id, Producto datos) {
        Producto actual = obtener(id);
        validarCodigoDisponible(datos.getCodigo(), id);
        datos.setId(actual.getId());
        datos.setFechaCreacion(actual.getFechaCreacion());
        return guardar(datos);
    }

    @CacheEvict(value = "productos", allEntries = true)
    public void eliminar(Integer id) {
        Producto producto = obtener(id);
        producto.setActivo(false);
        productos.save(producto);
    }

    private Producto guardar(Producto producto) {
        validarEmpaque(producto);
        producto.setCategoria(resolverCategoria(producto));
        return productos.save(producto);
    }

    private void validarEmpaque(Producto producto) {
        long camposConfigurados = java.util.stream.Stream.of(
                producto.getPesoEnvioKg(), producto.getAnchoEnvioCm(),
                producto.getLargoEnvioCm(), producto.getAltoEnvioCm())
            .filter(java.util.Objects::nonNull)
            .count();
        if (camposConfigurados != 0 && camposConfigurados != 4) {
            throw new IllegalArgumentException(
                "El empaque personalizado requiere peso, ancho, largo y alto");
        }
    }

    private Categoria resolverCategoria(Producto producto) {
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            return null;
        }

        return categorias.findById(producto.getCategoria().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    private void validarCodigoDisponible(String codigo, Integer idPermitido) {
        productos.findByCodigo(codigo)
            .filter(producto -> !producto.getId().equals(idPermitido))
            .ifPresent(producto -> {
                throw new ConflictoException("Ya existe un producto con ese código");
            });
    }
}
