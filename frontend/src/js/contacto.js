import { formulariosApi } from "./api.js";

const formulario = document.getElementById("form-contacto");
const telefono = document.getElementById("telefono");
telefono?.addEventListener("input", () => {
    telefono.value = telefono.value.replace(/\D/g, "").slice(0, 15);
});

formulario?.addEventListener("submit", async event => {
    event.preventDefault();
    const datos = {
        nombre: document.getElementById("nombre").value.trim(),
        telefono: telefono.value.trim(),
        email: document.getElementById("email").value.trim().toLowerCase(),
        mensaje: document.getElementById("mensaje").value.trim()
    };
    const boton = formulario.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
        await formulariosApi.contacto(datos);
        const texto = `Hola, quiero contactarme con Sierra Dorada.
Nombre: ${datos.nombre}
Teléfono: ${datos.telefono}
Correo: ${datos.email}${datos.mensaje ? `\nMensaje: ${datos.mensaje}` : ""}`;
        formulario.reset();
        alert("Tu mensaje quedó registrado. También puedes continuar por WhatsApp.");
        window.open(`https://wa.me/573138718154?text=${encodeURIComponent(texto)}`, "_blank", "noopener");
    } catch (error) {
        alert(error.message);
    } finally {
        boton.disabled = false;
    }
});

