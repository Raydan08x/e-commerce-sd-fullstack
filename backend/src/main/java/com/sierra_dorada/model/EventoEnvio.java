package com.sierra_dorada.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_envio")
public class EventoEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Integer id;
    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id")
    private Envio envio;
    @Column(length = 100, nullable = false)
    private String estado;
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;
    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    @PrePersist
    void prePersist() {
        if (fechaEvento == null) fechaEvento = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
