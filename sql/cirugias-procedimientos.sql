-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - CIRUGIAS, SOLICITUDES, AUDITORIA Y COTIZACION
-- Se puede ejecutar varias veces (DROP PROCEDURE IF EXISTS).
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS insertar_cirugia;
DROP PROCEDURE IF EXISTS modificar_cirugia;
DROP PROCEDURE IF EXISTS eliminar_cirugia;
DROP PROCEDURE IF EXISTS listar_cirugias;                    -- reemplazado por buscar_cirugias
DROP PROCEDURE IF EXISTS buscar_cirugia_por_id;
DROP PROCEDURE IF EXISTS cancelar_cirugia;
DROP PROCEDURE IF EXISTS listar_cirugias_por_estado;         -- reemplazado por buscar_cirugias
DROP PROCEDURE IF EXISTS listar_cirugias_por_rango_fechas;   -- reemplazado por buscar_cirugias
DROP PROCEDURE IF EXISTS buscar_cirugias;
DROP PROCEDURE IF EXISTS cambiar_estado_cirugia;
DROP PROCEDURE IF EXISTS marcar_consumo_completo;
DROP PROCEDURE IF EXISTS existe_cruce_equipo;
DROP PROCEDURE IF EXISTS bandeja_asignada_a_otra_cirugia;
DROP PROCEDURE IF EXISTS buscar_cirugia_vigente_por_solicitud;
DROP PROCEDURE IF EXISTS insertar_solicitud_cirugia;
DROP PROCEDURE IF EXISTS buscar_solicitudes;
DROP PROCEDURE IF EXISTS cambiar_estado_solicitud;
DROP PROCEDURE IF EXISTS insertar_auditoria_cirugia;
DROP PROCEDURE IF EXISTS buscar_auditoria_cirugia;
DROP PROCEDURE IF EXISTS insertar_cotizacion;
DROP PROCEDURE IF EXISTS modificar_cotizacion;
DROP PROCEDURE IF EXISTS eliminar_cotizacion;
DROP PROCEDURE IF EXISTS listar_cotizaciones;
DROP PROCEDURE IF EXISTS buscar_cotizacion_por_id;
DROP PROCEDURE IF EXISTS actualizar_estado_cotizacion;
DROP PROCEDURE IF EXISTS listar_cotizaciones_por_cirugia;

DELIMITER $$

-- ---------------------------------------------------------
-- REGLAS COMPARTIDAS (una sola definicion para BL, DAO y SQL)
-- ---------------------------------------------------------

-- Cruce de horario de un equipo con otra cirugia vigente. Si una cirugia
-- no tiene fecha de fin se asume una duracion de 4 horas.
CREATE PROCEDURE existe_cruce_equipo(
    IN  p_id_equipo        INT,
    IN  p_fecha_hora_inicio DATETIME,
    IN  p_fecha_hora_fin   DATETIME,
    IN  p_id_cirugia_excluir INT,
    OUT p_hay_cruce        TINYINT
)
BEGIN
    DECLARE v_fin DATETIME;
    SET v_fin = COALESCE(p_fecha_hora_fin, DATE_ADD(p_fecha_hora_inicio, INTERVAL 4 HOUR));

    SELECT EXISTS (
        SELECT 1
          FROM cirugia c
         WHERE c.id_equipo = p_id_equipo
           AND c.activo = 1
           AND c.estado IN ('PROGRAMADA', 'EN_PROCESO')
           AND (p_id_cirugia_excluir IS NULL OR c.id_cirugia <> p_id_cirugia_excluir)
           AND c.fecha_hora_inicio < v_fin
           AND COALESCE(c.fecha_hora_fin, DATE_ADD(c.fecha_hora_inicio, INTERVAL 4 HOUR)) > p_fecha_hora_inicio
    ) INTO p_hay_cruce;
END$$

-- Una bandeja fisica solo puede pertenecer a una cirugia.
CREATE PROCEDURE bandeja_asignada_a_otra_cirugia(
    IN  p_id_bandeja       INT,
    IN  p_id_cirugia_excluir INT,
    OUT p_asignada         TINYINT
)
BEGIN
    SELECT EXISTS (
        SELECT 1
          FROM cirugia c
         WHERE c.id_bandeja = p_id_bandeja
           AND (p_id_cirugia_excluir IS NULL OR c.id_cirugia <> p_id_cirugia_excluir)
    ) INTO p_asignada;
