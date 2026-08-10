import { ImageModalManager } from './components/ImageModalManager.js';
import { ConfirmModal } from './components/ConfirmModal.js';
import { ToastManager } from './components/ToastManager.js';
import {
    catalogoApi,
    cerrarSesion,
    normalizarProducto,
    obtenerSesion,
    productoParaApi
} from './api.js?v=20260810-2';

window.toastManager = new ToastManager();

let productos = [];
let categorias = [];

// --- HELPERS PARA GALERÍA DE IMÁGENES ---
const IMAGEN_PLACEHOLDER = "https://placehold.co/600x400/222223/B3A269?text=Imagen+Pendiente";

function normalizarImagenes(producto) {
    if (!producto) return [];

    // Si ya tiene array de imágenes, usarlo
    if (Array.isArray(producto.images) && producto.images.length > 0) {
        const tienePrincipalExplicita = producto.images.some(img => img && img.isMain === true);
        return producto.images.map((img, i) => ({
            url: typeof img === 'string' ? img : (img.url || ''),
            fit: img.fit || producto.imageFit || 'cover',
            isMain: tienePrincipalExplicita ? (img.isMain === true) : (i === 0)
        }));
    }

    // Compatibilidad hacia atrás: campo image individual
    const url = producto.image || IMAGEN_PLACEHOLDER;
    return [{
        url: url,
        fit: producto.imageFit || 'cover',
        isMain: true
    }];
}

function obtenerImagenPrincipal(producto) {
    const imagenes = normalizarImagenes(producto);
    const principal = imagenes.find(img => img.isMain) || imagenes[0];
    return principal || { url: IMAGEN_PLACEHOLDER, fit: 'cover', isMain: true };
}

function obtenerUrlPrincipal(producto) {
    return obtenerImagenPrincipal(producto).url;
}

function tieneImagenReal(producto) {
    const url = obtenerUrlPrincipal(producto);
    return url && !url.includes('placehold.co') && !url.includes('Imagen+Pendiente');
}

function contarImagenes(producto) {
    return normalizarImagenes(producto).length;
}

function comprimirImagen(base64, maxAncho = 800, maxAlto = 800, calidad = 0.8) {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => {
            let ancho = img.width;
            let alto = img.height;

            if (ancho > maxAncho || alto > maxAlto) {
                const ratio = Math.min(maxAncho / ancho, maxAlto / alto);
                ancho = Math.round(ancho * ratio);
                alto = Math.round(alto * ratio);
            }

            const canvas = document.createElement('canvas');
            canvas.width = ancho;
            canvas.height = alto;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, ancho, alto);

            resolve(canvas.toDataURL('image/jpeg', calidad));
        };
        img.onerror = reject;
        img.src = base64;
    });
}

function guardarImagenes(producto, imagenes) {
    // Asegurar que solo haya una imagen principal
    let lista = (imagenes || []).filter(img => img && img.url);
    if (lista.length === 0) {
        lista = [{ url: IMAGEN_PLACEHOLDER, fit: 'cover', isMain: true }];
    }

    const idxPrincipal = lista.findIndex(img => img.isMain);
    if (idxPrincipal === -1) {
        lista[0].isMain = true;
    } else {
        lista.forEach((img, i) => { img.isMain = (i === idxPrincipal); });
    }

    producto.images = lista;
    producto.image = lista.find(img => img.isMain).url;
    producto.imageFit = lista.find(img => img.isMain).fit;
    return producto;
}

// --- REFERENCIAS AL DOM ---
const formProducto = document.getElementById("formProducto");
// Campos del formulario
const id = document.getElementById("id");
const name = document.getElementById("name");
const inspiration = document.getElementById("inspiration");
const description = document.getElementById("description");
const price = document.getElementById("price");
const abv = document.getElementById("abv");
const ibu = document.getElementById("ibu");
const image = document.getElementById("image");
const colorHex = document.getElementById("colorHex");
const colorName = document.getElementById("colorName");
const temperature = document.getElementById("temperature");
const legend = document.getElementById("legend");
const fullDescription = document.getElementById("fullDescription");
const process = document.getElementById("process");
const characteristics_color = document.getElementById("characteristics_color");
const characteristics_aroma = document.getElementById("characteristics_aroma");
const characteristics_sabor = document.getElementById("characteristics_sabor");
const characteristics_maridaje = document.getElementById("characteristics_maridaje");
const maridaje = document.getElementById("maridaje");

