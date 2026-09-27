-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - ROL 5: GESTION DOCUMENTAL DE INGRESO
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS insertar_orden_compra;
DROP PROCEDURE IF EXISTS modificar_orden_compra;
DROP PROCEDURE IF EXISTS eliminar_orden_compra;
DROP PROCEDURE IF EXISTS listar_ordenes_compra;
DROP PROCEDURE IF EXISTS buscar_orden_compra_por_id;
DROP PROCEDURE IF EXISTS insertar_linea_orden_compra;
DROP PROCEDURE IF EXISTS eliminar_lineas_por_orden_compra;
DROP PROCEDURE IF EXISTS listar_lineas_por_orden_compra;
DROP PROCEDURE IF EXISTS insertar_documento_ingreso;
DROP PROCEDURE IF EXISTS modificar_documento_ingreso;
DROP PROCEDURE IF EXISTS eliminar_documento_ingreso;
DROP PROCEDURE IF EXISTS listar_documentos_ingreso;
DROP PROCEDURE IF EXISTS buscar_documento_ingreso_por_id;
DROP PROCEDURE IF EXISTS actualizar_estado_validacion_documento;
DROP PROCEDURE IF EXISTS listar_documentos_por_orden_compra;
DROP PROCEDURE IF EXISTS listar_documentos_por_estado;

DELIMITER $$

-- ---------------------------------------------------------
-- ORDEN_COMPRA  (ROL 5)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_orden_compra(
    OUT p_id INT,
    IN  p_archivo_respaldo_path VARCHAR(255),
    IN  p_fecha_recepcion DATETIME,
    IN  p_activo TINYINT
)
BEGIN
    -- id_orden_compra no es AUTO_INCREMENT: se genera el siguiente id
    SELECT COALESCE(MAX(id_orden_compra), 0) + 1 INTO p_id FROM orden_compra;

    INSERT INTO orden_compra (id_orden_compra, archivo_respaldo_path, fecha_recepcion, activo)
    VALUES (p_id, p_archivo_respaldo_path, p_fecha_recepcion, p_activo);
END$$

CREATE PROCEDURE modificar_orden_compra(
    IN p_id INT,
    IN p_archivo_respaldo_path VARCHAR(255),
    IN p_fecha_recepcion DATETIME,
    IN p_activo TINYINT
)
BEGIN
    UPDATE orden_compra
       SET archivo_respaldo_path = p_archivo_respaldo_path,
           fecha_recepcion = p_fecha_recepcion,
           activo = p_activo
     WHERE id_orden_compra = p_id;
END$$