END$$

-- ---------------------------------------------------------
-- CIRUGIA
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_cirugia(
    OUT p_id INT,
    IN  p_fecha_hora_inicio DATETIME,
    IN  p_fecha_hora_fin DATETIME,
    IN  p_tipo_procedimiento VARCHAR(100),
    IN  p_doctor_nombre VARCHAR(100),
    IN  p_diagnostico VARCHAR(300),
    IN  p_motivo_cancelacion VARCHAR(200),
    IN  p_estado VARCHAR(20),
    IN  p_id_equipo INT,
    IN  p_id_bandeja INT,
    IN  p_id_clinica_hospital INT,
    IN  p_id_paciente_particular INT,
    IN  p_id_solicitud_origen INT,
    IN  p_activo TINYINT
)
BEGIN
    DECLARE v_ocupada TINYINT DEFAULT 0;
    DECLARE v_cruce TINYINT DEFAULT 0;

    IF p_id_bandeja IS NOT NULL THEN
        CALL bandeja_asignada_a_otra_cirugia(p_id_bandeja, NULL, v_ocupada);
        IF v_ocupada = 1 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La bandeja ya esta asignada a otra cirugia';
        END IF;
    END IF;

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
    IN p_id INT,
    IN p_fecha_hora_inicio DATETIME,
    IN p_fecha_hora_fin DATETIME,
    IN p_tipo_procedimiento VARCHAR(100),
    IN p_doctor_nombre VARCHAR(100),
    IN p_diagnostico VARCHAR(300),
    IN p_motivo_cancelacion VARCHAR(200),
    IN p_estado VARCHAR(20),
    IN p_id_equipo INT,
    IN p_id_bandeja INT,
    IN p_id_clinica_hospital INT,
    IN p_id_paciente_particular INT,
    IN p_activo TINYINT
)
BEGIN
    DECLARE v_ocupada TINYINT DEFAULT 0;
    DECLARE v_cruce TINYINT DEFAULT 0;

    -- la propia cirugia se excluye de ambas validaciones
    IF p_id_bandeja IS NOT NULL THEN
        CALL bandeja_asignada_a_otra_cirugia(p_id_bandeja, p_id, v_ocupada);
        IF v_ocupada = 1 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La bandeja ya esta asignada a otra cirugia';
        END IF;
    END IF;

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

-- Baja logica. Libera la bandeja fisica para que pueda usarse en otra cirugia.
CREATE PROCEDURE eliminar_cirugia(
    IN p_id INT
)
BEGIN
    UPDATE cirugia
       SET activo = 0,
           id_bandeja = NULL
     WHERE id_cirugia = p_id;
END$$

