-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - INVENTARIO (EXTRA)
-- Bandejas disponibles, composicion de bandejas, consumos (RF15),
-- despacho y retorno (RF14).
-- Se puede ejecutar varias veces (DROP PROCEDURE IF EXISTS).
-- =========================================================
USE `klam`;

-- Nombres anteriores (SP_*), reemplazados por los de este script
DROP PROCEDURE IF EXISTS SP_EQUIPO_LISTAR_DISPONIBLES;
DROP PROCEDURE IF EXISTS SP_EQUIPO_REGISTRAR_RETORNO;
DROP PROCEDURE IF EXISTS SP_BANDEJA_ASOCIAR_CONSUMIBLE;
DROP PROCEDURE IF EXISTS SP_BANDEJA_LISTAR_CONSUMIBLES;
DROP PROCEDURE IF EXISTS SP_BANDEJA_REGISTRAR_CONSUMO;
DROP PROCEDURE IF EXISTS SP_LOGISTICA_DESPACHOS_PENDIENTES;
DROP PROCEDURE IF EXISTS SP_LOGISTICA_CIRUGIAS_PARA_CONSUMO;

DROP PROCEDURE IF EXISTS listar_equipos_disponibles;
DROP PROCEDURE IF EXISTS asociar_consumible_bandeja;
DROP PROCEDURE IF EXISTS actualizar_cantidad_despachada;
DROP PROCEDURE IF EXISTS quitar_consumible_bandeja;
DROP PROCEDURE IF EXISTS listar_bandejas_por_procedimiento;
DROP PROCEDURE IF EXISTS listar_bandejas_disponibles;
DROP PROCEDURE IF EXISTS buscar_cirugia_de_bandeja;
DROP PROCEDURE IF EXISTS bandeja_tiene_consumo;
DROP PROCEDURE IF EXISTS registrar_consumo_bandeja;
DROP PROCEDURE IF EXISTS buscar_consumos;
DROP PROCEDURE IF EXISTS insertar_despacho;
DROP PROCEDURE IF EXISTS registrar_retorno_despacho;
DROP PROCEDURE IF EXISTS actualizar_disponibilidad_equipo;
DROP PROCEDURE IF EXISTS buscar_despachos;

DELIMITER $$

-- ================= EQUIPOS =================
CREATE PROCEDURE listar_equipos_disponibles()
BEGIN
    SELECT id_equipo, nombre, categoria, disponible, activo
      FROM equipo
     WHERE disponible = 1 AND activo = 1;
END$$

CREATE PROCEDURE actualizar_disponibilidad_equipo(
    IN p_id_equipo INT,
    IN p_disponible TINYINT
)
BEGIN
    UPDATE equipo
       SET disponible = p_disponible
     WHERE id_equipo = p_id_equipo;
END$$

-- ================= BANDEJAS =================

-- Bandejas activas, esterilizadas y NO asignadas a ninguna cirugia.
-- p_procedimiento es opcional (NULL = todos los procedimientos).
CREATE PROCEDURE listar_bandejas_disponibles(
    IN p_procedimiento VARCHAR(100)
)
BEGIN
    SELECT b.id_bandeja, b.tipo, b.procedimiento, b.esterilizado, b.activo
      FROM bandeja_instrumental b
     WHERE b.activo = 1
       AND b.esterilizado = 1
       AND (p_procedimiento IS NULL OR b.procedimiento = p_procedimiento)
       AND NOT EXISTS (SELECT 1 FROM cirugia c WHERE c.id_bandeja = b.id_bandeja)
     ORDER BY b.id_bandeja;
END$$

CREATE PROCEDURE listar_bandejas_por_procedimiento(
    IN p_procedimiento VARCHAR(100)
)
BEGIN
    SELECT id_bandeja, tipo, procedimiento, esterilizado, activo
      FROM bandeja_instrumental
     WHERE activo = 1 AND procedimiento = p_procedimiento
     ORDER BY id_bandeja;
END$$

-- Cirugia a la que esta asignada una bandeja fisica (0 o 1 fila)
CREATE PROCEDURE buscar_cirugia_de_bandeja(
    IN p_id_bandeja INT
)
BEGIN
    SELECT id_cirugia, estado, consumo_completo, activo
      FROM cirugia
     WHERE id_bandeja = p_id_bandeja;
