package com.sierra_dorada.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    private String apellidos;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Size(max = 30, message = "El género no puede superar los 30 caracteres")
    private String genero;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Email(message = "El correo electrónico no tiene un formato válido")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
    @Column(unique = true)
    private String email;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
    private String contrasena;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    private Boolean activo = true;

    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = true;

    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.CLIENTE;

    @Column(name = "acepta_terminos", nullable = false)
    private Boolean aceptaTerminos = false;

    @Column(name = "autoriza_datos", nullable = false)
    private Boolean autorizaDatos = false;

    @Column(name = "autoriza_comunicaciones", nullable = false)
    private Boolean autorizaComunicaciones = false;

    @Column(name = "fecha_consentimiento")
    private LocalDateTime fechaConsentimiento;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (emailVerificado == null) {
            emailVerificado = true;
        }
        if (rol == null) {
            rol = Rol.CLIENTE;
        }
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
        if (Boolean.TRUE.equals(aceptaTerminos) && Boolean.TRUE.equals(autorizaDatos)
            && fechaConsentimiento == null) {
            fechaConsentimiento = LocalDateTime.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(Boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(Boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }
    public Boolean getAutorizaDatos() { return autorizaDatos; }
    public void setAutorizaDatos(Boolean autorizaDatos) { this.autorizaDatos = autorizaDatos; }
    public Boolean getAutorizaComunicaciones() { return autorizaComunicaciones; }
    public void setAutorizaComunicaciones(Boolean autorizaComunicaciones) {
        this.autorizaComunicaciones = autorizaComunicaciones;
    }
    public LocalDateTime getFechaConsentimiento() { return fechaConsentimiento; }
    public void setFechaConsentimiento(LocalDateTime fechaConsentimiento) {
        this.fechaConsentimiento = fechaConsentimiento;
    }
}
