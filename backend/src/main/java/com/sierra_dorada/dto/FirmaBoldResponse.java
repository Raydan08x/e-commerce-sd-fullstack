package com.sierra_dorada.dto;

public record FirmaBoldResponse(
    String integritySignature,
    int amount,
    String currency,
    Integer pedidoId
) { }
