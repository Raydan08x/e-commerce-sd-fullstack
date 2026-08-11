package com.sierra_dorada;

import com.sierra_dorada.dto.ResultadoPagoBoldResponse;
import com.sierra_dorada.model.DetallePedido;
import com.sierra_dorada.model.Envio;
import com.sierra_dorada.model.Pago;
import com.sierra_dorada.model.Pedido;
import com.sierra_dorada.model.Producto;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.MetodoPagoRepository;
import com.sierra_dorada.repository.PagoRepository;
import com.sierra_dorada.service.BoldClient;
import com.sierra_dorada.service.EnvioService;
import com.sierra_dorada.service.NotificacionPedidoService;
import com.sierra_dorada.service.PagoService;
import com.sierra_dorada.service.PedidoService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoServiceTests {

    @Test
    void confirmaBoldVaciaElFlujoUnaSolaVezSinCrearEnvioRealEnModoPrueba() {
        PagoRepository pagos = mock(PagoRepository.class);
        PedidoService pedidos = mock(PedidoService.class);
        EnvioService envios = mock(EnvioService.class);
        BoldClient bold = mock(BoldClient.class);
        NotificacionPedidoService notificaciones = mock(NotificacionPedidoService.class);
        PagoService servicio = new PagoService(
            pagos, mock(MetodoPagoRepository.class), pedidos,
            "secreto-prueba", "COP", mock(ObjectMapper.class), envios, bold,
            notificaciones, "https://app.mipaquete.com/seguimiento-envio");

        Pedido pedido = pedidoDePrueba();
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMonto(pedido.getTotal());
        pago.setTransaccionId("SD-PRUEBA-001");

        when(pagos.findByTransaccionId("SD-PRUEBA-001")).thenReturn(Optional.of(pago));
        when(pedidos.obtenerAutorizado(pedido.getId(), "cliente@example.com", false))
            .thenReturn(pedido);
        when(pedidos.cambiarEstado(pedido.getId(), "Confirmado")).thenAnswer(invocacion -> {
            pedido.setEstado("Confirmado");
            return pedido;
        });
        when(bold.consultarTransaccion("SD-PRUEBA-001")).thenReturn(Map.of(
            "reference_id", "SD-PRUEBA-001",
            "total", 26000,
            "payment_status", "APPROVED"));
        when(envios.generacionGuiasHabilitada()).thenReturn(false);

        ResultadoPagoBoldResponse resultado = servicio.confirmarRetorno(
            "SD-PRUEBA-001", "cliente@example.com", false);
        ResultadoPagoBoldResponse repetido = servicio.confirmarRetorno(
            "SD-PRUEBA-001", "cliente@example.com", false);

        assertTrue(resultado.confirmado());
        assertTrue(repetido.confirmado());
        assertEquals("APPROVED", resultado.estadoPago());
        assertEquals("Confirmado", resultado.pedido().estado());
        assertEquals("PENDIENTE_ACTIVACION", resultado.pedido().estadoEnvio());
        verify(envios, never()).generarGuia(pedido);
        verify(notificaciones, times(1)).enviarConfirmacion(pedido);
        verify(bold, times(1)).consultarTransaccion("SD-PRUEBA-001");
    }

    @Test
    void correoIncluyeOrdenEstadoResumenYAvisoDeModoPrueba() {
        JavaMailSender correo = mock(JavaMailSender.class);
        NotificacionPedidoService servicio = new NotificacionPedidoService(
            correo, "ventas@sierradorada.co", "smtp-user", "smtp-password",
            "https://app.mipaquete.com/seguimiento-envio");
        Pedido pedido = pedidoDePrueba();
        pedido.setEstado("Confirmado");

        servicio.enviarConfirmacion(pedido);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(correo).send(captor.capture());
        SimpleMailMessage mensaje = captor.getValue();
        assertEquals("Compra confirmada - Pedido #91", mensaje.getSubject());
        assertTrue(mensaje.getText().contains("Orden: #91"));
        assertTrue(mensaje.getText().contains("Estado del pedido: Confirmado"));
        assertTrue(mensaje.getText().contains("APA Premium x1"));
        assertTrue(mensaje.getText().contains("todavía no existe una guía real"));
    }

    private Pedido pedidoDePrueba() {
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@example.com");
        usuario.setNombres("Cliente");

        Producto producto = new Producto();
        producto.setNombre("APA Premium");
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("20000"));

        Pedido pedido = new Pedido();
        pedido.setId(91);
        pedido.setUsuario(usuario);
        pedido.setDireccionEnvio("Calle de prueba 1");
        pedido.setSubtotal(new BigDecimal("20000"));
        pedido.setCostoEnvio(new BigDecimal("6000"));
        pedido.setTotal(new BigDecimal("26000"));
        pedido.agregarDetalle(detalle);

        Envio envio = new Envio();
        envio.setEstado("PENDIENTE_ACTIVACION");
        envio.setDestinatarioEmail("cliente@example.com");
        pedido.setEnvio(envio);
        return pedido;
    }
}
