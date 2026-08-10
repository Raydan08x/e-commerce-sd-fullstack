// Componente para mostrar un modal de confirmación genérico usando <dialog>.

export class ConfirmModal {
    constructor() {
        this.modal = document.getElementById('confirmacionModal');
        this.titulo = document.getElementById('confirmacionModalTitulo');
        this.mensaje = document.getElementById('confirmacionModalMensaje');
        this.btnAceptar = document.getElementById('btnAceptarConfirmacion');
        this.btnCancelar = document.getElementById('btnCancelarConfirmacion');
    }

    mostrar(titulo, mensaje) {
        if (!this.modal) return Promise.resolve(false);

        if (this.titulo) this.titulo.textContent = titulo;
        if (this.mensaje) this.mensaje.textContent = mensaje;
        this.modal.showModal();

        return new Promise(resolve => {
            this.btnAceptar.onclick = () => {
                this.modal.close();
                resolve(true);
            };
            this.btnCancelar.onclick = () => {
                this.modal.close();
                resolve(false);
            };
        });
    }
}
