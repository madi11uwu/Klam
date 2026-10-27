DELIMITER $$

-- ================= EQUIPOS =================
CREATE PROCEDURE SP_EQUIPO_LISTAR_DISPONIBLES()
BEGIN
SELECT id_equipo, nombre, categoria, disponible, activo
FROM equipo
WHERE disponible = 1 AND activo = 1;
END$$

CREATE PROCEDURE SP_EQUIPO_REGISTRAR_RETORNO(IN p_id_equipo INT)
BEGIN
UPDATE equipo
SET disponible = 1
WHERE id_equipo = p_id_equipo AND activo = 1;
END$$

-- ================= BANDEJAS Y CONSUMIBLES =================
CREATE PROCEDURE SP_BANDEJA_ASOCIAR_CONSUMIBLE(
    IN p_id_bandeja INT,
    IN p_id_consumible INT,
    IN p_cantidad_despachada INT
)
BEGIN
INSERT INTO bandeja_consumible (id_bandeja, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES (p_id_bandeja, p_id_consumible, p_cantidad_despachada, 0)
    ON DUPLICATE KEY UPDATE cantidad_despachada = p_cantidad_despachada;
END$$

CREATE PROCEDURE SP_BANDEJA_LISTAR_CONSUMIBLES(IN p_id_bandeja INT)
BEGIN
SELECT c.id_consumible, c.nombre_comercial, c.marca, c.medida, c.activo,
       bc.cantidad_despachada, bc.cantidad_consumida
FROM bandeja_consumible bc
         INNER JOIN consumible c ON bc.id_consumible = c.id_consumible
WHERE bc.id_bandeja = p_id_bandeja;
END$$

CREATE PROCEDURE SP_BANDEJA_REGISTRAR_CONSUMO(
    IN p_id_bandeja INT,
    IN p_id_consumible INT,
    IN p_cantidad_consumida INT
)
BEGIN
UPDATE bandeja_consumible
SET cantidad_consumida = p_cantidad_consumida
WHERE id_bandeja = p_id_bandeja AND id_consumible = p_id_consumible;
END$$

-- ================= CONSULTAS DE LOGÍSTICA (CIRUGÍAS) =================
CREATE PROCEDURE SP_LOGISTICA_DESPACHOS_PENDIENTES()
BEGIN
    -- Retorna cirugías en proceso/finalizadas donde el equipo aún no está disponible
SELECT c.id_cirugia, c.fecha_hora_inicio, c.tipo_procedimiento, c.estado,
       e.id_equipo, e.nombre AS equipo_nombre
FROM cirugia c
         INNER JOIN equipo e ON c.id_equipo = e.id_equipo
WHERE c.estado IN ('EN_PROCESO', 'FINALIZADA')
  AND e.disponible = 0
  AND c.activo = 1;
END$$

CREATE PROCEDURE SP_LOGISTICA_CIRUGIAS_PARA_CONSUMO()
BEGIN
    -- Retorna cirugías finalizadas para registrar consumo de sus bandejas
SELECT id_cirugia, fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento, id_bandeja
FROM cirugia
WHERE estado = 'FINALIZADA' AND id_bandeja IS NOT NULL AND activo = 1;
END$$

DELIMITER ;