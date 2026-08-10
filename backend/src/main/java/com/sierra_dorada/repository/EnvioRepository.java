package com.sierra_dorada.repository;

import com.sierra_dorada.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    Optional<Envio> findByPedidoId(Integer pedidoId);
    Optional<Envio> findByCodigoMiPaquete(Long codigoMiPaquete);
}