// Campos ocultos para controlar el modo de edición.
const modoEdicion = document.getElementById("modoEdicion");
const idOriginal = document.getElementById("idOriginal");

// Referencias a otros elementos de la interfaz.
const tablaProductos = document.getElementById("tablaProductos");
const mensajeProducto = document.getElementById("mensajeProducto");
const mensajeTabla = document.getElementById("mensajeTabla");

const tituloFormulario = document.getElementById("tituloFormulario");
const btnGuardar = document.getElementById("btnGuardar");
const btnLimpiar = document.getElementById("btnLimpiar");
const btnCerrarSesion = document.getElementById("btnCerrarSesion");
const inputBusqueda = document.getElementById("inputBusqueda");

// Referencias para acciones en lote
const seleccionarTodo = document.getElementById("seleccionarTodo");
const btnAccionesLote = document.getElementById("btnAccionesLote");
const btnEliminarSeleccionados = document.getElementById("btnEliminarSeleccionados");

// Helper functions to switch tabs safely
function mostrarTabGestion() {
    const el = document.getElementById('gestion-tab');
    if (el) el.click();
}

function mostrarTabFormulario() {
    const el = document.getElementById('formulario-tab');
    if (el) el.click();
}

// Los modales se gestionan a través de componentes (ImageModalManager y ConfirmModal)
// definidos en src/js/components/ e instanciados al final de este archivo.

// Diccionario inteligente para mapear comidas a emojis de maridaje
const emojiMap = {
    "hamburguesa": "🍔",
    "hamburguesas": "🍔",
    "pollo": "🍗",
    "queso": "🧀",
    "quesos": "🧀",
    "picante": "🌶️",
    "ensalada": "🥗",
    "ensaladas": "🥗",
    "ceviche": "🐟",
    "ceviches": "🐟",
    "marisco": "🍤",
    "mariscos": "🍤",
    "pescado": "🐟",
    "postre": "🍰",
    "postres": "🍰",
    "chocolate": "🍫",
    "carne": "🥩",
    "carnes": "🥩",
    "ostra": "🦪",
    "ostras": "🦪",
    "café": "☕",
    "cafe": "☕",
    "miel": "🍯",
    "panela": "🍯",
    "roble": "🪵",
    "whiskey": "🥃",
    "caramelo": "🍮",
    "puro": "🚬",
    "puros": "🚬"
};

// --- LÓGICA DE AUTENTICACIÓN ---
function protegerRutaAdmin() {
    const sesion = obtenerSesion();
    if (!sesion || sesion.rol !== "admin") {
        window.location.href = "login.html";
        throw new Error("Se requiere una sesión de administrador.");
    }
}

// --- LÓGICA DE DATOS (PRODUCTOS) ---
async function cargarProductos() {
    mensajeTabla.textContent = "Cargando productos desde el servidor...";
    tablaProductos.innerHTML = Array.from({ length: 5 }, () => `
        <tr class="admin-tabla-skeleton" aria-hidden="true">
            <td colspan="9"><span></span></td>
        </tr>`).join("");
    try {
        [productos, categorias] = await Promise.all([
            catalogoApi.listar(false),
            catalogoApi.categorias()
        ]);
        mensajeTabla.textContent = "";
    } catch (error) {
        console.error("No se pudo cargar el catálogo desde la API:", error);
        mensajeTabla.textContent = `Error: ${error.message}`;
    }
}

async function guardarProductos() {
    try {
        await Promise.all(productos
            .filter(producto => producto.databaseId)
            .map(producto => catalogoApi.actualizar(
                producto.databaseId,
                productoParaApi(producto, obtenerCategoriaId(producto.categoria))
            )));
        return true;
    } catch (error) {
        console.error('Error al guardar productos en la API:', error);
        if (window.toastManager) window.toastManager.show(error.message, 'error');
        return false;
    }
}

function obtenerCategoriaId(nombre) {
    return categorias.find(categoria => categoria.nombre === nombre)?.id || null;
}

/**
 * Muestra los productos en la tabla.
 * @param {Array} productosAMostrar - La lista de productos que se va a renderizar.
 */
