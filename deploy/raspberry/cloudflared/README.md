# Tunel exclusivo del e-commerce

Este servicio publica `sierradorada.co`, `www.sierradorada.co` y el alias
`shop.sierradorada.co` desde el puerto local `127.0.0.1:3103`. El gastrobar
usa su propio servicio y dominio en `gastro.sierradorada.co`.

Archivos instalados en la Raspberry Pi:

- `/home/sdpi/.cloudflared-ecommerce/config.yml`
- `/home/sdpi/.cloudflared-ecommerce/credentials.json` (secreto, no versionado)
- `/etc/systemd/system/cloudflared-ecommerce.service`

El tunel registrado en Cloudflare es `ecommerce-sierradorada-co`, con ID
`335673f1-97dd-456f-9199-64e30c6bd08a`.
