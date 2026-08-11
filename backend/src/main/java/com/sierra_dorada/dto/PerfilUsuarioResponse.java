package com.sierra_dorada.dto;

public record PerfilUsuarioResponse(
    Integer id,
    String nombres,
    String apellidos,
    String email,
    String telefono,
    String direccion
) { }
