# Sierra Dorada — backend definitivo

API REST del e-commerce Sierra Dorada. Java 21, Spring Boot 4.1, Spring Security/JWT, JPA, Flyway y MySQL 8 (local o Amazon RDS).

## Fuente de verdad

MySQL es la única fuente de verdad. El frontend no almacena usuarios, catálogo, formularios, pedidos, pagos ni envíos.

El modelo está en:

- `../database/modelo-er-definitivo.dbml`: modelo editable para dbdiagram.io.
- `../database/modelo-er-definitivo.mmd`: modelo Mermaid.
- `../database/db/migration`: migraciones Flyway que crean/evolucionan el esquema y cargan el catálogo inicial mediante SQL.
- `../database/tablas ER ecommerce sierra dorada.png`: diagrama histórico anterior, conservado como referencia.

Flyway se ejecuta al iniciar la aplicación. En instalaciones antiguas usa baseline 0 y aplica la evolución sin borrar datos.

## Variables obligatorias

| Variable | Uso |
| --- | --- |
| `DB_URL` | JDBC de MySQL/RDS, por ejemplo `jdbc:mysql://host:3306/e-commerce-sierra-dorada` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de la aplicación |
| `JWT_SECRET` | Secreto JWT de al menos 32 bytes |
| `CORS_ALLOWED_ORIGINS` | Orígenes frontend separados por coma |
| `BOLD_SECRET_KEY` | Firma segura de pagos Bold |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Cuenta SMTP y contraseña de aplicación para confirmar registros |
| `MAIL_FROM` | Remitente visible de los correos de confirmación |
| `APP_PUBLIC_URL` | URL pública usada para construir el enlace de activación |
| `MIPAQUETE_API_KEY` | API key de Mi Paquete |
| `MIPAQUETE_SESSION_TRACKER` | Identificador de sesión de la integración |
| `MIPAQUETE_ORIGIN_DANE_CODE` | Código DANE de la bodega |
| `MIPAQUETE_SENDER_EMAIL` / `MIPAQUETE_SENDER_PHONE` | Datos del remitente |
| `MIPAQUETE_SENDER_DOCUMENT` / `MIPAQUETE_SENDER_ADDRESS` | Documento y dirección de recogida |
| `MIPAQUETE_USER_ID` | ID de usuario entregado por Mi Paquete |
| `MIPAQUETE_WEBHOOK_SECRET` | Secreto propio para proteger el webhook |

Variables opcionales: `CONTAINER_PORT` (8080), `MIPAQUETE_BASE_URL`, `MIPAQUETE_REQUEST_PICKUP`, `MIPAQUETE_FORBIDDEN_PRODUCT`, nombres del remitente y tiempo de expiración JWT.

No se incluyen contraseñas ni API keys reales en el repositorio.

## Ejecución

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

Por defecto la API queda en `http://localhost:8080/api`.

## Contratos principales

- `POST /api/auth/registro`, `POST /api/auth/login`
- `GET /api/productos` público; escrituras solo ADMIN
- `POST /api/contacto`, `POST /api/newsletter` públicos y persistidos
- `POST /api/envios/cotizaciones` y `GET /api/envios/ubicaciones`
- `POST /api/pedidos` autenticado; usuario, precios, stock y flete se validan en servidor
- `POST /api/pagos/bold/firma` autenticado; usa el total guardado del pedido
- `POST /api/envios/pedidos/{id}/guia` solo ADMIN
- `GET /api/envios/pedidos/{id}/tracking` propietario o ADMIN
- `POST /api/envios/webhook/estados` protegido con `X-Webhook-Secret`

La guía debe generarse después de confirmar el pago; crear el pedido no solicita automáticamente una recogida.
