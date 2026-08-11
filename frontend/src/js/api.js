const ES_LOCAL = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const API_POR_DEFECTO = ES_LOCAL
    ? `http://${window.location.hostname}:8080/api`
    : `${window.location.origin}/api`;
const API_BASE_URL = String(
    window.SIERRA_DORADA_API_URL || API_POR_DEFECTO
).replace(/\/$/, "");

const CLAVE_SESION = "sesionSierraDorada";

export function obtenerSesion() {
    try {
        return JSON.parse(localStorage.getItem(CLAVE_SESION));
    } catch {
        localStorage.removeItem(CLAVE_SESION);
        return null;
    }
}

export function guardarSesion(respuesta) {
    const sesion = {
        token: respuesta.token,
        tipo: respuesta.tipo || "Bearer",
        id: respuesta.id,
        email: respuesta.email,
        nombreCompleto: respuesta.nombreCompleto,
        rol: String(respuesta.rol || "cliente").toLowerCase()
    };
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    return sesion;
}

export function cerrarSesion() {
    localStorage.removeItem(CLAVE_SESION);
}

export async function api(path, options = {}) {
    const sesion = obtenerSesion();
    const headers = new Headers(options.headers || {});
    const bodyEsJson = options.body && !(options.body instanceof FormData);

    if (bodyEsJson && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }
    if (sesion?.token && options.autenticado !== false) {
        headers.set("Authorization", `${sesion.tipo || "Bearer"} ${sesion.token}`);
    }

    let response;
    try {
        response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
    } catch {
        throw new Error("No fue posible conectar con el servidor.");
    }

    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
        ? await response.json()
        : await response.text();

    if (!response.ok) {
        if (response.status === 401 && options.autenticado !== false) {
            cerrarSesion();
        }
        const errores = payload?.errors ? Object.values(payload.errors).join(" ") : "";
        throw new Error(errores || payload?.message || payload?.mensaje
            || `Error del servidor (HTTP ${response.status})`);
    }
    return response.status === 204 ? null : payload;
}

export function normalizarProducto(producto) {
    return {
        databaseId: producto.id,
        id: producto.codigo,
        name: producto.nombre,
        categoria: producto.categoria?.nombre || "Sin categoría",
        categoriaId: producto.categoria?.id || null,
        activo: producto.activo !== false,
        inspiration: producto.inspiracion || "",
        description: producto.descripcion || "",
        price: Number(producto.precio || 0),
        abv: producto.abv == null ? "" : `${producto.abv}%`,
        ibu: producto.ibu == null ? "" : String(producto.ibu),
        image: producto.imagenUrl || "",
        colorHex: producto.colorHex || "",
        colorName: producto.colorNombre || "",
        temperature: producto.temperatura || "",
        legend: producto.leyenda || "",
        fullDescription: producto.descripcionCompleta || "",
        characteristics: producto.caracteristicas || {},
        process: producto.proceso || "",
        maridaje: producto.maridaje || [],
        stock: Number(producto.stock || 0),
        unidadesPorProducto: Number(producto.unidadesPorProducto || 1),
        pesoEnvioKg: producto.pesoEnvioKg == null ? "" : Number(producto.pesoEnvioKg),
        anchoEnvioCm: producto.anchoEnvioCm ?? "",
        largoEnvioCm: producto.largoEnvioCm ?? "",
        altoEnvioCm: producto.altoEnvioCm ?? ""
    };
}

export function productoParaApi(producto, categoriaId) {
    return {
        codigo: producto.id,
        nombre: producto.name,
        descripcion: producto.description,
        precio: Number(producto.price),
        categoria: categoriaId ? { id: Number(categoriaId) } : null,
        marca: "Sierra Dorada",
        tipoCerveza: null,
        estiloCerveza: null,
        stock: Number(producto.stock ?? 0),
        unidadesPorProducto: Number(producto.unidadesPorProducto ?? 1),
        pesoEnvioKg: producto.pesoEnvioKg === "" ? null : Number(producto.pesoEnvioKg),
        anchoEnvioCm: producto.anchoEnvioCm === "" ? null : Number(producto.anchoEnvioCm),
        largoEnvioCm: producto.largoEnvioCm === "" ? null : Number(producto.largoEnvioCm),
        altoEnvioCm: producto.altoEnvioCm === "" ? null : Number(producto.altoEnvioCm),
        abv: producto.abv ? Number(String(producto.abv).replace("%", "")) : null,
        ibu: producto.ibu ? Number(producto.ibu) : null,
        imagenUrl: producto.image,
        inspiracion: producto.inspiration,
        colorHex: producto.colorHex,
        colorNombre: producto.colorName,
        temperatura: producto.temperature,
        leyenda: producto.legend,
        descripcionCompleta: producto.fullDescription,
        proceso: producto.process,
        caracteristicas: producto.characteristics,
        maridaje: producto.maridaje,
        activo: producto.activo !== false
    };
}

export const authApi = {
    login: datos => api("/auth/login", {
        method: "POST", autenticado: false, body: JSON.stringify(datos)
    }),
    registro: datos => api("/auth/registro", {
        method: "POST", autenticado: false, body: JSON.stringify(datos)
    }),
    verificarCorreo: token => api("/auth/verificar-correo", {
        method: "POST", autenticado: false, body: JSON.stringify({ token })
    }),
    reenviarVerificacion: email => api("/auth/reenviar-verificacion", {
        method: "POST", autenticado: false, body: JSON.stringify({ email })
    })
};

export const catalogoApi = {
    listar: (soloActivos = true) => api(`/productos?soloActivos=${soloActivos}`, {
        autenticado: false
    }).then(lista => lista.map(normalizarProducto)),
    crear: producto => api("/productos", { method: "POST", body: JSON.stringify(producto) }),
    actualizar: (id, producto) => api(`/productos/${id}`, {
        method: "PUT", body: JSON.stringify(producto)
    }),
    eliminar: id => api(`/productos/${id}`, { method: "DELETE" }),
    categorias: () => api("/categorias", { autenticado: false })
};

export const formulariosApi = {
    contacto: datos => api("/contacto", { method: "POST", body: JSON.stringify(datos) }),
    newsletter: email => api("/newsletter", {
        method: "POST", body: JSON.stringify({ email })
    })
};

export const enviosApi = {
    ubicaciones: consulta => api(
        `/envios/ubicaciones${consulta ? `?q=${encodeURIComponent(consulta)}` : ""}`,
        { autenticado: false }
    ),
    cotizar: datos => api("/envios/cotizaciones", {
        method: "POST", autenticado: false, body: JSON.stringify(datos)
    })
};

export const perfilApi = {
    obtener: () => api("/perfil")
};

export const pedidosApi = {
    crear: datos => api("/pedidos", { method: "POST", body: JSON.stringify(datos) }),
    listar: () => api("/pedidos")
};

export const pagosApi = {
    confirmarBold: referencia => api(
        `/pagos/bold/ordenes/${encodeURIComponent(referencia)}/confirmacion`,
        { method: "POST" }
    )
};
