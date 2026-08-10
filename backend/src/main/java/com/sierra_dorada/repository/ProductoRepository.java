package com.sierra_dorada.repository;

import com.sierra_dorada.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    @EntityGraph(attributePaths = {"categoria", "maridaje"})
    List<Producto> findByActivoTrue();

    @EntityGraph(attributePaths = {"categoria", "maridaje"})
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = {"categoria", "maridaje"})
    List<Producto> findByCategoriaId(Integer categoriaId);

    @Override
    @EntityGraph(attributePaths = {"categoria", "maridaje"})
    List<Producto> findAll();

    Optional<Producto> findByCodigo(String codigo);

    Optional<Producto> findFirstByNombreIgnoreCase(String nombre);

    boolean existsByCodigo(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdParaActualizar(@Param("id") Integer id);
}
