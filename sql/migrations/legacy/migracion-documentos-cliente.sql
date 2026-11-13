-- =====================================================================
-- Migración: documentos presentados por clientes (RF05/RF07)
-- Para bases ya creadas antes de este cambio. En una base nueva no hace falta:
-- DDL_KLAM.sql ya incluye la columna. Después de ejecutarla, vuelva a ejecutar
-- documentos-procedimientos.sql (los procedimientos reciben p_id_cuenta_usuario).
-- =====================================================================
USE `klam`;

ALTER TABLE `documento_ingreso`
    ADD COLUMN `id_cuenta_usuario` INT NULL AFTER `id_tecnico`,
    ADD CONSTRAINT `fk_documento_ingreso_cuenta`
        FOREIGN KEY (`id_cuenta_usuario`) REFERENCES `cuenta_usuario` (`id_usuario`)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `documento_ingreso` DROP CHECK `chk_documento_ingreso_un_usuario`;
ALTER TABLE `documento_ingreso` ADD CONSTRAINT `chk_documento_ingreso_un_usuario` CHECK (
    (`id_administrador` IS NOT NULL) + (`id_vendedor` IS NOT NULL)
    + (`id_tecnico` IS NOT NULL) + (`id_cuenta_usuario` IS NOT NULL) <= 1);