let columnaOrdenamiento = "";
let ordenAscendente = true;

function ordenarProductos(lista) {
    if (!columnaOrdenamiento) return lista;

    return [...lista].sort((a, b) => {
        let valA = a[columnaOrdenamiento];
        let valB = b[columnaOrdenamiento];

        if (columnaOrdenamiento === 'categoria') {
            valA = valA || 'Cervezas';
            valB = valB || 'Cervezas';
        } else if (columnaOrdenamiento === 'activo') {
            valA = (valA !== false) ? 1 : 0;
            valB = (valB !== false) ? 1 : 0;
        } else if (columnaOrdenamiento === 'price') {
            valA = Number(valA) || 0;
            valB = Number(valB) || 0;
        } else if (columnaOrdenamiento === 'abv') {
            valA = parseFloat(String(valA).replace(/[^0-9.]/g, '')) || 0;
            valB = parseFloat(String(valB).replace(/[^0-9.]/g, '')) || 0;
        } else if (columnaOrdenamiento === 'ibu') {
            valA = parseInt(String(valA).replace(/[^0-9]/g, '')) || 0;
            valB = parseInt(String(valB).replace(/[^0-9]/g, '')) || 0;
        } else {
            valA = String(valA || '').toLowerCase();
            valB = String(valB || '').toLowerCase();
        }

        if (valA < valB) return ordenAscendente ? -1 : 1;
        if (valA > valB) return ordenAscendente ? 1 : -1;
        return 0;
    });
}

function mostrarProductos(productosAMostrar) {
    tablaProductos.innerHTML = "";

    const productosOrdenados = ordenarProductos(productosAMostrar);

    if (productosOrdenados.length === 0) {
        tablaProductos.innerHTML = `<tr><td colspan="9" class="text-center">No se encontraron productos.</td></tr>`;
        return;
    }

    productosOrdenados.forEach(producto => {
        // Lógica para el botón de la imagen
        const imgReal = tieneImagenReal(producto);
        const totalImgs = contarImagenes(producto);
        const claseBotonImagen = imgReal ? 'btn-warning text-dark' : 'btn-outline-secondary text-secondary';
        const tituloBoton = totalImgs > 1 ? `Gestionar ${totalImgs} imágenes` : 'Gestionar imagen';
        const botonImagen = `
            <button class="btn btn-sm ${claseBotonImagen}" onclick="abrirModalImagen('${producto.id}')" data-bs-toggle="tooltip" data-bs-placement="top" title="${tituloBoton}">
                <i class="bi bi-camera"></i>${totalImgs > 1 ? `<span class="badge bg-dark ms-1" style="font-size: 0.6rem;">${totalImgs}</span>` : ''}
            </button>`;

        const estadoLabel = (producto.activo !== false)
            ? `<span class="estado-activo">Activo</span>`
            : `<span class="estado-inactivo">Inactivo</span>`;

        // Plantillas literales para construir el HTML de la fila.
        tablaProductos.innerHTML += `
            <tr>
                <td><input type="checkbox" class="form-check-input" data-id="${producto.id}"></td>
                <td>${producto.id}</td>
                <td>${producto.name}</td>
                <td>${producto.categoria || 'Cervezas'}</td>
                <td>$${producto.price.toLocaleString('es-CO')}</td>
                <td>${producto.abv || 'N/A'}</td>
                <td>${producto.ibu || 'N/A'}</td>
                <td>${estadoLabel}</td>
                <td class="text-center">
                    <div class="acciones-tabla">
                        <button class="btn btn-sm btn-warning" onclick="editarProducto('${producto.id}')">Editar</button>
                        <button class="btn btn-sm btn-danger" onclick="eliminarProducto('${producto.id}')">Eliminar</button>
                        ${botonImagen}
                    </div>
                </td>
            </tr>
        `;
    });

    // Inicializar los tooltips de Bootstrap después de crear los elementos
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    actualizarEstadoAccionesLote();
}

function toggleSeccionCerveza(categoria) {
    const seccionCerveza = document.getElementById("seccionCervezaDetalles");
    if (seccionCerveza) {
        if (categoria === "Cervezas") {
            seccionCerveza.style.display = "block";
        } else {
            seccionCerveza.style.display = "none";
        }
    }
}

