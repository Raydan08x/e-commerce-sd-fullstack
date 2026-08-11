package com.sierra_dorada;

import com.jayway.jsonpath.JsonPath;
import com.sierra_dorada.model.Rol;
import com.sierra_dorada.model.Producto;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.ProductoRepository;
import com.sierra_dorada.repository.UsuarioRepository;
import com.sierra_dorada.security.JwtService;
import com.sierra_dorada.service.MiPaqueteClient;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
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
    private ProductoRepository productos;

    @Autowired
    private PasswordEncoder codificadorContrasenas;

    @MockitoBean
    private JavaMailSender correo;

    @MockitoBean
    private MiPaqueteClient miPaquete;

    @Test
    void ejecutaConJava21() {
        assertEquals(21, Runtime.version().feature());
    }

    @Test
    void buscaMunicipioPorNombreSinExponerElCatalogoCompleto() throws Exception {
        when(miPaquete.obtenerUbicaciones()).thenReturn(List.of(
            Map.of("locationName", "ZIPAQUIRÁ", "departmentOrStateName", "CUNDINAMARCA",
                "locationCode", "25899000"),
            Map.of("locationName", "ZIPACÓN", "departmentOrStateName", "CUNDINAMARCA",
                "locationCode", "25898000"),
            Map.of("locationName", "ZIPAQUIRÁ", "departmentOrStateName", "CUNDINAMARCA",
                "locationCode", "25899000")));

        mvc.perform(get("/api/envios/ubicaciones").param("q", "zipaquirá"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nombre").value("ZIPAQUIRÁ"))
            .andExpect(jsonPath("$[0].departamento").value("CUNDINAMARCA"))
            .andExpect(jsonPath("$[0].codigo").value("25899000"));

        mvc.perform(get("/api/envios/ubicaciones").param("q", "z"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void calculaBultosRealesParaUnidadPackYCajaSinMultiplicarElPeso() throws Exception {
        Producto unidad = productoEnvio("QA-U1", 14_000, 1);
        Producto pack = productoEnvio("QA-P4", 50_000, 4);
        Producto caja = productoEnvio("QA-C24", 192_000, 24);
        Producto hoodie = productoEnvio("QA-M1", 120_000, 1);
        hoodie.setPesoEnvioKg(new BigDecimal("1.2"));
        hoodie.setAnchoEnvioCm(32);
        hoodie.setLargoEnvioCm(38);
        hoodie.setAltoEnvioCm(12);
        productos.save(hoodie);
        when(miPaquete.cotizar(anyMap())).thenReturn(List.of());
        clearInvocations(miPaquete);

        cotizar(unidad.getId(), 3);
        cotizar(pack.getId(), 3);
        cotizar(caja.getId(), 2);
        cotizar(hoodie.getId(), 1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(miPaquete, times(4)).cotizar(captor.capture());
        List<Map<String, Object>> solicitudes = captor.getAllValues();

        assertPaquete(solicitudes.get(0), 1, 18, 18, 25, 3);
        assertPaquete(solicitudes.get(1), 1, 18, 51, 25, 9);
        assertPaquete(solicitudes.get(2), 2, 29, 42, 27, 18);
        assertPaquete(solicitudes.get(3), 1, 32, 38, 14, 2);
    }

    @Test
    void registraConfirmaCorreoPermiteLoginYExponeCatalogoPublico() throws Exception {
        clearInvocations(correo);
        String registro = """
            {"nombre":"Ana","apellidos":"Cliente","fechaNacimiento":"1995-05-10",
             "genero":"Femenino","direccion":"Bogotá","telefono":"3001234567",
             "email":"ana@example.com","password":"secreto123","aceptaTerminos":true,
             "autorizaDatos":true,"autorizaComunicaciones":false}
        """;
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registro))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.email").value("ana@example.com"))
            .andExpect(jsonPath("$.mensaje").isNotEmpty());

        String login = """
            {"usuario":"ana@example.com","password":"secreto123"}
            """;
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login))
            .andExpect(status().isForbidden());

        Usuario pendiente = usuarios.findByEmailIgnoreCase("ana@example.com").orElseThrow();
        assertFalse(pendiente.getActivo());
        assertFalse(pendiente.getEmailVerificado());

        ArgumentCaptor<SimpleMailMessage> mensaje = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(correo).send(mensaje.capture());
        String texto = mensaje.getValue().getText();
        int inicioToken = texto.indexOf("?token=") + 7;
        String tokenVerificacion = texto.substring(inicioToken).split("\\s")[0];

        mvc.perform(post("/api/auth/verificar-correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + tokenVerificacion + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.rol").value("cliente"));

        mvc.perform(get("/api/pedidos")
                .header("Authorization", "Bearer " + tokenVerificacion))
            .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/verificar-correo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + tokenVerificacion + "\"}"))
            .andExpect(status().isBadRequest());

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

        mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
            .andExpect(status().isAccepted());

        Usuario usuario = usuarios.findByEmailIgnoreCase("laura.admin@example.com").orElseThrow();
        Integer usuarioId = usuario.getId();
        usuario.setRol(Rol.ADMIN);
        usuario.setActivo(true);
        usuario.setEmailVerificado(true);
        usuarios.save(usuario);
        String token = servicioJwt.generar(usuario);

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

        mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
            .andExpect(status().isAccepted());

        Usuario usuario = usuarios.findByEmailIgnoreCase("cliente.permisos@example.com")
            .orElseThrow();
        usuario.setActivo(true);
        usuario.setEmailVerificado(true);
        usuarios.save(usuario);
        return servicioJwt.generar(usuario);
    }

    private Producto productoEnvio(String codigo, int precio, int unidades) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre("Producto de envío " + codigo);
        producto.setPrecio(BigDecimal.valueOf(precio));
        producto.setStock(100);
        producto.setUnidadesPorProducto(unidades);
        producto.setActivo(true);
        return productos.save(producto);
    }

    private void cotizar(Integer productoId, int cantidad) throws Exception {
        mvc.perform(post("/api/envios/cotizaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"destinoCodigo\":\"25899000\",\"detalles\":[{\"productoId\":"
                    + productoId + ",\"cantidad\":" + cantidad + "}]}"))
            .andExpect(status().isOk());
    }

    private void assertPaquete(Map<String, Object> solicitud, int cantidad, int ancho,
                               int largo, int alto, int peso) {
        assertEquals(cantidad, solicitud.get("quantity"));
        assertEquals(ancho, solicitud.get("width"));
        assertEquals(largo, solicitud.get("length"));
        assertEquals(alto, solicitud.get("height"));
        assertEquals(peso, solicitud.get("weight"));
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
