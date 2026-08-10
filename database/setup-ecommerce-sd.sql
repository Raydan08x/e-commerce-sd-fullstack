-- Preparación manual opcional para MySQL local.
-- En AWS/RDS cree la base desde la consola y configure DB_URL, DB_USERNAME y DB_PASSWORD.
-- No se crean usuarios ni se almacenan contraseñas en el repositorio.
CREATE DATABASE IF NOT EXISTS `e-commerce-sierra-dorada`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `e-commerce-sierra-dorada`;

-- La aplicación ejecuta automáticamente los archivos de data-base/db/migration con Flyway.
-- Desde mysql CLI también pueden ejecutarse manualmente:
SOURCE db/migration/V1__modelo_relacional_definitivo.sql;
SOURCE db/migration/V2__evolucion_modelo_anterior.sql;
SOURCE db/migration/V3__catalogo_inicial.sql;

