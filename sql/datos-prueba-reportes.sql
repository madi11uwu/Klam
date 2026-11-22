-- ============================================================
-- DATOS DE PRUEBA PARA RF18 (Uso de insumos) y RF19 (Comparativo)
-- Base: klam  |  Fecha: noviembre 2026
-- Ejecutar DESPUES de DML_KLAM.sql (ya existen administrador,
-- vendedor, consumibles 1-5, bandejas 1-5, clínicas 2-3,
-- pacientes 1-9 y cirugías 1-5).
-- Puedes ejecutarlo varias veces: usa INSERT IGNORE y UPDATE
-- para evitar duplicados.
-- ============================================================
USE `klam`;

-- ============================================================
-- 0.  CONSUMIBLES EXTRA (reutiliza los existentes 1-5)
-- ============================================================
-- Solo insertamos si no existen para no romper UNIQUE
INSERT IGNORE INTO consumible
    (id_consumible, nombre_comercial, marca, medida, activo)
VALUES
    (6, 'Dren de Jackson-Pratt', 'Ethicon',  '10 Fr',   1),
    (7, 'Hilo de sutura absorbible', 'Covidien', '3-0', 1);

-- ============================================================
-- 1.  CIRUGÍAS DE COLUMNA — FINALIZADAS CON CONSUMO COMPLETO
--     (id_cirugia 10, 11, 12  — rango seguro sin conflictos)
-- ============================================================

-- Cirugía 10: columna, clínica académica norte (id_cliente=2), paciente: Luis Muroya
INSERT IGNORE INTO cirugia
    (id_cirugia, fecha_hora_inicio, fecha_hora_fin,
     tipo_procedimiento, doctor_nombre, diagnostico,
     paciente_nombre, paciente_documento, motivo_cancelacion,
     estado, id_equipo, id_bandeja,
     id_clinica_hospital, id_paciente_particular,
     consumo_completo, activo)
VALUES
    (10,
     '2026-10-05 08:00:00',
     '2026-10-05 11:00:00',
     'Cirugía de columna',
     'Dr. Carlos Vega',
     'Hernia discal L4-L5',
     'Luis Muroya',
     '90000009',
     NULL,
     'FINALIZADA',
     NULL, 4,       -- bandeja 4 = "Bandeja larga de columna"
     2,             -- id_clinica_hospital = Clínica Académica Norte
     NULL,
     1,             -- consumo_completo = 1
     1);

-- Cirugía 11: columna, hospital simulado sur (id_cliente=3), paciente: Erasmo Gómez
INSERT IGNORE INTO cirugia
    (id_cirugia, fecha_hora_inicio, fecha_hora_fin,
     tipo_procedimiento, doctor_nombre, diagnostico,
     paciente_nombre, paciente_documento, motivo_cancelacion,
     estado, id_equipo, id_bandeja,
     id_clinica_hospital, id_paciente_particular,
     consumo_completo, activo)
VALUES
    (11,
     '2026-10-18 10:00:00',
     '2026-10-18 13:30:00',
     'Cirugía de columna',
     'Dra. Ana Peralta',
     'Estenosis espinal lumbar',
     'Erasmo Gómez',
     '90000004',
     NULL,
     'FINALIZADA',
     NULL, 5,       -- bandeja 5 = "Bandeja larga de vértebras"
     3,             -- id_clinica_hospital = Hospital Simulado Sur
     NULL,
     1,             -- consumo_completo = 1
     1);

-- Cirugía 12: columna, paciente particular (id_cliente=5), David Allasi
INSERT IGNORE INTO cirugia
    (id_cirugia, fecha_hora_inicio, fecha_hora_fin,
     tipo_procedimiento, doctor_nombre, diagnostico,
     paciente_nombre, paciente_documento, motivo_cancelacion,
     estado, id_equipo, id_bandeja,
     id_clinica_hospital, id_paciente_particular,
     consumo_completo, activo)
VALUES
    (12,
     '2026-11-10 09:00:00',
     '2026-11-10 12:00:00',
     'Cirugía de columna',
     'Dr. Carlos Vega',
     'Fractura vertebral T12',
     'David Allasi',
     '90000005',
     NULL,
     'FINALIZADA',
     NULL, 4,
     NULL,          -- no es clínica
     5,             -- id_paciente_particular = David Allasi
     1,             -- consumo_completo = 1
     1);

-- ============================================================
-- 2.  CIRUGÍA DE CRÁNEO — FINALIZADA CON CONSUMO COMPLETO
--     Corrige la cirugía 1 (ya existe, solo actualiza consumo_completo)
-- ============================================================
UPDATE cirugia
   SET consumo_completo = 1
 WHERE id_cirugia = 1
   AND estado = 'FINALIZADA';

-- ============================================================
-- 3.  CONSUMO REAL (cirugia_consumible_real)
--     Una fila por (cirugía, consumible).
--     La vista del reporte usa cantidad_despachada y cantidad_consumida.
-- ============================================================

-- ----- Cirugía 1 (cráneo): Sutura de nylon + Clip de aneurisma -----
INSERT IGNORE INTO cirugia_consumible_real
    (id_cirugia, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES
    (1, 1, 10, 8),   -- Sutura de nylon 4-0
    (1, 3, 4,  3);   -- Clip de aneurisma Mediano

-- ----- Cirugía 10 (columna): Cera para hueso + Tornillo pedicular + Sutura -----
INSERT IGNORE INTO cirugia_consumible_real
    (id_cirugia, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES
    (10, 4, 5, 4),   -- Cera para hueso 2.5g  ← el filtro del usuario
    (10, 5, 6, 6),   -- Tornillo pedicular 6 mm (consumo total)
    (10, 1, 8, 7);   -- Sutura de nylon 4-0

-- ----- Cirugía 11 (columna): Cera para hueso + Tornillo pedicular + Gasa -----
INSERT IGNORE INTO cirugia_consumible_real
    (id_cirugia, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES
    (11, 4, 3, 2),   -- Cera para hueso 2.5g
    (11, 5, 8, 8),   -- Tornillo pedicular 6 mm (consumo total)
    (11, 2, 20, 18); -- Gasa estéril

-- ----- Cirugía 12 (columna): Cera para hueso + Tornillo pedicular + Dren -----
INSERT IGNORE INTO cirugia_consumible_real
    (id_cirugia, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES
    (12, 4, 4, 4),   -- Cera para hueso 2.5g (consumo total)
    (12, 5, 6, 5),   -- Tornillo pedicular 6 mm
    (12, 6, 2, 1);   -- Dren de Jackson-Pratt

-- ============================================================
-- 4.  VERIFICACIÓN RÁPIDA
--     Copia estas consultas en MySQL Workbench para confirmar
--     que el procedimiento almacenado devuelve datos.
-- ============================================================

-- RF18/RF19 sin filtros de tipo ni consumible (debe devolver 10, 11, 12 y también la 1):
-- CALL buscar_consumos_reporte(NULL, NULL, NULL, '2026-01-01', '2026-12-31', NULL, NULL, NULL);

-- Solo cirugías de columna (debe devolver 10, 11, 12):
-- CALL buscar_consumos_reporte(NULL, NULL, NULL, '2026-01-01', '2026-12-31', 'Cirugía de columna', NULL, NULL);

-- Solo columna + Cera para hueso (el filtro exacto de la captura):
-- CALL buscar_consumos_reporte(NULL, NULL, NULL, '2026-01-01', '2026-12-31', 'Cirugía de columna', 4, NULL);
