package com.sierra_dorada.config;

import com.sierra_dorada.service.ProductoService;
import com.sierra_dorada.service.MiPaqueteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogoCacheWarmup implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogoCacheWarmup.class);
    private final ProductoService productos;
    private final MiPaqueteClient miPaquete;

    public CatalogoCacheWarmup(ProductoService productos, MiPaqueteClient miPaquete) {
        this.productos = productos;
        this.miPaquete = miPaquete;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        try {
            productos.listar(true, null, null);
            productos.listar(false, null, null);
            LOGGER.info("Cache del catalogo preparada");
        } catch (RuntimeException excepcion) {
            LOGGER.warn("No fue posible preparar la cache del catalogo", excepcion);
        }
        try {
            miPaquete.obtenerUbicaciones();
            LOGGER.info("Cache de municipios preparada");
        } catch (RuntimeException excepcion) {
            LOGGER.warn("No fue posible preparar la cache de municipios", excepcion);
        }
    }
}
