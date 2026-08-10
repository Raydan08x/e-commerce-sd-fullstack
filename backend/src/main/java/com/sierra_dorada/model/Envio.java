package com.sierra_dorada.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Integer id;

    @JsonIgnore
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(length = 30, nullable = false)
    private String proveedor = "MIPAQUETE";
    @Column(name = "codigo_mipaquete", unique = true)
    private Long codigoMiPaquete;
    @Column(name = "transportadora_id")
    private String transportadoraId;
    @Column(name = "transportadora_nombre")
    private String transportadoraNombre;
    @Column(name = "codigo_dane_origen", nullable = false)
    private String codigoDaneOrigen;
    @Column(name = "codigo_dane_destino", nullable = false)
    private String codigoDaneDestino;
    @Column(name = "destinatario_nombre", nullable = false)
    private String destinatarioNombre;
    @Column(name = "destinatario_apellido")
    private String destinatarioApellido;
    @Column(name = "destinatario_email", nullable = false)
    private String destinatarioEmail;
    @Column(name = "destinatario_telefono", nullable = false)
    private String destinatarioTelefono;
    @Column(name = "direccion_destino", nullable = false, columnDefinition = "TEXT")
    private String direccionDestino;
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal costo = BigDecimal.ZERO;
    @Column(name = "numero_guia")
    private String numeroGuia;
    @Column(name = "url_guia", columnDefinition = "TEXT")
    private String urlGuia;
    @Column(length = 100, nullable = false)
    private String estado = "PENDIENTE";
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void preUpdate() { fechaActualizacion = LocalDateTime.now(); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public Long getCodigoMiPaquete() { return codigoMiPaquete; }
    public void setCodigoMiPaquete(Long codigoMiPaquete) { this.codigoMiPaquete = codigoMiPaquete; }
    public String getTransportadoraId() { return transportadoraId; }
    public void setTransportadoraId(String transportadoraId) { this.transportadoraId = transportadoraId; }
    public String getTransportadoraNombre() { return transportadoraNombre; }
    public void setTransportadoraNombre(String transportadoraNombre) { this.transportadoraNombre = transportadoraNombre; }
    public String getCodigoDaneOrigen() { return codigoDaneOrigen; }
    public void setCodigoDaneOrigen(String codigoDaneOrigen) { this.codigoDaneOrigen = codigoDaneOrigen; }
    public String getCodigoDaneDestino() { return codigoDaneDestino; }
    public void setCodigoDaneDestino(String codigoDaneDestino) { this.codigoDaneDestino = codigoDaneDestino; }
    public String getDestinatarioNombre() { return destinatarioNombre; }
    public void setDestinatarioNombre(String destinatarioNombre) { this.destinatarioNombre = destinatarioNombre; }
    public String getDestinatarioApellido() { return destinatarioApellido; }
    public void setDestinatarioApellido(String destinatarioApellido) { this.destinatarioApellido = destinatarioApellido; }
    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }
    public String getDestinatarioTelefono() { return destinatarioTelefono; }
    public void setDestinatarioTelefono(String destinatarioTelefono) { this.destinatarioTelefono = destinatarioTelefono; }
    public String getDireccionDestino() { return direccionDestino; }
    public void setDireccionDestino(String direccionDestino) { this.direccionDestino = direccionDestino; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }
    public String getUrlGuia() { return urlGuia; }
    public void setUrlGuia(String urlGuia) { this.urlGuia = urlGuia; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
