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

## Verificación

```powershell
cd backend
.\gradlew.bat test --no-daemon
```

Consulte `database/README.md` para el modelo ER y el orden de migraciones.

El despliegue de AWS se ejecuta manualmente desde GitHub Actions. Configure primero
los secretos descritos en `backend/README.md`; después puede habilitar despliegues
automáticos sobre `main` si el entorno ya está preparado.
