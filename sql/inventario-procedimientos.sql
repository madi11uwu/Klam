-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - INVENTARIO (ROL 2)
-- Equipo + especificaciones
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS insertar_equipo;
DROP PROCEDURE IF EXISTS modificar_equipo;
DROP PROCEDURE IF EXISTS eliminar_equipo;
DROP PROCEDURE IF EXISTS listar_equipos;
DROP PROCEDURE IF EXISTS buscar_equipo_por_id;
DROP PROCEDURE IF EXISTS listar_especificaciones_equipo;
DROP PROCEDURE IF EXISTS insertar_especificacion_equipo;
DROP PROCEDURE IF EXISTS eliminar_especificaciones_equipo;

DELIMITER $$

-- ---------------------------------------------------------
-- EQUIPO
-- ---------------------------------------------------------

CREATE PROCEDURE insertar_equipo(
    OUT p_id         INT,
    IN  p_nombre     VARCHAR(150),
    IN  p_categoria  VARCHAR(20),
    IN  p_disponible TINYINT
)
BEGIN
    INSERT INTO equipo (nombre, categoria, disponible)
    VALUES (p_nombre, p_categoria, p_disponible);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_equipo(
    IN p_id         INT,
    IN p_nombre     VARCHAR(150),
    IN p_categoria  VARCHAR(20),
    IN p_disponible TINYINT,
    IN p_activo     TINYINT
)
BEGIN
    UPDATE equipo
       SET nombre     = p_nombre,
           categoria  = p_categoria,
           disponible = p_disponible,
           activo     = p_activo
     WHERE id_equipo = p_id;
END$$

-- Baja logica: el equipo se conserva (Cirugia lo referencia)
CREATE PROCEDURE eliminar_equipo(
    IN p_id INT
)
BEGIN
    UPDATE equipo
       SET activo = 0
     WHERE id_equipo = p_id;
END$$

CREATE PROCEDURE listar_equipos()
BEGIN
    SELECT id_equipo, nombre, categoria, disponible, activo
      FROM equipo
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_equipo_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_equipo, nombre, categoria, disponible, activo
      FROM equipo
     WHERE id_equipo = p_id;
END$$

-- ---------------------------------------------------------
-- EQUIPO_ESPECIFICACION (detalle de Equipo)
-- ---------------------------------------------------------

CREATE PROCEDURE listar_especificaciones_equipo(
    IN p_id_equipo INT
)
BEGIN
    SELECT clave, valor
      FROM equipo_especificacion
     WHERE id_equipo = p_id_equipo;
END$$

CREATE PROCEDURE insertar_especificacion_equipo(
    IN p_id_equipo INT,
    IN p_clave     VARCHAR(50),
    IN p_valor     VARCHAR(255)
)
BEGIN
    INSERT INTO equipo_especificacion (id_equipo, clave, valor)
    VALUES (p_id_equipo, p_clave, p_valor);
END$$

-- Se usa al modificar: borrar todas y volver a insertar (en una transaccion desde BL)
CREATE PROCEDURE eliminar_especificaciones_equipo(
    IN p_id_equipo INT
)
BEGIN
    DELETE FROM equipo_especificacion
     WHERE id_equipo = p_id_equipo;
END$$

DELIMITER ;

-- ---------------------------------------------------------
-- CONSUMIBLE
-- ---------------------------------------------------------
DROP PROCEDURE IF EXISTS insertar_consumible;
DROP PROCEDURE IF EXISTS modificar_consumible;
DROP PROCEDURE IF EXISTS eliminar_consumible;
DROP PROCEDURE IF EXISTS listar_consumibles;
DROP PROCEDURE IF EXISTS buscar_consumible_por_id;
DROP PROCEDURE IF EXISTS buscar_consumibles_por_nombre;

DELIMITER $$

CREATE PROCEDURE insertar_consumible(
    OUT p_id               INT,
    IN  p_nombre_comercial VARCHAR(150),
    IN  p_marca            VARCHAR(100),
    IN  p_medida           VARCHAR(50)
)
BEGIN
INSERT INTO consumible (nombre_comercial, marca, medida)
VALUES (p_nombre_comercial, p_marca, p_medida);

SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_consumible(
    IN p_id               INT,
    IN p_nombre_comercial VARCHAR(150),
    IN p_marca            VARCHAR(100),
    IN p_medida           VARCHAR(50),
    IN p_activo           TINYINT
)
BEGIN
UPDATE consumible
SET nombre_comercial = p_nombre_comercial,
    marca            = p_marca,
    medida           = p_medida,
    activo           = p_activo
