package com.sierra_dorada.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CaracteristicasProducto {
    @Column(name = "caracteristica_color", columnDefinition = "TEXT")
    private String color;

    @Column(name = "caracteristica_aroma", columnDefinition = "TEXT")
    private String aroma;

    @Column(name = "caracteristica_sabor", columnDefinition = "TEXT")
    private String sabor;

    @Column(name = "caracteristica_maridaje", columnDefinition = "TEXT")
    private String maridaje;

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getAroma() { return aroma; }
    public void setAroma(String aroma) { this.aroma = aroma; }
    public String getSabor() { return sabor; }
    public void setSabor(String sabor) { this.sabor = sabor; }
    public String getMaridaje() { return maridaje; }
    public void setMaridaje(String maridaje) { this.maridaje = maridaje; }
}

