import {
    actualizarCantidad,
    eliminarProducto,
    obtenerCarrito
} from "./carritoStorage.js?v=20260715-3";
import { enviosApi, obtenerSesion, pedidosApi } from "./api.js";

const contenedor = document.getElementById("contenedorCarrito");
const subtotalEl = document.getElementById("subtotalCarrito");
const envioEl = document.getElementById("envioCarrito");
const totalEl = document.getElementById("totalCarrito");
const formEnvio = document.getElementById("formEnvio");
const opciones = document.getElementById("opcionesEnvio");
const mensajeEnvio = document.getElementById("mensajeEnvio");
const botonCotizar = document.getElementById("btnCotizarEnvio");
const botonValidarDestino = document.getElementById("btnValidarDestino");
const municipioSeleccionado = document.getElementById("municipioSeleccionado");
const botonPagar = document.getElementById("btnPagarBold");

let cotizacionSeleccionada = null;

function nombreUbicacion(ubicacion) {
    const nombre = ubicacion.locationName || ubicacion.cityName || ubicacion.name
        || ubicacion.nombre || ubicacion.municipalityName || "Municipio encontrado";
    const departamento = ubicacion.departmentName || ubicacion.department
        || ubicacion.departamento || "";
    return departamento ? `${nombre}, ${departamento}` : nombre;
}

function moneda(valor) {
    return new Intl.NumberFormat("es-CO", {
        style: "currency", currency: "COP", maximumFractionDigits: 0
    }).format(valor);
}

function detallesApi() {
    return obtenerCarrito().map(item => ({
        productoId: Number(item.databaseId),
        cantidad: Number(item.cantidad)
    }));
}

function subtotal() {
    return obtenerCarrito().reduce(
        (total, producto) => total + Number(producto.price) * Number(producto.cantidad), 0
    );
}

function actualizarTotales() {
    const valorSubtotal = subtotal();
    const costo = Number(cotizacionSeleccionada?.shippingCost || 0);
    subtotalEl.textContent = moneda(valorSubtotal);
    envioEl.textContent = cotizacionSeleccionada ? moneda(costo) : "Por cotizar";
    totalEl.textContent = moneda(valorSubtotal + costo);
}

function invalidarPedido() {
    cotizacionSeleccionada = null;
    opciones.innerHTML = "";
    delete botonPagar.dataset.pedidoId;
    delete botonPagar.dataset.total;
    actualizarTotales();
}

function mostrarCarrito() {
    const carrito = obtenerCarrito();
    if (!carrito.length) {
        contenedor.innerHTML = `
          <div class="carrito-vacio"><i class="bi bi-cart-x"></i>
            <h2>Carrito vacío</h2><p>Agrega productos para continuar.</p>
            <a href="productos.html" class="btn-explorar">Explorar productos</a>
          </div>`;
        actualizarTotales();
        return;
    }

    contenedor.innerHTML = carrito.map(producto => `
      <div class="carrito-card">
        <div class="carrito-imagen"><img src="${producto.image}" class="img-fluid" alt="${producto.name}"></div>
        <div class="carrito-contenido">
          <h3>${producto.name}</h3>
          <p>${producto.description}</p>
          <div class="carrito-precio">${moneda(producto.price)}</div>
          <div class="carrito-controles">
            <button class="btn btn-outline-light btn-disminuir" data-id="${producto.id}">−</button>
            <span>${producto.cantidad}</span>
            <button class="btn btn-outline-light btn-aumentar" data-id="${producto.id}">+</button>
            <button class="btn btn-danger btn-eliminar" data-id="${producto.id}">Eliminar</button>
          </div>
          <div class="carrito-subtotal">Subtotal:
            <strong>${moneda(producto.price * producto.cantidad)}</strong>
          </div>
        </div>
      </div>`).join("");

    document.querySelectorAll(".btn-aumentar,.btn-disminuir").forEach(boton => {
        boton.addEventListener("click", () => {
            const producto = obtenerCarrito().find(p => p.id === boton.dataset.id);
            const delta = boton.classList.contains("btn-aumentar") ? 1 : -1;
            actualizarCantidad(producto.id, producto.cantidad + delta);
            invalidarPedido();
            mostrarCarrito();
        });
    });
    document.querySelectorAll(".btn-eliminar").forEach(boton => {
        boton.addEventListener("click", () => {
            eliminarProducto(boton.dataset.id);
            invalidarPedido();
            mostrarCarrito();
        });
    });
    actualizarTotales();
}

const sesion = obtenerSesion();
if (sesion) {
    const partes = String(sesion.nombreCompleto || "").trim().split(/\s+/);
    document.getElementById("destinatarioNombre").value = partes.shift() || "";
    document.getElementById("destinatarioApellido").value = partes.join(" ");
    document.getElementById("destinatarioEmail").value = sesion.email || "";
}

