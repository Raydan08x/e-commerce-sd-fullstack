package com.sierra_dorada.repository;

import com.sierra_dorada.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    List<MetodoPago> findByActivoTrue();
    Optional<MetodoPago> findByNombreIgnoreCase(String nombre);
}
