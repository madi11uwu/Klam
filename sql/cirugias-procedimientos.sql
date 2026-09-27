-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - ROL 3: CIRUGIAS Y COTIZACION
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS insertar_cirugia;
DROP PROCEDURE IF EXISTS modificar_cirugia;
DROP PROCEDURE IF EXISTS eliminar_cirugia;
DROP PROCEDURE IF EXISTS listar_cirugias;
DROP PROCEDURE IF EXISTS buscar_cirugia_por_id;
DROP PROCEDURE IF EXISTS cancelar_cirugia;
DROP PROCEDURE IF EXISTS listar_cirugias_por_estado;
DROP PROCEDURE IF EXISTS listar_cirugias_por_rango_fechas;
DROP PROCEDURE IF EXISTS insertar_cotizacion;
DROP PROCEDURE IF EXISTS modificar_cotizacion;
DROP PROCEDURE IF EXISTS eliminar_cotizacion;
DROP PROCEDURE IF EXISTS listar_cotizaciones;
DROP PROCEDURE IF EXISTS buscar_cotizacion_por_id;
DROP PROCEDURE IF EXISTS actualizar_estado_cotizacion;
DROP PROCEDURE IF EXISTS listar_cotizaciones_por_cirugia;

DELIMITER $$

-- ---------------------------------------------------------
-- CIRUGIA  (ROL 3)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_cirugia(
    OUT p_id INT,
    IN  p_fecha_hora_inicio DATETIME,
    IN  p_fecha_hora_fin DATETIME,
    IN  p_tipo_procedimiento VARCHAR(150),
    IN  p_doctor_nombre VARCHAR(150),
    IN  p_motivo_cancelacion VARCHAR(255),
    IN  p_estado VARCHAR(20),
    IN  p_id_equipo INT,
    IN  p_id_bandeja INT,
    IN  p_id_clinica_hospital INT,
    IN  p_id_paciente_particular INT,
    IN  p_activo TINYINT
)
BEGIN
    INSERT INTO cirugia (fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento, doctor_nombre, motivo_cancelacion, estado, id_equipo, id_bandeja, id_clinica_hospital, id_paciente_particular, activo)
    VALUES (p_fecha_hora_inicio, p_fecha_hora_fin, p_tipo_procedimiento, p_doctor_nombre, p_motivo_cancelacion, p_estado, p_id_equipo, p_id_bandeja, p_id_clinica_hospital, p_id_paciente_particular, p_activo);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_cirugia(
    IN p_id INT,
    IN p_fecha_hora_inicio DATETIME,
    IN p_fecha_hora_fin DATETIME,
    IN p_tipo_procedimiento VARCHAR(150),
    IN p_doctor_nombre VARCHAR(150),
    IN p_motivo_cancelacion VARCHAR(255),
    IN p_estado VARCHAR(20),
    IN p_id_equipo INT,
    IN p_id_bandeja INT,
    IN p_id_clinica_hospital INT,
    IN p_id_paciente_particular INT,
    IN p_activo TINYINT
)
BEGIN
    UPDATE cirugia
       SET fecha_hora_inicio = p_fecha_hora_inicio,
           fecha_hora_fin = p_fecha_hora_fin,
           tipo_procedimiento = p_tipo_procedimiento,
           doctor_nombre = p_doctor_nombre,
           motivo_cancelacion = p_motivo_cancelacion,
           estado = p_estado,
           id_equipo = p_id_equipo,
           id_bandeja = p_id_bandeja,
           id_clinica_hospital = p_id_clinica_hospital,
           id_paciente_particular = p_id_paciente_particular,
           activo = p_activo
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE eliminar_cirugia(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE cirugia
       SET activo = 0
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE listar_cirugias()
BEGIN
    SELECT id_cirugia, fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento,
           doctor_nombre, motivo_cancelacion, estado, id_equipo,
           id_bandeja, id_clinica_hospital, id_paciente_particular, activo
      FROM cirugia
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_cirugia_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_cirugia, fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento,
           doctor_nombre, motivo_cancelacion, estado, id_equipo,
           id_bandeja, id_clinica_hospital, id_paciente_particular, activo
      FROM cirugia
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE cancelar_cirugia(
    IN p_id INT,
    IN p_motivo_cancelacion VARCHAR(255)
)
BEGIN
    UPDATE cirugia
       SET estado = 'CANCELADA',
           motivo_cancelacion = p_motivo_cancelacion
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE listar_cirugias_por_estado(
    IN p_estado VARCHAR(20)
)
BEGIN
    SELECT id_cirugia, fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento,
           doctor_nombre, motivo_cancelacion, estado, id_equipo,
           id_bandeja, id_clinica_hospital, id_paciente_particular, activo
      FROM cirugia
     WHERE activo = 1 AND estado = p_estado;
END$$

CREATE PROCEDURE listar_cirugias_por_rango_fechas(
    IN p_desde DATETIME,
    IN p_hasta DATETIME
)
BEGIN
    SELECT id_cirugia, fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento,
           doctor_nombre, motivo_cancelacion, estado, id_equipo,
           id_bandeja, id_clinica_hospital, id_paciente_particular, activo
      FROM cirugia
     WHERE activo = 1
       AND fecha_hora_inicio BETWEEN p_desde AND p_hasta
     ORDER BY fecha_hora_inicio;
END$$

-- ---------------------------------------------------------
-- COTIZACION  (ROL 3)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_cotizacion(
    OUT p_id INT,
    IN  p_id_cirugia INT,
    IN  p_precio_pactado DECIMAL(10,2),
    IN  p_estado VARCHAR(20),
    IN  p_fecha_emision DATETIME,
    IN  p_activo TINYINT
)
BEGIN
    INSERT INTO cotizacion (id_cirugia, precio_pactado, estado, fecha_emision, activo)
    VALUES (p_id_cirugia, p_precio_pactado, p_estado, p_fecha_emision, p_activo);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_cotizacion(
    IN p_id INT,
    IN p_id_cirugia INT,
    IN p_precio_pactado DECIMAL(10,2),
    IN p_estado VARCHAR(20),
    IN p_fecha_emision DATETIME,
    IN p_activo TINYINT
)
BEGIN
    UPDATE cotizacion
       SET id_cirugia = p_id_cirugia,
           precio_pactado = p_precio_pactado,
           estado = p_estado,
           fecha_emision = p_fecha_emision,
           activo = p_activo
     WHERE id_cotizacion = p_id;
END$$

CREATE PROCEDURE eliminar_cotizacion(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE cotizacion
       SET activo = 0
     WHERE id_cotizacion = p_id;
END$$

CREATE PROCEDURE listar_cotizaciones()
BEGIN
    SELECT id_cotizacion, id_cirugia, precio_pactado, estado,
           fecha_emision, activo
      FROM cotizacion
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_cotizacion_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_cotizacion, id_cirugia, precio_pactado, estado,
           fecha_emision, activo
      FROM cotizacion
     WHERE id_cotizacion = p_id;
END$$

CREATE PROCEDURE actualizar_estado_cotizacion(
    IN p_id INT,
    IN p_estado VARCHAR(20)
)
BEGIN
    UPDATE cotizacion
       SET estado = p_estado
     WHERE id_cotizacion = p_id;
END$$

CREATE PROCEDURE listar_cotizaciones_por_cirugia(
    IN p_id_cirugia INT
)
BEGIN
    SELECT id_cotizacion, id_cirugia, precio_pactado, estado,
           fecha_emision, activo
      FROM cotizacion
     WHERE activo = 1 AND id_cirugia = p_id_cirugia;
END$$

DELIMITER ;
