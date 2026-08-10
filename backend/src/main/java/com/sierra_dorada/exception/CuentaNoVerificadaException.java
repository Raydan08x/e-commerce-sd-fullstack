package com.sierra_dorada.exception;

public class CuentaNoVerificadaException extends RuntimeException {
    public CuentaNoVerificadaException(String mensaje) {
        super(mensaje);
    }
}
