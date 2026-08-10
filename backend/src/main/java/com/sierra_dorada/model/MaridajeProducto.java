package com.sierra_dorada.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MaridajeProducto {
    private String emoji;

    @Column(name = "nombre")
    @JsonProperty("name")
    private String nombre;

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

