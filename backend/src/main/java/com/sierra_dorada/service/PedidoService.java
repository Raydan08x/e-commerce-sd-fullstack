package com.sierra_dorada.service;

import com.sierra_dorada.dto.CrearPedidoRequest;
import com.sierra_dorada.dto.DetallePedidoRequest;
import com.sierra_dorada.exception.RecursoNoEncontradoException;
import com.sierra_dorada.model.DetallePedido;
import com.sierra_dorada.model.MetodoPago;
import com.sierra_dorada.model.Pedido;
import com.sierra_dorada.model.Producto;
import com.sierra_dorada.model.Usuario;
import com.sierra_dorada.repository.MetodoPagoRepository;
import com.sierra_dorada.repository.PedidoRepository;
import com.sierra_dorada.repository.ProductoRepository;
import com.sierra_dorada.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final MetodoPagoRepository metodosPago;
    private final EnvioService envios;

    public PedidoService(PedidoRepository pedidos, UsuarioRepository usuarios,
                         ProductoRepository productos, MetodoPagoRepository metodosPago,
                         EnvioService envios) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.productos = productos;
        this.metodosPago = metodosPago;
        this.envios = envios;
    }

    public List<Pedido> listar(String email, boolean administrador, Integer usuarioId) {
        if (administrador && usuarioId == null) return pedidos.findAll();
        Integer id = administrador ? usuarioId : obtenerUsuario(email).getId();
        return pedidos.findByUsuarioId(id);
    }

    public Pedido obtenerAutorizado(Integer id, String email, boolean administrador) {
        Pedido pedido = obtener(id);
        if (!administrador && !pedido.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new SecurityException("No puedes consultar pedidos de otro usuario");
        }
        return pedido;
    }

    public Pedido obtener(Integer id) {
        return pedidos.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }

    @Transactional
    public Pedido crear(CrearPedidoRequest solicitud, String email) {
        Usuario usuario = obtenerUsuario(email);
        EnvioService.OpcionEnvio opcion = envios.seleccionar(
            solicitud.cotizacion(), solicitud.transportadoraId());

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEnvio(solicitud.direccionEnvio());
        pedido.setMetodoPago(obtenerMetodoPago(solicitud.metodoPagoId()));
        pedido.setNotas(solicitud.notas());

        BigDecimal subtotal = prepararDetalles(pedido, solicitud.detalles());
        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(opcion.costo());
        pedido.setTotal(subtotal.add(opcion.costo()));
        pedido.setEnvio(envios.preparar(pedido, solicitud, opcion));
        return pedidos.save(pedido);
    }

    @Transactional
    public Pedido cambiarEstado(Integer id, String estado) {
        Pedido pedido = obtener(id);
        if ("Cancelado".equals(estado) && !"Cancelado".equals(pedido.getEstado())) {
            pedido.getDetalles().forEach(detalle -> {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
            });
        }
        pedido.setEstado(estado);
        return pedidos.save(pedido);
    }

    @Transactional
    public void eliminar(Integer id) {
        // Los pedidos forman parte del historial contable y logístico: se cancelan,
        // nunca se eliminan físicamente de la base de datos.
        cambiarEstado(id, "Cancelado");
    }

    private Usuario obtenerUsuario(String email) {
        return usuarios.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private MetodoPago obtenerMetodoPago(Integer id) {
        if (id == null) return null;
        return metodosPago.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Método de pago no encontrado"));
    }

    private BigDecimal prepararDetalles(Pedido pedido, List<DetallePedidoRequest> solicitudes) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetallePedidoRequest solicitud : solicitudes) {
            Producto producto = productos.findByIdParaActualizar(solicitud.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
            if (!Boolean.TRUE.equals(producto.getActivo()) || producto.getStock() < solicitud.cantidad()) {
                throw new IllegalArgumentException(
                    "Producto inactivo o sin stock suficiente: " + producto.getNombre());
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(solicitud.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            pedido.agregarDetalle(detalle);
            subtotal = subtotal.add(
                producto.getPrecio().multiply(BigDecimal.valueOf(solicitud.cantidad())));

            producto.setStock(producto.getStock() - solicitud.cantidad());
        }
        return subtotal;
    }
}
