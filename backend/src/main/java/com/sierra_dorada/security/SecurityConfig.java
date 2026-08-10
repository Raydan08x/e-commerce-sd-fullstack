package com.sierra_dorada.security;

import com.sierra_dorada.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    private static final String ROL_ADMIN = "ADMIN";
    private static final String[] RUTAS_CATALOGO = {
        "/api/productos/**",
        "/api/categorias/**",
        "/api/metodos-pago/**"
    };

    @Bean
    PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService servicioDetallesUsuario(UsuarioRepository usuarios) {
        return email -> usuarios.findByEmailIgnoreCase(email)
            .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
            .map(usuario -> User.withUsername(usuario.getEmail())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().name())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    AuthenticationManager administradorAutenticacion(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain cadenaFiltrosSeguridad(HttpSecurity http,
                                                JwtAuthenticationFilter filtroJwt) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sesiones -> sesiones.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(excepciones -> excepciones
                .authenticationEntryPoint((solicitud, respuesta, excepcion) ->
                    escribirError(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                        "Se requiere autenticación"))
                .accessDeniedHandler((solicitud, respuesta, excepcion) ->
                    escribirError(respuesta, HttpServletResponse.SC_FORBIDDEN,
                        "No tienes permisos para realizar esta acción")))
            .authorizeHttpRequests(autorizacion -> autorizacion
                .requestMatchers("/api/auth/**", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contacto", "/api/newsletter").permitAll()
                .requestMatchers("/api/contacto/**", "/api/newsletter/**").hasRole(ROL_ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/envios/ubicaciones").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/envios/cotizaciones",
                    "/api/envios/webhook/estados").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/envios/pedidos/*/guia").hasRole(ROL_ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado").hasRole(ROL_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**").hasRole(ROL_ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/pagos/bold/firma").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/pagos/bold/webhook").permitAll()
                .requestMatchers("/api/pagos/**").hasRole(ROL_ADMIN)
                .requestMatchers(HttpMethod.GET, RUTAS_CATALOGO).permitAll()
                .requestMatchers(RUTAS_CATALOGO).hasRole(ROL_ADMIN)
                .requestMatchers("/api/usuarios/**").hasRole(ROL_ADMIN)
                .anyRequest().authenticated())
            .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    CorsConfigurationSource fuenteConfiguracionCors(
        @Value("${app.cors.allowed-origins}") String origenesPermitidos) {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.stream(origenesPermitidos.split(","))
            .map(String::trim)
            .toList());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    private static void escribirError(HttpServletResponse respuesta, int estado, String mensaje)
        throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("{\"estado\":" + estado + ",\"mensaje\":\"" + mensaje + "\"}");
    }
}
