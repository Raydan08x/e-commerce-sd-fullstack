package com.sierra_dorada.repository;

import com.sierra_dorada.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    List<Pago> findByPedidoId(Integer pedidoId);
    boolean existsByTransaccionId(String transaccionId);
    Optional<Pago> findByTransaccionId(String transaccionId);
}
