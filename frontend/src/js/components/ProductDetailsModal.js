// Componente para mostrar el modal premium de detalles de un producto.

export class ProductDetailsModal {
    constructor() {
        this.IMAGEN_PLACEHOLDER = "https://placehold.co/600x400/222223/B3A269?text=Imagen+Pendiente";
    }

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

    obtenerImagenes(producto) {
        return this.normalizarImagenes(producto);
    }

    abrir(producto) {
        if (!producto) return;

        // Eliminar modal anterior si existe
        const modalExistente = document.getElementById('modalDetallesProducto');
        if (modalExistente) {
            modalExistente.remove();
        }

        const precioFormateado = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(producto.price);
        const chars = producto.characteristics || {};
        const imagenesModal = this.obtenerImagenes(producto);
        const modalCarouselId = `modal-carousel-${producto.id}`;

        let modalImagenHtml = '';
        if (imagenesModal.length > 1) {
            const indicadores = imagenesModal.map((img, i) => `
                <button type="button" data-bs-target="#${modalCarouselId}" data-bs-slide-to="${i}" ${i === 0 ? 'class="active" aria-current="true"' : ''} aria-label="Slide ${i + 1}"></button>
            `).join('');
            const items = imagenesModal.map((img, i) => `
                <div class="carousel-item ${i === 0 ? 'active' : ''}">
                    <img src="${img.url}" alt="${producto.name} - ${i + 1}" class="img-fluid rounded producto-modal__imagen ${img.fit === 'contain' ? 'producto-modal__imagen--contain' : ''}">
                </div>
            `).join('');
            modalImagenHtml = `
                <div id="${modalCarouselId}" class="carousel slide mb-3 producto-modal__carousel" data-bs-ride="carousel">
                    <div class="carousel-indicators">${indicadores}</div>
                    <div class="carousel-inner">${items}</div>
                    <button class="carousel-control-prev" type="button" data-bs-target="#${modalCarouselId}" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Anterior</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#${modalCarouselId}" data-bs-slide="next">
                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Siguiente</span>
                    </button>
                </div>
            `;
        } else {
            const img = imagenesModal[0] || { url: this.IMAGEN_PLACEHOLDER, fit: 'cover' };
            modalImagenHtml = `
                <img src="${img.url}" alt="${producto.name}" class="img-fluid rounded mb-3 producto-modal__imagen ${img.fit === 'contain' ? 'producto-modal__imagen--contain' : ''}">
            `;
        }

        const modalHtml = `
            <div class="modal fade" id="modalDetallesProducto" tabindex="-1" aria-labelledby="modalDetallesProductoLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content bg-dark text-light border-secondary">
                        <div class="modal-header border-secondary">
                            <h5 class="modal-title producto-modal__titulo" id="modalDetallesProductoLabel">
                                ${producto.name} - Detalle Premium
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body p-4">
                            <div class="row">
                                <div class="col-md-5 mb-3 text-center">
                                    ${modalImagenHtml}
                                    ${producto.colorHex ? `
                                        <div class="d-flex align-items-center justify-content-center gap-2 mb-3">
                                            <div class="producto-modal__srm" style="background-color: ${producto.colorHex};"></div>
                                            <span>SRM: <strong>${producto.colorName || 'N/A'}</strong></span>
                                        </div>
                                    ` : ''}
                                    <div class="p-3 rounded producto-modal__especificaciones">
                                        <h6 class="producto-modal__subtitulo">Especificaciones</h6>
                                        <p class="mb-1 producto-modal__texto"><strong>Precio:</strong> ${precioFormateado}</p>
                                        ${producto.abv ? `<p class="mb-1 producto-modal__texto"><strong>ABV:</strong> ${producto.abv}</p>` : ''}
                                        ${producto.ibu ? `<p class="mb-1 producto-modal__texto"><strong>IBU:</strong> ${producto.ibu}</p>` : ''}
                                        ${producto.temperature ? `<p class="mb-0 producto-modal__texto"><strong>Temperatura:</strong> ${producto.temperature}</p>` : ''}
                                    </div>
                                </div>
                                <div class="col-md-7">
                                    ${producto.inspiration ? `
                                        <h5 class="producto-modal__subtitulo" style="font-style: italic;">"${producto.inspiration}"</h5>
                                    ` : ''}
                                    ${producto.legend ? `
                                        <p class="text-white-50 producto-modal__leyenda">
                                            ${producto.legend}
                                        </p>
                                    ` : ''}
                                    
                                    <h6 class="mt-4 producto-modal__subtitulo">Descripción Completa</h6>
                                    <p class="producto-modal__texto" style="text-align: justify;">${producto.fullDescription || producto.description}</p>
                                    
                                    ${producto.process ? `
                                        <h6 class="mt-3 producto-modal__subtitulo">${producto.id.startsWith('M') ? 'Detalles de Confección' : 'Proceso de Elaboración'}</h6>
                                        <p class="producto-modal__texto" style="text-align: justify;">${producto.process}</p>
                                    ` : ''}
                                    
                                    <h6 class="mt-4 producto-modal__subtitulo">${producto.id.startsWith('M') ? 'Detalles del Artículo' : 'Perfil Sensorial (Características)'}</h6>
                                    <div class="row g-2 producto-modal__caracteristicas">
                                        ${chars.color ? `<div class="col-6"><strong>Color:</strong> ${chars.color}</div>` : ''}
                                        ${!producto.id.startsWith('M') && chars.aroma ? `<div class="col-6"><strong>Aroma:</strong> ${chars.aroma}</div>` : ''}
                                        ${!producto.id.startsWith('M') && chars.sabor ? `<div class="col-6"><strong>Sabor:</strong> ${chars.sabor}</div>` : ''}
                                        ${chars.maridaje ? `<div class="col-6"><strong>Sugerencia:</strong> ${chars.maridaje}</div>` : ''}
                                    </div>
                                    
                                    ${producto.maridaje && producto.maridaje.length > 0 ? `
                                        <h6 class="mt-4 producto-modal__subtitulo">${producto.id.startsWith('M') ? 'Uso Recomendado' : 'Maridaje Sugerido'}</h6>
                                        <div class="d-flex flex-wrap gap-2">
                                            ${producto.maridaje.map(item => `
                                                <span class="badge bg-secondary p-2 producto-modal__badge">
                                                    ${item.emoji} ${item.name}
                                                </span>
                                            `).join('')}
                                        </div>
                                    ` : ''}
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer border-secondary">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cerrar</button>
                            <button type="button" class="btn btn-dorado"><i class="bi bi-cart-plus"></i> Agregar al Carrito</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        const modalElement = document.getElementById('modalDetallesProducto');
        const bModal = new bootstrap.Modal(modalElement);
        bModal.show();
    }
}