// --- LÓGICA DEL FORMULARIO ---
function limpiarFormulario() {
    formProducto.reset();

    modoEdicion.value = "false";
    idOriginal.value = "";

    tituloFormulario.textContent = "Agregar producto";
    btnGuardar.textContent = "Guardar producto";
    id.disabled = false;
    mensajeProducto.textContent = "";

    const catSelector = document.getElementById("categoriaSelector");
    if (catSelector) {
        catSelector.value = "Cervezas";
    }
    const activoProd = document.getElementById("activoProducto");
    if (activoProd) {
        activoProd.checked = true;
    }
    toggleSeccionCerveza("Cervezas");
}

function existeProducto(id) {
    return productos.some(producto => producto.id === id);
}

function sugerirSiguienteId() {
    const maxId = productos
        .map(p => p.id)
        .map(id => id.substring(1))
        .filter(numStr => !isNaN(Number(numStr)))
        .map(numStr => Number(numStr))
        .reduce((max, current) => (current > max ? current : max), 999);

    const siguienteIdNumerico = maxId + 1;
    return `C${siguienteIdNumerico}`;
}

function validarFormulario() {
    const idVal = id ? id.value.trim() : '';
    const nameVal = name ? name.value.trim() : '';
    const priceVal = price ? Number(price.value) : 0;

    if (mensajeProducto) {
        mensajeProducto.textContent = '';
        mensajeProducto.className = '';
    }

    if (!idVal || !nameVal) {
        if (window.toastManager) {
            window.toastManager.show('Completa los campos obligatorios (ID, Nombre).', 'error');
        }
        return false;
    }

    if (isNaN(priceVal) || priceVal < 0) {
        if (window.toastManager) {
            window.toastManager.show('El precio debe ser un número positivo.', 'error');
        }
        return false;
    }

    // Si estamos agregando uno nuevo, validar que el ID no exista
    if (modoEdicion && modoEdicion.value !== "true" && existeProducto(idVal)) {
        if (window.toastManager) {
            window.toastManager.show('El ID ingresado ya existe.', 'error');
        }
        return false;
    }

    return true;
}

function parsearMaridaje(texto) {
    if (!texto || !texto.trim()) return [];
    return texto.split(',').map(item => {
        const name = item.trim();
        const nameLower = name.toLowerCase();
        let emoji = "🍽️"; // Emoji por defecto

        for (const key in emojiMap) {
            if (nameLower.includes(key)) {
                emoji = emojiMap[key];
                break;
            }
        }
        return { emoji, name };
    }).slice(0, 3);
}

// Agrega un nuevo producto a la lista.
async function agregarProducto() {
    const catSelector = document.getElementById("categoriaSelector");
    const activoProd = document.getElementById("activoProducto");

    const nuevoProducto = {
        id: id ? id.value.trim() : '',
        name: name ? name.value.trim() : '',
        categoria: catSelector ? catSelector.value : "Cervezas",
        activo: activoProd ? activoProd.checked : true,
        inspiration: inspiration ? inspiration.value.trim() : '',
        description: description ? description.value.trim() : '',
        price: price ? Number(price.value) : 0,
        stock: Number(document.getElementById("stock")?.value || 0),
        abv: abv ? abv.value.trim() : '',
        ibu: ibu ? ibu.value.trim() : '',
        image: (image ? image.value.trim() : '') || IMAGEN_PLACEHOLDER,
        colorHex: colorHex ? colorHex.value.trim() : '',
        colorName: colorName ? colorName.value.trim() : '',
        temperature: temperature ? temperature.value.trim() : '',
        legend: legend ? legend.value.trim() : '',
        fullDescription: fullDescription ? fullDescription.value.trim() : '',
        process: process ? process.value.trim() : '',
        characteristics: {
            color: characteristics_color ? characteristics_color.value.trim() : '',
            aroma: characteristics_aroma ? characteristics_aroma.value.trim() : '',
            sabor: characteristics_sabor ? characteristics_sabor.value.trim() : '',
            maridaje: characteristics_maridaje ? characteristics_maridaje.value.trim() : ''
        },
        maridaje: maridaje ? parsearMaridaje(maridaje.value) : []
    };

    // Normalizar imágenes para el nuevo producto
    guardarImagenes(nuevoProducto, [{
        url: nuevoProducto.image,
        fit: 'cover',
        isMain: true
    }]);

    try {
        const guardado = await catalogoApi.crear(productoParaApi(
            nuevoProducto, obtenerCategoriaId(nuevoProducto.categoria)));
        productos.push(normalizarProducto(guardado));
    } catch (error) {
        window.toastManager?.show(error.message, "error");
        return;
    }
    filtrarYMostrarProductos();
    limpiarFormulario();

    if (window.toastManager) {
        window.toastManager.show("Producto agregado correctamente.", 'success');
    }
    mostrarTabGestion(); // Volver a la pestaña de gestión
}

