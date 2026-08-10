# Sierra Dorada — frontend definitivo

Frontend estático conectado al backend Spring Boot.

## Configuración

La API se toma de `window.SIERRA_DORADA_API_URL`; si no se define, usa `http://localhost:8080/api`.

En producción defina la URL antes de cargar los módulos, por ejemplo:

```html
<script>window.SIERRA_DORADA_API_URL = "https://api.ejemplo.com/api";</script>
```

## Persistencia

- MySQL/backend: usuarios, catálogo, contacto, newsletter, pedidos, pagos y envíos.
- Navegador: JWT de sesión y carrito temporal.
- No existen catálogos ni usuarios de prueba en JSON/localStorage.

El checkout consulta Mi Paquete a través del backend, crea el pedido con precios recalculados y solicita al backend la firma de integridad de Bold.

