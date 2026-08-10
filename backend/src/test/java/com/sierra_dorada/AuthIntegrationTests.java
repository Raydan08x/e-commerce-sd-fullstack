package com.sierra_dorada;

import com.jayway.jsonpath.JsonPath;
import com.sierra_dorada.model.Rol;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.UsuarioRepository;
import com.sierra_dorada.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService servicioJwt;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder codificadorContrasenas;

    @Test
    void ejecutaConJava21() {
        assertEquals(21, Runtime.version().feature());
    }

    @Test
    void registraUsuarioPermiteLoginYExponeCatalogoPublico() throws Exception {
        String registro = """
            {"nombre":"Ana","apellidos":"Cliente","fechaNacimiento":"1995-05-10",
             "genero":"Femenino","direccion":"Bogotá","telefono":"3001234567",
             "email":"ana@example.com","password":"secreto123","aceptaTerminos":true,
             "autorizaDatos":true,"autorizaComunicaciones":false}
            """;
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registro))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.rol").value("cliente"));

        String login = """
            {"usuario":"ana@example.com","password":"secreto123"}
            """;
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("Bearer"));

        mvc.perform(get("/api/productos")).andExpect(status().isOk());
    }

    @Test
    void rechazaLoginConCredencialesInvalidas() throws Exception {
        String login = """
            {"usuario":"nadie@example.com","password":"incorrecta"}
            """;
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void respondeNoAutorizadoCuandoFaltaElToken() throws Exception {
        mvc.perform(get("/api/pedidos"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.estado").value(401))
            .andExpect(jsonPath("$.mensaje").value("Se requiere autenticación"));
    }

    @Test
    void respondeNoAutorizadoCuandoElTokenEsInvalido() throws Exception {
        mvc.perform(get("/api/pedidos")
                .header("Authorization", "Bearer token-invalido"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void respondeNoAutorizadoCuandoElUsuarioDelTokenNoExiste() throws Exception {
        Usuario usuarioInexistente = new Usuario();
        usuarioInexistente.setId(999_999);
        usuarioInexistente.setEmail("usuario.inexistente@example.com");
        String token = servicioJwt.generar(usuarioInexistente);

        mvc.perform(get("/api/pedidos")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void respondeProhibidoCuandoElClienteNoEsAdministrador() throws Exception {
        String token = registrarClienteYObtenerToken();

        mvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.estado").value(403))
            .andExpect(jsonPath("$.mensaje").value("No tienes permisos para realizar esta acción"));
    }

    @Test
    void actualizaUsuarioSinReemplazarLaContrasena() throws Exception {
        String registro = """
            {"nombre":"Laura","apellidos":"Administradora","fechaNacimiento":"1992-04-15",
             "genero":"Femenino","direccion":"Cali","telefono":"3021234567",
             "email":"laura.admin@example.com","password":"secreto123","aceptaTerminos":true,
             "autorizaDatos":true,"autorizaComunicaciones":true}
            """;

        String respuestaRegistro = mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Integer usuarioId = JsonPath.read(respuestaRegistro, "$.id");
        String token = JsonPath.read(respuestaRegistro, "$.token");
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow();
        usuario.setRol(Rol.ADMIN);
        usuarios.save(usuario);

        String actualizacion = """
            {"nombres":"Laura María","apellidos":"Administradora","fechaNacimiento":"1992-04-15",
             "genero":"Femenino","direccion":"Cali","telefono":"3021234567",
             "email":"laura.admin@example.com","activo":true,"rol":"ADMIN"}
            """;

        mvc.perform(put("/api/usuarios/{id}", usuarioId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(actualizacion))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombres").value("Laura María"));

        Usuario actualizado = usuarios.findById(usuarioId).orElseThrow();
        assertTrue(codificadorContrasenas.matches("secreto123", actualizado.getContrasena()));
    }

    private String registrarClienteYObtenerToken() throws Exception {
        String registro = """
            {"nombre":"Carlos","apellidos":"Cliente","fechaNacimiento":"1994-08-12",
             "genero":"Masculino","direccion":"Medellín","telefono":"3011234567",
             "email":"cliente.permisos@example.com","password":"secreto123","aceptaTerminos":true,
             "autorizaDatos":true,"autorizaComunicaciones":false}
            """;

        String respuesta = mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return JsonPath.read(respuesta, "$.token");
    }

    @Test
    void guardaContactoYNewsletterSinAutenticacion() throws Exception {
        mvc.perform(post("/api/contacto")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Cliente Web","telefono":"3007654321",
                     "email":"contacto@example.com","mensaje":"Quiero más información"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber());

        mvc.perform(post("/api/newsletter")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"newsletter@example.com\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("newsletter@example.com"));
    }
}
