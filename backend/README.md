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
| `BOLD_IDENTITY_KEY` | Llave de identidad del botón; permite confirmar el estado al regresar de Bold |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Cuenta SMTP y contraseña de aplicación para confirmar registros |
| `MAIL_FROM` | Remitente visible de los correos de confirmación |
| `APP_PUBLIC_URL` | URL pública usada para construir el enlace de activación |
| `MIPAQUETE_API_KEY` | API key de Mi Paquete |
| `MIPAQUETE_BASE_URL` | API de Mi Paquete; para pruebas `https://api-v2.dev.mpr.mipaquete.com` |
| `MIPAQUETE_SESSION_TRACKER` | Identificador de sesión de la integración |
| `MIPAQUETE_ORIGIN_DANE_CODE` | Código DANE de la bodega |
| `MIPAQUETE_SENDER_EMAIL` / `MIPAQUETE_SENDER_PHONE` | Datos del remitente |
| `MIPAQUETE_SENDER_DOCUMENT` / `MIPAQUETE_SENDER_ADDRESS` | Documento y dirección de recogida |
| `MIPAQUETE_USER_ID` | ID de usuario entregado por Mi Paquete |
| `MIPAQUETE_WEBHOOK_SECRET` | Secreto propio para proteger el webhook |

Variables opcionales: `CONTAINER_PORT` (8080), `MIPAQUETE_REQUEST_PICKUP`, `MIPAQUETE_FORBIDDEN_PRODUCT`, nombres del remitente y tiempo de expiración JWT.

## Modo seguro de Mi Paquete

`MIPAQUETE_CREATE_SHIPMENT_ENABLED=false` es el valor predeterminado. En este
modo se pueden consultar ubicaciones y cotizaciones en la API de pruebas, pero
un pago confirmado no llama a `createSending`, no crea una guía, no solicita
recogida y no dispara avisos logísticos por WhatsApp. El envío queda con estado
`PENDIENTE_ACTIVACION`.

Sólo cuando el comercio esté activo se debe cambiar simultáneamente la URL a
`https://api-v2.mpr.mipaquete.com` y establecer
`MIPAQUETE_CREATE_SHIPMENT_ENABLED=true`. Las notificaciones por WhatsApp son
una prestación de la cuenta/plan de Mi Paquete y usan el teléfono del
destinatario que ya se envía en `receiver.cellPhone`.

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
- `POST /api/pagos/bold/ordenes/{referencia}/confirmacion` autentica el retorno consultando Bold
- `POST /api/envios/pedidos/{id}/guia` solo ADMIN
- `GET /api/envios/pedidos/{id}/tracking` propietario o ADMIN
- `POST /api/envios/webhook/estados` protegido con `X-Webhook-Secret`

La guía debe generarse después de confirmar el pago; crear el pedido no solicita automáticamente una recogida.

## Embalaje y cotización

`productos.unidades_por_producto` indica el contenido físico vendido: 1 para una
unidad, 4 para un 4-pack y 24 para una caja. El servidor consolida hasta 24
unidades por bulto y usa la misma especificación al cotizar y al crear la guía.

| Contenido máximo por bulto | Medidas exteriores (ancho × largo × alto) | Peso enviado a la API |
| --- | --- | --- |
| 1 | 10 × 10 × 25 cm | 1 kg |
| 2 | 10 × 18 × 25 cm | 2 kg |
| 3 | 18 × 18 × 25 cm | 3 kg |
| 4 | 18 × 18 × 25 cm | 4 kg |
| 12 | 18 × 51 × 25 cm | 9 kg |
| 24 | 29 × 42 × 27 cm | 18 kg |

Las medidas incluyen protección exterior. El peso parte de 16,6 kg por 24
unidades, suma cartón, burbuja y cinta, y se redondea hacia arriba porque la API
de MiPaquete exige kilogramos enteros. Para cantidades mayores se balancean los
bultos sin superar 24 unidades por cada uno.

Los productos de merchandising usan peso y medidas propios guardados en MySQL.
Si se completa un dato de empaque personalizado, los cuatro campos son
obligatorios. Al combinar varios artículos, el servidor reparte peso y volumen
en bultos de máximo 25 kg y 60 cm de alto.