CREATE PROCEDURE eliminar_orden_compra(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE orden_compra
       SET activo = 0
     WHERE id_orden_compra = p_id;
END$$

CREATE PROCEDURE listar_ordenes_compra()
BEGIN
    SELECT id_orden_compra, archivo_respaldo_path, fecha_recepcion, activo
      FROM orden_compra
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_orden_compra_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_orden_compra, archivo_respaldo_path, fecha_recepcion, activo
      FROM orden_compra
     WHERE id_orden_compra = p_id;
END$$

-- ---------------------------------------------------------
-- LINEA_ORDEN_COMPRA  (detalle de orden_compra, ROL 5)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_linea_orden_compra(
    OUT p_id INT,
    IN  p_id_orden_compra INT,
    IN  p_id_consumible INT,
    IN  p_item_referencia VARCHAR(50),
    IN  p_descripcion VARCHAR(150),
    IN  p_cantidad INT,
    IN  p_precio_unitario DECIMAL(10,2)
)
BEGIN
    INSERT INTO linea_orden_compra (id_orden_compra, id_consumible, item_referencia, descripcion, cantidad, precio_unitario)
    VALUES (p_id_orden_compra, p_id_consumible, p_item_referencia, p_descripcion, p_cantidad, p_precio_unitario);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE eliminar_lineas_por_orden_compra(
    IN p_id_orden_compra INT
)
BEGIN
    -- Se usa al modificar: borrar todas las lineas y volver a insertarlas
    DELETE FROM linea_orden_compra
     WHERE id_orden_compra = p_id_orden_compra;
END$$

CREATE PROCEDURE listar_lineas_por_orden_compra(
    IN p_id_orden_compra INT
)
BEGIN
    SELECT id_linea, id_orden_compra, id_consumible, item_referencia,
           descripcion, cantidad, precio_unitario
      FROM linea_orden_compra
     WHERE id_orden_compra = p_id_orden_compra;
END$$

-- ---------------------------------------------------------
-- DOCUMENTO_INGRESO  (ROL 5)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_documento_ingreso(
    OUT p_id INT,
    IN  p_tipo_documento VARCHAR(20),
    IN  p_archivo_path VARCHAR(255),
    IN  p_estado_validacion VARCHAR(20),
    IN  p_fecha_carga DATETIME,
    IN  p_id_orden_compra INT,
    IN  p_id_administrador INT,
    IN  p_id_vendedor INT,
    IN  p_id_tecnico INT,
    IN  p_activo TINYINT
)
BEGIN
    -- id_documento no es AUTO_INCREMENT: se genera el siguiente id
    SELECT COALESCE(MAX(id_documento), 0) + 1 INTO p_id FROM documento_ingreso;

    INSERT INTO documento_ingreso (id_documento, tipo_documento, archivo_path, estado_validacion, fecha_carga, id_orden_compra, id_administrador, id_vendedor, id_tecnico, activo)
    VALUES (p_id, p_tipo_documento, p_archivo_path, p_estado_validacion, p_fecha_carga, p_id_orden_compra, p_id_administrador, p_id_vendedor, p_id_tecnico, p_activo);
END$$

CREATE PROCEDURE modificar_documento_ingreso(
    IN p_id INT,
    IN p_tipo_documento VARCHAR(20),
    IN p_archivo_path VARCHAR(255),
    IN p_estado_validacion VARCHAR(20),
    IN p_fecha_carga DATETIME,
    IN p_id_orden_compra INT,
    IN p_id_administrador INT,
    IN p_id_vendedor INT,
    IN p_id_tecnico INT,
    IN p_activo TINYINT
)
BEGIN
    UPDATE documento_ingreso
       SET tipo_documento = p_tipo_documento,
           archivo_path = p_archivo_path,
           estado_validacion = p_estado_validacion,
           fecha_carga = p_fecha_carga,
           id_orden_compra = p_id_orden_compra,
           id_administrador = p_id_administrador,
           id_vendedor = p_id_vendedor,
           id_tecnico = p_id_tecnico,
           activo = p_activo
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE eliminar_documento_ingreso(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE documento_ingreso
       SET activo = 0
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE listar_documentos_ingreso()
BEGIN
    SELECT id_documento, tipo_documento, archivo_path, estado_validacion,
           fecha_carga, id_orden_compra, id_administrador, id_vendedor,
           id_tecnico, activo
      FROM documento_ingreso
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_documento_ingreso_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_documento, tipo_documento, archivo_path, estado_validacion,
           fecha_carga, id_orden_compra, id_administrador, id_vendedor,
           id_tecnico, activo
      FROM documento_ingreso
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE actualizar_estado_validacion_documento(
    IN p_id INT,
    IN p_estado_validacion VARCHAR(20)
)
BEGIN
    UPDATE documento_ingreso
       SET estado_validacion = p_estado_validacion
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE listar_documentos_por_orden_compra(
    IN p_id_orden_compra INT
)
BEGIN
    SELECT id_documento, tipo_documento, archivo_path, estado_validacion,
           fecha_carga, id_orden_compra, id_administrador, id_vendedor,
           id_tecnico, activo
      FROM documento_ingreso
     WHERE activo = 1 AND id_orden_compra = p_id_orden_compra;
END$$

CREATE PROCEDURE listar_documentos_por_estado(
    IN p_estado_validacion VARCHAR(20)
)
BEGIN
    SELECT id_documento, tipo_documento, archivo_path, estado_validacion,
           fecha_carga, id_orden_compra, id_administrador, id_vendedor,
           id_tecnico, activo
      FROM documento_ingreso
     WHERE activo = 1 AND estado_validacion = p_estado_validacion;
END$$

DELIMITER ;
