package com.sierra_dorada.service;

import com.sierra_dorada.model.DetallePedido;
import com.sierra_dorada.model.Envio;
import com.sierra_dorada.model.Pedido;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificacionPedidoService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificacionPedidoService.class);
    private final JavaMailSender correo;
    private final String remitente;
    private final String usuarioSmtp;
    private final String claveSmtp;
    private final String urlSeguimiento;

    public NotificacionPedidoService(
            JavaMailSender correo,
            @Value("${app.mail.from:}") String remitente,
            @Value("${spring.mail.username:}") String usuarioSmtp,
            @Value("${spring.mail.password:}") String claveSmtp,
            @Value("${app.mipaquete.tracking-url:https://app.mipaquete.com/seguimiento-envio}")
                String urlSeguimiento) {
        this.correo = correo;
        this.remitente = remitente;
        this.usuarioSmtp = usuarioSmtp;
        this.claveSmtp = claveSmtp;
        this.urlSeguimiento = urlSeguimiento;
    }

    public void enviarConfirmacion(Pedido pedido) {
        Envio envio = pedido.getEnvio();
        String destinatario = envio != null && StringUtils.hasText(envio.getDestinatarioEmail())
            ? envio.getDestinatarioEmail() : pedido.getUsuario().getEmail();
        if (!StringUtils.hasText(remitente) || !StringUtils.hasText(usuarioSmtp)
                || !StringUtils.hasText(claveSmtp) || !StringUtils.hasText(destinatario)) {
            LOG.warn("Pedido {} confirmado sin correo: SMTP no está configurado", pedido.getId());
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Compra confirmada - Pedido #" + pedido.getId());
        mensaje.setText(construirContenido(pedido, envio));
        try {
            correo.send(mensaje);
        } catch (MailException excepcion) {
            // Una caída de SMTP no puede revertir un pago que Bold ya confirmó.
            LOG.error("No fue posible enviar el correo del pedido {}", pedido.getId(), excepcion);
        }
    }

    private String construirContenido(Pedido pedido, Envio envio) {
        StringBuilder texto = new StringBuilder()
            .append("Hola ").append(pedido.getUsuario().getNombres()).append(",\n\n")
            .append("Tu compra fue realizada con éxito.\n")
            .append("Orden: #").append(pedido.getId()).append('\n')
            .append("Estado del pedido: ").append(pedido.getEstado()).append("\n\n")
            .append("Resumen:\n");
        for (DetallePedido detalle : pedido.getDetalles()) {
            texto.append("- ").append(detalle.getProducto().getNombre())
                .append(" x").append(detalle.getCantidad())
                .append(": ").append(moneda(detalle.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad())))).append('\n');
        }
        texto.append("\nSubtotal: ").append(moneda(pedido.getSubtotal()))
            .append("\nEnvío: ").append(moneda(pedido.getCostoEnvio()))
            .append("\nTotal: ").append(moneda(pedido.getTotal()))
            .append("\nDirección: ").append(pedido.getDireccionEnvio()).append('\n');

        if (envio != null && envio.getCodigoMiPaquete() != null) {
            texto.append("\nCódigo Mi Paquete: ").append(envio.getCodigoMiPaquete());
            if (StringUtils.hasText(envio.getNumeroGuia())) {
                texto.append("\nNúmero de guía: ").append(envio.getNumeroGuia());
            }
            texto.append("\nSigue tu envío: ").append(urlSeguimiento)
                .append("\nEscribe allí el número de guía o el código Mi Paquete.");
        } else if (envio != null && "PENDIENTE_ACTIVACION".equals(envio.getEstado())) {
            texto.append("\nEl envío está pendiente de activación; todavía no existe una guía real.");
        } else {
            texto.append("\nLa guía está en proceso de generación. Te informaremos cuando esté disponible.");
        }
        return texto.append("\n\nGracias por comprar en Sierra Dorada.").toString();
    }

    private String moneda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        formato.setMaximumFractionDigits(0);
        return formato.format(valor);
    }
}
