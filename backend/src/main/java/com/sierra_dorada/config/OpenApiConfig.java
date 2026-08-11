package com.sierra_dorada.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    public static final String ESQUEMA_JWT = "bearerAuth";
    public static final String ESQUEMA_WEBHOOK_MIPAQUETE = "miPaqueteWebhookSecret";
    public static final String ESQUEMA_FIRMA_BOLD = "boldWebhookSignature";

    @Bean
    OpenAPI apiSierraDorada() {
        return new OpenAPI()
            // Una URL relativa conserva el esquema HTTPS usado por el navegador y
            // evita contenido mixto cuando la aplicacion esta detras de Cloudflare.
            .servers(List.of(new Server()
                .url("/")
                .description("Servidor actual")))
            .info(new Info()
                .title("Sierra Dorada API")
                .version("1.0")
                .description("API REST del e-commerce Sierra Dorada. La documentación "
                    + "solo está disponible para administradores autenticados.")
                .contact(new Contact()
                    .name("Sierra Dorada")
                    .email("sierradoradacb@gmail.com")))
            .components(new Components()
                .addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                    .name(ESQUEMA_JWT)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Token de acceso obtenido en POST /api/auth/login"))
                .addSecuritySchemes(ESQUEMA_WEBHOOK_MIPAQUETE, new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-Webhook-Secret")
                    .description("Secreto de integración; nunca debe exponerse al frontend"))
                .addSecuritySchemes(ESQUEMA_FIRMA_BOLD, new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("x-bold-signature")
                    .description("Firma enviada por Bold para verificar la integridad del evento")))
            .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
