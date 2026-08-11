package com.sierra_dorada.model;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @NotBlank(message = "El código del producto es obligatorio")
    @Size(max = 20, message = "El código no puede superar los 20 caracteres")
    @Column(unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 200, message = "El nombre del producto no puede superar los 200 caracteres")
    @Column(name = "nombre_produ")
    private String nombre;

    @Column(name = "descripcion_produ", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(name = "precio_base", precision = 12, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Size(max = 100, message = "La marca no puede superar los 100 caracteres")
    private String marca;

    @Column(name = "tipo_cerveza")
    @Size(max = 100, message = "El tipo de cerveza no puede superar los 100 caracteres")
    private String tipoCerveza;

    @Column(name = "estilo_cerveza")
    @Size(max = 100, message = "El estilo de cerveza no puede superar los 100 caracteres")
    private String estiloCerveza;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    @NotNull(message = "Las unidades físicas por producto son obligatorias")
    @Min(value = 1, message = "Las unidades físicas por producto deben ser al menos 1")
    @Column(name = "unidades_por_producto", nullable = false)
    private Integer unidadesPorProducto = 1;

    @DecimalMin(value = "0.1", message = "El peso de envío debe ser mayor que cero")
    @Column(name = "peso_envio_kg", precision = 8, scale = 3)
    private BigDecimal pesoEnvioKg;

    @Min(value = 1, message = "El ancho de envío debe ser mayor que cero")
    @Column(name = "ancho_envio_cm")
    private Integer anchoEnvioCm;

    @Min(value = 1, message = "El largo de envío debe ser mayor que cero")
    @Column(name = "largo_envio_cm")
    private Integer largoEnvioCm;

    @Min(value = 1, message = "El alto de envío debe ser mayor que cero")
    @Column(name = "alto_envio_cm")
    private Integer altoEnvioCm;

    @DecimalMin(value = "0.0", message = "El porcentaje de alcohol no puede ser negativo")
    @Column(precision = 4, scale = 2)
    private BigDecimal abv;

    private Integer ibu;

    @Column(name = "imagen_url", columnDefinition = "LONGTEXT")
    private String imagenUrl;

    @Column(columnDefinition = "TEXT")
    private String inspiracion;

    @Column(name = "color_hex", length = 20)
    private String colorHex;

    @Column(name = "color_nombre", length = 100)
    private String colorNombre;

    @Column(length = 50)
    private String temperatura;

    @Column(columnDefinition = "TEXT")
    private String leyenda;

    @Column(name = "descripcion_completa", columnDefinition = "TEXT")
    private String descripcionCompleta;

    @Column(columnDefinition = "TEXT")
    private String proceso;

    @Embedded
    private CaracteristicasProducto caracteristicas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_maridajes", joinColumns = @JoinColumn(name = "producto_id"))
    @OrderColumn(name = "orden")
    private List<MaridajeProducto> maridaje = new ArrayList<>();

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    private Boolean activo = true;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (stock == null) {
            stock = 0;
        }
        if (unidadesPorProducto == null) {
            unidadesPorProducto = 1;
        }
        if (activo == null) {
            activo = true;
        }
    }

    public Integer getId() {
        return id;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTipoCerveza() {
        return tipoCerveza;
    }

    public void setTipoCerveza(String tipoCerveza) {
        this.tipoCerveza = tipoCerveza;
    }

    public String getEstiloCerveza() {
        return estiloCerveza;
    }

    public void setEstiloCerveza(String estiloCerveza) {
        this.estiloCerveza = estiloCerveza;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getUnidadesPorProducto() {
        return unidadesPorProducto;
    }

    public void setUnidadesPorProducto(Integer unidadesPorProducto) {
        this.unidadesPorProducto = unidadesPorProducto;
    }

    public BigDecimal getPesoEnvioKg() { return pesoEnvioKg; }
    public void setPesoEnvioKg(BigDecimal pesoEnvioKg) { this.pesoEnvioKg = pesoEnvioKg; }
    public Integer getAnchoEnvioCm() { return anchoEnvioCm; }
    public void setAnchoEnvioCm(Integer anchoEnvioCm) { this.anchoEnvioCm = anchoEnvioCm; }
    public Integer getLargoEnvioCm() { return largoEnvioCm; }
    public void setLargoEnvioCm(Integer largoEnvioCm) { this.largoEnvioCm = largoEnvioCm; }
    public Integer getAltoEnvioCm() { return altoEnvioCm; }
    public void setAltoEnvioCm(Integer altoEnvioCm) { this.altoEnvioCm = altoEnvioCm; }

    public BigDecimal getAbv() {
        return abv;
    }

    public void setAbv(BigDecimal abv) {
        this.abv = abv;
    }

    public Integer getIbu() { return ibu; }
    public void setIbu(Integer ibu) { this.ibu = ibu; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getInspiracion() { return inspiracion; }
    public void setInspiracion(String inspiracion) { this.inspiracion = inspiracion; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public String getColorNombre() { return colorNombre; }
    public void setColorNombre(String colorNombre) { this.colorNombre = colorNombre; }
    public String getTemperatura() { return temperatura; }
    public void setTemperatura(String temperatura) { this.temperatura = temperatura; }
    public String getLeyenda() { return leyenda; }
    public void setLeyenda(String leyenda) { this.leyenda = leyenda; }
    public String getDescripcionCompleta() { return descripcionCompleta; }
    public void setDescripcionCompleta(String descripcionCompleta) { this.descripcionCompleta = descripcionCompleta; }
    public String getProceso() { return proceso; }
    public void setProceso(String proceso) { this.proceso = proceso; }
    public CaracteristicasProducto getCaracteristicas() { return caracteristicas; }
    public void setCaracteristicas(CaracteristicasProducto caracteristicas) { this.caracteristicas = caracteristicas; }
    public List<MaridajeProducto> getMaridaje() { return maridaje; }
    public void setMaridaje(List<MaridajeProducto> maridaje) {
        this.maridaje = maridaje == null ? new ArrayList<>() : maridaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