botonValidarDestino.addEventListener("click", async () => {
    const codigo = document.getElementById("destinoCodigo").value.trim();
    if (!/^[0-9]{5,12}$/.test(codigo)) {
        municipioSeleccionado.textContent = "Ingresa un código DANE válido.";
        return;
    }
    botonValidarDestino.disabled = true;
    municipioSeleccionado.textContent = "Validando con Mi Paquete...";
    try {
        const ubicaciones = await enviosApi.ubicaciones(codigo);
        if (!Array.isArray(ubicaciones) || !ubicaciones.length) {
            throw new Error("Mi Paquete no encontró ese municipio.");
        }
        municipioSeleccionado.textContent = `Destino: ${nombreUbicacion(ubicaciones[0])}`;
    } catch (error) {
        municipioSeleccionado.textContent = error.message;
    } finally {
        botonValidarDestino.disabled = false;
    }
});

botonCotizar.addEventListener("click", async () => {
    const destinoCodigo = document.getElementById("destinoCodigo").value.trim();
    if (!destinoCodigo || !obtenerCarrito().length) {
        mensajeEnvio.textContent = "Indica el código DANE y agrega productos.";
        return;
    }
    if (detallesApi().some(detalle => !Number.isInteger(detalle.productoId))) {
        mensajeEnvio.textContent = "Actualiza el carrito desde el catálogo conectado al servidor.";
        return;
    }

    botonCotizar.disabled = true;
    mensajeEnvio.textContent = "Consultando transportadoras...";
    try {
        const lista = await enviosApi.cotizar({ destinoCodigo, detalles: detallesApi() });
        if (!lista.length) throw new Error("No hay transportadoras disponibles para esta ruta.");
        opciones.innerHTML = lista
            .sort((a, b) => Number(a.shippingCost) - Number(b.shippingCost))
            .map((opcion, indice) => `
              <label class="envio-opcion">
                <input type="radio" name="transportadora" value="${opcion.deliveryCompanyId}"
                  data-indice="${indice}">
                <span><strong>${opcion.deliveryCompanyName}</strong><br>
                  ${moneda(opcion.shippingCost)} · aprox.
                  ${Math.ceil(Number(opcion.shippingTime || 0) / 1440)} día(s)
                </span>
              </label>`).join("");

        const ordenadas = lista.sort((a, b) => Number(a.shippingCost) - Number(b.shippingCost));
        opciones.querySelectorAll('input[name="transportadora"]').forEach(input => {
            input.addEventListener("change", () => {
                cotizacionSeleccionada = ordenadas[Number(input.dataset.indice)];
                delete botonPagar.dataset.pedidoId;
                actualizarTotales();
                mensajeEnvio.textContent = "Transportadora seleccionada.";
            });
        });
        mensajeEnvio.textContent = "Selecciona una opción de envío.";
    } catch (error) {
        mensajeEnvio.textContent = error.message;
    } finally {
        botonCotizar.disabled = false;
    }
});

botonPagar.addEventListener("click", async event => {
    if (botonPagar.dataset.pedidoId) return;
    event.preventDefault();
    event.stopImmediatePropagation();

    if (!obtenerSesion()) {
        sessionStorage.setItem("volverDespuesLogin", "carrito.html");
        window.location.href = "login.html";
        return;
    }
    if (!cotizacionSeleccionada) {
        mensajeEnvio.textContent = "Primero cotiza y selecciona el envío.";
        return;
    }
    if (!formEnvio.reportValidity()) return;

    botonPagar.disabled = true;
    mensajeEnvio.textContent = "Creando pedido seguro...";
    try {
        const pedido = await pedidosApi.crear({
            direccionEnvio: document.getElementById("direccionEnvio").value.trim(),
            destinoCodigo: document.getElementById("destinoCodigo").value.trim(),
            destinatarioNombre: document.getElementById("destinatarioNombre").value.trim(),
            destinatarioApellido: document.getElementById("destinatarioApellido").value.trim(),
            destinatarioEmail: document.getElementById("destinatarioEmail").value.trim(),
            destinatarioTelefono: document.getElementById("destinatarioTelefono").value.trim(),
            transportadoraId: cotizacionSeleccionada.deliveryCompanyId,
            metodoPagoId: null,
            notas: document.getElementById("notasPedido").value.trim(),
            detalles: detallesApi()
        });
        botonPagar.dataset.pedidoId = pedido.id;
        botonPagar.dataset.total = Number(pedido.total);
        mensajeEnvio.textContent = `Pedido #${pedido.id} creado. Abriendo pago...`;
        botonPagar.disabled = false;
        botonPagar.click();
    } catch (error) {
        mensajeEnvio.textContent = error.message;
        botonPagar.disabled = false;
    }
}, true);

mostrarCarrito();
