-- Solo para bases antiguas SIN id_solicitud en cotizacion, despues de comparar
-- el esquema y respaldar. NO ejecutar despues de DDL_KLAM.sql: el DDL definitivo
-- ya contiene estas columnas, indices y procedimientos.
-- Ejecutar una sola vez; requiere MySQL 8 con CHECK y columnas generadas.
-- No ejecutado por esta entrega. Las filas históricas conservan id_cirugia.
USE `klam`;
ALTER TABLE cotizacion
    MODIFY id_cirugia INT NULL,
    ADD COLUMN id_solicitud INT NULL,
    ADD COLUMN version INT NOT NULL DEFAULT 1,
    ADD COLUMN vigente TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN archivo_path VARCHAR(255) NULL,
    ADD COLUMN fecha_decision DATETIME NULL,
    ADD COLUMN id_cuenta_decision INT NULL,
    ADD COLUMN motivo_rechazo VARCHAR(200) NULL,
    ADD COLUMN solicitud_vigente INT GENERATED ALWAYS AS
        (CASE WHEN vigente = 1 THEN id_solicitud ELSE NULL END) STORED,
    ADD UNIQUE KEY uq_cotizacion_solicitud_version (id_solicitud, version),
    ADD UNIQUE KEY uq_cotizacion_solicitud_vigente (solicitud_vigente),
    ADD CONSTRAINT fk_cotizacion_cuenta_decision FOREIGN KEY (id_cuenta_decision)
        REFERENCES cuenta_usuario(id_usuario) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_cotizacion_solicitud FOREIGN KEY (id_solicitud)
        REFERENCES solicitud_cirugia(id_solicitud) ON DELETE RESTRICT,
    ADD CONSTRAINT chk_cotizacion_origen CHECK
        ((id_cirugia IS NOT NULL AND id_solicitud IS NULL AND vigente = 0)
         OR (id_cirugia IS NULL AND id_solicitud IS NOT NULL
             AND archivo_path IS NOT NULL AND version > 0));

DROP PROCEDURE IF EXISTS listar_cotizaciones;
DROP PROCEDURE IF EXISTS buscar_cotizacion_por_id;
DROP PROCEDURE IF EXISTS listar_cotizaciones_por_cirugia;
DROP PROCEDURE IF EXISTS actualizar_estado_cotizacion;
DROP PROCEDURE IF EXISTS insertar_cotizacion_solicitud;
DROP PROCEDURE IF EXISTS listar_cotizaciones_por_solicitud;
DROP PROCEDURE IF EXISTS buscar_cotizacion_vigente_solicitud;
DELIMITER $$
CREATE PROCEDURE listar_cotizaciones()
BEGIN
    SELECT id_cotizacion, id_cirugia, id_solicitud, version, vigente, archivo_path, fecha_decision, id_cuenta_decision, motivo_rechazo,
           precio_pactado, estado, fecha_emision, activo
      FROM cotizacion WHERE activo = 1;
END$$
CREATE PROCEDURE buscar_cotizacion_por_id(IN p_id INT)
BEGIN
    SELECT id_cotizacion, id_cirugia, id_solicitud, version, vigente, archivo_path, fecha_decision, id_cuenta_decision, motivo_rechazo,
           precio_pactado, estado, fecha_emision, activo
      FROM cotizacion WHERE id_cotizacion = p_id;
END$$
CREATE PROCEDURE actualizar_estado_cotizacion(IN p_id INT, IN p_estado VARCHAR(20))
BEGIN
    UPDATE cotizacion SET estado = p_estado
     WHERE id_cotizacion = p_id
       AND id_solicitud IS NULL;
END$$
CREATE PROCEDURE listar_cotizaciones_por_cirugia(IN p_id_cirugia INT)
BEGIN
    SELECT id_cotizacion, id_cirugia, id_solicitud, version, vigente, archivo_path, fecha_decision, id_cuenta_decision, motivo_rechazo,
           precio_pactado, estado, fecha_emision, activo
      FROM cotizacion WHERE activo = 1 AND id_cirugia = p_id_cirugia;
END$$
CREATE PROCEDURE insertar_cotizacion_solicitud(
    IN p_id_solicitud INT,
    IN p_precio_pactado DECIMAL(10,2),
    IN p_fecha_emision DATETIME,
    IN p_archivo_path VARCHAR(255),
    OUT p_id INT,
    OUT p_version INT
)
BEGIN
    DECLARE v_estado VARCHAR(32);
    DECLARE v_clinica INT;
    DECLARE v_version INT DEFAULT 0;
    DECLARE v_estado_vigente VARCHAR(20);
    SELECT estado_solicitud, id_clinica_solicitante INTO v_estado, v_clinica
      FROM solicitud_cirugia WHERE id_solicitud = p_id_solicitud FOR UPDATE;
    IF v_estado IS NULL OR v_estado <> 'APROBADA' OR v_clinica IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La solicitud clínica debe estar aprobada';
    END IF;
    IF p_archivo_path IS NULL OR TRIM(p_archivo_path) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El archivo de cotización es obligatorio';
    END IF;
    SELECT COALESCE(MAX(version), 0) INTO v_version
      FROM cotizacion WHERE id_solicitud = p_id_solicitud;
    SELECT MAX(CASE WHEN vigente = 1 THEN estado END) INTO v_estado_vigente
      FROM cotizacion WHERE id_solicitud = p_id_solicitud;
    IF v_estado_vigente IN ('EMITIDA', 'ACEPTADA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cotización vigente no permite reemplazo';
    END IF;
    UPDATE cotizacion SET vigente = 0 WHERE id_solicitud = p_id_solicitud AND vigente = 1;
    INSERT INTO cotizacion (id_solicitud, version, vigente, archivo_path, precio_pactado, estado, fecha_emision, activo)
    VALUES (p_id_solicitud, v_version + 1, 1, p_archivo_path, p_precio_pactado, 'EMITIDA', p_fecha_emision, 1);
    SET p_id = LAST_INSERT_ID();
    SET p_version = v_version + 1;
END$$

CREATE PROCEDURE listar_cotizaciones_por_solicitud(IN p_id_solicitud INT)
BEGIN
    SELECT id_cotizacion, id_cirugia, id_solicitud, version, vigente, archivo_path, fecha_decision, id_cuenta_decision, motivo_rechazo,
           precio_pactado, estado, fecha_emision, activo
      FROM cotizacion WHERE id_solicitud = p_id_solicitud ORDER BY version DESC;
END$$

CREATE PROCEDURE buscar_cotizacion_vigente_solicitud(IN p_id_solicitud INT)
BEGIN
    SELECT id_cotizacion, id_cirugia, id_solicitud, version, vigente, archivo_path, fecha_decision, id_cuenta_decision, motivo_rechazo,
           precio_pactado, estado, fecha_emision, activo
      FROM cotizacion WHERE id_solicitud = p_id_solicitud AND vigente = 1;
END$$
DELIMITER ;