-- Cancelar tambien libera la bandeja fisica.
CREATE PROCEDURE cancelar_cirugia(
    IN p_id INT,
    IN p_motivo_cancelacion VARCHAR(200)
)
BEGIN
    UPDATE cirugia
       SET estado = 'CANCELADA',
           motivo_cancelacion = p_motivo_cancelacion,
           id_bandeja = NULL
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE cambiar_estado_cirugia(
    IN p_id INT,
    IN p_estado VARCHAR(20),
    IN p_fecha_hora_fin DATETIME
)
BEGIN
    UPDATE cirugia
       SET estado = p_estado,
           fecha_hora_fin = COALESCE(p_fecha_hora_fin, fecha_hora_fin)
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE marcar_consumo_completo(
    IN p_id INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM cirugia WHERE id_cirugia = p_id AND estado = 'FINALIZADA' AND activo = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede completar el consumo de una cirugia finalizada';
    END IF;

    UPDATE cirugia
       SET consumo_completo = 1
     WHERE id_cirugia = p_id;
END$$

CREATE PROCEDURE buscar_cirugia_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_cirugia, fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento,
           doctor_nombre, diagnostico, motivo_cancelacion, estado, id_equipo,
           id_bandeja, id_clinica_hospital, id_paciente_particular,
           id_solicitud_origen, consumo_completo, activo
      FROM cirugia
     WHERE id_cirugia = p_id;
END$$

-- Unica consulta de filtros (calendario, lista e historial). Todos los
-- parametros son opcionales: NULL = sin filtrar.
--   p_tipo_cliente: 'CLINICA_HOSPITAL' o 'PACIENTE_PARTICULAR'
--   p_id_cliente  : requiere p_tipo_cliente (los ids de ambas tablas se solapan)
CREATE PROCEDURE buscar_cirugias(
    IN p_desde        DATETIME,
    IN p_hasta        DATETIME,
    IN p_tipo_cliente VARCHAR(20),
    IN p_id_cliente   INT,
    IN p_id_equipo    INT,
    IN p_estado       VARCHAR(20)
)
BEGIN
    SELECT c.id_cirugia, c.fecha_hora_inicio, c.fecha_hora_fin, c.tipo_procedimiento,
           c.doctor_nombre, c.diagnostico, c.motivo_cancelacion, c.estado,
           c.id_equipo, c.id_bandeja, c.id_clinica_hospital, c.id_paciente_particular,
           c.id_solicitud_origen, c.consumo_completo, c.activo,
           CASE WHEN c.id_clinica_hospital IS NOT NULL THEN 'CLINICA_HOSPITAL'
                ELSE 'PACIENTE_PARTICULAR' END                       AS tipo_cliente,
           COALESCE(c.id_clinica_hospital, c.id_paciente_particular) AS id_cliente,
           COALESCE(ch.nombre, pp.nombre)                            AS cliente_nombre,
           e.nombre                                                  AS equipo_nombre,
           b.tipo                                                    AS bandeja_tipo,
           s.paciente_nombre                                         AS paciente_nombre,
           COALESCE(sch.nombre, ch.nombre)                           AS clinica_nombre,
           (SELECT k.precio_pactado FROM cotizacion k
             WHERE k.id_cirugia = c.id_cirugia AND k.activo = 1
             ORDER BY k.fecha_emision DESC, k.id_cotizacion DESC LIMIT 1) AS precio_pactado,
           (SELECT k.estado FROM cotizacion k
             WHERE k.id_cirugia = c.id_cirugia AND k.activo = 1
             ORDER BY k.fecha_emision DESC, k.id_cotizacion DESC LIMIT 1) AS estado_cotizacion
      FROM cirugia c
      LEFT JOIN clinica_hospital ch     ON ch.id_cliente = c.id_clinica_hospital
      LEFT JOIN paciente_particular pp  ON pp.id_cliente = c.id_paciente_particular
      LEFT JOIN equipo e                ON e.id_equipo = c.id_equipo
      LEFT JOIN bandeja_instrumental b  ON b.id_bandeja = c.id_bandeja
      LEFT JOIN solicitud_cirugia s     ON s.id_solicitud = c.id_solicitud_origen
      LEFT JOIN clinica_hospital sch    ON sch.id_cliente = s.id_clinica_hospital
     WHERE c.activo = 1
       AND (p_desde IS NULL OR c.fecha_hora_inicio >= p_desde)
       AND (p_hasta IS NULL OR c.fecha_hora_inicio <= p_hasta)
       AND (p_estado IS NULL OR c.estado = p_estado)
       AND (p_id_equipo IS NULL OR c.id_equipo = p_id_equipo)
       AND (p_tipo_cliente IS NULL
            OR (p_tipo_cliente = 'CLINICA_HOSPITAL'
                AND c.id_clinica_hospital IS NOT NULL
                AND (p_id_cliente IS NULL OR c.id_clinica_hospital = p_id_cliente))
            OR (p_tipo_cliente = 'PACIENTE_PARTICULAR'
                AND c.id_paciente_particular IS NOT NULL
                AND (p_id_cliente IS NULL OR c.id_paciente_particular = p_id_cliente)))
     ORDER BY c.fecha_hora_inicio, c.id_cirugia;
END$$

CREATE PROCEDURE buscar_cirugia_vigente_por_solicitud(
    IN p_id_solicitud INT
)
BEGIN
    SELECT id_cirugia
      FROM cirugia
     WHERE id_solicitud_origen = p_id_solicitud
       AND activo = 1
       AND estado <> 'CANCELADA';
END$$

-- ---------------------------------------------------------
-- SOLICITUD DE CIRUGIA
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_solicitud_cirugia(
    OUT p_id INT,
    IN  p_id_clinica_solicitante INT,
    IN  p_id_paciente_solicitante INT,
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
    INSERT INTO solicitud_cirugia (id_clinica_solicitante, id_paciente_solicitante, id_clinica_hospital,
                                   paciente_nombre, diagnostico, tipo_procedimiento, doctor_nombre,
                                   fecha_hora_solicitada, observaciones, estado_solicitud,
                                   fecha_registro, activo)
    VALUES (p_id_clinica_solicitante, p_id_paciente_solicitante, p_id_clinica_hospital,
            p_paciente_nombre, p_diagnostico, p_tipo_procedimiento, p_doctor_nombre,
            p_fecha_hora_solicitada, p_observaciones, 'POR_REVISAR', p_fecha_registro, 1);

    SET p_id = LAST_INSERT_ID();
END$$

-- Una sola consulta para buscar por id, listar y listar por cliente.
-- Todos los parametros son opcionales.
CREATE PROCEDURE buscar_solicitudes(
    IN p_id_solicitud INT,
    IN p_estado       VARCHAR(30),
    IN p_tipo_cliente VARCHAR(20),
    IN p_id_cliente   INT
)
BEGIN
    SELECT s.id_solicitud, s.id_clinica_solicitante, s.id_paciente_solicitante,
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
       AND (p_tipo_cliente IS NULL
            OR (p_tipo_cliente = 'CLINICA_HOSPITAL'
                AND s.id_clinica_solicitante IS NOT NULL
                AND (p_id_cliente IS NULL OR s.id_clinica_solicitante = p_id_cliente))
            OR (p_tipo_cliente = 'PACIENTE_PARTICULAR'
                AND s.id_paciente_solicitante IS NOT NULL
                AND (p_id_cliente IS NULL OR s.id_paciente_solicitante = p_id_cliente)))
     ORDER BY s.fecha_registro DESC, s.id_solicitud DESC;
END$$

CREATE PROCEDURE cambiar_estado_solicitud(
    IN p_id INT,
    IN p_estado VARCHAR(30),
    IN p_motivo_rechazo VARCHAR(200),
    IN p_fecha_revision DATETIME
)
BEGIN
    UPDATE solicitud_cirugia
       SET estado_solicitud = p_estado,
           motivo_rechazo = p_motivo_rechazo,
           fecha_revision = COALESCE(p_fecha_revision, fecha_revision)
     WHERE id_solicitud = p_id
       AND activo = 1;
END$$

-- ---------------------------------------------------------
-- AUDITORIA DE CIRUGIAS (RF16)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_auditoria_cirugia(
    OUT p_id INT,
    IN  p_id_cirugia INT,
    IN  p_fecha_hora DATETIME,
    IN  p_accion VARCHAR(40),
    IN  p_responsable VARCHAR(150),
    IN  p_detalle VARCHAR(500),
    IN  p_valores_anteriores TEXT,
    IN  p_valores_posteriores TEXT
)
BEGIN
    INSERT INTO auditoria_cirugia (id_cirugia, fecha_hora, accion, responsable, detalle,
                                   valores_anteriores, valores_posteriores)
    VALUES (p_id_cirugia, p_fecha_hora, p_accion, p_responsable, p_detalle,
            p_valores_anteriores, p_valores_posteriores);

    SET p_id = LAST_INSERT_ID();
END$$

-- Bitacora completa, por cirugia, detalle de una entrada y filtros: todo opcional.
CREATE PROCEDURE buscar_auditoria_cirugia(
    IN p_id_auditoria INT,
    IN p_id_cirugia   INT,
    IN p_desde        DATETIME,
    IN p_hasta        DATETIME,
    IN p_accion       VARCHAR(40),
    IN p_responsable  VARCHAR(150)
)
BEGIN
    SELECT id_auditoria, id_cirugia, fecha_hora, accion, responsable, detalle,
           valores_anteriores, valores_posteriores
      FROM auditoria_cirugia
     WHERE (p_id_auditoria IS NULL OR id_auditoria = p_id_auditoria)
       AND (p_id_cirugia IS NULL OR id_cirugia = p_id_cirugia)
       AND (p_desde IS NULL OR fecha_hora >= p_desde)
       AND (p_hasta IS NULL OR fecha_hora <= p_hasta)
       AND (p_accion IS NULL OR accion = p_accion)
       AND (p_responsable IS NULL OR responsable LIKE CONCAT('%', p_responsable, '%'))
     ORDER BY fecha_hora DESC, id_auditoria DESC;
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
