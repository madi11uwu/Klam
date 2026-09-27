-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - ROL 6: FACTURACION Y NOTAS DE CREDITO
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS insertar_factura;
DROP PROCEDURE IF EXISTS modificar_factura;
DROP PROCEDURE IF EXISTS eliminar_factura;
DROP PROCEDURE IF EXISTS listar_facturas;
DROP PROCEDURE IF EXISTS buscar_factura_por_id;
DROP PROCEDURE IF EXISTS anular_factura;
DROP PROCEDURE IF EXISTS actualizar_estado_pago_factura;
DROP PROCEDURE IF EXISTS listar_facturas_por_cirugia;
DROP PROCEDURE IF EXISTS insertar_linea_factura;
DROP PROCEDURE IF EXISTS eliminar_lineas_por_factura;
DROP PROCEDURE IF EXISTS listar_lineas_por_factura;
DROP PROCEDURE IF EXISTS insertar_boleta;
DROP PROCEDURE IF EXISTS modificar_boleta;
DROP PROCEDURE IF EXISTS eliminar_boleta;
DROP PROCEDURE IF EXISTS listar_boletas;
DROP PROCEDURE IF EXISTS buscar_boleta_por_id;
DROP PROCEDURE IF EXISTS anular_boleta;
DROP PROCEDURE IF EXISTS actualizar_estado_pago_boleta;
DROP PROCEDURE IF EXISTS listar_boletas_por_cirugia;
DROP PROCEDURE IF EXISTS insertar_linea_boleta;
DROP PROCEDURE IF EXISTS eliminar_lineas_por_boleta;
DROP PROCEDURE IF EXISTS listar_lineas_por_boleta;
DROP PROCEDURE IF EXISTS insertar_nota_credito;
DROP PROCEDURE IF EXISTS modificar_nota_credito;
DROP PROCEDURE IF EXISTS eliminar_nota_credito;
DROP PROCEDURE IF EXISTS listar_notas_credito;
DROP PROCEDURE IF EXISTS buscar_nota_credito_por_id;
DROP PROCEDURE IF EXISTS listar_notas_credito_por_factura;
DROP PROCEDURE IF EXISTS listar_notas_credito_por_boleta;
DROP PROCEDURE IF EXISTS insertar_linea_nota_credito;
DROP PROCEDURE IF EXISTS eliminar_lineas_por_nota_credito;
DROP PROCEDURE IF EXISTS listar_lineas_por_nota_credito;

DELIMITER $$

