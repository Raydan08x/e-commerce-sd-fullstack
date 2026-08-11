package com.sierra_dorada;

import com.sierra_dorada.model.Rol;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.UsuarioRepository;
import com.sierra_dorada.service.AdministradorPrincipalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSecurityIntegrationTests {
    private static final String EMAIL_ADMIN = "sierradoradacb@gmail.com";
    private static final String EMAIL_CLIENTE = "cliente.swagger@example.com";
    private static final String CONTRASENA = "Contrasena-Segura-2026";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder codificadorContrasenas;

    @Autowired
    private AdministradorPrincipalService administradorPrincipal;

    @BeforeEach
    void limpiarUsuariosDePrueba() {
        usuarios.findByEmailIgnoreCase(EMAIL_ADMIN).ifPresent(usuarios::delete);
        usuarios.findByEmailIgnoreCase(EMAIL_CLIENTE).ifPresent(usuarios::delete);
    }

    @Test
    void protegeSwaggerYElContratoOpenApiConRolAdministrador() throws Exception {
        guardarUsuario(EMAIL_CLIENTE, Rol.CLIENTE);
        guardarUsuario(EMAIL_ADMIN, Rol.ADMIN);

        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE,
                containsString("Basic realm=\"Sierra Dorada API Docs\"")));

        mvc.perform(get("/v3/api-docs").with(httpBasic(EMAIL_CLIENTE, CONTRASENA)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.estado").value(403));

        mvc.perform(get("/v3/api-docs").with(httpBasic(EMAIL_ADMIN, CONTRASENA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("Sierra Dorada API"))
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
            .andExpect(jsonPath("$.components.schemas.Usuario.properties.contrasena.writeOnly")
                .value(true))
            .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty())
            .andExpect(jsonPath(
                "$.paths['/api/envios/webhook/estados'].post.security[0].miPaqueteWebhookSecret")
                .isArray());

        mvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/swagger-ui/index.html").with(httpBasic(EMAIL_ADMIN, CONTRASENA)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Swagger UI")));
    }

    @Test
    void promueveLaCuentaPrincipalSoloDespuesDeVerificarla() throws Exception {
        Usuario usuario = guardarUsuario(EMAIL_ADMIN, Rol.CLIENTE);
        usuario.setEmailVerificado(false);
        usuarios.saveAndFlush(usuario);

        administradorPrincipal.run(null);

        Usuario pendiente = usuarios.findById(usuario.getId()).orElseThrow();
        assertEquals(Rol.CLIENTE, pendiente.getRol());
        pendiente.setEmailVerificado(true);
        usuarios.saveAndFlush(pendiente);

        administradorPrincipal.run(null);

        Usuario actualizado = usuarios.findById(usuario.getId()).orElseThrow();
        assertEquals(Rol.ADMIN, actualizado.getRol());
    }

    private Usuario guardarUsuario(String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombres("Prueba");
        usuario.setApellidos("Swagger");
        usuario.setEmail(email);
        usuario.setContrasena(codificadorContrasenas.encode(CONTRASENA));
        usuario.setActivo(true);
        usuario.setEmailVerificado(true);
        usuario.setRol(rol);
        return usuarios.saveAndFlush(usuario);
    }
}
