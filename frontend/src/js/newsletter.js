import { formulariosApi } from "./api.js";

const form = document.getElementById("newsletterForm");
const emailInput = document.getElementById("newsletterEmail");
const mensaje = document.getElementById("newsletterMessage");

function mostrar(texto, tipo) {
    mensaje.textContent = texto;
    mensaje.className = `newsletter-message newsletter-message--${tipo}`;
}

form?.addEventListener("submit", async event => {
    event.preventDefault();
    const email = emailInput.value.trim().toLowerCase();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        mostrar("Ingresa un correo electrónico válido.", "error");
        return;
    }
    try {
        await formulariosApi.newsletter(email);
        form.reset();
        mostrar("¡Bienvenido! Tu suscripción quedó guardada.", "success");
    } catch (error) {
        mostrar(error.message, "error");
    }
});

