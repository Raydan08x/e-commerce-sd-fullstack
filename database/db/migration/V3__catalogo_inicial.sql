-- Catálogo inicial persistido en MySQL. El frontend nunca lee JSON locales.
INSERT INTO categorias (nombre) VALUES ('Cervezas') ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);
INSERT INTO categorias (nombre) VALUES ('Packs') ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);
INSERT INTO categorias (nombre) VALUES ('Merchandising') ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO metodos_pago (nombre, activo) VALUES ('Bold', TRUE) ON DUPLICATE KEY UPDATE activo = TRUE;

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'C1000', 'APA Premium', 'Cerveza artesanal de estilo American Pale Ale. Seca, equilibrada y refrescante.', 14000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Cervezas'), 'Sierra Dorada',
  100, 4.8, 38, '../src/assets/images/ale-apa.png', 'Xue, dios del sol Muisca', '#D99A2B',
  'Dorado claro brillante', '8-10°C', 'Brillante como la luz del dios Xue, trayendo energía y fuerza en cada sorbo.', 'Nuestra APA Premium es una cerveza American Pale Ale dorada y cristalina. Destaca por su cuerpo medio y un final agradablemente seco que invita a seguir bebiendo, ideal para refrescar el alma.',
  'Elaborada con lúpulos seleccionados de la más alta calidad y un cuidadoso proceso de dry-hopping.', 'Dorado claro y brillante.', 'Predominancia de notas cítricas, flores blancas y resinas frescas.', 'Lúpulo fresco y perfil maltoso perfectamente equilibrado, con final seco y amargor limpio.', 'Hamburguesas artesanales, pollo a la parrilla y quesos semiduros.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='C1000');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🍔', 'Hamburguesas artesanales' FROM productos WHERE codigo='C1000';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🍗', 'Pollo a la parrilla' FROM productos WHERE codigo='C1000';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 2, '🧀', 'Quesos semiduros' FROM productos WHERE codigo='C1000';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'C1001', 'Sour de Corozo', 'Una Sour Ale exótica elaborada con corozo 100% natural, ofreciendo acidez frutal refrescante.', 15000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Cervezas'), 'Sierra Dorada',
  100, 5, 8, '../src/assets/images/corozo-sour.png', 'Biodiversidad andina y raíces colombianas', '#8B0015',
  'Rubí intenso', '6-8°C', 'El misticismo de nuestros frutos ancestrales reunido en una pócima vibrante y frutal.', 'Sour Ale fermentada con levaduras salvajes y madurada con corozo seleccionado a mano. Posee una acidez marcada, un perfil frutal único y un color rubí espectacular que deleita a la vista y al paladar.',
  'Fermentación especial con levaduras salvajes y pulpa de corozo natural agregada durante la maduración.', 'Rubí intenso y vibrante.', 'Frutos rojos silvestres, toques cítricos y notas florales delicadas.', 'Predominantemente ácido, refrescante, con marcada presencia del corozo natural.', 'Ceviches de la costa, mariscos y quesos de cabra.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='C1001');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🐟', 'Ceviches' FROM productos WHERE codigo='C1001';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🍤', 'Mariscos' FROM productos WHERE codigo='C1001';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 2, '🧀', 'Quesos de cabra' FROM productos WHERE codigo='C1001';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'C1002', 'Sour de Piña', 'Catarina Sour con piña 100% natural, jugosa, tropical y sumamente refrescante.', 15000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Cervezas'), 'Sierra Dorada',
  100, 4, NULL, '../src/assets/images/sour-pina.png', 'Adaptación de Catarina Sour y tropicalidad colombiana', '#F2B705',
  'Dorado pálido', '6-8°C', 'El sol del trópico colombiano capturado en un elixir refrescante y frutal.', 'Inspirada en el estilo Catarina Sour, esta cerveza incorpora la dulzura y frescura de la piña tropical colombiana balanceada con una acidez limpia lograda mediante fermentación salvaje.',
  'Elaborada con piña seleccionada al 100% y fermentada a temperaturas controladas con bacterias lácticas y levaduras salvajes.', 'Dorado pálido, brillante y traslúcido.', 'Piña madura fresca, matices cítricos y ligeros aromas campestres.', 'Frutal a piña jugosa, final limpio, seco y agradablemente ácido.', 'Ensaladas frescas, ceviches y quesos de cabra.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='C1002');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥗', 'Ensaladas frescas' FROM productos WHERE codigo='C1002';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🐟', 'Ceviches' FROM productos WHERE codigo='C1002';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 2, '🧀', 'Quesos de cabra' FROM productos WHERE codigo='C1002';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'C1003', 'Stout Premium', 'Una Stout robusta, noble y auténtica. Perfil oscuro y profundo con notas intensas.', 16000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Cervezas'), 'Sierra Dorada',
  100, 6.1, 45, '../src/assets/images/ale-stout.png', 'Profundidad de la tierra andina', '#080606',
  'Negro intenso', '10-12°C', 'Oscura como la noche Muisca en la sagrada cordillera, un tributo al fuego y a la tierra.', 'Nuestra Stout Premium ofrece una experiencia de cuerpo pleno y sabor audaz. Combina maltas tostadas que evocan notas a cacao fino y café de origen, con un final equilibrado.',
  'Elaboración tradicional utilizando agua pura de los Andes e infusión lenta de maltas tostadas especiales.', 'Negro intenso con espuma cremosa tono canela.', 'Café fresco tostado, cacao amargo y sutiles notas a madera.', 'Cuerpo pleno y robusto, notas de malta tostada, café espresso y chocolate oscuro con final ligeramente dulce.', 'Carnes a la parrilla, quesos maduros y postres de chocolate.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='C1003');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥩', 'Carnes asadas' FROM productos WHERE codigo='C1003';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🧀', 'Quesos maduros' FROM productos WHERE codigo='C1003';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 2, '🍰', 'Postres de chocolate' FROM productos WHERE codigo='C1003';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'C1004', 'Barley Wine', 'Cerveza compleja, robusta y licorosa, madurada en roble francés e infusionada en whiskey.', 18000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Cervezas'), 'Sierra Dorada',
  100, 10, 50, '../src/assets/images/ale-barleywine.png', 'El paso del tiempo y la madurez', '#5A1F0F',
  'Ámbar oscuro', '12-14°C', 'Madurada en el silencio de los Andes, paciencia y carácter transformados en oro líquido.', 'Una cerveza de guarda excepcional, de carácter robusto e intensa tibieza alcohólica. Las notas de roble francés y whiskey realzan su dulzura balanceada de miel y panela.',
  'Madurada sobre astillas de roble francés previamente infusionadas en selecto whiskey de malta.', 'Ámbar oscuro con destellos cobrizos.', 'Caramelo tostado, miel silvestre, panela, roble andino y notas cálidas de whiskey.', 'Dulzor complejo y robusto, con recuerdos de madera, licor fino y final prolongado único.', 'Carnes asadas, quesos maduros y postres de caramelo.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='C1004');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥩', 'Carnes asadas' FROM productos WHERE codigo='C1004';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🧀', 'Quesos maduros' FROM productos WHERE codigo='C1004';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 2, '🍮', 'Postres de caramelo' FROM productos WHERE codigo='C1004';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1005', '4-Pack APA Premium', 'Edición especial que contiene 4 latas de nuestra clásica American Pale Ale.', 50000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 4.8, 38, '../src/assets/images/ale-apa.png', 'Xue en manada', '#D99A2B',
  'Dorado claro', '8-10°C', 'Perfecto para llevar la luz del sol Muisca a tus reuniones.', '4 latas de APA Premium empacadas en una caja especial de cartón kraft reforzado con manija para transporte fácil.',
  'Empacado manual en caja eco-amigable con separadores.', 'Dorado claro.', 'Cítricos y flores.', 'Lúpulo fresco con final seco.', 'Hamburguesas y parrilladas con amigos.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1005');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🍔', 'Hamburguesas' FROM productos WHERE codigo='P1005';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🍻', 'Reunión de amigos' FROM productos WHERE codigo='P1005';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1006', '4-Pack Sour de Corozo', 'Edición especial con 4 latas de nuestra Sour Ale con corozo 100% natural.', 54000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 5, 8, '../src/assets/images/corozo-sour.png', 'Fruto andino multiplicado', '#8B0015',
  'Rubí intenso', '6-8°C', 'Acidez y frescura frutal para compartir con los que más quieres.', 'Lleva 4 latas de nuestra exótica cerveza de corozo rubí intenso en su empaque especial de colección.',
  'Empacado manual en caja de cartón kraft ecológica con manija.', 'Rubí intenso.', 'Frutos rojos y cítricos.', 'Ácido y marcadamente refrescante.', 'Ceviche y ensaladas frescas.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1006');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🐟', 'Ceviche' FROM productos WHERE codigo='P1006';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🥗', 'Ensaladas frescas' FROM productos WHERE codigo='P1006';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1007', '4-Pack Sour de Piña', 'Caja especial con 4 latas de nuestra refrescante Catarina Sour de piña.', 54000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 4, NULL, '../src/assets/images/sour-pina.png', 'El sabor del trópico x4', '#F2B705',
  'Dorado pálido', '6-8°C', 'La frescura tropical de la piña multiplicada por cuatro.', 'Disfruta de 4 latas de nuestra Catarina Sour de piña, empacadas en una caja kraft transportadora.',
  'Seleccionado y empacado manualmente en cajas reforzadas biodegradables.', 'Dorado pálido.', 'Piña fresca tropical.', 'Ácido, jugoso y frutal.', 'Mariscos, ceviche y ensaladas.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1007');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🍤', 'Mariscos' FROM productos WHERE codigo='P1007';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🥗', 'Ensaladas' FROM productos WHERE codigo='P1007';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1008', '4-Pack Stout Premium', 'Caja especial de 4 latas de nuestra Stout robusta, noble y profunda.', 58000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 6.1, 45, '../src/assets/images/ale-stout.png', 'Profundidad x4', '#080606',
  'Negro intenso', '10-12°C', 'Comparte el misticismo y la fuerza de la Stout Premium.', 'Lleva 4 latas de nuestra Stout Premium en una caja transportadora con manija, ideal para los amantes del chocolate y café.',
  'Empacado manual en caja kraft biodegradable con soportes antivibración.', 'Negro intenso.', 'Café y chocolate tostado.', 'Cuerpo pleno, tostado y ligeramente dulce.', 'Carnes asadas y postres de chocolate.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1008');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥩', 'Carnes asadas' FROM productos WHERE codigo='P1008';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🍫', 'Postres de chocolate' FROM productos WHERE codigo='P1008';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1009', '4-Pack Barley Wine', 'Caja de colección con 4 latas de nuestra Barley Wine madurada en roble y whiskey.', 66000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 10, 50, '../src/assets/images/ale-barleywine.png', 'Tiempo para compartir', '#5A1F0F',
  'Ámbar oscuro', '12-14°C', 'El regalo perfecto para los paladares más exigentes.', 'Caja premium con 4 latas de nuestra cerveza de guarda Barley Wine. Un verdadero tesoro líquido de 10.0% ABV.',
  'Empacado cuidadoso a mano en caja protectora de cartón kraft.', 'Ámbar oscuro.', 'Miel, caramelo, roble y whiskey.', 'Complejo, licoroso y robusto.', 'Quesos maduros, carnes asadas y postres de caramelo.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1009');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥩', 'Carnes asadas' FROM productos WHERE codigo='P1009';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🧀', 'Quesos maduros' FROM productos WHERE codigo='P1009';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1010', 'Caja 24 APA Premium', 'Caja de 24 latas de APA Premium. Ideal para eventos y abastecer tu reserva personal.', 192000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 4.8, 38, '../src/assets/images/ale-apa.png', 'Xue en Abundancia', '#D99A2B',
  'Dorado claro', '8-10°C', 'Un gran tesoro para grandes celebraciones.', 'Caja de cartón corrugado reforzado conteniendo 24 latas de nuestra American Pale Ale. La mejor forma de llevar frescura Muisca a gran escala.',
  'Empacado y sellado de fábrica en cajas de alta resistencia.', 'Dorado claro brillante.', 'Cítricos y flores.', 'Lúpulo fresco con final seco.', 'Fiestas, asados y hamburguesadas.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1010');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🍔', 'Hamburguesas' FROM productos WHERE codigo='P1010';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🎉', 'Eventos especiales' FROM productos WHERE codigo='P1010';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1011', 'Caja 24 Sour de Corozo', 'Caja de 24 latas de Sour de Corozo. Acidez y color rubí en abundancia para compartir.', 192000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 5, 8, '../src/assets/images/corozo-sour.png', 'Tradición Ancestral x24', '#8B0015',
  'Rubí intenso', '6-8°C', 'Toda la biodiversidad y el sabor de nuestra tierra en tu reserva.', 'Caja con 24 latas de Sour Ale de corozo natural. La perfecta combinación de acidez y color rubí en un empaque de gran capacidad.',
  'Sellado industrial en cajas protectoras de cartón rígido.', 'Rubí intenso.', 'Frutos rojos del bosque.', 'Ácido, afrutado y refrescante.', 'Ceviches, mariscos y asados veraniegos.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1011');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🐟', 'Ceviche' FROM productos WHERE codigo='P1011';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🍤', 'Mariscos' FROM productos WHERE codigo='P1011';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1012', 'Caja 24 Sour de Piña', 'Caja de 24 latas de Sour de Piña. Refrescante acidez tropical para todo momento.', 192000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 4, NULL, '../src/assets/images/sour-pina.png', 'Trópico Colombiano x24', '#F2B705',
  'Dorado pálido', '6-8°C', 'El sol del trópico andino para abastecer cualquier celebración.', 'Caja de 24 unidades de Catarina Sour con piña 100% natural, ideal para calmar la sed en grandes reuniones.',
  'Empacado de alta protección en caja corrugada eco-amigable.', 'Dorado pálido.', 'Piña fresca tropical.', 'Ácido, jugoso y frutal.', 'Ensaladas frescas, mariscos y pasabocas.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1012');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥗', 'Ensaladas frescas' FROM productos WHERE codigo='P1012';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🍤', 'Mariscos' FROM productos WHERE codigo='P1012';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1013', 'Caja 24 Stout Premium', 'Caja de 24 latas de Stout Premium. Cuerpo robusto y notas a café para abastecer tu hogar.', 192000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 6.1, 45, '../src/assets/images/ale-stout.png', 'Fuerza de la Tierra x24', '#080606',
  'Negro intenso', '10-12°C', 'La profundidad del cacao y café de los Andes a gran escala.', 'Caja conteniendo 24 latas de nuestra Stout Premium. Un verdadero festín para quienes aprecian los tonos oscuros, robustos y tostados.',
  'Empacado automatizado con insertos protectores contra impactos.', 'Negro intenso.', 'Café espresso y chocolate.', 'Tostado intenso con final sedoso y balanceado.', 'Carnes a la parrilla, quesos maduros y chocolates.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1013');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥩', 'Carnes a la parrilla' FROM productos WHERE codigo='P1013';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🧀', 'Quesos maduros' FROM productos WHERE codigo='P1013';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'P1014', 'Caja 24 Barley Wine', 'Caja de 24 latas de Barley Wine. Cerveza de guarda con notas de roble y whiskey.', 192000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Packs'), 'Sierra Dorada',
  100, 10, 50, '../src/assets/images/ale-barleywine.png', 'Sabiduría Licorosa x24', '#5A1F0F',
  'Ámbar oscuro', '12-14°C', 'El tesoro de guarda definitivo para coleccionistas y conocedores.', 'Caja de 24 unidades de Barley Wine. Robusta, compleja e ideal para añejar y disfrutar lentamente en grandes ocasiones.',
  'Empacado manual en caja protectora reforzada de gran calibre.', 'Ámbar oscuro.', 'Miel, caramelo, roble y whiskey.', 'Dulzor complejo, licoroso y final prolongado único.', 'Quesos curados, asados selectos y postres finos.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='P1014');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🥩', 'Asados selectos' FROM productos WHERE codigo='P1014';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🧀', 'Quesos curados' FROM productos WHERE codigo='P1014';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'M1015', 'Gorra Trucker Oso', 'Gorra premium tipo trucker con bordado dorado en alto relieve de nuestro oso de anteojos.', 35000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Merchandising'), 'Sierra Dorada',
  100, NULL, NULL, 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=80', 'Símbolo de los Andes', '#222223',
  'Negro y Dorado', NULL, 'Lleva el guardián de los Andes con orgullo y estilo en cada aventura.', 'Gorra de alta calidad estilo Trucker con visera curva, malla respirable y ajuste regulable. Destaca por su bordado frontal dorado del oso de anteojos, emblema de Sierra Dorada.',
  'Confeccionada con algodón de alta resistencia y poliéster respirable, con costuras reforzadas.', 'Negro con detalles dorados.', 'N/A', 'N/A', 'Tu pinta diaria y una buena cerveza fría.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='M1015');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '🧢', 'Pinta diaria' FROM productos WHERE codigo='M1015';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🍺', 'Cerveza fría' FROM productos WHERE codigo='M1015';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'M1016', 'Buzo Hoodie Muisca', 'Hoodie premium de algodón perchado con diseños geométricos inspirados en el arte Muisca.', 120000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Merchandising'), 'Sierra Dorada',
  100, NULL, NULL, 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=80', 'Geometría Ancestral', '#034638',
  'Verde Selva', NULL, 'El abrigo de la cordillera andina expresado en texturas de alta calidad.', 'Buzo con capota y bolsillo tipo canguro, confeccionado con algodón perchado ultra suave. Lleva un grabado frontal de simbología Muisca en tinta metalizada dorada.',
  'Elaborado con algodón 100% perchado nacional y estampado térmico de alta durabilidad.', 'Verde selva con estampado dorado.', 'N/A', 'N/A', 'Climas fríos andinos.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='M1016');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '❄️', 'Climas fríos' FROM productos WHERE codigo='M1016';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '🪵', 'Fogata' FROM productos WHERE codigo='M1016';

