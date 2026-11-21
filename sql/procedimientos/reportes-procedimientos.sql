-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - REPORTES (RF18 y RF19)
-- Fuente unica de consumo para los reportes: cirugia_consumible_real.cantidad_consumida
-- (consumo real, RF15). Nunca usa cantidades previstas (cirugia_consumible_previsto).
-- Solo cuenta cirugias FINALIZADAS con consumo_completo = 1: un consumo parcial
-- subreportaria el uso de insumos.
-- Se puede ejecutar varias veces (DROP PROCEDURE IF EXISTS).
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS buscar_consumos_reporte;

DELIMITER $$

-- Una fila por cirugia y consumible de su bandeja. Todos los filtros son opcionales (NULL = sin filtro).
--   p_tipo_procedimiento : tipo de cirugia (texto exacto, p. ej. 'Artroscopia de rodilla')
--   p_id_consumible      : consumible exacto
--   p_medida             : medida exacta (p. ej. '70 mm')
CREATE PROCEDURE buscar_consumos_reporte(
    IN p_id_cirugia         INT,
    IN p_tipo_cliente       VARCHAR(20),
    IN p_id_cliente         INT,
    IN p_desde              DATETIME,
    IN p_hasta              DATETIME,
    IN p_tipo_procedimiento VARCHAR(100),
    IN p_id_consumible      INT,
    IN p_medida             VARCHAR(50)
)
BEGIN
    SELECT c.id_cirugia,
           c.fecha_hora_inicio,
           c.tipo_procedimiento,
           c.doctor_nombre,
           c.paciente_nombre,
           c.paciente_documento,
           CASE WHEN c.id_clinica_hospital IS NOT NULL THEN 'CLINICA_HOSPITAL'
                ELSE 'PACIENTE_PARTICULAR' END                        AS tipo_cliente,
           COALESCE(c.id_clinica_hospital, c.id_paciente_particular)  AS id_cliente,
           COALESCE(ch.nombre, pp.nombre)                             AS cliente_nombre,
           k.id_consumible,
           k.nombre_comercial,
           k.medida,
           bc.cantidad_despachada,
           bc.cantidad_consumida,
           (bc.cantidad_despachada - bc.cantidad_consumida)           AS diferencia
      FROM cirugia c
      JOIN cirugia_consumible_real bc  ON bc.id_cirugia = c.id_cirugia
      JOIN consumible k                ON k.id_consumible = bc.id_consumible
      LEFT JOIN clinica_hospital ch    ON ch.id_cliente = c.id_clinica_hospital
      LEFT JOIN paciente_particular pp ON pp.id_cliente = c.id_paciente_particular
     WHERE c.activo = 1
       AND c.estado = 'FINALIZADA'
       AND c.consumo_completo = 1
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
       AND (p_tipo_procedimiento IS NULL OR c.tipo_procedimiento = p_tipo_procedimiento)
       AND (p_id_consumible IS NULL OR k.id_consumible = p_id_consumible)
       AND (p_medida IS NULL OR k.medida = p_medida)
     ORDER BY c.fecha_hora_inicio, c.id_cirugia, k.nombre_comercial;
END$$

DELIMITER ;