END$$

-- La composicion despachada no se puede modificar si ya hay consumo registrado
CREATE PROCEDURE bandeja_tiene_consumo(
    IN  p_id_bandeja INT,
    OUT p_tiene TINYINT
)
BEGIN
    SELECT (EXISTS (SELECT 1 FROM bandeja_consumible
                     WHERE id_bandeja = p_id_bandeja AND cantidad_consumida > 0)
            OR EXISTS (SELECT 1 FROM cirugia
                        WHERE id_bandeja = p_id_bandeja AND consumo_completo = 1))
      INTO p_tiene;
END$$

-- ================= CONSUMIBLES DE LA BANDEJA =================
CREATE PROCEDURE asociar_consumible_bandeja(
    IN p_id_bandeja INT,
    IN p_id_consumible INT,
    IN p_cantidad_despachada INT
)
BEGIN
    DECLARE v_consumo TINYINT DEFAULT 0;
    CALL bandeja_tiene_consumo(p_id_bandeja, v_consumo);
    IF v_consumo = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede modificar la composicion de una bandeja con consumo registrado';
    END IF;
    IF p_cantidad_despachada IS NULL OR p_cantidad_despachada <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad despachada debe ser mayor que cero';
    END IF;

    -- si el consumible ya esta en la bandeja la PK lo rechaza (no se duplica)
    INSERT INTO bandeja_consumible (id_bandeja, id_consumible, cantidad_despachada, cantidad_consumida)
    VALUES (p_id_bandeja, p_id_consumible, p_cantidad_despachada, 0);
END$$

CREATE PROCEDURE actualizar_cantidad_despachada(
    IN p_id_bandeja INT,
    IN p_id_consumible INT,
    IN p_cantidad_despachada INT
)
BEGIN
    DECLARE v_consumo TINYINT DEFAULT 0;
    CALL bandeja_tiene_consumo(p_id_bandeja, v_consumo);
    IF v_consumo = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede modificar la composicion de una bandeja con consumo registrado';
    END IF;
    IF p_cantidad_despachada IS NULL OR p_cantidad_despachada <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad despachada debe ser mayor que cero';
    END IF;

    UPDATE bandeja_consumible
       SET cantidad_despachada = p_cantidad_despachada
     WHERE id_bandeja = p_id_bandeja AND id_consumible = p_id_consumible;
END$$

CREATE PROCEDURE quitar_consumible_bandeja(
    IN p_id_bandeja INT,
    IN p_id_consumible INT
)
BEGIN
    DECLARE v_consumo TINYINT DEFAULT 0;
    CALL bandeja_tiene_consumo(p_id_bandeja, v_consumo);
    IF v_consumo = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede modificar la composicion de una bandeja con consumo registrado';
    END IF;

    DELETE FROM bandeja_consumible
     WHERE id_bandeja = p_id_bandeja AND id_consumible = p_id_consumible;
END$$

-- ================= CONSUMO REAL (RF15) =================
-- Validaciones de datos tambien aqui (ademas de la BL): cirugia finalizada,
-- consumo no cerrado, consumible de la bandeja y 0 <= consumido <= despachado.
CREATE PROCEDURE registrar_consumo_bandeja(
    IN p_id_bandeja INT,
    IN p_id_consumible INT,
    IN p_cantidad_consumida INT
)
BEGIN
    DECLARE v_estado VARCHAR(20);
    DECLARE v_completo TINYINT;
    DECLARE v_despachada INT;

    SELECT estado, consumo_completo INTO v_estado, v_completo
      FROM cirugia
     WHERE id_bandeja = p_id_bandeja AND activo = 1;

    IF v_estado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La bandeja no esta asignada a ninguna cirugia';
    END IF;
    IF v_estado <> 'FINALIZADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El consumo solo se registra en cirugias finalizadas';
    END IF;
    IF v_completo = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El consumo de la cirugia ya fue marcado como completo';
    END IF;

    SELECT cantidad_despachada INTO v_despachada
      FROM bandeja_consumible
     WHERE id_bandeja = p_id_bandeja AND id_consumible = p_id_consumible;

    IF v_despachada IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El consumible no pertenece a la bandeja';
    END IF;
    IF p_cantidad_consumida IS NULL OR p_cantidad_consumida < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad consumida no puede ser negativa';
    END IF;
    IF p_cantidad_consumida > v_despachada THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cantidad consumida no puede superar la despachada';
    END IF;

    UPDATE bandeja_consumible
       SET cantidad_consumida = p_cantidad_consumida
     WHERE id_bandeja = p_id_bandeja AND id_consumible = p_id_consumible;
