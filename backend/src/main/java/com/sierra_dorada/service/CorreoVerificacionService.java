package com.sierra_dorada.service;

import com.sierra_dorada.exception.IntegracionExternaException;
import com.sierra_dorada.model.Usuario;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CorreoVerificacionService {
    private final JavaMailSender correo;
    private final String remitente;
    private final String usuarioSmtp;
    private final String claveSmtp;
    private final String urlPublica;

    public CorreoVerificacionService(
            JavaMailSender correo,
            @Value("${app.mail.from:}") String remitente,
            @Value("${spring.mail.username:}") String usuarioSmtp,
            @Value("${spring.mail.password:}") String claveSmtp,
            @Value("${app.public-url}") String urlPublica) {
        this.correo = correo;
        this.remitente = remitente;
        this.usuarioSmtp = usuarioSmtp;
        this.claveSmtp = claveSmtp;
        this.urlPublica = urlPublica.replaceAll("/+$", "");
    }

    public void enviar(Usuario usuario, String token) {
        if (!StringUtils.hasText(remitente) || !StringUtils.hasText(usuarioSmtp)
                || !StringUtils.hasText(claveSmtp)) {
            throw new IntegracionExternaException(
                "El servicio de confirmacion de correo aun no esta configurado");
        }

        String enlace = urlPublica + "/html/verificar-correo.html?token="
            + URLEncoder.encode(token, StandardCharsets.UTF_8);
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(usuario.getEmail());
        mensaje.setSubject("Confirma tu cuenta de Sierra Dorada");
        mensaje.setText("Hola " + usuario.getNombres() + ",\n\n"
            + "Confirma tu correo para activar la cuenta y comenzar a comprar:\n"
            + enlace + "\n\n"
            + "El enlace vence en 24 horas. Si no solicitaste esta cuenta, ignora el mensaje.\n\n"
            + "Sierra Dorada");
        try {
            correo.send(mensaje);
        } catch (MailException excepcion) {
            throw new IntegracionExternaException(
                "No fue posible enviar el correo de confirmacion", excepcion);
        }
    }
}
