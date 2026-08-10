export class Preloader {
  constructor({ duration = 200 } = {}) {
    this.duration = duration;
    this.element = null;
    this.timer = null;
    this.removeTimer = null;
    this.logoUrl = new URL('../../assets/icons/isotipo-dorado-y-blanco.png', import.meta.url).href;
  }

  getTemplate() {
    return `
      <div class="sd-preloader" id="sdPreloader" role="status" aria-live="polite" aria-label="Cargando Sierra Dorada">
        <div class="sd-preloader__content">
          <div class="sd-preloader__spinner" aria-hidden="true"></div>
          <img
            class="sd-preloader__logo"
            src="${this.logoUrl}"
            alt=""
            width="200"
            height="200"
          >
          <span class="visually-hidden">Cargando...</span>
        </div>
      </div>
    `;
  }

  render() {
    if (document.getElementById('sdPreloader')) return;

    document.documentElement.classList.add('sd-preloader-active');
    document.body.insertAdjacentHTML('afterbegin', this.getTemplate());
    this.element = document.getElementById('sdPreloader');

    this.timer = window.setTimeout(() => this.hide(), this.duration);
  }

  hide() {
    if (!this.element) return;

    window.clearTimeout(this.timer);
    this.element.classList.add('sd-preloader--hidden');
    document.documentElement.classList.remove('sd-preloader-active');

    const remove = () => {
      window.clearTimeout(this.removeTimer);
      this.element?.remove();
      this.element = null;
    };

    this.element.addEventListener('transitionend', remove, { once: true });
    this.removeTimer = window.setTimeout(remove, 50);
  }
}
