-- =====================================================================
-- DEMO ROL 2 - Historial de cirugias y bitacora de auditoria (RF10, RF16)
-- Requisito: DDL_KLAM.sql y DML_KLAM.sql ejecutados sobre la base `klam`.
-- Cliente de prueba: paciente_particular id 1 (Juan Perez Alarcon).
-- Idempotente: borra solo las filas cuyo diagnostico empieza con "[DEMO R2]".
-- Guardar el archivo en UTF-8 (utf8mb4) para conservar tildes y la eñe.
-- =====================================================================
USE `klam`;

-- Permite DELETE sin clave primaria en MySQL Workbench (safe updates).
SET SQL_SAFE_UPDATES = 0;

START TRANSACTION;

-- 0) Limpieza previa. Primero la auditoria (FK RESTRICT) y luego las cirugias.
DELETE a FROM auditoria_cirugia a
  JOIN cirugia c ON c.id_cirugia = a.id_cirugia
 WHERE c.diagnostico LIKE '[DEMO R2]%';
DELETE FROM cirugia WHERE diagnostico LIKE '[DEMO R2]%';

-- ---------------------------------------------------------------------
-- 1) CREACION: cirugia A programada para el 20/11/2026 a las 10:00
-- ---------------------------------------------------------------------
INSERT INTO cirugia
    (fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento, doctor_nombre, diagnostico,
     motivo_cancelacion, estado, id_equipo, id_bandeja,
     id_clinica_hospital, id_paciente_particular, activo)
VALUES
    ('2026-11-20 10:00:00', NULL, 'Artroscopia de rodilla', 'Dr. Alberto Núñez',
     '[DEMO R2] Lesión de menisco medial', NULL, 'PROGRAMADA', NULL, NULL,
     NULL, 1, 1);
SET @cir_a = LAST_INSERT_ID();

INSERT INTO auditoria_cirugia
    (id_cirugia, fecha_hora, accion, responsable, detalle, valores_anteriores, valores_posteriores)
VALUES
    (@cir_a, '2026-11-10 09:15:00', 'CREAR', 'Valeria Torres',
     'Alta de programación',
     NULL,
     CONCAT('estado=PROGRAMADA', CHAR(10),
            'fechaHoraInicio=2026-11-20T10:00', CHAR(10),
            'tipoProcedimiento=Artroscopia de rodilla', CHAR(10),
            'doctorNombre=Dr. Alberto Núñez'));

-- ---------------------------------------------------------------------
-- 2) EDICION: se cambian la hora de inicio y el doctor de la cirugia A
-- ---------------------------------------------------------------------
UPDATE cirugia
   SET fecha_hora_inicio = '2026-11-20 11:30:00',
       doctor_nombre     = 'Dra. Silvia Ramos'
 WHERE id_cirugia = @cir_a;

INSERT INTO auditoria_cirugia
    (id_cirugia, fecha_hora, accion, responsable, detalle, valores_anteriores, valores_posteriores)
VALUES
    (@cir_a, '2026-11-10 09:40:00', 'EDITAR', 'Lucía Salazar',
     'Datos de la cirugía modificados: fechaHoraInicio, doctorNombre',
     CONCAT('fechaHoraInicio=2026-11-20T10:00', CHAR(10),
            'doctorNombre=Dr. Alberto Núñez'),
     CONCAT('fechaHoraInicio=2026-11-20T11:30', CHAR(10),
            'doctorNombre=Dra. Silvia Ramos'));

-- ---------------------------------------------------------------------
-- 3) CANCELACION: cirugia B programada y luego cancelada con motivo
-- ---------------------------------------------------------------------
INSERT INTO cirugia
    (fecha_hora_inicio, fecha_hora_fin, tipo_procedimiento, doctor_nombre, diagnostico,
     motivo_cancelacion, estado, id_equipo, id_bandeja,
     id_clinica_hospital, id_paciente_particular, activo)
VALUES
    ('2026-11-12 08:00:00', NULL, 'Hernia inguinal', 'Dr. Alberto Núñez',
     '[DEMO R2] Hernia inguinal derecha', NULL, 'PROGRAMADA', NULL, NULL,
     NULL, 1, 1);
SET @cir_b = LAST_INSERT_ID();

INSERT INTO auditoria_cirugia
    (id_cirugia, fecha_hora, accion, responsable, detalle, valores_anteriores, valores_posteriores)
VALUES
    (@cir_b, '2026-11-10 09:20:00', 'CREAR', 'Valeria Torres',
     'Alta de programación',
     NULL,
     CONCAT('estado=PROGRAMADA', CHAR(10),
            'fechaHoraInicio=2026-11-12T08:00', CHAR(10),
            'tipoProcedimiento=Hernia inguinal', CHAR(10),
            'doctorNombre=Dr. Alberto Núñez'));

UPDATE cirugia
   SET estado = 'CANCELADA',
       motivo_cancelacion = 'Paciente no apto'
 WHERE id_cirugia = @cir_b;

INSERT INTO auditoria_cirugia
    (id_cirugia, fecha_hora, accion, responsable, detalle, valores_anteriores, valores_posteriores)
VALUES
    (@cir_b, '2026-11-10 10:05:00', 'CANCELAR', 'Valeria Torres',
     'Cirugía cancelada: Paciente no apto',
     'estado=PROGRAMADA',
     CONCAT('estado=CANCELADA', CHAR(10),
            'motivoCancelacion=Paciente no apto'));

COMMIT;

SET SQL_SAFE_UPDATES = 1;

-- ---------------------------------------------------------------------
-- Verificacion: deberian aparecer 2 cirugias y 4 entradas de bitacora.
-- Para el historial del cliente 1 (PACIENTE_PARTICULAR) solo debe salir la B,
-- porque la A sigue programada.
-- ---------------------------------------------------------------------
SELECT c.id_cirugia, c.estado, c.tipo_procedimiento, c.fecha_hora_inicio,
       c.motivo_cancelacion, c.id_paciente_particular
  FROM cirugia c
 WHERE c.diagnostico LIKE '[DEMO R2]%'
 ORDER BY c.id_cirugia;

SELECT a.id_auditoria, a.id_cirugia, a.fecha_hora, a.accion, a.responsable, a.detalle
  FROM auditoria_cirugia a
  JOIN cirugia c ON c.id_cirugia = a.id_cirugia
 WHERE c.diagnostico LIKE '[DEMO R2]%'
 ORDER BY a.fecha_hora, a.id_auditoria;