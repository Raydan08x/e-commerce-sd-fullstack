export class ToastManager {
    constructor(containerId = "toastContainer") {
        let container = document.getElementById(containerId);
        if (!container) {
            container = document.createElement("div");
            container.id = containerId;
            document.body.appendChild(container);
        }
        this.container = container;
    }

    /**
     * Muestra un toast.
     * @param {string} message - El mensaje a mostrar.
     * @param {string} type - El tipo de toast ('success', 'error', 'info').
     * @param {number} time - La duración en milisegundos.
     */
    show(message, type = 'info', time = 3000) {
        const iconMap = {
            success: 'bi-check-circle-fill',
            error: 'bi-exclamation-circle-fill',
            info: 'bi-info-circle-fill'
        };
        const iconClass = iconMap[type] || iconMap.info;

        const toast = document.createElement("div");
        toast.className = `sd-toast sd-toast--${type}`;
        toast.dataset.toastKey = `${type}:${message}`;
        toast.innerHTML = `
            <i class="bi ${iconClass} toast__icon"></i>
            <p>${message}</p>
            <button type="button" class="toast__close" aria-label="Cerrar">&times;</button>
        `;

        this.container.querySelectorAll('.sd-toast').forEach((visibleToast) => {
            if (visibleToast.dataset.toastKey === toast.dataset.toastKey) {
                visibleToast.remove();
            }
        });

        let removalTimer;
        let exitTimer;
        let isClosing = false;

        const closeToast = () => {
            if (isClosing || !toast.isConnected) return;
            isClosing = true;
            clearTimeout(exitTimer);
            toast.classList.add('sd-toast--out');

            removalTimer = window.setTimeout(() => toast.remove(), 650);
            toast.addEventListener('animationend', () => {
                clearTimeout(removalTimer);
                toast.remove();
            }, { once: true });
        };

        // Cerrar al hacer clic en la X
        const closeBtn = toast.querySelector('.toast__close');
        if (closeBtn) {
            closeBtn.addEventListener('click', closeToast);
        }

        this.container.appendChild(toast);

        exitTimer = window.setTimeout(closeToast, time);
    }
}