WHERE id_consumible = p_id;
END$$

-- Baja logica: bandejas y lineas de documentos lo referencian
CREATE PROCEDURE eliminar_consumible(
    IN p_id INT
)
BEGIN
UPDATE consumible
SET activo = 0
WHERE id_consumible = p_id;
END$$

CREATE PROCEDURE listar_consumibles()
BEGIN
SELECT id_consumible, nombre_comercial, marca, medida, activo
FROM consumible
WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_consumible_por_id(
    IN p_id INT
)
BEGIN
SELECT id_consumible, nombre_comercial, marca, medida, activo
FROM consumible
WHERE id_consumible = p_id;
END$$

-- Busqueda parcial: 'gasa' encuentra 'Gasa esteril 10x10'
CREATE PROCEDURE buscar_consumibles_por_nombre(
    IN p_nombre VARCHAR(150)
)
BEGIN
SELECT id_consumible, nombre_comercial, marca, medida, activo
FROM consumible
WHERE activo = 1
  AND nombre_comercial LIKE CONCAT('%', p_nombre, '%');
END$$

DELIMITER ;

-- ---------------------------------------------------------
-- BANDEJA INSTRUMENTAL
-- ---------------------------------------------------------
DROP PROCEDURE IF EXISTS insertar_bandeja;
DROP PROCEDURE IF EXISTS modificar_bandeja;
DROP PROCEDURE IF EXISTS eliminar_bandeja;
DROP PROCEDURE IF EXISTS listar_bandejas;
DROP PROCEDURE IF EXISTS buscar_bandeja_por_id;
DROP PROCEDURE IF EXISTS listar_consumibles_bandeja;
DROP PROCEDURE IF EXISTS insertar_consumible_bandeja;
DROP PROCEDURE IF EXISTS eliminar_consumibles_bandeja;

DELIMITER $$

CREATE PROCEDURE insertar_bandeja(
    OUT p_id           INT,
    IN  p_tipo         VARCHAR(100),
    IN  p_esterilizado TINYINT
)
BEGIN
INSERT INTO bandeja_instrumental (tipo, esterilizado)
VALUES (p_tipo, p_esterilizado);

SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_bandeja(
    IN p_id           INT,
    IN p_tipo         VARCHAR(100),
    IN p_esterilizado TINYINT,
    IN p_activo       TINYINT
)
BEGIN
UPDATE bandeja_instrumental
SET tipo         = p_tipo,
    esterilizado = p_esterilizado,
    activo       = p_activo
WHERE id_bandeja = p_id;
END$$

-- Baja logica: cirugia referencia a la bandeja
CREATE PROCEDURE eliminar_bandeja(
    IN p_id INT
)
BEGIN
UPDATE bandeja_instrumental
SET activo = 0
WHERE id_bandeja = p_id;
END$$

CREATE PROCEDURE listar_bandejas()
BEGIN
SELECT id_bandeja, tipo, esterilizado, activo
FROM bandeja_instrumental
WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_bandeja_por_id(
    IN p_id INT
)
BEGIN
SELECT id_bandeja, tipo, esterilizado, activo
FROM bandeja_instrumental
WHERE id_bandeja = p_id;
END$$

-- ---------------------------------------------------------
-- BANDEJA_CONSUMIBLE (detalle de BandejaInstrumental)
-- ---------------------------------------------------------

-- Trae los datos del consumible + sus cantidades en la bandeja
CREATE PROCEDURE listar_consumibles_bandeja(
    IN p_id_bandeja INT
)
BEGIN
SELECT c.id_consumible, c.nombre_comercial, c.marca, c.medida, c.activo,
       bc.cantidad_despachada, bc.cantidad_consumida
FROM bandeja_consumible bc
         JOIN consumible c ON c.id_consumible = bc.id_consumible
WHERE bc.id_bandeja = p_id_bandeja;
END$$

CREATE PROCEDURE insertar_consumible_bandeja(
    IN p_id_bandeja          INT,
    IN p_id_consumible       INT,
    IN p_cantidad_despachada INT,
    IN p_cantidad_consumida  INT
)
BEGIN
INSERT INTO bandeja_consumible
(id_bandeja, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES
    (p_id_bandeja, p_id_consumible, p_cantidad_despachada, p_cantidad_consumida);
END$$

-- Se usa al modificar: borrar todo el detalle y volver a insertarlo
CREATE PROCEDURE eliminar_consumibles_bandeja(
    IN p_id_bandeja INT
)
BEGIN
DELETE FROM bandeja_consumible
WHERE id_bandeja = p_id_bandeja;
END$$

DELIMITER ;