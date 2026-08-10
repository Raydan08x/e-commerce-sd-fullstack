package com.sierra_dorada.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException excepcion) {
        return respuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    ResponseEntity<Map<String, Object>> conflicto(ConflictoException excepcion) {
        return respuesta(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException excepcion) {
        Map<String, String> errores = new LinkedHashMap<>();
        excepcion.getBindingResult().getFieldErrors()
            .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now());
        cuerpo.put("status", HttpStatus.BAD_REQUEST.value());
        cuerpo.put("message", "Datos inválidos");
        cuerpo.put("errors", errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> solicitudInvalida(IllegalArgumentException excepcion) {
        return respuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, Object>> credenciales(BadCredentialsException excepcion) {
        return respuesta(HttpStatus.UNAUTHORIZED, excepcion.getMessage());
    }

    @ExceptionHandler(CuentaNoVerificadaException.class)
    ResponseEntity<Map<String, Object>> cuentaNoVerificada(
            CuentaNoVerificadaException excepcion) {
        return respuesta(HttpStatus.FORBIDDEN, excepcion.getMessage());
    }

    @ExceptionHandler(IntegracionExternaException.class)
    ResponseEntity<Map<String, Object>> integracion(IntegracionExternaException excepcion) {
        return respuesta(HttpStatus.BAD_GATEWAY, excepcion.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    ResponseEntity<Map<String, Object>> prohibido(SecurityException excepcion) {
        return respuesta(HttpStatus.FORBIDDEN, excepcion.getMessage());
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of(
            "timestamp", LocalDateTime.now(),
            "status", estado.value(),
            "message", mensaje
        ));
    }
}
