// Componente para gestionar el modal de galería de imágenes de un producto.
// NOTA: Las imágenes cargadas desde PC se guardan como base64 en localStorage.
// Por el límite de localStorage (~5-10 MB), en el futuro se debe implementar
// un backend con base de datos o servicio de almacenamiento de archivos.

export class ImageModalManager {
    constructor({ getProductos, productos, guardarProductos, filtrarYMostrarProductos, toastManager }) {
        this.getProductos = getProductos || (() => productos || []);
        this.guardarProductos = guardarProductos;
        this.filtrarYMostrarProductos = filtrarYMostrarProductos;
        this.toastManager = toastManager || window.toastManager;

        this.IMAGEN_PLACEHOLDER = "https://placehold.co/600x400/222223/B3A269?text=Imagen+Pendiente";

        this.modalEl = document.getElementById('modalCambiarImagen');
        this.modal = this.modalEl && typeof bootstrap !== 'undefined'
            ? new bootstrap.Modal(this.modalEl)
            : null;

        this.nombreProductoModal = document.getElementById('nombreProductoModal');
        this.galeriaImagenesModal = document.getElementById('galeriaImagenesModal');
        this.inputUrlImagenModal = document.getElementById('inputUrlImagenModal');
        this.inputFileImagenModal = document.getElementById('inputFileImagenModal');
        this.idProductoModal = document.getElementById('idProductoModal');
        this.btnGuardarImagenModal = document.getElementById('btnGuardarImagenModal');
        this.btnQuitarImagenModal = document.getElementById('btnQuitarImagenModal');
        this.btnAgregarImagenModal = document.getElementById('btnAgregarImagenModal');
        this.mensajeModal = document.getElementById('mensajeModal');
        this.mensajeGaleriaVacia = document.getElementById('mensajeGaleriaVacia');

        this.imagenesModal = [];
        this.archivoModalBase64 = null;

        this.bindEvents();
    }

    // --- HELPERS DE IMÁGENES ---

    normalizarImagenes(producto) {
        if (!producto) return [];

        if (Array.isArray(producto.images) && producto.images.length > 0) {
            const tienePrincipalExplicita = producto.images.some(img => img && img.isMain === true);
            return producto.images.map((img, i) => ({
                url: typeof img === 'string' ? img : (img.url || ''),
                fit: img.fit || producto.imageFit || 'cover',
                isMain: tienePrincipalExplicita ? (img.isMain === true) : (i === 0)
            }));
        }

        const url = producto.image || this.IMAGEN_PLACEHOLDER;
        return [{
            url: url,
            fit: producto.imageFit || 'cover',
            isMain: true
        }];
    }

    guardarImagenes(producto, imagenes) {
        let lista = (imagenes || []).filter(img => img && img.url);
        if (lista.length === 0) {
            lista = [{ url: this.IMAGEN_PLACEHOLDER, fit: 'cover', isMain: true }];
        }

        const idxPrincipal = lista.findIndex(img => img.isMain);
        if (idxPrincipal === -1) {
            lista[0].isMain = true;
        } else {
            lista.forEach((img, i) => { img.isMain = (i === idxPrincipal); });
        }

        producto.images = lista;
        producto.image = lista.find(img => img.isMain).url;
        producto.imageFit = lista.find(img => img.isMain).fit;
        return producto;
    }

