-- RF08: asociar la solicitud con el vendedor que la registró.
-- Aplicar solo tras respaldar datos/procedimientos y detener escrituras REST.
-- Las solicitudes anteriores conservan NULL; no se infiere el vendedor desde documentos.
USE `klam`;

ALTER TABLE `solicitud_cirugia`
    ADD COLUMN `id_vendedor_registrador` INT NULL AFTER `id_paciente_solicitante`,
    ADD KEY `idx_solicitud_vendedor_registrador` (`id_vendedor_registrador`),
    ADD CONSTRAINT `fk_solicitud_vendedor_registrador`
        FOREIGN KEY (`id_vendedor_registrador`) REFERENCES `vendedor` (`id_usuario`)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

DELIMITER $$

DROP PROCEDURE IF EXISTS insertar_solicitud_cirugia$$
CREATE PROCEDURE insertar_solicitud_cirugia(
    OUT p_id INT,
    IN  p_id_clinica_solicitante INT,
    IN  p_id_paciente_solicitante INT,
    IN  p_id_vendedor_registrador INT,
    IN  p_id_clinica_hospital INT,
    IN  p_paciente_nombre VARCHAR(150),
    IN  p_diagnostico VARCHAR(300),
    IN  p_tipo_procedimiento VARCHAR(100),
    IN  p_doctor_nombre VARCHAR(100),
    IN  p_fecha_hora_solicitada DATETIME,
    IN  p_observaciones VARCHAR(500),
    IN  p_fecha_registro DATETIME
)
BEGIN
    INSERT INTO solicitud_cirugia (id_clinica_solicitante, id_paciente_solicitante,
                                   id_vendedor_registrador, id_clinica_hospital,
                                   paciente_nombre, diagnostico, tipo_procedimiento, doctor_nombre,
                                   fecha_hora_solicitada, observaciones, estado_solicitud,
                                   fecha_registro, activo)
    VALUES (p_id_clinica_solicitante, p_id_paciente_solicitante,
            p_id_vendedor_registrador, p_id_clinica_hospital,
            p_paciente_nombre, p_diagnostico, p_tipo_procedimiento, p_doctor_nombre,
            p_fecha_hora_solicitada, p_observaciones, 'POR_REVISAR', p_fecha_registro, 1);

    SET p_id = LAST_INSERT_ID();
END$$

DROP PROCEDURE IF EXISTS buscar_solicitudes$$
CREATE PROCEDURE buscar_solicitudes(
    IN p_id_solicitud INT,
    IN p_estado       VARCHAR(30),
    IN p_tipo_cliente VARCHAR(20),
    IN p_id_cliente   INT,
    IN p_id_vendedor_registrador INT
)
BEGIN
    SELECT s.id_solicitud, s.id_clinica_solicitante, s.id_paciente_solicitante,
           s.id_vendedor_registrador,
           s.id_clinica_hospital, s.paciente_nombre, s.diagnostico, s.tipo_procedimiento,
           s.doctor_nombre, s.fecha_hora_solicitada, s.observaciones, s.estado_solicitud,
           s.motivo_rechazo, s.fecha_registro, s.fecha_revision, s.activo,
           CASE WHEN s.id_clinica_solicitante IS NOT NULL THEN 'CLINICA_HOSPITAL'
                ELSE 'PACIENTE_PARTICULAR' END                           AS tipo_cliente,
           COALESCE(s.id_clinica_solicitante, s.id_paciente_solicitante) AS id_cliente,
           COALESCE(cs.nombre, ps.nombre)                                AS cliente_nombre,
           ps.dni                                                        AS dni,
           cd.nombre                                                     AS clinica_nombre
      FROM solicitud_cirugia s
      LEFT JOIN clinica_hospital cs    ON cs.id_cliente = s.id_clinica_solicitante
      LEFT JOIN paciente_particular ps ON ps.id_cliente = s.id_paciente_solicitante
      JOIN clinica_hospital cd         ON cd.id_cliente = s.id_clinica_hospital
     WHERE s.activo = 1
       AND (p_id_solicitud IS NULL OR s.id_solicitud = p_id_solicitud)
       AND (p_estado IS NULL OR s.estado_solicitud = p_estado)
       AND (p_id_vendedor_registrador IS NULL
            OR s.id_vendedor_registrador = p_id_vendedor_registrador)
       AND (p_tipo_cliente IS NULL
            OR (p_tipo_cliente = 'CLINICA_HOSPITAL'
                AND s.id_clinica_solicitante IS NOT NULL
                AND (p_id_cliente IS NULL OR s.id_clinica_solicitante = p_id_cliente))
            OR (p_tipo_cliente = 'PACIENTE_PARTICULAR'
                AND s.id_paciente_solicitante IS NOT NULL
                AND (p_id_cliente IS NULL OR s.id_paciente_solicitante = p_id_cliente)))
     ORDER BY s.fecha_registro DESC, s.id_solicitud DESC;
END$$

DELIMITER ;