END$$

-- Despachado / consumido / diferencia por consumible. Filtros opcionales:
-- cirugia, cliente (tipo + id) y rango de fechas de la cirugia.
-- Solo cirugias FINALIZADAS (flujo de cierre).
CREATE PROCEDURE buscar_consumos(
    IN p_id_cirugia   INT,
    IN p_tipo_cliente VARCHAR(20),
    IN p_id_cliente   INT,
    IN p_desde        DATETIME,
    IN p_hasta        DATETIME
)
BEGIN
    SELECT c.id_cirugia, c.fecha_hora_inicio, c.fecha_hora_fin, c.tipo_procedimiento,
           c.doctor_nombre, c.consumo_completo,
           CASE WHEN c.id_clinica_hospital IS NOT NULL THEN 'CLINICA_HOSPITAL'
                ELSE 'PACIENTE_PARTICULAR' END                       AS tipo_cliente,
           COALESCE(c.id_clinica_hospital, c.id_paciente_particular) AS id_cliente,
           COALESCE(ch.nombre, pp.nombre)                            AS cliente_nombre,
           c.id_bandeja,
           k.id_consumible, k.nombre_comercial, k.medida,
           bc.cantidad_despachada, bc.cantidad_consumida,
           (bc.cantidad_despachada - bc.cantidad_consumida)          AS diferencia
      FROM cirugia c
      JOIN bandeja_consumible bc       ON bc.id_bandeja = c.id_bandeja
      JOIN consumible k                ON k.id_consumible = bc.id_consumible
      LEFT JOIN clinica_hospital ch    ON ch.id_cliente = c.id_clinica_hospital
      LEFT JOIN paciente_particular pp ON pp.id_cliente = c.id_paciente_particular
     WHERE c.activo = 1
       AND c.estado = 'FINALIZADA'
       AND (p_id_cirugia IS NULL OR c.id_cirugia = p_id_cirugia)
       AND (p_desde IS NULL OR c.fecha_hora_inicio >= p_desde)
       AND (p_hasta IS NULL OR c.fecha_hora_inicio <= p_hasta)
       AND (p_tipo_cliente IS NULL
            OR (p_tipo_cliente = 'CLINICA_HOSPITAL'
                AND c.id_clinica_hospital IS NOT NULL
                AND (p_id_cliente IS NULL OR c.id_clinica_hospital = p_id_cliente))
            OR (p_tipo_cliente = 'PACIENTE_PARTICULAR'
                AND c.id_paciente_particular IS NOT NULL
                AND (p_id_cliente IS NULL OR c.id_paciente_particular = p_id_cliente)))
     ORDER BY c.fecha_hora_inicio, c.id_cirugia, k.nombre_comercial;
END$$

