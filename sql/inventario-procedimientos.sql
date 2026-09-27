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

-- =========================================================
-- PRUEBAS (ejecutar a mano en Workbench)
-- =========================================================
-- CALL insertar_equipo(@id, 'Craneotomo prueba', 'CRANEOTOMO', 1);
-- SELECT @id;
-- CALL insertar_especificacion_equipo(@id, 'marca', 'Medtronic');
-- CALL insertar_especificacion_equipo(@id, 'voltaje', '220V');
-- CALL buscar_equipo_por_id(@id);
-- CALL listar_especificaciones_equipo(@id);
-- CALL modificar_equipo(@id, 'Craneotomo editado', 'OTRO', 0, 1);
-- CALL eliminar_especificaciones_equipo(@id);
-- CALL eliminar_equipo(@id);
-- CALL listar_equipos();