async function actualizarProducto() { // Actualiza un producto existente.
    const idActual = id ? id.value.trim() : '';
    const idAnterior = idOriginal ? idOriginal.value : '';

    const indice = productos.findIndex(p => p.id === idAnterior);
    if (indice === -1) return;
    const databaseId = productos[indice].databaseId;

    const catSelector = document.getElementById("categoriaSelector");
    const activoProd = document.getElementById("activoProducto");

    const nuevaUrlImagen = (image ? image.value.trim() : '') || IMAGEN_PLACEHOLDER;
    const imagenesExistentes = normalizarImagenes(productos[indice]);

    // Si el campo image del formulario cambió respecto a la imagen principal actual,
    // actualizamos la primera imagen del array; de lo contrario conservamos el array.
    const urlPrincipalAnterior = obtenerUrlPrincipal(productos[indice]);
    let nuevasImagenes = imagenesExistentes;
    if (nuevaUrlImagen !== urlPrincipalAnterior) {
        if (imagenesExistentes.length > 0) {
            imagenesExistentes[0] = { url: nuevaUrlImagen, fit: 'cover', isMain: true };
            nuevasImagenes = imagenesExistentes;
        } else {
            nuevasImagenes = [{ url: nuevaUrlImagen, fit: 'cover', isMain: true }];
        }
    }

    productos[indice] = {
        databaseId,
        id: idActual,
        name: name ? name.value.trim() : '',
        categoria: catSelector ? catSelector.value : "Cervezas",
        activo: activoProd ? activoProd.checked : true,
        inspiration: inspiration ? inspiration.value.trim() : '',
        description: description ? description.value.trim() : '',
        price: price ? Number(price.value) : 0,
        stock: Number(document.getElementById("stock")?.value || 0),
        abv: abv ? abv.value.trim() : '',
        ibu: ibu ? ibu.value.trim() : '',
        image: nuevaUrlImagen,
        colorHex: colorHex ? colorHex.value.trim() : '',
        colorName: colorName ? colorName.value.trim() : '',
        temperature: temperature ? temperature.value.trim() : '',
        legend: legend ? legend.value.trim() : '',
        fullDescription: fullDescription ? fullDescription.value.trim() : '',
        process: process ? process.value.trim() : '',
        characteristics: {
            color: characteristics_color ? characteristics_color.value.trim() : '',
            aroma: characteristics_aroma ? characteristics_aroma.value.trim() : '',
            sabor: characteristics_sabor ? characteristics_sabor.value.trim() : '',
            maridaje: characteristics_maridaje ? characteristics_maridaje.value.trim() : ''
        },
        maridaje: maridaje ? parsearMaridaje(maridaje.value) : []
    };

    // Sincronizar el array de imágenes con la imagen principal editada
    guardarImagenes(productos[indice], nuevasImagenes);

    try {
        const guardado = await catalogoApi.actualizar(
            productos[indice].databaseId,
            productoParaApi(productos[indice], obtenerCategoriaId(productos[indice].categoria))
        );
        productos[indice] = normalizarProducto(guardado);
    } catch (error) {
        window.toastManager?.show(error.message, "error");
        return;
    }
    filtrarYMostrarProductos();
    limpiarFormulario();

    if (window.toastManager) {
        window.toastManager.show("Producto actualizado correctamente.", 'success');
    }
    mostrarTabGestion();
}