-- ================= DESPACHO Y RETORNO (RF14) =================
-- Solo se despachan los recursos ASOCIADOS a la cirugia. El equipo debe estar
-- disponible; la bandeja debe ser la de esa cirugia; no hay doble despacho.
CREATE PROCEDURE insertar_despacho(
    OUT p_id INT,
    IN  p_id_cirugia INT,
    IN  p_id_equipo INT,
    IN  p_id_bandeja INT,
    IN  p_fecha_salida DATETIME
)
BEGIN
    DECLARE v_estado VARCHAR(20);
    DECLARE v_activo TINYINT;
    DECLARE v_equipo_cirugia INT;
    DECLARE v_bandeja_cirugia INT;
    DECLARE v_disponible TINYINT;
    DECLARE v_equipo_activo TINYINT;

    SELECT estado, activo, id_equipo, id_bandeja
      INTO v_estado, v_activo, v_equipo_cirugia, v_bandeja_cirugia
      FROM cirugia
     WHERE id_cirugia = p_id_cirugia;

    IF v_estado IS NULL OR v_activo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cirugia no existe o esta dada de baja';
    END IF;
    IF v_estado NOT IN ('PROGRAMADA', 'EN_PROCESO') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se despacha para cirugias programadas o en curso';
    END IF;
    IF p_id_equipo IS NULL AND p_id_bandeja IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Debe despachar al menos un equipo o una bandeja';
    END IF;
    IF p_id_equipo IS NOT NULL AND (v_equipo_cirugia IS NULL OR v_equipo_cirugia <> p_id_equipo) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El equipo no esta asociado a la cirugia';
    END IF;
    IF p_id_bandeja IS NOT NULL AND (v_bandeja_cirugia IS NULL OR v_bandeja_cirugia <> p_id_bandeja) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La bandeja esta asignada a otra cirugia o a ninguna';
    END IF;

    IF p_id_equipo IS NOT NULL THEN
        SELECT disponible, activo INTO v_disponible, v_equipo_activo
          FROM equipo
         WHERE id_equipo = p_id_equipo;
        IF v_disponible IS NULL OR v_disponible = 0 OR v_equipo_activo = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El equipo no esta disponible';
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM despacho
                WHERE id_cirugia = p_id_cirugia AND fecha_retorno IS NULL AND activo = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La cirugia ya tiene un despacho pendiente de retorno';
    END IF;

    INSERT INTO despacho (codigo, id_cirugia, id_equipo, id_bandeja, fecha_salida, estado, activo)
    VALUES (CONCAT('T', RIGHT(UUID_SHORT(), 15)), p_id_cirugia, p_id_equipo, p_id_bandeja, p_fecha_salida, 'DESPACHADO', 1);

    SET p_id = LAST_INSERT_ID();

    UPDATE despacho
       SET codigo = CONCAT('DSP-', LPAD(p_id, 4, '0'))
     WHERE id_despacho = p_id;
END$$

CREATE PROCEDURE registrar_retorno_despacho(
    IN p_id_despacho INT,
    IN p_fecha_retorno DATETIME
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM despacho WHERE id_despacho = p_id_despacho AND activo = 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El despacho no existe';
    END IF;
    IF EXISTS (SELECT 1 FROM despacho WHERE id_despacho = p_id_despacho AND fecha_retorno IS NOT NULL) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El retorno de este despacho ya fue registrado';
    END IF;

    UPDATE despacho
       SET fecha_retorno = p_fecha_retorno,
           estado = 'RETORNADO'
     WHERE id_despacho = p_id_despacho
       AND fecha_retorno IS NULL;
END$$

-- Una sola consulta: por id, por codigo, por cirugia, todos o solo pendientes.
CREATE PROCEDURE buscar_despachos(
    IN p_id_despacho    INT,
    IN p_codigo         VARCHAR(20),
    IN p_id_cirugia     INT,
    IN p_solo_pendientes TINYINT
)
BEGIN
    SELECT d.id_despacho, d.codigo, d.id_cirugia, d.id_equipo, e.nombre AS equipo_nombre,
           d.id_bandeja, b.tipo AS bandeja_tipo, d.fecha_salida, d.fecha_retorno, d.estado
      FROM despacho d
      LEFT JOIN equipo e               ON e.id_equipo = d.id_equipo
      LEFT JOIN bandeja_instrumental b ON b.id_bandeja = d.id_bandeja
     WHERE d.activo = 1
       AND (p_id_despacho IS NULL OR d.id_despacho = p_id_despacho)
       AND (p_codigo IS NULL OR d.codigo = p_codigo)
       AND (p_id_cirugia IS NULL OR d.id_cirugia = p_id_cirugia)
       AND (p_solo_pendientes = 0 OR d.fecha_retorno IS NULL)
     ORDER BY d.fecha_salida DESC, d.id_despacho DESC;
END$$

DELIMITER ;
