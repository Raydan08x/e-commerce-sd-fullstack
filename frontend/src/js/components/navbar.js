import { cerrarSesion, obtenerSesion } from "../api.js?v=20260811-1";

export class Navbar {
  constructor() {
    this.isInHtmlFolder = window.location.pathname.includes('/html/');
    this.basePath = this.isInHtmlFolder ? '../' : '';
    this.homePath = this.isInHtmlFolder ? '../' : './';
    this.htmlPath = this.isInHtmlFolder ? '' : 'html/';
    this.currentPage = window.location.pathname.split('/').pop() || 'index.html';

    this.sesion = this.obtenerSesion();

    this.links = [
      { name: 'Inicio', href: this.homePath, page: 'index.html' },
      { name: 'Nosotros', href: `${this.htmlPath}nosotros.html`, page: 'nosotros.html' },
      { name: 'Contacto', href: `${this.htmlPath}contacto.html`, page: 'contacto.html' },
      { name: 'Productos', href: `${this.htmlPath}productos.html`, page: 'productos.html' },
    ];

    if (this.sesion !== null && this.sesion.rol === "admin") {
      this.links.push({ name: 'Admin', href: `${this.htmlPath}admin.html`, page: 'admin.html' });
    }
  }

  obtenerSesion() {
    return obtenerSesion();
  }

  obtenerCantidadCarrito() {
    try {
      const carrito = JSON.parse(localStorage.getItem('carritoSierraDorada')) || [];
      return carrito.reduce(
        (total, producto) => total + Number(producto.cantidad || 0),
        0
      );
    } catch (error) {
      return 0;
    }
  }

  actualizarBadgeCarrito(cantidad = this.obtenerCantidadCarrito()) {
    const badge = document.getElementById('navbarCartBadge');
    if (!badge) return;

    const cantidadSegura = Math.max(0, Number(cantidad) || 0);
    badge.textContent = cantidadSegura > 99 ? '99+' : cantidadSegura.toString();
    badge.hidden = cantidadSegura === 0;
    badge.setAttribute(
      'aria-label',
      `${cantidadSegura} ${cantidadSegura === 1 ? 'producto' : 'productos'} en el carrito`
    );

    if (cantidadSegura > 0) {
      badge.classList.remove('cart-badge--updated');
      void badge.offsetWidth;
      badge.classList.add('cart-badge--updated');
    }
  }

  getLinksHTML() {
    return this.links
      .map((link) => {
        const isActive = this.currentPage === link.page ? 'is-active' : '';
        return `<a class="nav-link ${isActive}" href="${link.href}">${link.name}</a>`;
      })
      .join('');
  }

  getBotonSesionHTML() {
    if (this.sesion !== null) {
      return `
        <button class="icon-button icon-button--primary" type="button" id="btnNavbarLogout">
          <i class="bi bi-person"></i> Salir
        </button>
      `;
    }

    return `
      <a class="icon-button icon-button--primary" href="${this.htmlPath}login.html">
        <i class="bi bi-person"></i> Login
      </a>
    `;
  }

  getTemplate() {
    const logoSrc = `${this.basePath}src/assets/icons/isotipo-dorado-y-blanco.png`;
    return `
      <header class="glass-navbar">
        <a href="${this.homePath}" class="brand">
          <img src="${logoSrc}" alt="Logo Sierra Dorada" class="brand__logo" />
          <span class="brand__text">Sierra Dorada</span>
        </a>

        <button class="navbar-toggle" type="button" aria-label="Abrir menu" aria-expanded="false" aria-controls="navbarMenu">
          <i class="bi bi-list"></i>
        </button>

        <div class="navbar-menu" id="navbarMenu">
          <nav class="navbar-links" aria-label="Navegacion principal">
            ${this.getLinksHTML()}
          </nav>

          <div class="navbar-actions">
            <button type="button" id="navbarCartButton" class="icon-button icon-button--outline cart-button">
              <i class="bi bi-cart"></i> Carrito
              <span id="navbarCartBadge" class="cart-badge" aria-live="polite" hidden>0</span>
            </button>

            ${this.getBotonSesionHTML()}
          </div>
        </div>
      </header>
    `;
  }

  render() {
    document.body.insertAdjacentHTML('afterbegin', this.getTemplate());
    this.attachListeners();
    this.actualizarBadgeCarrito();
  }

  attachListeners() {
    const renderedLinks = document.querySelectorAll('.glass-navbar .nav-link');
    const navbar = document.querySelector('.glass-navbar');
    const navbarToggle = document.querySelector('.navbar-toggle');

    renderedLinks.forEach((link) => {
      link.addEventListener('click', () => {
        renderedLinks.forEach((item) => item.classList.remove('is-active'));
        link.classList.add('is-active');
        navbar.classList.remove('is-open');
        navbarToggle.setAttribute('aria-expanded', 'false');
      });
    });

    navbarToggle.addEventListener('click', () => {
      const estaAbierto = navbar.classList.toggle('is-open');
      navbarToggle.setAttribute('aria-expanded', estaAbierto.toString());
    });

    window.addEventListener('sierra-dorada:carrito-actualizado', (event) => {
      this.actualizarBadgeCarrito(event.detail?.cantidadTotal);
    });

    window.addEventListener('storage', (event) => {
      if (event.key === 'carritoSierraDorada') {
        this.actualizarBadgeCarrito();
      }
    });

    const navbarCartButton = document.getElementById('navbarCartButton');
    navbarCartButton?.addEventListener('click', () => {
      window.dispatchEvent(new CustomEvent('sierra-dorada:abrir-carrito'));
    });

    const btnNavbarLogout = document.getElementById("btnNavbarLogout");

    if (btnNavbarLogout !== null) {
      btnNavbarLogout.addEventListener("click", () => {
        cerrarSesion();
        window.location.href = this.isInHtmlFolder ? "login.html" : "html/login.html";
      });
    }
  }
}