async function editarProducto(idProducto) {
    const productoEncontrado = productos.find(p => p.id === idProducto);

    if (!productoEncontrado) return;

    const confirmado = await window.confirmacionModal.mostrar(
        'Editar Producto',
        `¿Estás seguro de que deseas editar el producto "${productoEncontrado.name}"?`
    );

    if (!confirmado) return;

    if (window.toastManager) {
        window.toastManager.show("Cargando datos del producto para editar...", 'info');
    }

    // Llenar el formulario con los datos del producto safely.
    if (id) id.value = productoEncontrado.id || '';
    if (name) name.value = productoEncontrado.name || '';

    const catSelector = document.getElementById("categoriaSelector");
    if (catSelector) {
        catSelector.value = productoEncontrado.categoria || 'Cervezas';
        toggleSeccionCerveza(catSelector.value);
    }
    const activoProd = document.getElementById("activoProducto");
    if (activoProd) {
        activoProd.checked = productoEncontrado.activo !== false;
    }

    if (inspiration) inspiration.value = productoEncontrado.inspiration || '';
    if (description) description.value = productoEncontrado.description || '';
    if (price) price.value = productoEncontrado.price || 0;
    const stock = document.getElementById("stock");
    if (stock) stock.value = productoEncontrado.stock || 0;
    if (abv) abv.value = productoEncontrado.abv || '';
    if (ibu) ibu.value = productoEncontrado.ibu || '';
    if (image) image.value = productoEncontrado.image || '';
    const imageFileSelector = document.getElementById("imageFileSelector");
    if (imageFileSelector) imageFileSelector.value = productoEncontrado.image || '';
    if (colorHex) colorHex.value = productoEncontrado.colorHex || '';
    const colorHexPicker = document.getElementById("colorHexPicker");
    if (colorHexPicker && productoEncontrado.colorHex && /^#[0-9A-F]{6}$/i.test(productoEncontrado.colorHex)) {
        colorHexPicker.value = productoEncontrado.colorHex;
    }
    if (colorName) colorName.value = productoEncontrado.colorName || '';
    if (temperature) temperature.value = productoEncontrado.temperature || '';
    if (legend) legend.value = productoEncontrado.legend || '';
    if (fullDescription) fullDescription.value = productoEncontrado.fullDescription || '';
    if (process) process.value = productoEncontrado.process || '';

    const chars = productoEncontrado.characteristics || {};
    if (characteristics_color) characteristics_color.value = chars.color || '';
    if (characteristics_aroma) characteristics_aroma.value = chars.aroma || '';
    if (characteristics_sabor) characteristics_sabor.value = chars.sabor || '';
    if (characteristics_maridaje) characteristics_maridaje.value = chars.maridaje || '';

    // Convertir de [{emoji, name}] de vuelta a "Comida 1, Comida 2, Comida 3" para fácil edición
    if (maridaje) maridaje.value = (productoEncontrado.maridaje || []).map(m => m.name).join(', ');

    if (modoEdicion) modoEdicion.value = "true";
    if (idOriginal) idOriginal.value = productoEncontrado.id;

    if (tituloFormulario) tituloFormulario.textContent = "Editar producto";
    if (id) id.disabled = true; // No se puede cambiar el ID al editar.
    if (btnGuardar) btnGuardar.textContent = "Actualizar producto";

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });

    mostrarTabFormulario(); // Cambiar a la pestaña del formulario
}

async function eliminarProducto(idProducto) {
    const producto = productos.find(p => p.id === idProducto);
    if (!producto) return;

    const confirmado = await window.confirmacionModal.mostrar(
        'Eliminar Producto',
        `¿Estás seguro de que deseas eliminar el producto "${producto.name}"? Esta acción no se puede deshacer.`
    );

    if (confirmado) {
        try {
            await catalogoApi.eliminar(producto.databaseId);
            productos = productos.filter(p => p.id !== idProducto);
        } catch (error) {
            window.toastManager?.show(error.message, "error");
            return;
        }
        filtrarYMostrarProductos();

        if (window.toastManager) {
            window.toastManager.show("Producto eliminado correctamente.", 'success');
        }
    }
}

// --- LÓGICA DEL MODAL DE GALERÍA DE IMÁGENES ---
// --- ACCIONES EN LOTE ---
function obtenerIdsSeleccionados() {
    const checkboxes = document.querySelectorAll('#tablaProductos input[type="checkbox"]:checked');
    return Array.from(checkboxes).map(cb => cb.getAttribute('data-id'));
}

