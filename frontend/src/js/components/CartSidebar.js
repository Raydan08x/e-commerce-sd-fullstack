import {
    obtenerCarrito,
    actualizarCantidad,
    eliminarProducto
} from '../carritoStorage.js?v=20260715-3';

export class CartSidebar {
    constructor() {
        this.isInHtmlFolder = window.location.pathname.includes('/html/');
        this.cartPath = this.isInHtmlFolder ? 'carrito.html' : 'html/carrito.html';
        this.root = null;
        this.lastFocusedElement = null;
    }

    render() {
        if (document.getElementById('cartSidebar')) return;

        document.body.insertAdjacentHTML('beforeend', `
            <div class="cart-sidebar" id="cartSidebar" aria-hidden="true">
                <button class="cart-sidebar__backdrop" type="button" data-cart-close aria-label="Cerrar carrito"></button>

                <aside class="cart-sidebar__panel" role="dialog" aria-modal="true" aria-labelledby="cartSidebarTitle">
                    <header class="cart-sidebar__header">
                        <div>
                            <span class="cart-sidebar__eyebrow">SIERRA DORADA</span>
                            <h2 id="cartSidebarTitle" class="cart-sidebar__title">CARRITO DE COMPRAS</h2>
                        </div>
                        <button class="cart-sidebar__close" type="button" data-cart-close aria-label="Cerrar carrito">
                            <i class="bi bi-x-lg" aria-hidden="true"></i>
                        </button>
                    </header>

                    <div class="cart-sidebar__content" id="cartSidebarContent"></div>

                    <footer class="cart-sidebar__footer" id="cartSidebarFooter"></footer>
                </aside>
            </div>
        `);

        this.root = document.getElementById('cartSidebar');
        this.attachListeners();
        this.update();
    }

    escapeHTML(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#039;');
    }

    resolveImage(image) {
        const source = String(image || '');

        if (!this.isInHtmlFolder && source.startsWith('../src/')) {
            return source.slice(3);
        }

        if (this.isInHtmlFolder && source.startsWith('src/')) {
            return `../${source}`;
        }

        return source;
    }

    formatPrice(value) {
        return `$${Number(value || 0).toLocaleString('es-CO')}`;
    }

    update() {
        if (!this.root) return;

        const carrito = obtenerCarrito();
        const content = this.root.querySelector('#cartSidebarContent');
        const footer = this.root.querySelector('#cartSidebarFooter');
        const cantidadTotal = carrito.reduce(
            (total, producto) => total + Number(producto.cantidad || 0),
            0
        );
        const total = carrito.reduce(
            (subtotal, producto) => subtotal + Number(producto.price || 0) * Number(producto.cantidad || 0),
            0
        );

        if (carrito.length === 0) {
            content.innerHTML = `
                <div class="cart-sidebar__empty">
                    <i class="bi bi-bag cart-sidebar__empty-icon" aria-hidden="true"></i>
                    <h3>Tu carrito está vacío</h3>
                    <p>Explora nuestro catálogo y agrega tu próxima cerveza favorita.</p>
                    <button class="cart-sidebar__continue" type="button" data-cart-close>SEGUIR COMPRANDO</button>
                </div>
            `;
            footer.innerHTML = '';
            return;
        }

        content.innerHTML = `
            <div class="cart-sidebar__summary">
                <span>${cantidadTotal} ${cantidadTotal === 1 ? 'producto' : 'productos'}</span>
                <span>En tu selección</span>
            </div>
            <div class="cart-sidebar__items">
                ${carrito.map((producto) => `
                    <article class="cart-sidebar__item" data-cart-item="${this.escapeHTML(producto.id)}">
                        <img
                            class="cart-sidebar__image"
                            src="${this.escapeHTML(this.resolveImage(producto.image))}"
                            alt="${this.escapeHTML(producto.name)}"
                        >
                        <div class="cart-sidebar__details">
                            <h3>${this.escapeHTML(producto.name)}</h3>
                            <p>${this.formatPrice(producto.price)}</p>
                            <div class="cart-sidebar__controls">
                                <button type="button" data-cart-action="decrease" data-id="${this.escapeHTML(producto.id)}" aria-label="Disminuir cantidad">
                                    <i class="bi bi-dash" aria-hidden="true"></i>
                                </button>
                                <span aria-label="Cantidad">${Number(producto.cantidad || 0)}</span>
                                <button type="button" data-cart-action="increase" data-id="${this.escapeHTML(producto.id)}" aria-label="Aumentar cantidad">
                                    <i class="bi bi-plus" aria-hidden="true"></i>
                                </button>
                                <button class="cart-sidebar__remove" type="button" data-cart-action="remove" data-id="${this.escapeHTML(producto.id)}" aria-label="Eliminar ${this.escapeHTML(producto.name)}">
                                    <i class="bi bi-trash3" aria-hidden="true"></i>
                                </button>
                            </div>
                        </div>
                    </article>
                `).join('')}
            </div>
        `;

        footer.innerHTML = `
            <div class="cart-sidebar__total">
                <span>Total</span>
                <strong>${this.formatPrice(total)}</strong>
            </div>
            <a class="cart-sidebar__checkout" href="${this.cartPath}">
                PROCEDER AL PAGO
                <i class="bi bi-arrow-right" aria-hidden="true"></i>
            </a>
            <button class="cart-sidebar__continue" type="button" data-cart-close>SEGUIR COMPRANDO</button>
        `;
    }

    open() {
        if (!this.root) return;
        this.lastFocusedElement = document.activeElement;
        this.update();
        this.root.classList.add('cart-sidebar--open');
        this.root.setAttribute('aria-hidden', 'false');
        document.body.classList.add('cart-sidebar-open');
        window.requestAnimationFrame(() => this.root.querySelector('.cart-sidebar__close')?.focus());
    }

    close() {
        if (!this.root) return;
        this.root.classList.remove('cart-sidebar--open');
        this.root.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('cart-sidebar-open');
        this.lastFocusedElement?.focus?.();
    }

    handleCartAction(button) {
        const id = button.dataset.id;
        const action = button.dataset.cartAction;
        const producto = obtenerCarrito().find((item) => item.id === id);
        if (!producto) return;

        if (action === 'increase') {
            actualizarCantidad(id, Number(producto.cantidad) + 1);
        }

        if (action === 'decrease') {
            if (Number(producto.cantidad) <= 1) {
                eliminarProducto(id);
            } else {
                actualizarCantidad(id, Number(producto.cantidad) - 1);
            }
        }

        if (action === 'remove') {
            eliminarProducto(id);
        }
    }

    attachListeners() {
        this.root.addEventListener('click', (event) => {
            const closeButton = event.target.closest('[data-cart-close]');
            if (closeButton) {
                this.close();
                return;
            }

            const actionButton = event.target.closest('[data-cart-action]');
            if (actionButton) {
                this.handleCartAction(actionButton);
            }
        });

        window.addEventListener('sierra-dorada:abrir-carrito', () => this.open());
        window.addEventListener('sierra-dorada:carrito-actualizado', () => this.update());
        window.addEventListener('storage', (event) => {
            if (event.key === 'carritoSierraDorada') this.update();
        });
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && this.root.classList.contains('cart-sidebar--open')) {
                this.close();
            }
        });
    }
}
