package com.sierra_dorada.service;

import com.sierra_dorada.exception.IntegracionExternaException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BoldClient {
    private final RestClient cliente;
    private final String llaveIdentidad;

    public BoldClient(
            @Value("${app.bold.base-url:https://payments.api.bold.co}") String baseUrl,
            @Value("${app.bold.identity-key:}") String llaveIdentidad) {
        this.llaveIdentidad = llaveIdentidad;
        SimpleClientHttpRequestFactory transporte = new SimpleClientHttpRequestFactory();
        transporte.setConnectTimeout(Duration.ofSeconds(10));
        transporte.setReadTimeout(Duration.ofSeconds(20));
        this.cliente = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(transporte)
            .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> consultarTransaccion(String referencia) {
        if (!StringUtils.hasText(llaveIdentidad)) {
            throw new IntegracionExternaException(
                "La consulta de transacciones Bold no está configurada en el servidor");
        }
        try {
            return cliente.get()
                .uri("/v2/payment-voucher/{referencia}", referencia)
                .header("Authorization", "x-api-key " + llaveIdentidad)
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException excepcion) {
            if (excepcion.getStatusCode().value() == 404) {
                Map<String, Object> pendiente = new LinkedHashMap<>();
                pendiente.put("reference_id", referencia);
                pendiente.put("payment_status", "NO_TRANSACTION_FOUND");
                return pendiente;
            }
            throw new IntegracionExternaException(
                "Bold no pudo consultar la transacción (HTTP "
                    + excepcion.getStatusCode().value() + ")", excepcion);
        } catch (RestClientException excepcion) {
            throw new IntegracionExternaException(
                "No fue posible conectar con Bold para confirmar el pago", excepcion);
        }
    }
}
