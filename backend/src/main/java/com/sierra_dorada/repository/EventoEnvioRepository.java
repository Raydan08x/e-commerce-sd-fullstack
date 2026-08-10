package com.sierra_dorada.repository;

import com.sierra_dorada.model.EventoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoEnvioRepository extends JpaRepository<EventoEnvio, Integer> {
    List<EventoEnvio> findByEnvioIdOrderByFechaEventoDesc(Integer envioId);
}
