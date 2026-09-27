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