    comprimirImagen(base64, maxAncho = 800, maxAlto = 800, calidad = 0.8) {
        return new Promise((resolve, reject) => {
            const img = new Image();
            img.onload = () => {
                let ancho = img.width;
                let alto = img.height;

                if (ancho > maxAncho || alto > maxAlto) {
                    const ratio = Math.min(maxAncho / ancho, maxAlto / alto);
                    ancho = Math.round(ancho * ratio);
                    alto = Math.round(alto * ratio);
                }

                const canvas = document.createElement('canvas');
                canvas.width = ancho;
                canvas.height = alto;
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, ancho, alto);

                resolve(canvas.toDataURL('image/jpeg', calidad));
            };
            img.onerror = reject;
            img.src = base64;
        });
    }

    // --- RENDERIZADO ---

    renderizarGaleria() {
        if (!this.galeriaImagenesModal) return;

        this.galeriaImagenesModal.innerHTML = '';

        const imagenesValidas = this.imagenesModal.filter(img => img && img.url);

        if (this.mensajeGaleriaVacia) {
            this.mensajeGaleriaVacia.style.display = imagenesValidas.length === 0 ? 'block' : 'none';
        }

        imagenesValidas.forEach((img, index) => {
            const esPrincipal = img.isMain === true;
            const item = document.createElement('div');
            item.className = `galeria-item ${esPrincipal ? 'galeria-item--principal' : ''}`;
            item.innerHTML = `
                ${esPrincipal ? '<span class="galeria-item__badge">PRINCIPAL</span>' : ''}
                <img src="${img.url}" alt="Imagen ${index + 1}" style="object-fit: ${img.fit || 'cover'};">
                <div class="galeria-item__actions">
                    <button type="button" class="btn btn-sm btn-outline-dorado" onclick="window.imageModalManager.establecerPrincipal(${index})" title="Establecer como principal y guardar" ${esPrincipal ? 'disabled' : ''}>
                        <i class="bi bi-star${esPrincipal ? '-fill' : ''}"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="window.imageModalManager.eliminarImagen(${index})" title="Eliminar imagen">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            `;
            this.galeriaImagenesModal.appendChild(item);
        });
    }

    // --- EVENTOS ---

    bindEvents() {
        if (this.btnGuardarImagenModal) {
            this.btnGuardarImagenModal.addEventListener('click', () => this.guardar());
        }
        if (this.btnQuitarImagenModal) {
            this.btnQuitarImagenModal.addEventListener('click', () => this.quitarTodas());
        }
        if (this.btnAgregarImagenModal) {
            this.btnAgregarImagenModal.addEventListener('click', () => this.agregarImagen());
        }
        if (this.inputFileImagenModal) {
            this.inputFileImagenModal.addEventListener('change', (e) => this.codificarImagen(e));
        }
    }

    // --- ACCIONES PÚBLICAS ---

    abrir(idProducto) {
        const productos = this.getProductos();
        const producto = productos.find(p => p.id === idProducto);
        if (!producto) return;

        this.imagenesModal = this.normalizarImagenes(producto);
        this.archivoModalBase64 = null;

        if (this.nombreProductoModal) this.nombreProductoModal.textContent = producto.name;
        if (this.idProductoModal) this.idProductoModal.value = producto.id;
        if (this.inputUrlImagenModal) this.inputUrlImagenModal.value = '';
        if (this.inputFileImagenModal) this.inputFileImagenModal.value = '';

        const selectFit = document.getElementById('selectImageFitModal');
        if (selectFit) selectFit.value = 'cover';

        const selectLocal = document.getElementById('selectImagenLocalModal');
        if (selectLocal) selectLocal.value = '';

        if (this.mensajeModal) this.mensajeModal.textContent = '';

        this.renderizarGaleria();

        if (this.modal) this.modal.show();
    }

    agregarImagen(urlForzada = null) {
        const selectFit = document.getElementById('selectImageFitModal');
        const fit = selectFit ? selectFit.value : 'cover';

        let nuevaUrl = urlForzada || '';
        if (!nuevaUrl && this.archivoModalBase64) {
            nuevaUrl = this.archivoModalBase64;
        }
        if (!nuevaUrl && this.inputUrlImagenModal) {
            nuevaUrl = this.inputUrlImagenModal.value.trim();
        }

        if (!nuevaUrl) {
            const selectLocal = document.getElementById('selectImagenLocalModal');
            if (selectLocal) nuevaUrl = selectLocal.value;
        }

        if (!nuevaUrl) {
            if (this.toastManager) {
                this.toastManager.show('Por favor, ingresa una URL o selecciona una imagen.', 'error');
            }
            return false;
        }

        const urlPrincipalActual = this.imagenesModal.length > 0 ? this.imagenesModal[0].url : '';
        const soloPlaceholder = this.imagenesModal.length === 1 &&
            (!urlPrincipalActual || urlPrincipalActual.includes('placehold.co') || urlPrincipalActual.includes('Imagen+Pendiente'));
        if (soloPlaceholder) {
            this.imagenesModal = [];
        }

        const esPrimera = this.imagenesModal.length === 0;
        this.imagenesModal.push({
            url: nuevaUrl,
            fit: fit,
            isMain: esPrimera
        });

        if (this.inputUrlImagenModal) this.inputUrlImagenModal.value = '';
        const selectLocal = document.getElementById('selectImagenLocalModal');
        if (selectLocal) selectLocal.value = '';
        this.archivoModalBase64 = null;

        this.renderizarGaleria();

        if (this.toastManager) {
            this.toastManager.show('Imagen agregada a la galería.', 'success');
        }
        return true;
    }

    eliminarImagen(index) {
        if (index < 0 || index >= this.imagenesModal.length) return;

        const eraPrincipal = this.imagenesModal[index].isMain;
        this.imagenesModal.splice(index, 1);

        if (eraPrincipal && this.imagenesModal.length > 0) {
            this.imagenesModal[0].isMain = true;
        }

        this.renderizarGaleria();
    }

    establecerPrincipal(index) {
        if (index < 0 || index >= this.imagenesModal.length) return;

        this.imagenesModal.forEach((img, i) => {
            img.isMain = (i === index);
        });

        const productos = this.getProductos();
        const idProducto = this.idProductoModal ? this.idProductoModal.value : '';
        const indice = productos.findIndex(p => p.id === idProducto);
        if (indice !== -1) {
            this.guardarImagenes(productos[indice], this.imagenesModal);
            const guardado = this.guardarProductos();
            if (!guardado) return;
            this.filtrarYMostrarProductos();
        }

        if (this.modal) this.modal.hide();

        if (this.toastManager) {
            this.toastManager.show('Imagen principal actualizada correctamente.', 'success');
        }
    }

    guardar() {
        const productos = this.getProductos();
        const idProducto = this.idProductoModal ? this.idProductoModal.value : '';
        if (!idProducto) {
            if (this.toastManager) this.toastManager.show('Error: no se identificó el producto.', 'error');
            return;
        }

        const indice = productos.findIndex(p => p.id === idProducto);
        if (indice === -1) {
            if (this.toastManager) this.toastManager.show('Error: producto no encontrado.', 'error');
            return;
        }

        const tienePendiente = this.inputUrlImagenModal && this.inputUrlImagenModal.value.trim();
        if (tienePendiente) {
            const agregado = this.agregarImagen();
            if (!agregado) return;
        }

        this.guardarImagenes(productos[indice], this.imagenesModal);
        const guardado = this.guardarProductos();
        if (!guardado) return;
        this.filtrarYMostrarProductos();

        if (this.modal) {
            this.modal.hide();
        } else {
            const modalEl = document.getElementById('modalCambiarImagen');
            if (modalEl && typeof bootstrap !== 'undefined') {
                const instancia = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                instancia.hide();
            } else if (modalEl) {
                modalEl.classList.remove('show');
                modalEl.style.display = 'none';
                document.body.classList.remove('modal-open');
                const backdrop = document.querySelector('.modal-backdrop');
                if (backdrop) backdrop.remove();
            }
        }

        if (this.toastManager) {
            this.toastManager.show('Galería de imágenes actualizada correctamente.', 'success');
        }
    }

    async quitarTodas() {
        const productos = this.getProductos();
        const idProducto = this.idProductoModal ? this.idProductoModal.value : '';
        const indice = productos.findIndex(p => p.id === idProducto);
        if (indice === -1) return;

        if (typeof window.confirmacionModal !== 'undefined' && window.confirmacionModal) {
            const confirmado = await window.confirmacionModal.mostrar(
                'Restablecer imágenes',
                '¿Seguro que deseas dejar solo la imagen placeholder? Se eliminarán todas las imágenes reales del producto.'
            );
            if (!confirmado) return;
        }

        this.guardarImagenes(productos[indice], [{
            url: this.IMAGEN_PLACEHOLDER,
            fit: 'cover',
            isMain: true
        }]);
        const guardado = this.guardarProductos();
        if (!guardado) return;
        this.filtrarYMostrarProductos();
        if (this.modal) this.modal.hide();

        if (this.toastManager) {
            this.toastManager.show('Imágenes restablecidas correctamente.', 'success');
        }
    }

    async codificarImagen(evento) {
        const archivos = evento.target.files;
        if (!archivos || archivos.length === 0) {
            this.archivoModalBase64 = null;
            return;
        }

        const archivosArray = Array.from(archivos);
        const archivosGrandes = archivosArray.filter(archivo => archivo.size > 20 * 1024 * 1024);
        if (archivosGrandes.length > 0) {
            if (this.toastManager) {
                this.toastManager.show('Una o más imágenes superan el tamaño máximo de 20MB.', 'error');
            }
            if (this.inputFileImagenModal) this.inputFileImagenModal.value = '';
            return;
        }

        for (const archivo of archivosArray) {
            try {
                const base64Original = await new Promise((resolve, reject) => {
                    const reader = new FileReader();
                    reader.onloadend = () => resolve(reader.result);
                    reader.onerror = reject;
                    reader.readAsDataURL(archivo);
                });
                const base64Comprimida = await this.comprimirImagen(base64Original);
                this.agregarImagen(base64Comprimida);
            } catch (error) {
                console.error('Error al comprimir imagen:', error);
                if (this.toastManager) this.toastManager.show('Error al procesar la imagen.', 'error');
            }
            if (this.mensajeModal) this.mensajeModal.textContent = '';
        }

        if (this.inputFileImagenModal) this.inputFileImagenModal.value = '';
    }
}
