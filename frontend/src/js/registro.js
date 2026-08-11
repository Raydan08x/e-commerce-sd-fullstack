import { authApi } from "./api.js?v=20260811-1";

const form = document.getElementById("formRegistro");
const $ = id => document.getElementById(id);
const fechaNacimiento = $("fechaNacimiento");
const telefono = $("telefono");
const mensajes = $("mensajesRegistro");
const dialogo = $("terminosDialog");

function fechaMaxima() {
    const fecha = new Date();
    fecha.setFullYear(fecha.getFullYear() - 18);
    return fecha.toISOString().slice(0, 10);
}

function mostrarErrores(errores) {
    mensajes.className = "alert alert-danger mt-3";
    mensajes.innerHTML = `<strong>Revisa estos datos:</strong><ul class="mb-0 mt-2">
        ${errores.map(error => `<li>${error}</li>`).join("")}</ul>`;
}

fechaNacimiento.max = fechaMaxima();
telefono.addEventListener("input", () => {
    const prefijo = telefono.value.trim().startsWith("+") ? "+" : "";
    telefono.value = prefijo + telefono.value.replace(/\D/g, "").slice(0, 15);
});

document.querySelectorAll("[data-password-toggle]").forEach(boton => {
    boton.addEventListener("click", () => {
        const campo = $(boton.dataset.passwordToggle);
        const mostrar = campo.type === "password";
        campo.type = mostrar ? "text" : "password";
        boton.setAttribute("aria-pressed", String(mostrar));
        boton.querySelector("i")?.classList.toggle("bi-eye-slash", mostrar);
    });
});

$("abrirTerminos").addEventListener("click", () => dialogo.showModal());
$("cerrarTerminos").addEventListener("click", () => dialogo.close());
$("entendidoTerminos").addEventListener("click", () => dialogo.close());
dialogo.addEventListener("click", event => {
    if (event.target === dialogo) dialogo.close();
});

form.addEventListener("submit", async event => {
    event.preventDefault();
    const datos = {
        nombre: $("nombre").value.trim(),
        apellidos: $("apellidos").value.trim(),
        fechaNacimiento: fechaNacimiento.value,
        genero: $("genero").value,
        direccion: $("direccion").value.trim(),
        telefono: telefono.value.trim(),
        email: $("email").value.trim().toLowerCase(),
        password: $("password").value,
        aceptaTerminos: $("aceptaTerminos").checked,
        autorizaDatos: $("autorizaDatos").checked,
        autorizaComunicaciones: $("autorizaComunicaciones").checked
    };
    const errores = [];
    if (Object.values(datos).slice(0, 8).some(valor => !valor)) {
        errores.push("Todos los campos personales son obligatorios.");
    }
    if (datos.fechaNacimiento > fechaMaxima()) errores.push("Debes ser mayor de 18 años.");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(datos.email)) errores.push("Correo inválido.");
    if (!/^\+?\d{7,15}$/.test(datos.telefono)) errores.push("Teléfono inválido.");
    if (datos.password.length < 6) errores.push("La contraseña debe tener mínimo 6 caracteres.");
    if (datos.password !== $("confirmarPassword").value) errores.push("Las contraseñas no coinciden.");
    if (!datos.aceptaTerminos) errores.push("Debes aceptar los términos.");
    if (!datos.autorizaDatos) errores.push("Debes autorizar el tratamiento de datos.");
    if (errores.length) return mostrarErrores(errores);

    const boton = form.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
        const respuesta = await authApi.registro(datos);
        sessionStorage.setItem("ultimoRegistroSierraDorada", datos.email);
        mensajes.className = "alert alert-success mt-3";
        mensajes.textContent = respuesta.mensaje
            || "Te enviamos un correo para confirmar y activar la cuenta.";
        setTimeout(() => {
            window.location.href = `verificar-correo.html?email=${encodeURIComponent(datos.email)}`;
        }, 1200);
    } catch (error) {
        mostrarErrores([error.message]);
    } finally {
        boton.disabled = false;
    }
});
