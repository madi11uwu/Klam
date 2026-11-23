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
DROP PROCEDURE IF EXISTS listar_documentos_origen;
DROP PROCEDURE IF EXISTS siguiente_version_documento;
DROP PROCEDURE IF EXISTS insertar_auditoria_documental;
DROP PROCEDURE IF EXISTS buscar_auditoria_documental;

DELIMITER $$

CREATE PROCEDURE insertar_orden_compra(
    OUT p_id INT,
    IN  p_archivo_respaldo_path VARCHAR(255),
    IN  p_fecha_recepcion DATETIME,
    IN  p_activo TINYINT
)
BEGIN
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

CREATE PROCEDURE insertar_documento_ingreso(
    OUT p_id INT,
    IN  p_tipo_documento VARCHAR(20),
    IN  p_archivo_path VARCHAR(255),
    IN  p_estado_validacion VARCHAR(20),
    IN  p_fecha_carga DATETIME,
    IN  p_fecha_validacion DATETIME,
    IN  p_motivo_rechazo VARCHAR(200),
    IN  p_version INT,
    IN  p_responsable VARCHAR(150),
    IN  p_id_solicitud INT,
    IN  p_id_cirugia INT,
    IN  p_id_orden_compra INT,
    IN  p_id_administrador INT,
    IN  p_id_vendedor INT,
    IN  p_id_tecnico INT,
    IN  p_id_cuenta_usuario INT,
    IN  p_activo TINYINT
)
BEGIN
    INSERT INTO documento_ingreso (tipo_documento, archivo_path, estado_validacion, fecha_carga,
                                   fecha_validacion, motivo_rechazo, habilitar_resubida, version, responsable,
                                   id_solicitud, id_cirugia, id_orden_compra,
                                   id_administrador, id_vendedor, id_tecnico, id_cuenta_usuario, activo)
    VALUES (p_tipo_documento, p_archivo_path, p_estado_validacion, p_fecha_carga,
            p_fecha_validacion, p_motivo_rechazo, 0, p_version, p_responsable,
            p_id_solicitud, p_id_cirugia, p_id_orden_compra,
            p_id_administrador, p_id_vendedor, p_id_tecnico, p_id_cuenta_usuario, p_activo);

    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_documento_ingreso(
    IN p_id INT,
    IN p_tipo_documento VARCHAR(20),
    IN p_archivo_path VARCHAR(255),
    IN p_estado_validacion VARCHAR(20),
    IN p_fecha_carga DATETIME,
    IN p_fecha_validacion DATETIME,
    IN p_motivo_rechazo VARCHAR(200),
    IN p_version INT,
    IN p_responsable VARCHAR(150),
    IN p_id_solicitud INT,
    IN p_id_cirugia INT,
    IN p_id_orden_compra INT,
    IN p_id_administrador INT,
    IN p_id_vendedor INT,
    IN p_id_tecnico INT,
    IN p_id_cuenta_usuario INT,
    IN p_activo TINYINT
)
BEGIN
    UPDATE documento_ingreso
       SET tipo_documento = p_tipo_documento,
           archivo_path = p_archivo_path,
           estado_validacion = p_estado_validacion,
           fecha_carga = p_fecha_carga,
           fecha_validacion = p_fecha_validacion,
           motivo_rechazo = p_motivo_rechazo,
           version = p_version,
           responsable = p_responsable,
           id_solicitud = p_id_solicitud,
           id_cirugia = p_id_cirugia,
           id_orden_compra = p_id_orden_compra,
           id_administrador = p_id_administrador,
           id_vendedor = p_id_vendedor,
           id_tecnico = p_id_tecnico,
           id_cuenta_usuario = p_id_cuenta_usuario,
           activo = p_activo
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE eliminar_documento_ingreso(
    IN p_id INT
)
BEGIN
    UPDATE documento_ingreso
       SET activo = 0
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE listar_documentos_ingreso()
BEGIN
    SELECT id_documento, tipo_documento, archivo_path, estado_validacion,
           fecha_carga, fecha_validacion, motivo_rechazo, habilitar_resubida, version, responsable,
           id_solicitud, id_cirugia, id_orden_compra, id_administrador,
           id_vendedor, id_tecnico, id_cuenta_usuario, activo
      FROM documento_ingreso
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_documento_ingreso_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_documento, tipo_documento, archivo_path, estado_validacion,
           fecha_carga, fecha_validacion, motivo_rechazo, habilitar_resubida, version, responsable,
           id_solicitud, id_cirugia, id_orden_compra, id_administrador,
           id_vendedor, id_tecnico, id_cuenta_usuario, activo
      FROM documento_ingreso
     WHERE id_documento = p_id;
