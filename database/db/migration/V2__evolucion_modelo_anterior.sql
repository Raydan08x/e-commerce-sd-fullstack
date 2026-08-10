-- Evolución segura para bases creadas con el diagrama anterior.
-- Cada ALTER se ejecuta solo si la columna todavía no existe.

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE usuarios ADD COLUMN acepta_terminos BOOLEAN NOT NULL DEFAULT FALSE', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'acepta_terminos'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE usuarios ADD COLUMN autoriza_datos BOOLEAN NOT NULL DEFAULT FALSE', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'autoriza_datos'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE usuarios ADD COLUMN autoriza_comunicaciones BOOLEAN NOT NULL DEFAULT FALSE', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'autoriza_comunicaciones'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE usuarios ADD COLUMN fecha_consentimiento DATETIME', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'fecha_consentimiento'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN codigo VARCHAR(20)', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'codigo'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN ibu INT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'ibu'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN imagen_url LONGTEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'imagen_url'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN inspiracion TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'inspiracion'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN color_hex VARCHAR(20)', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'color_hex'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN color_nombre VARCHAR(100)', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'color_nombre'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN temperatura VARCHAR(50)', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'temperatura'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN leyenda TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'leyenda'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN descripcion_completa TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'descripcion_completa'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN proceso TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'proceso'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN caracteristica_color TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'caracteristica_color'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN caracteristica_aroma TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'caracteristica_aroma'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN caracteristica_sabor TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'caracteristica_sabor'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE productos ADD COLUMN caracteristica_maridaje TEXT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'caracteristica_maridaje'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE pedidos ADD COLUMN subtotal DECIMAL(12,2) NOT NULL DEFAULT 0', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'pedidos' AND column_name = 'subtotal'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE pedidos ADD COLUMN costo_envio DECIMAL(12,2) NOT NULL DEFAULT 0', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'pedidos' AND column_name = 'costo_envio'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE mensajes_contacto ADD COLUMN usuario_id INT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'mensajes_contacto' AND column_name = 'usuario_id'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE mensajes_contacto ADD COLUMN estado VARCHAR(30) NOT NULL DEFAULT ''NUEVO''', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'mensajes_contacto' AND column_name = 'estado'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE suscripciones_newsletter ADD COLUMN usuario_id INT', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'suscripciones_newsletter' AND column_name = 'usuario_id'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0, 'ALTER TABLE suscripciones_newsletter ADD COLUMN fecha_baja DATETIME', 'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'suscripciones_newsletter' AND column_name = 'fecha_baja'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

-- Conserva las filas del catálogo anterior y les asigna sus códigos definitivos.
-- Así V3 actualiza el mismo registro en vez de duplicarlo.
UPDATE productos SET codigo = 'C1000' WHERE nombre_produ = 'APA Premium' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'C1001' WHERE nombre_produ = 'Sour de Corozo' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'C1002' WHERE nombre_produ = 'Sour de Piña' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'C1003' WHERE nombre_produ = 'Stout Premium' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'C1004' WHERE nombre_produ = 'Barley Wine' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1005' WHERE nombre_produ = '4-Pack APA Premium' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1006' WHERE nombre_produ = '4-Pack Sour de Corozo' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1007' WHERE nombre_produ = '4-Pack Sour de Piña' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1008' WHERE nombre_produ = '4-Pack Stout Premium' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1009' WHERE nombre_produ = '4-Pack Barley Wine' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1010' WHERE nombre_produ = 'Caja 24 APA Premium' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1011' WHERE nombre_produ = 'Caja 24 Sour de Corozo' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1012' WHERE nombre_produ = 'Caja 24 Sour de Piña' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1013' WHERE nombre_produ = 'Caja 24 Stout Premium' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'P1014' WHERE nombre_produ = 'Caja 24 Barley Wine' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'M1015' WHERE nombre_produ = 'Gorra Trucker Oso' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'M1016' WHERE nombre_produ = 'Buzo Hoodie Muisca' AND (codigo IS NULL OR codigo = '');
UPDATE productos SET codigo = 'M1017' WHERE nombre_produ = 'Suéter Sol Dorado' AND (codigo IS NULL OR codigo = '');

UPDATE productos
SET codigo = CONCAT('LEGACY-', id_producto)
WHERE codigo IS NULL OR codigo = '';

ALTER TABLE productos MODIFY codigo VARCHAR(20) NOT NULL;
ALTER TABLE productos MODIFY imagen_url LONGTEXT;
ALTER TABLE usuarios MODIFY genero VARCHAR(30);
ALTER TABLE usuarios MODIFY rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE';
ALTER TABLE pedidos MODIFY estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente';
ALTER TABLE pagos MODIFY estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente';

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE productos ADD CONSTRAINT uk_productos_codigo UNIQUE (codigo)',
    'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'productos'
    AND column_name = 'codigo' AND non_unique = 0
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE mensajes_contacto ADD CONSTRAINT fk_contacto_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE SET NULL',
    'SELECT 1')
  FROM information_schema.table_constraints
  WHERE constraint_schema = DATABASE() AND table_name = 'mensajes_contacto'
    AND constraint_name = 'fk_contacto_usuario'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;

SET @sd_ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE suscripciones_newsletter ADD CONSTRAINT fk_newsletter_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE SET NULL',
    'SELECT 1')
  FROM information_schema.table_constraints
  WHERE constraint_schema = DATABASE() AND table_name = 'suscripciones_newsletter'
    AND constraint_name = 'fk_newsletter_usuario'
);
PREPARE sd_stmt FROM @sd_ddl;
EXECUTE sd_stmt;
DEALLOCATE PREPARE sd_stmt;
