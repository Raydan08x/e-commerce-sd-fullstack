package com.sierra_dorada.dto;

public record AuthResponse(String token, String tipo, Integer id, String email,
                           String nombreCompleto, String rol) {
}
