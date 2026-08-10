-- Modelo relacional definitivo para MySQL 8 / Amazon RDS.
-- La aplicación y Flyway deben conectarse a una base ya creada mediante DB_URL.
CREATE TABLE IF NOT EXISTS metodos_pago (
    id_metodo_pago INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    genero VARCHAR(30),
    direccion TEXT,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    contrasena VARCHAR(255) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
    acepta_terminos BOOLEAN NOT NULL DEFAULT FALSE,
    autoriza_datos BOOLEAN NOT NULL DEFAULT FALSE,
    autoriza_comunicaciones BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_consentimiento DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categorias (
    id_categoria INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    categoria_principal_id INT,
    CONSTRAINT fk_categoria_principal
        FOREIGN KEY (categoria_principal_id) REFERENCES categorias(id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS productos (
    id_producto INT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre_produ VARCHAR(200) NOT NULL,
    descripcion_produ TEXT,
    precio_base DECIMAL(12,2) NOT NULL,
    categoria_id INT,
    marca VARCHAR(100),
    tipo_cerveza VARCHAR(100),
    estilo_cerveza VARCHAR(100),
    stock INT NOT NULL DEFAULT 0,
    abv DECIMAL(4,2),
    ibu INT,
    imagen_url LONGTEXT,
    inspiracion TEXT,
    color_hex VARCHAR(20),
    color_nombre VARCHAR(100),
    temperatura VARCHAR(50),
    leyenda TEXT,
    descripcion_completa TEXT,
    proceso TEXT,
    caracteristica_color TEXT,
    caracteristica_aroma TEXT,
    caracteristica_sabor TEXT,
    caracteristica_maridaje TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id) REFERENCES categorias(id_categoria),
    CONSTRAINT chk_producto_precio CHECK (precio_base >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS producto_maridajes (
    producto_id INT NOT NULL,
    orden INT NOT NULL,
    emoji VARCHAR(255),
    nombre VARCHAR(255),
    PRIMARY KEY (producto_id, orden),
    CONSTRAINT fk_maridaje_producto
        FOREIGN KEY (producto_id) REFERENCES productos(id_producto) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS mensajes_contacto (
    id_mensaje INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL,
    mensaje TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'NUEVO',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contacto_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    INDEX idx_contacto_estado_fecha (estado, fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS suscripciones_newsletter (
    id_suscripcion INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT,
    email VARCHAR(150) NOT NULL UNIQUE,
    fecha_suscripcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_baja DATETIME,
    CONSTRAINT fk_newsletter_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    costo_envio DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_pedido DECIMAL(12,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
    direccion_envio TEXT NOT NULL,
    metodo_pago_id INT,
    fecha_confirmacion DATETIME,
    fecha_envio DATETIME,
    fecha_entrega DATETIME,
    notas TEXT,
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_pedido_metodo_pago
        FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id_metodo_pago),
    CONSTRAINT chk_pedido_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_pedido_envio CHECK (costo_envio >= 0),
    CONSTRAINT chk_total_pedido CHECK (total_pedido >= 0),
    INDEX idx_pedido_usuario_fecha (usuario_id, fecha_pedido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS detalle_pedidos (
    id_detalle INT PRIMARY KEY AUTO_INCREMENT,
    pedido_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (producto_id) REFERENCES productos(id_producto),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pagos (
    id_pagos INT PRIMARY KEY AUTO_INCREMENT,
    pedido_id INT NOT NULL,
    metodo_pago_id INT NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
    transaccion_id VARCHAR(100) UNIQUE,
    CONSTRAINT fk_pago_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_pago_metodo
        FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id_metodo_pago),
    CONSTRAINT chk_pago_monto CHECK (monto >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS envios (
    id_envio INT PRIMARY KEY AUTO_INCREMENT,
    pedido_id INT NOT NULL UNIQUE,
    proveedor VARCHAR(30) NOT NULL DEFAULT 'MIPAQUETE',
    codigo_mipaquete BIGINT UNIQUE,
    transportadora_id VARCHAR(100),
    transportadora_nombre VARCHAR(100),
    codigo_dane_origen VARCHAR(12) NOT NULL,
    codigo_dane_destino VARCHAR(12) NOT NULL,
    destinatario_nombre VARCHAR(150) NOT NULL,
    destinatario_apellido VARCHAR(150),
    destinatario_email VARCHAR(150) NOT NULL,
    destinatario_telefono VARCHAR(20) NOT NULL,
    direccion_destino TEXT NOT NULL,
    costo DECIMAL(12,2) NOT NULL DEFAULT 0,
    numero_guia VARCHAR(100),
    url_guia TEXT,
    estado VARCHAR(100) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_envio_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    CONSTRAINT chk_envio_costo CHECK (costo >= 0),
    INDEX idx_envio_codigo_estado (codigo_mipaquete, estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS eventos_envio (
    id_evento INT PRIMARY KEY AUTO_INCREMENT,
    envio_id INT NOT NULL,
    estado VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_evento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payload LONGTEXT,
    CONSTRAINT fk_evento_envio
        FOREIGN KEY (envio_id) REFERENCES envios(id_envio) ON DELETE CASCADE,
    INDEX idx_evento_envio_fecha (envio_id, fecha_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