END$$

CREATE PROCEDURE actualizar_estado_validacion_documento(
    IN p_id INT,
    IN p_estado_validacion VARCHAR(20),
    IN p_fecha_validacion DATETIME,
    IN p_motivo_rechazo VARCHAR(200),
    IN p_responsable VARCHAR(150),
    IN p_habilitar_resubida TINYINT
)
BEGIN
    IF p_habilitar_resubida IS NULL OR p_habilitar_resubida NOT IN (0, 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La autorizacion de resubida debe ser 0 o 1';
    END IF;
    IF p_habilitar_resubida = 1 AND (p_estado_validacion IS NULL OR p_estado_validacion <> 'RECHAZADO') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se autoriza resubida al rechazar';
    END IF;

    IF p_estado_validacion = 'RECHAZADO'
       AND (p_motivo_rechazo IS NULL OR CHAR_LENGTH(TRIM(p_motivo_rechazo)) = 0) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El rechazo de un documento exige un motivo';
    END IF;

    UPDATE documento_ingreso
       SET estado_validacion = p_estado_validacion,
           fecha_validacion = p_fecha_validacion,
           motivo_rechazo = p_motivo_rechazo,
           responsable = COALESCE(p_responsable, responsable),
           habilitar_resubida = p_habilitar_resubida
     WHERE id_documento = p_id
       AND activo = 1;
END$$

CREATE PROCEDURE listar_documentos_origen(
    IN p_id_solicitud   INT,
    IN p_id_cirugia     INT,
    IN p_solo_vigentes  TINYINT
)
BEGIN
    SELECT id_documento, tipo_documento, archivo_path, estado_validacion,
           fecha_carga, fecha_validacion, motivo_rechazo, habilitar_resubida, version, responsable,
           id_solicitud, id_cirugia, id_orden_compra, id_administrador,
           id_vendedor, id_tecnico, id_cuenta_usuario, activo
      FROM documento_ingreso d
     WHERE d.activo = 1
       AND ((p_id_solicitud IS NOT NULL AND d.id_solicitud = p_id_solicitud)
         OR (p_id_cirugia IS NOT NULL AND d.id_cirugia = p_id_cirugia))
       AND (p_solo_vigentes = 0
            OR d.version = (SELECT MAX(d2.version)
                              FROM documento_ingreso d2
                             WHERE d2.activo = 1
                               AND d2.tipo_documento = d.tipo_documento
                               AND d2.id_solicitud <=> d.id_solicitud
                               AND d2.id_cirugia   <=> d.id_cirugia))
     ORDER BY d.fecha_carga, d.id_documento;
END$$

CREATE PROCEDURE siguiente_version_documento(
    IN  p_id_solicitud INT,
    IN  p_id_cirugia   INT,
    IN  p_tipo_documento VARCHAR(20),
    OUT p_version INT
)
BEGIN
    SELECT COALESCE(MAX(version), 0) + 1 INTO p_version
      FROM documento_ingreso
     WHERE tipo_documento = p_tipo_documento
       AND id_solicitud <=> p_id_solicitud
       AND id_cirugia   <=> p_id_cirugia;
END$$

CREATE PROCEDURE insertar_auditoria_documental(
    IN p_id_documento INT, IN p_id_solicitud INT, IN p_id_cirugia INT,
    IN p_tipo_documento VARCHAR(40), IN p_accion VARCHAR(40),
    IN p_responsable VARCHAR(150), IN p_rol_responsable VARCHAR(40),
    IN p_id_usuario_responsable INT, IN p_estado_anterior VARCHAR(40),
    IN p_estado_nuevo VARCHAR(40), IN p_descripcion VARCHAR(255)
)
BEGIN
    INSERT INTO auditoria_documental
        (id_documento, id_solicitud, id_cirugia, tipo_documento, accion,
         responsable, rol_responsable, id_usuario_responsable,
         estado_anterior, estado_nuevo, descripcion)
    VALUES
        (p_id_documento, p_id_solicitud, p_id_cirugia, p_tipo_documento, p_accion,
         p_responsable, p_rol_responsable, p_id_usuario_responsable,
         p_estado_anterior, p_estado_nuevo, p_descripcion);
END$$

CREATE PROCEDURE buscar_auditoria_documental(
    IN p_id_evento INT, IN p_cliente VARCHAR(150), IN p_paciente VARCHAR(150),
    IN p_desde DATETIME, IN p_hasta DATETIME,
    IN p_tipo_documento VARCHAR(40), IN p_accion VARCHAR(40),
    IN p_responsable VARCHAR(150), IN p_id_solicitud INT, IN p_id_cirugia INT,
    IN p_limite INT, IN p_desplazamiento INT
)
BEGIN
    SELECT a.id_evento, a.id_documento, a.id_solicitud, a.id_cirugia,
           a.fecha_hora, a.tipo_documento, a.accion, a.responsable,
           a.rol_responsable, a.id_usuario_responsable,
           a.estado_anterior, a.estado_nuevo, a.descripcion,
           d.archivo_path,
           COALESCE(cs.nombre, ps.nombre, cc.nombre, pc.nombre) AS cliente,
           COALESCE(s.paciente_nombre, c.paciente_nombre) AS paciente
      FROM auditoria_documental a
      LEFT JOIN documento_ingreso d ON d.id_documento = a.id_documento
      LEFT JOIN solicitud_cirugia s ON s.id_solicitud = a.id_solicitud
      LEFT JOIN cirugia c ON c.id_cirugia = a.id_cirugia
      LEFT JOIN clinica_hospital cs ON cs.id_cliente = s.id_clinica_solicitante
      LEFT JOIN paciente_particular ps ON ps.id_cliente = s.id_paciente_solicitante
      LEFT JOIN clinica_hospital cc ON cc.id_cliente = c.id_clinica_hospital
      LEFT JOIN paciente_particular pc ON pc.id_cliente = c.id_paciente_particular
     WHERE (p_id_evento IS NULL OR a.id_evento = p_id_evento)
       AND (p_cliente IS NULL OR COALESCE(cs.nombre, ps.nombre, cc.nombre, pc.nombre)
            LIKE CONCAT('%', p_cliente, '%'))
       AND (p_paciente IS NULL OR COALESCE(s.paciente_nombre, c.paciente_nombre)
            LIKE CONCAT('%', p_paciente, '%'))
       AND (p_desde IS NULL OR a.fecha_hora >= p_desde)
       AND (p_hasta IS NULL OR a.fecha_hora <= p_hasta)
       AND (p_tipo_documento IS NULL OR a.tipo_documento = p_tipo_documento)
       AND (p_accion IS NULL OR a.accion = p_accion)
       AND (p_responsable IS NULL OR a.responsable LIKE CONCAT('%', p_responsable, '%'))
       AND (p_id_solicitud IS NULL OR a.id_solicitud = p_id_solicitud
            OR EXISTS (SELECT 1 FROM cirugia sx
                       WHERE sx.id_cirugia = a.id_cirugia
                         AND sx.id_solicitud_origen = p_id_solicitud))
       AND (p_id_cirugia IS NULL OR a.id_cirugia = p_id_cirugia
            OR EXISTS (SELECT 1 FROM cirugia cx
                       WHERE cx.id_cirugia = p_id_cirugia
                         AND cx.id_solicitud_origen = a.id_solicitud))
     ORDER BY a.fecha_hora DESC, a.id_evento DESC
     LIMIT p_limite OFFSET p_desplazamiento;
END$$

DELIMITER ;
