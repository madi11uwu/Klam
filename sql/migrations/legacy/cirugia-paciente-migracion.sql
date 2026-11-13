-- Aplicar antes del WAR actualizado. Los registros anteriores permanecen NULL.
-- Las columnas son datos del paciente intervenido, no del cliente comercial.
SET @agregar_paciente_nombre = IF(
    EXISTS (SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cirugia' AND COLUMN_NAME = 'paciente_nombre'),
    'SELECT 1', 'ALTER TABLE cirugia ADD COLUMN paciente_nombre VARCHAR(150) NULL AFTER diagnostico');
PREPARE migracion_paciente_nombre FROM @agregar_paciente_nombre;
EXECUTE migracion_paciente_nombre;
DEALLOCATE PREPARE migracion_paciente_nombre;

SET @agregar_paciente_documento = IF(
    EXISTS (SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cirugia' AND COLUMN_NAME = 'paciente_documento'),
    'SELECT 1', 'ALTER TABLE cirugia ADD COLUMN paciente_documento VARCHAR(20) NULL AFTER paciente_nombre');
PREPARE migracion_paciente_documento FROM @agregar_paciente_documento;
EXECUTE migracion_paciente_documento;
DEALLOCATE PREPARE migracion_paciente_documento;
