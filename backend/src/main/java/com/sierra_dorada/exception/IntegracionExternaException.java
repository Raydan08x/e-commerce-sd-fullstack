package com.sierra_dorada.exception;

public class IntegracionExternaException extends RuntimeException {
    public IntegracionExternaException(String message) {
        super(message);
    }

    public IntegracionExternaException(String message, Throwable cause) {
        super(message, cause);
    }
}