-- ---------------------------------------------------------
-- FACTURA  (ROL 6)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_factura(
    OUT p_id INT,
    IN  p_id_cirugia INT,
    IN  p_fecha_emision DATETIME,
    IN  p_monto_base DECIMAL(10,2),
    IN  p_tasa_igv DECIMAL(5,4),
    IN  p_igv DECIMAL(10,2),
    IN  p_monto_total DECIMAL(10,2),
    IN  p_estado_pago VARCHAR(20),
    IN  p_ruc_receptor CHAR(11),
    IN  p_activo TINYINT
)
BEGIN
    INSERT INTO factura (id_cirugia, fecha_emision, monto_base, tasa_igv, igv, monto_total, estado_pago, ruc_receptor, activo)
    VALUES (p_id_cirugia, p_fecha_emision, p_monto_base, p_tasa_igv, p_igv, p_monto_total, p_estado_pago, p_ruc_receptor, p_activo);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_factura(
    IN p_id INT,
    IN p_id_cirugia INT,
    IN p_fecha_emision DATETIME,
    IN p_monto_base DECIMAL(10,2),
    IN p_tasa_igv DECIMAL(5,4),
    IN p_igv DECIMAL(10,2),
    IN p_monto_total DECIMAL(10,2),
    IN p_estado_pago VARCHAR(20),
    IN p_ruc_receptor CHAR(11),
    IN p_activo TINYINT
)
BEGIN
    UPDATE factura
       SET id_cirugia = p_id_cirugia,
           fecha_emision = p_fecha_emision,
           monto_base = p_monto_base,
           tasa_igv = p_tasa_igv,
           igv = p_igv,
           monto_total = p_monto_total,
           estado_pago = p_estado_pago,
           ruc_receptor = p_ruc_receptor,
           activo = p_activo
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE eliminar_factura(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE factura
       SET activo = 0
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE listar_facturas()
BEGIN
    SELECT id_documento, id_cirugia, fecha_emision, monto_base,
           tasa_igv, igv, monto_total, estado_pago,
           ruc_receptor, activo
      FROM factura
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_factura_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_documento, id_cirugia, fecha_emision, monto_base,
           tasa_igv, igv, monto_total, estado_pago,
           ruc_receptor, activo
      FROM factura
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE anular_factura(
    IN p_id INT
)
BEGIN
    UPDATE factura
       SET estado_pago = 'ANULADO'
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE actualizar_estado_pago_factura(
    IN p_id INT,
    IN p_estado_pago VARCHAR(20)
)
BEGIN
    UPDATE factura
       SET estado_pago = p_estado_pago
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE listar_facturas_por_cirugia(
    IN p_id_cirugia INT
)
BEGIN
    SELECT id_documento, id_cirugia, fecha_emision, monto_base,
           tasa_igv, igv, monto_total, estado_pago,
           ruc_receptor, activo
      FROM factura
     WHERE activo = 1 AND id_cirugia = p_id_cirugia;
END$$

-- ---------------------------------------------------------
-- LINEA_FACTURA  (detalle de factura, ROL 6)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_linea_factura(
    OUT p_id INT,
    IN  p_id_documento INT,
    IN  p_id_consumible INT,
    IN  p_item_referencia VARCHAR(50),
    IN  p_descripcion VARCHAR(150),
    IN  p_cantidad INT,
    IN  p_precio_unitario DECIMAL(10,2)
)
BEGIN
    INSERT INTO linea_factura (id_documento, id_consumible, item_referencia, descripcion, cantidad, precio_unitario)
    VALUES (p_id_documento, p_id_consumible, p_item_referencia, p_descripcion, p_cantidad, p_precio_unitario);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE eliminar_lineas_por_factura(
    IN p_id_documento INT
)
BEGIN
    -- Se usa al modificar: borrar todas las lineas y volver a insertarlas
    DELETE FROM linea_factura
     WHERE id_documento = p_id_documento;
END$$

CREATE PROCEDURE listar_lineas_por_factura(
    IN p_id_documento INT
)
BEGIN
    SELECT id_linea, id_documento, id_consumible, item_referencia,
           descripcion, cantidad, precio_unitario
      FROM linea_factura
     WHERE id_documento = p_id_documento;
END$$

-- ---------------------------------------------------------
-- BOLETA  (ROL 6)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_boleta(
    OUT p_id INT,
    IN  p_id_cirugia INT,
    IN  p_fecha_emision DATETIME,
    IN  p_monto_base DECIMAL(10,2),
    IN  p_tasa_igv DECIMAL(5,4),
    IN  p_igv DECIMAL(10,2),
    IN  p_monto_total DECIMAL(10,2),
    IN  p_estado_pago VARCHAR(20),
    IN  p_dni_receptor CHAR(8),
    IN  p_activo TINYINT
)
BEGIN
    INSERT INTO boleta (id_cirugia, fecha_emision, monto_base, tasa_igv, igv, monto_total, estado_pago, dni_receptor, activo)
    VALUES (p_id_cirugia, p_fecha_emision, p_monto_base, p_tasa_igv, p_igv, p_monto_total, p_estado_pago, p_dni_receptor, p_activo);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_boleta(
    IN p_id INT,
    IN p_id_cirugia INT,
    IN p_fecha_emision DATETIME,
    IN p_monto_base DECIMAL(10,2),
    IN p_tasa_igv DECIMAL(5,4),
    IN p_igv DECIMAL(10,2),
    IN p_monto_total DECIMAL(10,2),
    IN p_estado_pago VARCHAR(20),
    IN p_dni_receptor CHAR(8),
    IN p_activo TINYINT
)
BEGIN
    UPDATE boleta
       SET id_cirugia = p_id_cirugia,
           fecha_emision = p_fecha_emision,
           monto_base = p_monto_base,
           tasa_igv = p_tasa_igv,
           igv = p_igv,
           monto_total = p_monto_total,
           estado_pago = p_estado_pago,
           dni_receptor = p_dni_receptor,
           activo = p_activo
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE eliminar_boleta(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE boleta
       SET activo = 0
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE listar_boletas()
BEGIN
    SELECT id_documento, id_cirugia, fecha_emision, monto_base,
           tasa_igv, igv, monto_total, estado_pago,
           dni_receptor, activo
      FROM boleta
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_boleta_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_documento, id_cirugia, fecha_emision, monto_base,
           tasa_igv, igv, monto_total, estado_pago,
           dni_receptor, activo
      FROM boleta
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE anular_boleta(
    IN p_id INT
)
BEGIN
    UPDATE boleta
       SET estado_pago = 'ANULADO'
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE actualizar_estado_pago_boleta(
    IN p_id INT,
    IN p_estado_pago VARCHAR(20)
)
BEGIN
    UPDATE boleta
       SET estado_pago = p_estado_pago
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE listar_boletas_por_cirugia(
    IN p_id_cirugia INT
)
BEGIN
    SELECT id_documento, id_cirugia, fecha_emision, monto_base,
           tasa_igv, igv, monto_total, estado_pago,
           dni_receptor, activo
      FROM boleta
     WHERE activo = 1 AND id_cirugia = p_id_cirugia;
END$$

-- ---------------------------------------------------------
-- LINEA_BOLETA  (detalle de boleta, ROL 6)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_linea_boleta(
    OUT p_id INT,
    IN  p_id_documento INT,
    IN  p_id_consumible INT,
    IN  p_item_referencia VARCHAR(50),
    IN  p_descripcion VARCHAR(150),
    IN  p_cantidad INT,
    IN  p_precio_unitario DECIMAL(10,2)
)
BEGIN
    INSERT INTO linea_boleta (id_documento, id_consumible, item_referencia, descripcion, cantidad, precio_unitario)
    VALUES (p_id_documento, p_id_consumible, p_item_referencia, p_descripcion, p_cantidad, p_precio_unitario);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE eliminar_lineas_por_boleta(
    IN p_id_documento INT
)
BEGIN
    -- Se usa al modificar: borrar todas las lineas y volver a insertarlas
    DELETE FROM linea_boleta
     WHERE id_documento = p_id_documento;
END$$

CREATE PROCEDURE listar_lineas_por_boleta(
    IN p_id_documento INT
)
BEGIN
    SELECT id_linea, id_documento, id_consumible, item_referencia,
           descripcion, cantidad, precio_unitario
      FROM linea_boleta
     WHERE id_documento = p_id_documento;
END$$

-- ---------------------------------------------------------
-- NOTA_CREDITO  (ROL 6)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_nota_credito(
    OUT p_id INT,
    IN  p_id_factura_original INT,
    IN  p_id_boleta_original INT,
    IN  p_motivo VARCHAR(255),
    IN  p_fecha_emision DATETIME,
    IN  p_monto_total DECIMAL(10,2),
    IN  p_activo TINYINT
)
BEGIN
    INSERT INTO nota_credito (id_factura_original, id_boleta_original, motivo, fecha_emision, monto_total, activo)
    VALUES (p_id_factura_original, p_id_boleta_original, p_motivo, p_fecha_emision, p_monto_total, p_activo);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_nota_credito(
    IN p_id INT,
    IN p_id_factura_original INT,
    IN p_id_boleta_original INT,
    IN p_motivo VARCHAR(255),
    IN p_fecha_emision DATETIME,
    IN p_monto_total DECIMAL(10,2),
    IN p_activo TINYINT
)
BEGIN
    UPDATE nota_credito
       SET id_factura_original = p_id_factura_original,
           id_boleta_original = p_id_boleta_original,
           motivo = p_motivo,
           fecha_emision = p_fecha_emision,
           monto_total = p_monto_total,
           activo = p_activo
     WHERE id_nota_credito = p_id;
END$$

CREATE PROCEDURE eliminar_nota_credito(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE nota_credito
       SET activo = 0
     WHERE id_nota_credito = p_id;
END$$

CREATE PROCEDURE listar_notas_credito()
BEGIN
    SELECT id_nota_credito, id_factura_original, id_boleta_original, motivo,
           fecha_emision, monto_total, activo
      FROM nota_credito
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_nota_credito_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_nota_credito, id_factura_original, id_boleta_original, motivo,
           fecha_emision, monto_total, activo
      FROM nota_credito
     WHERE id_nota_credito = p_id;
END$$

CREATE PROCEDURE listar_notas_credito_por_factura(
    IN p_id_factura INT
)
BEGIN
    SELECT id_nota_credito, id_factura_original, id_boleta_original, motivo,
           fecha_emision, monto_total, activo
      FROM nota_credito
     WHERE activo = 1 AND id_factura_original = p_id_factura;
END$$

CREATE PROCEDURE listar_notas_credito_por_boleta(
    IN p_id_boleta INT
)
BEGIN
    SELECT id_nota_credito, id_factura_original, id_boleta_original, motivo,
           fecha_emision, monto_total, activo
      FROM nota_credito
     WHERE activo = 1 AND id_boleta_original = p_id_boleta;
END$$

-- ---------------------------------------------------------
-- LINEA_NOTA_CREDITO  (detalle de nota_credito, ROL 6)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_linea_nota_credito(
    OUT p_id INT,
    IN  p_id_nota_credito INT,
    IN  p_id_consumible INT,
    IN  p_item_referencia VARCHAR(50),
    IN  p_descripcion VARCHAR(150),
    IN  p_cantidad INT,
    IN  p_precio_unitario DECIMAL(10,2),
    IN  p_motivo_devolucion VARCHAR(255)
)
BEGIN
    INSERT INTO linea_nota_credito (id_nota_credito, id_consumible, item_referencia, descripcion, cantidad, precio_unitario, motivo_devolucion)
    VALUES (p_id_nota_credito, p_id_consumible, p_item_referencia, p_descripcion, p_cantidad, p_precio_unitario, p_motivo_devolucion);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE eliminar_lineas_por_nota_credito(
    IN p_id_nota_credito INT
)
BEGIN
    -- Se usa al modificar: borrar todas las lineas y volver a insertarlas
    DELETE FROM linea_nota_credito
     WHERE id_nota_credito = p_id_nota_credito;
END$$

CREATE PROCEDURE listar_lineas_por_nota_credito(
    IN p_id_nota_credito INT
)
BEGIN
    SELECT id_linea, id_nota_credito, id_consumible, item_referencia,
           descripcion, cantidad, precio_unitario, motivo_devolucion
      FROM linea_nota_credito
     WHERE id_nota_credito = p_id_nota_credito;
END$$

DELIMITER ;
