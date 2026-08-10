import { authApi, guardarSesion } from "./api.js";

const form = document.getElementById("formLogin");
const usuarioInput = document.getElementById("usuario");
const passwordInput = document.getElementById("password");
const mensaje = document.getElementById("mensajeLogin");
const togglePassword = document.getElementById("togglePassword");

function mostrarMensaje(tipo, texto) {
    mensaje.className = `alert alert-${tipo} mt-3`;
    mensaje.textContent = texto;
}

const emailRegistro = sessionStorage.getItem("ultimoRegistroSierraDorada");
if (emailRegistro) {
    usuarioInput.value = emailRegistro;
    sessionStorage.removeItem("ultimoRegistroSierraDorada");
    mostrarMensaje("success", "Cuenta creada correctamente. Inicia sesión.");
}

form.addEventListener("submit", async event => {
    event.preventDefault();
    const usuario = usuarioInput.value.trim();
    const password = passwordInput.value;
    if (!usuario || !password) {
        mostrarMensaje("danger", "Debes escribir el correo y la contraseña.");
        return;
    }

    const boton = form.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
        const respuesta = await authApi.login({ usuario, password });
        const sesion = guardarSesion(respuesta);
        const regreso = sessionStorage.getItem("volverDespuesLogin");
        sessionStorage.removeItem("volverDespuesLogin");
        window.location.href = sesion.rol === "admin"
            ? "admin.html"
            : (regreso || "productos.html");
    } catch (error) {
        mostrarMensaje("danger", error.message);
    } finally {
        boton.disabled = false;
    }
});

togglePassword.addEventListener("click", () => {
    const icono = togglePassword.querySelector("i");
    const oculto = passwordInput.type === "password";
    passwordInput.type = oculto ? "text" : "password";
    icono.classList.toggle("bi-eye", !oculto);
    icono.classList.toggle("bi-eye-slash", oculto);
});
