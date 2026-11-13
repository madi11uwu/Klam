-- Ejecutar sobre la base BIOKLAM seleccionada, antes de desplegar el backend actualizado.
-- Conserva las cirugías y sus consumibles; cambia solo la exclusividad de id_bandeja.
-- Respaldar primero el esquema y comprobar las definiciones vigentes de estos cuatro procedimientos.

SET @indice_plantilla = IF(
    EXISTS (SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cirugia'
              AND INDEX_NAME = 'idx_cirugia_bandeja'),
    'SELECT 1', 'ALTER TABLE cirugia ADD INDEX idx_cirugia_bandeja (id_bandeja)');
PREPARE migracion_indice FROM @indice_plantilla;
EXECUTE migracion_indice;
DEALLOCATE PREPARE migracion_indice;

SET @quitar_exclusividad = IF(
    EXISTS (SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cirugia'
              AND INDEX_NAME = 'uq_cirugia_bandeja' AND NON_UNIQUE = 0),
    'ALTER TABLE cirugia DROP INDEX uq_cirugia_bandeja', 'SELECT 1');
PREPARE migracion_exclusividad FROM @quitar_exclusividad;
EXECUTE migracion_exclusividad;
DEALLOCATE PREPARE migracion_exclusividad;

DROP PROCEDURE IF EXISTS insertar_cirugia;
DROP PROCEDURE IF EXISTS modificar_cirugia;
DROP PROCEDURE IF EXISTS eliminar_cirugia;
DROP PROCEDURE IF EXISTS cancelar_cirugia;
DELIMITER $$

CREATE PROCEDURE insertar_cirugia(
    OUT p_id INT, IN p_fecha_hora_inicio DATETIME, IN p_fecha_hora_fin DATETIME,
    IN p_tipo_procedimiento VARCHAR(100), IN p_doctor_nombre VARCHAR(100),
    IN p_diagnostico VARCHAR(300), IN p_motivo_cancelacion VARCHAR(200),
    IN p_estado VARCHAR(20), IN p_id_equipo INT, IN p_id_bandeja INT,
    IN p_id_clinica_hospital INT, IN p_id_paciente_particular INT,
    IN p_id_solicitud_origen INT, IN p_activo TINYINT
)
BEGIN
    DECLARE v_cruce TINYINT DEFAULT 0;
    IF p_id_equipo IS NOT NULL AND p_estado IN ('PROGRAMADA', 'EN_PROCESO') THEN
        CALL existe_cruce_equipo(p_id_equipo, p_fecha_hora_inicio, p_fecha_hora_fin, NULL, v_cruce);
        IF v_cruce = 1 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El equipo ya esta asignado a otra cirugia en ese horario';
        END IF;
    END IF;
    INSERT INTO cirugia (fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento, doctor_nombre, diagnostico,
                         motivo_cancelacion, estado, id_equipo, id_bandeja, id_clinica_hospital,
                         id_paciente_particular, id_solicitud_origen, activo)
    VALUES (p_fecha_hora_inicio, p_fecha_hora_fin, p_tipo_procedimiento, p_doctor_nombre, p_diagnostico,
            p_motivo_cancelacion, p_estado, p_id_equipo, p_id_bandeja, p_id_clinica_hospital,
            p_id_paciente_particular, p_id_solicitud_origen, p_activo);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_cirugia(
    IN p_id INT, IN p_fecha_hora_inicio DATETIME, IN p_fecha_hora_fin DATETIME,
    IN p_tipo_procedimiento VARCHAR(100), IN p_doctor_nombre VARCHAR(100),
    IN p_diagnostico VARCHAR(300), IN p_motivo_cancelacion VARCHAR(200),
    IN p_estado VARCHAR(20), IN p_id_equipo INT, IN p_id_bandeja INT,
    IN p_id_clinica_hospital INT, IN p_id_paciente_particular INT, IN p_activo TINYINT
)
BEGIN
    DECLARE v_cruce TINYINT DEFAULT 0;
    IF p_id_equipo IS NOT NULL AND p_activo = 1 AND p_estado IN ('PROGRAMADA', 'EN_PROCESO') THEN
        CALL existe_cruce_equipo(p_id_equipo, p_fecha_hora_inicio, p_fecha_hora_fin, p_id, v_cruce);
        IF v_cruce = 1 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El equipo ya esta asignado a otra cirugia en ese horario';
        END IF;
    END IF;
    UPDATE cirugia
       SET fecha_hora_inicio = p_fecha_hora_inicio,
           fecha_hora_fin = p_fecha_hora_fin,
           tipo_procedimiento = p_tipo_procedimiento,
           doctor_nombre = p_doctor_nombre,
           diagnostico = p_diagnostico,
           motivo_cancelacion = p_motivo_cancelacion,
           estado = p_estado,
           id_equipo = p_id_equipo,
           id_bandeja = p_id_bandeja,
           id_clinica_hospital = p_id_clinica_hospital,
           id_paciente_particular = p_id_paciente_particular,
           activo = p_activo
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE eliminar_cirugia(IN p_id INT)
BEGIN
    UPDATE cirugia SET activo = 0 WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE cancelar_cirugia(IN p_id INT, IN p_motivo_cancelacion VARCHAR(200))
BEGIN
    UPDATE cirugia SET estado = 'CANCELADA', motivo_cancelacion = p_motivo_cancelacion
     WHERE id_cirugia = p_id;
END$$
DELIMITER ;
