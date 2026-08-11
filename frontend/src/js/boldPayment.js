import { obtenerCarrito, vaciarCarrito } from './carritoStorage.js?v=20260810-1';
import { BOLD_CONFIG } from './config/bold.js?v=20260715-2';
import { api, pagosApi } from './api.js?v=20260810-3';

const BOLD_LIBRARY_URL = 'https://checkout.bold.co/library/boldPaymentButton.js';
const CLAVE_PAGO_PENDIENTE = 'pagoBoldPendienteSierraDorada';
const ESTADOS_FINALES = new Set(['APPROVED', 'REJECTED', 'FAILED', 'VOIDED']);

class BoldPayment {
    constructor(buttonId = 'btnPagarBold') {
        this.button = document.getElementById(buttonId);
        this.originalText = this.button?.textContent.trim() || 'Pagar';
        this.libraryPromise = null;
        this.referenciaRetorno = null;
    }

    init() {
        if (!this.button) return;
        this.updateButtonState();
        this.button.addEventListener('click', () => this.startCheckout());
        window.addEventListener('sierra-dorada:carrito-actualizado', () => this.updateButtonState());
        window.addEventListener('sierra-dorada:reintentar-confirmacion-bold', () => {
            if (this.referenciaRetorno) this.confirmReturn(this.referenciaRetorno, 0);
        });
        window.addEventListener('storage', event => {
            if (event.key === 'carritoSierraDorada') this.updateButtonState();
        });
        this.handleReturn();
    }

    getOrderTotal() {
        const totalPedido = Number(this.button?.dataset.total);
        if (Number.isFinite(totalPedido) && totalPedido > 0) return totalPedido;
        return obtenerCarrito().reduce(
            (total, producto) => total + Number(producto.price || 0) * Number(producto.cantidad || 0), 0);
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

    setLoading(isLoading, text = 'Preparando pago') {
        this.button.disabled = isLoading;
        this.button.classList.toggle('btn-dorado--loading', isLoading);
        this.button.innerHTML = isLoading
            ? `<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> ${text}`
            : this.originalText;
    }

    showMessage(message, type = 'info') {
        if (window.toastManager) window.toastManager.show(message, type, 5000);
        else window.alert(message);
    }

    async loadBoldLibrary() {
        if (window.BoldCheckout) return window.BoldCheckout;
        if (this.libraryPromise) return this.libraryPromise;
        this.libraryPromise = new Promise((resolve, reject) => {
            const existing = document.querySelector(`script[src="${BOLD_LIBRARY_URL}"]`);
            const script = existing || document.createElement('script');
            const loaded = () => window.BoldCheckout
                ? resolve(window.BoldCheckout) : reject(new Error('BoldCheckout no está disponible.'));
            script.addEventListener('load', loaded, { once: true });
            script.addEventListener('error', () => reject(new Error('No fue posible cargar Bold.')), { once: true });
            if (!existing) {
                script.src = BOLD_LIBRARY_URL;
                script.defer = true;
                document.head.appendChild(script);
            } else if (window.BoldCheckout) loaded();
        });
        return this.libraryPromise;
    }

    async requestIntegritySignature(orderId) {
        const pedidoId = Number(this.button.dataset.pedidoId);
        if (!Number.isInteger(pedidoId)) {
            throw new Error('Primero debes crear el pedido y seleccionar el envío.');
        }
        return api('/pagos/bold/firma', {
            method: 'POST', body: JSON.stringify({ orderId, pedidoId })
        });
    }

    async startCheckout() {
        if (this.getOrderTotal() <= 0) {
            this.showMessage('Tu carrito está vacío.', 'error');
            return;
        }
        this.setLoading(true);
        try {
            const orderId = this.createOrderId();
            const [BoldCheckout, signedPayment] = await Promise.all([
                this.loadBoldLibrary(), this.requestIntegritySignature(orderId)
            ]);
            localStorage.setItem(CLAVE_PAGO_PENDIENTE, JSON.stringify({
                orderId, pedidoId: signedPayment.pedidoId, creadoEn: Date.now()
            }));
            new BoldCheckout({
                orderId,
                currency: BOLD_CONFIG.currency,
                amount: String(signedPayment.amount),
                apiKey: BOLD_CONFIG.apiKey,
                integritySignature: signedPayment.integritySignature,
                redirectionUrl: BOLD_CONFIG.redirectionUrl,
                description: `Compra Sierra Dorada - ${obtenerCarrito().length} productos`
            }).open();
        } catch (error) {
            console.error('Error al iniciar Bold Checkout:', error);
            this.showMessage(`No fue posible iniciar el pago con Bold: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
            this.updateButtonState();
        }
    }

    handleReturn() {
        const params = new URLSearchParams(window.location.search);
        const reference = params.get('bold-order-id');
        if (!reference) return;
        this.referenciaRetorno = reference;
        this.confirmReturn(reference, 0);
    }

    async confirmReturn(reference, attempt) {
        this.setLoading(true, 'Confirmando pago');
        try {
            const result = await pagosApi.confirmarBold(reference);
            if (!ESTADOS_FINALES.has(result.estadoPago) && attempt < 5) {
                await new Promise(resolve => window.setTimeout(resolve, 2000));
                return this.confirmReturn(reference, attempt + 1);
            }
            window.dispatchEvent(new CustomEvent('sierra-dorada:resultado-pago', { detail: result }));
            if (result.confirmado) {
                vaciarCarrito();
                localStorage.removeItem(CLAVE_PAGO_PENDIENTE);
            } else if (ESTADOS_FINALES.has(result.estadoPago)) {
                localStorage.removeItem(CLAVE_PAGO_PENDIENTE);
            }
            if (ESTADOS_FINALES.has(result.estadoPago)) {
                const cleanUrl = new URL(window.location.href);
                cleanUrl.searchParams.delete('bold-order-id');
                cleanUrl.searchParams.delete('bold-tx-status');
                window.history.replaceState({}, '', cleanUrl);
            }
        } catch (error) {
            this.showMessage(`No fue posible confirmar el pago: ${error.message}`, 'error');
        } finally {
            this.setLoading(false);
            this.updateButtonState();
        }
    }
}

new BoldPayment().init();
