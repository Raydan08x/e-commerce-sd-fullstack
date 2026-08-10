import { obtenerCarrito } from './carritoStorage.js?v=20260715-3';
import { BOLD_CONFIG } from './config/bold.js?v=20260715-2';
import { api } from './api.js';

const BOLD_LIBRARY_URL = 'https://checkout.bold.co/library/boldPaymentButton.js';

class BoldPayment {
    constructor(buttonId = 'btnPagarBold') {
        this.button = document.getElementById(buttonId);
        this.originalText = this.button?.textContent.trim() || 'Pagar';
        this.libraryPromise = null;
    }

    init() {
        if (!this.button) return;

        this.updateButtonState();
        this.button.addEventListener('click', () => this.startCheckout());
        window.addEventListener('sierra-dorada:carrito-actualizado', () => this.updateButtonState());
        window.addEventListener('storage', (event) => {
            if (event.key === 'carritoSierraDorada') this.updateButtonState();
        });
    }

    getOrderTotal() {
        const totalPedido = Number(this.button?.dataset.total);
        if (Number.isFinite(totalPedido) && totalPedido > 0) return totalPedido;
        const carrito = obtenerCarrito();
        if (carrito.length === 0) return 0;

        const subtotal = carrito.reduce(
            (total, producto) => total + Number(producto.price || 0) * Number(producto.cantidad || 0),
            0
        );

        return subtotal;
    }

    createOrderId() {
        const randomPart = window.crypto.randomUUID?.().replaceAll('-', '').slice(0, 10)
            || Math.random().toString(36).slice(2, 12);
        return `SD-${Date.now()}-${randomPart}`;
    }

    updateButtonState() {
        const hasProducts = this.getOrderTotal() > 0;
        this.button.disabled = !hasProducts;
        this.button.setAttribute('aria-disabled', (!hasProducts).toString());
        this.button.title = hasProducts ? 'Pagar de forma segura con Bold' : 'Tu carrito está vacío';
    }

    setLoading(isLoading) {
        this.button.disabled = isLoading;
        this.button.classList.toggle('btn-dorado--loading', isLoading);
        this.button.innerHTML = isLoading
            ? '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Preparando pago'
            : this.originalText;
    }

    showMessage(message, type = 'info') {
        if (window.toastManager) {
            window.toastManager.show(message, type, 4000);
            return;
        }
        window.alert(message);
    }

    async loadBoldLibrary() {
        if (window.BoldCheckout) return window.BoldCheckout;
        if (this.libraryPromise) return this.libraryPromise;

        this.libraryPromise = new Promise((resolve, reject) => {
            const existingScript = document.querySelector(`script[src="${BOLD_LIBRARY_URL}"]`);
            const script = existingScript || document.createElement('script');

            const handleLoad = () => {
                if (window.BoldCheckout) {
                    resolve(window.BoldCheckout);
                } else {
                    reject(new Error('BoldCheckout no está disponible.'));
                }
            };

            script.addEventListener('load', handleLoad, { once: true });
            script.addEventListener('error', () => reject(new Error('No fue posible cargar Bold.')), { once: true });

            if (!existingScript) {
                script.src = BOLD_LIBRARY_URL;
                script.defer = true;
                document.head.appendChild(script);
            } else if (window.BoldCheckout) {
                handleLoad();
            }
        });

        return this.libraryPromise;
    }

    async requestIntegritySignature(orderId) {
        const pedidoId = Number(this.button.dataset.pedidoId);
        if (!Number.isInteger(pedidoId)) {
            throw new Error('Primero debes crear el pedido y seleccionar el envío.');
        }
        return api('/pagos/bold/firma', {
            method: 'POST',
            body: JSON.stringify({ orderId, pedidoId })
        });
    }

    async startCheckout() {
        const amount = this.getOrderTotal();
        if (amount <= 0) {
            this.showMessage('Tu carrito está vacío.', 'error');
            return;
        }

        this.setLoading(true);

        try {
            const orderId = this.createOrderId();
            const [BoldCheckout, signedPayment] = await Promise.all([
                this.loadBoldLibrary(),
                this.requestIntegritySignature(orderId)
            ]);

            const checkout = new BoldCheckout({
                orderId,
                currency: BOLD_CONFIG.currency,
                amount: String(signedPayment.amount),
                apiKey: BOLD_CONFIG.apiKey,
                integritySignature: signedPayment.integritySignature,
                redirectionUrl: BOLD_CONFIG.redirectionUrl,
                description: `Compra Sierra Dorada - ${obtenerCarrito().length} productos`
            });

            checkout.open();
        } catch (error) {
            console.error('Error al iniciar Bold Checkout:', error);
            this.showMessage(`No fue posible iniciar el pago con Bold: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
            this.updateButtonState();
        }
    }
}

const boldPayment = new BoldPayment();
boldPayment.init();
