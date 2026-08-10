document.addEventListener("DOMContentLoaded", async () => {
    if(localStorage.getItem("sd-is-of-age") === "true") return;

    const container = document.getElementById("sd-age-gate-container");
    if (!container) return;

    const response = await fetch("html/validacion.html");
    container.innerHTML = await response.text();

    const ageModalElement = document.getElementById("sdAgeModal");
    if (!ageModalElement) return;

    const ageModal = new bootstrap.Modal(ageModalElement, { backdrop: 'static', keyboard: false });
    ageModal.show();

    

    const yesBtn = document.getElementById("sd-btn-yes");
    const noBtn = document.getElementById("sd-btn-no");

    if (yesBtn) {
        yesBtn.addEventListener("click", () => {
            localStorage.setItem("sd-is-of-age", "true");
            ageModal.hide();
        });
    }

    if (noBtn) {
        noBtn.addEventListener("click", () => {
          
            alert("Lo sentimos, debes ser mayor de 18 años para ingresar.");
            window.location.href = "/";
        });
    }
});