INSERT INTO productos (
  codigo, nombre_produ, descripcion_produ, precio_base, categoria_id, marca,
  stock, abv, ibu, imagen_url, inspiracion, color_hex, color_nombre, temperatura,
  leyenda, descripcion_completa, proceso, caracteristica_color, caracteristica_aroma,
  caracteristica_sabor, caracteristica_maridaje, activo
) VALUES (
  'M1017', 'Suéter Sol Dorado', 'Camiseta (suéter) premium en algodón peinado con el isotipo de Sierra Dorada en el pecho.', 45000,
  (SELECT id_categoria FROM categorias WHERE nombre = 'Merchandising'), 'Sierra Dorada',
  100, NULL, NULL, 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=80', 'Xue y la luz eterna', '#E5E1E6',
  'Blanco Hueso', NULL, 'El sol dorado Muisca para acompañar tu día a día.', 'Camiseta de cuello redondo y manga corta, hecha en algodón peinado de tacto suave. Lleva en el pecho el sol de Sierra Dorada impreso en screen dorado de alta definición.',
  'Hilado de algodón peinado con suavizado textil y costuras invisibles.', 'Blanco hueso con screen dorado.', 'N/A', 'N/A', 'Comodidad andina.', TRUE
) ON DUPLICATE KEY UPDATE
  nombre_produ=VALUES(nombre_produ), descripcion_produ=VALUES(descripcion_produ),
  precio_base=VALUES(precio_base), categoria_id=VALUES(categoria_id), marca=VALUES(marca),
  abv=VALUES(abv), ibu=VALUES(ibu), imagen_url=VALUES(imagen_url), inspiracion=VALUES(inspiracion),
  color_hex=VALUES(color_hex), color_nombre=VALUES(color_nombre), temperatura=VALUES(temperatura),
  leyenda=VALUES(leyenda), descripcion_completa=VALUES(descripcion_completa), proceso=VALUES(proceso),
  caracteristica_color=VALUES(caracteristica_color), caracteristica_aroma=VALUES(caracteristica_aroma),
  caracteristica_sabor=VALUES(caracteristica_sabor), caracteristica_maridaje=VALUES(caracteristica_maridaje),
  activo=VALUES(activo);
DELETE FROM producto_maridajes WHERE producto_id=(SELECT id_producto FROM productos WHERE codigo='M1017');
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 0, '👕', 'Estilo casual' FROM productos WHERE codigo='M1017';
INSERT INTO producto_maridajes (producto_id, orden, emoji, nombre)
SELECT id_producto, 1, '☀️', 'Días soleados' FROM productos WHERE codigo='M1017';


