package com.sierra_dorada.service;

import com.sierra_dorada.config.MiPaqueteProperties;
import com.sierra_dorada.exception.IntegracionExternaException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.time.Duration;

@Component
public class MiPaqueteClient {
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA_MAPAS =
        new ParameterizedTypeReference<>() { };

    private final RestClient cliente;
    private final MiPaqueteProperties propiedades;

    public MiPaqueteClient(MiPaqueteProperties propiedades) {
        this.propiedades = propiedades;
        SimpleClientHttpRequestFactory transporte = new SimpleClientHttpRequestFactory();
        transporte.setConnectTimeout(Duration.ofSeconds(10));
        transporte.setReadTimeout(Duration.ofSeconds(30));
        this.cliente = RestClient.builder()
            .baseUrl(propiedades.getBaseUrl())
            .requestFactory(transporte)
            .build();
    }

    @Cacheable("ubicacionesMiPaquete")
    public List<Map<String, Object>> obtenerUbicaciones() {
        validarConfiguracion(false);
        try {
            return cliente.get()
                .uri("/getLocations")
                .headers(this::agregarAutenticacion)
                .retrieve()
                .body(LISTA_MAPAS);
        } catch (RestClientResponseException excepcion) {
            throw errorExterno("consultar ubicaciones", excepcion);
        } catch (RestClientException excepcion) {
            throw errorConexion("consultar ubicaciones", excepcion);
        }
    }

    public List<Map<String, Object>> cotizar(Map<String, Object> solicitud) {
        validarConfiguracion(true);
        try {
            return cliente.post()
                .uri("/quoteShipping")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(this::agregarAutenticacion)
                .body(solicitud)
                .retrieve()
                .body(LISTA_MAPAS);
        } catch (RestClientResponseException excepcion) {
            throw errorExterno("cotizar el envío", excepcion);
        } catch (RestClientException excepcion) {
            throw errorConexion("cotizar el envío", excepcion);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearEnvio(Map<String, Object> solicitud) {
        validarConfiguracion(true);
        try {
            return cliente.post()
                .uri("/createSending")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(this::agregarAutenticacion)
                .body(solicitud)
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException excepcion) {
            throw errorExterno("crear el envío", excepcion);
        } catch (RestClientException excepcion) {
            throw errorConexion("crear el envío", excepcion);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerTracking(Long codigoMiPaquete) {
        validarConfiguracion(true);
        try {
            return cliente.get()
                .uri(uri -> uri.path("/getSendingTracking")
                    .queryParam("mpCode", codigoMiPaquete).build())
                .headers(this::agregarAutenticacion)
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException excepcion) {
            throw errorExterno("consultar el tracking", excepcion);
        } catch (RestClientException excepcion) {
            throw errorConexion("consultar el tracking", excepcion);
        }
    }

    private void agregarAutenticacion(org.springframework.http.HttpHeaders encabezados) {
        encabezados.set("apikey", propiedades.getApiKey());
        encabezados.set("session-tracker", propiedades.getSessionTracker());
    }

    private void validarConfiguracion(boolean completa) {
        boolean basica = StringUtils.hasText(propiedades.getBaseUrl())
            && StringUtils.hasText(propiedades.getApiKey())
            && StringUtils.hasText(propiedades.getSessionTracker());
        boolean envio = StringUtils.hasText(propiedades.getOriginDaneCode());
        if (!basica || (completa && !envio)) {
            throw new IntegracionExternaException(
                "La integración con Mi Paquete no está configurada en el servidor");
        }
    }

    private IntegracionExternaException errorExterno(
        String operacion, RestClientResponseException excepcion) {
        return new IntegracionExternaException(
            "Mi Paquete no pudo " + operacion + " (HTTP " + excepcion.getStatusCode().value() + ")",
            excepcion);
    }

    private IntegracionExternaException errorConexion(
        String operacion, RestClientException excepcion) {
        return new IntegracionExternaException(
            "No fue posible conectar con Mi Paquete para " + operacion, excepcion);
    }
}
