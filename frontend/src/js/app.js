import { Navbar } from './components/navbar.js?v=20260715-4';
import { Footer } from './components/footer.js?v=20260715-3';
import { Preloader } from './components/Preloader.js?v=20260715-1';
import { ToastManager } from './components/ToastManager.js?v=20260715-3';
import { CartSidebar } from './components/CartSidebar.js?v=20260715-2';

const preloader = new Preloader({ duration: 200 });
preloader.render();

const toastManager = new ToastManager();
window.toastManager = toastManager;

const navbar = new Navbar();
navbar.render();

const cartSidebar = new CartSidebar();
cartSidebar.render();

const footer = new Footer();
footer.render();
