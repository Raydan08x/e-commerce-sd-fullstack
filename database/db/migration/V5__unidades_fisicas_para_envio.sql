-- Una fila del catálogo puede representar una botella, un 4-pack o una caja de 24.
-- MiPaquete necesita la cantidad de bultos, no la cantidad de filas del carrito.
ALTER TABLE productos
    ADD COLUMN unidades_por_producto INT NOT NULL DEFAULT 1 AFTER stock,
    ADD COLUMN peso_envio_kg DECIMAL(8,3) NULL AFTER unidades_por_producto,
    ADD COLUMN ancho_envio_cm INT NULL AFTER peso_envio_kg,
    ADD COLUMN largo_envio_cm INT NULL AFTER ancho_envio_cm,
    ADD COLUMN alto_envio_cm INT NULL AFTER largo_envio_cm;

UPDATE productos SET unidades_por_producto = 4
WHERE codigo IN ('P1005', 'P1006', 'P1007', 'P1008', 'P1009');

UPDATE productos SET unidades_por_producto = 24
WHERE codigo IN ('P1010', 'P1011', 'P1012', 'P1013', 'P1014');

-- Medidas exteriores conservadoras para mercancía ya empacada. El administrador
-- puede afinarlas cuando se pesen los productos físicos definitivos.
UPDATE productos SET peso_envio_kg = 0.500, ancho_envio_cm = 25, largo_envio_cm = 30, alto_envio_cm = 15
WHERE codigo = 'M1015';
UPDATE productos SET peso_envio_kg = 1.200, ancho_envio_cm = 32, largo_envio_cm = 38, alto_envio_cm = 12
WHERE codigo = 'M1016';
UPDATE productos SET peso_envio_kg = 0.600, ancho_envio_cm = 27, largo_envio_cm = 32, alto_envio_cm = 8
WHERE codigo = 'M1017';

ALTER TABLE productos
    ADD CONSTRAINT chk_producto_unidades_envio CHECK (unidades_por_producto >= 1),
    ADD CONSTRAINT chk_producto_peso_envio CHECK (peso_envio_kg IS NULL OR peso_envio_kg > 0),
    ADD CONSTRAINT chk_producto_ancho_envio CHECK (ancho_envio_cm IS NULL OR ancho_envio_cm > 0),
    ADD CONSTRAINT chk_producto_largo_envio CHECK (largo_envio_cm IS NULL OR largo_envio_cm > 0),
    ADD CONSTRAINT chk_producto_alto_envio CHECK (alto_envio_cm IS NULL OR alto_envio_cm > 0);
