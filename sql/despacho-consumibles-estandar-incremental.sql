-- Para bases existentes: el despacho toma lo previsto de la cirugia o, si no hay,
-- la composicion estandar de la plantilla (antes leia la tabla antigua bandeja_consumible).
USE `klam`;
DELIMITER $$
DROP PROCEDURE IF EXISTS insertar_despacho$$
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

    -- Fotografia del despacho de esta cirugia. No se copian consumos antiguos
    -- de la plantilla ni se modifica su configuracion estandar.
    -- Se despacha lo previsto para la cirugia; si no hay previsión, la composicion
    -- estandar de la plantilla (la que se edita en Plantillas de bandejas).
    IF p_id_bandeja IS NOT NULL THEN
        DELETE FROM cirugia_consumible_real WHERE id_cirugia = p_id_cirugia;
        IF EXISTS (SELECT 1 FROM cirugia_consumible_previsto WHERE id_cirugia = p_id_cirugia) THEN
            INSERT INTO cirugia_consumible_real
                (id_cirugia, id_consumible, cantidad_despachada, cantidad_consumida)
            SELECT p_id_cirugia, id_consumible, cantidad_prevista, 0
              FROM cirugia_consumible_previsto
             WHERE id_cirugia = p_id_cirugia AND cantidad_prevista > 0;
        ELSE
            INSERT INTO cirugia_consumible_real
                (id_cirugia, id_consumible, cantidad_despachada, cantidad_consumida)
            SELECT p_id_cirugia, id_consumible, cantidad_estandar, 0
              FROM bandeja_consumible_estandar
             WHERE id_bandeja = p_id_bandeja AND cantidad_estandar > 0;
        END IF;
    END IF;
END$$
DELIMITER ;
