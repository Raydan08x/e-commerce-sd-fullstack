# E-commerce Sierra Dorada — Full Stack

Monorepo definitivo del e-commerce Sierra Dorada. El frontend, backend y modelo
relacional se versionan juntos para mantener sincronizados los contratos de la API.

## Estructura

```text
e-commerce-sd-fullstack/
├── frontend/              Sitio web HTML, CSS y JavaScript
├── backend/               API Java 21, Spring Boot, JWT y JPA
├── database/              Modelo ER y migraciones Flyway para MySQL 8/RDS
└── .github/workflows/     Despliegue del backend en AWS ECS
```

MySQL o Amazon RDS es la única fuente de verdad. El navegador conserva solamente
el JWT de sesión y el carrito temporal; usuarios, productos, formularios, pedidos,
pagos y envíos se almacenan mediante el backend.

## Ejecución local

1. Configure las variables indicadas en `backend/README.md`.
2. Inicie el backend:

```powershell
cd backend
.\gradlew.bat bootRun
```

3. Sirva `frontend/` con un servidor HTTP estático.
4. Para otra URL de API, defina `window.SIERRA_DORADA_API_URL` antes de cargar los módulos.

Frontend de producción: `https://shop.sierradorada.co`.

Swagger administrativo: `https://shop.sierradorada.co/swagger-ui.html` (requiere
las credenciales de una cuenta activa y verificada con rol `ADMIN`).

## Raspberry Pi

El despliegue definitivo corre con Docker Compose en
`/home/sdpi/e-commerce-sd-fullstack`. La configuración está en
`deploy/raspberry/compose.yaml` e incluye frontend Nginx, backend Spring Boot y
un MySQL local de contingencia. El backend de producción conserva la conexión
MySQL/AWS indicada por `DB_URL`; el MySQL local no publica puertos al host.

El workflow `Deploy to Raspberry Pi` se ejecuta en un runner autoalojado con la
etiqueta `raspberry-sd`, genera `.env.runtime` desde GitHub Secrets y nunca
versiona contraseñas.

## Verificación

```powershell
cd backend
.\gradlew.bat test --no-daemon
```

Consulte `database/README.md` para el modelo ER y el orden de migraciones.

El despliegue de AWS se ejecuta manualmente desde GitHub Actions. Configure primero
los secretos descritos en `backend/README.md`; después puede habilitar despliegues
automáticos sobre `main` si el entorno ya está preparado.
