import { authApi, guardarSesion } from "./api.js?v=20260811-1";

const parametros = new URLSearchParams(window.location.search);
const token = parametros.get("token");
const emailInicial = parametros.get("email")
    || sessionStorage.getItem("ultimoRegistroSierraDorada") || "";
const mensaje = document.getElementById("mensajeVerificacion");
const descripcion = document.getElementById("descripcionVerificacion");
const formReenvio = document.getElementById("formReenvio");
const emailReenvio = document.getElementById("emailReenvio");

function mostrar(tipo, texto) {
    mensaje.className = `alert alert-${tipo} mt-3`;
    mensaje.textContent = texto;
}

function mostrarReenvio() {
    formReenvio.classList.remove("d-none");
    emailReenvio.value = emailInicial;
}

async function verificar() {
    if (!token) {
        descripcion.textContent = "Revisa tu bandeja de entrada y abre el enlace que te enviamos.";
        mostrar("info", "La cuenta permanecerá inactiva hasta confirmar el correo.");
        mostrarReenvio();
        return;
    }

    try {
        const respuesta = await authApi.verificarCorreo(token);
        const sesion = guardarSesion(respuesta);
        sessionStorage.removeItem("ultimoRegistroSierraDorada");
        descripcion.textContent = "Tu identidad de correo fue confirmada correctamente.";
        mostrar("success", "Cuenta activada. Ingresando a Sierra Dorada…");
        setTimeout(() => {
            window.location.href = sesion.rol === "admin" ? "admin.html" : "productos.html";
        }, 1200);
    } catch (error) {
        descripcion.textContent = "No pudimos confirmar la cuenta con este enlace.";
        mostrar("danger", error.message);
        mostrarReenvio();
    }
}

formReenvio.addEventListener("submit", async event => {
    event.preventDefault();
    const email = emailReenvio.value.trim().toLowerCase();
    if (!emailReenvio.reportValidity()) return;
    const boton = formReenvio.querySelector("button[type='submit']");
    boton.disabled = true;
    try {
        await authApi.reenviarVerificacion(email);
        mostrar("success", "Si la cuenta está pendiente, enviamos un enlace nuevo a ese correo.");
    } catch (error) {
        mostrar("danger", error.message);
    } finally {
        boton.disabled = false;
    }
});

verificar();