function actualizarEstadoAccionesLote() {
    const seleccionados = obtenerIdsSeleccionados();
    btnAccionesLote.disabled = seleccionados.length === 0;
}

async function ejecutarAccionEnLote(accion) {
    const ids = obtenerIdsSeleccionados();
    if (ids.length === 0) return;

    if (accion === 'eliminar') {
        const confirmado = await window.confirmacionModal.mostrar(
            'Eliminar Productos',
            `¿Seguro que deseas eliminar ${ids.length} producto(s) seleccionados?`
        );
        if (!confirmado) return;

        try {
            const seleccionados = productos.filter(p => ids.includes(p.id));
            await Promise.all(seleccionados.map(p => catalogoApi.eliminar(p.databaseId)));
            productos = productos.filter(p => !ids.includes(p.id));
        } catch (error) {
            window.toastManager?.show(error.message, "error");
            return;
        }
    }
    filtrarYMostrarProductos();
    if (window.toastManager) {
        window.toastManager.show(`Acción en lote '${accion}' completada.`, 'info');
    }
    seleccionarTodo.checked = false;
}

// --- FILTRADO DE BÚSQUEDA Y CATEGORÍA ---
let crudCategoriaActiva = 'todos';

function filtrarYMostrarProductos() {
    const consulta = inputBusqueda.value.toLowerCase().trim();
    let filtrados = productos;

    // 1. Filtrar por categoría
    if (crudCategoriaActiva !== 'todos') {
        filtrados = filtrados.filter(p => (p.categoria || 'Cervezas') === crudCategoriaActiva);
    }

    // 2. Filtrar por texto de búsqueda
    if (consulta) {
        filtrados = filtrados.filter(p =>
            p.id.toLowerCase().includes(consulta) ||
            p.name.toLowerCase().includes(consulta) ||
            (p.inspiration && p.inspiration.toLowerCase().includes(consulta))
        );
    }

    mostrarProductos(filtrados);
}

// --- OTRAS FUNCIONES ---
// Event listener para el envío del formulario
formProducto.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    if (!validarFormulario()) {
        return;
    }

    if (modoEdicion.value === "true") {
        await actualizarProducto();
    } else {
        await agregarProducto();
    }
});

btnLimpiar.addEventListener("click", function () {
    limpiarFormulario();
});

btnCerrarSesion.addEventListener("click", function () {
    cerrarSesion();
    window.location.href = "login.html";
});

document.getElementById('formulario-tab').addEventListener('show.bs.tab', function () {
    if (modoEdicion.value !== "true") {
        limpiarFormulario();
        id.value = sugerirSiguienteId();
    }
});

inputBusqueda.addEventListener("input", filtrarYMostrarProductos);

seleccionarTodo.addEventListener("change", function () {
    const checkboxes = document.querySelectorAll('#tablaProductos input[type="checkbox"]');
    checkboxes.forEach(cb => cb.checked = seleccionarTodo.checked);
    actualizarEstadoAccionesLote();
});

tablaProductos.addEventListener('change', function (e) {
    if (e.target.matches('input[type="checkbox"]')) {
        actualizarEstadoAccionesLote();
    }
});

btnEliminarSeleccionados.addEventListener("click", () => ejecutarAccionEnLote('eliminar'));
// Exponer funciones al objeto global
window.editarProducto = editarProducto;
window.eliminarProducto = eliminarProducto;

function seleccionarColorPredefinido(hex, name) {
    const colorHex = document.getElementById("colorHex");
    const colorHexPicker = document.getElementById("colorHexPicker");
    const colorName = document.getElementById("colorName");
    if (colorHex) colorHex.value = hex;
    if (colorHexPicker) colorHexPicker.value = hex;
    if (colorName) colorName.value = name;
}

const contenedorColoresPredefinidos = document.getElementById("contenedorColoresPredefinidos");
if (contenedorColoresPredefinidos) {
    contenedorColoresPredefinidos.addEventListener("click", function (e) {
        const btn = e.target.closest(".btn-color-predefinido");
        if (!btn) return;
        seleccionarColorPredefinido(btn.dataset.color, btn.dataset.nombre);
    });
}

// Enlazar picker de color con input de texto
{
    const _colorHex = document.getElementById("colorHex");
    const _colorHexPicker = document.getElementById("colorHexPicker");
    if (_colorHex && _colorHexPicker) {
        _colorHexPicker.addEventListener("input", function () {
            _colorHex.value = _colorHexPicker.value.toUpperCase();
        });
        _colorHex.addEventListener("input", function () {
            if (/^#[0-9A-F]{6}$/i.test(_colorHex.value)) {
                _colorHexPicker.value = _colorHex.value;
            }
        });
    }
}

// Enlazar selector de categoría para ocultar/mostrar detalles de cerveza
const catSelector = document.getElementById("categoriaSelector");
if (catSelector) {
    catSelector.addEventListener("change", function () {
        toggleSeccionCerveza(catSelector.value);
    });
}

// Enlazar selector de imagen local del proyecto
const imageFileSelector = document.getElementById("imageFileSelector");
if (imageFileSelector) {
    imageFileSelector.addEventListener("change", function (evento) {
        const ruta = evento.target.value;
        if (!ruta) return;

        const imageInput = document.getElementById("image");
        if (imageInput) {
            imageInput.value = ruta;
            if (window.toastManager) {
                window.toastManager.show('Imagen local seleccionada correctamente.', 'success');
            }
        }
    });
}

// Enlazar input de archivo local para cargar imagen desde PC (comprimida para localStorage)
const imageFile = document.getElementById("imageFile");
if (imageFile) {
    imageFile.addEventListener("change", async function (evento) {
        const archivo = evento.target.files[0];
        if (!archivo) return;

        if (archivo.size > 20 * 1024 * 1024) {
            if (window.toastManager) {
                window.toastManager.show('La imagen es muy grande (máx 20MB).', 'error');
            }
            imageFile.value = '';
            return;
        }

        try {
            const base64Original = await new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onloadend = () => resolve(reader.result);
                reader.onerror = reject;
                reader.readAsDataURL(archivo);
            });
            const base64Comprimida = await comprimirImagen(base64Original);
            const imageInput = document.getElementById("image");
            if (imageInput) {
                imageInput.value = base64Comprimida;
                if (window.toastManager) {
                    window.toastManager.show('Imagen cargada correctamente desde la PC.', 'success');
                }
            }
        } catch (error) {
            console.error('Error al procesar imagen:', error);
            if (window.toastManager) {
                window.toastManager.show('Error al procesar la imagen.', 'error');
            }
        }
        imageFile.value = '';
    });
}

// Enlazar ordenamiento de columnas por clic en los encabezados
const sortableHeaders = document.querySelectorAll(".sortable-header");
sortableHeaders.forEach(header => {
    header.addEventListener("click", () => {
        const campo = header.getAttribute("data-sort");

        if (columnaOrdenamiento === campo) {
            ordenAscendente = !ordenAscendente;
        } else {
            columnaOrdenamiento = campo;
            ordenAscendente = true;
        }

        // Actualizar iconos visuales de todos los encabezados
        sortableHeaders.forEach(h => {
            const icono = h.querySelector("i");
            if (icono) {
                const hCampo = h.getAttribute("data-sort");
                if (hCampo === columnaOrdenamiento) {
                    icono.className = ordenAscendente ? "bi bi-arrow-up ms-1" : "bi bi-arrow-down ms-1";
                } else {
                    icono.className = "bi bi-arrow-down-up ms-1";
                }
            }
        });

        filtrarYMostrarProductos();
    });
});

// --- INICIALIZACIÓN ASÍNCRONA ---
async function inicializarAdmin() {
    protegerRutaAdmin();
    await cargarProductos();
    mostrarProductos(productos);

    // Inicializar componentes de modales
    window.confirmacionModal = new ConfirmModal();
    window.imageModalManager = new ImageModalManager({
        getProductos: () => productos,
        guardarProductos: guardarProductos,
        filtrarYMostrarProductos: filtrarYMostrarProductos,
        toastManager: window.toastManager
    });

    // Wrapper global para abrir el modal desde los botones de la tabla
    window.abrirModalImagen = function (idProducto) {
        if (window.imageModalManager) window.imageModalManager.abrir(idProducto);
    };

}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarAdmin);
} else {
    inicializarAdmin();
}
