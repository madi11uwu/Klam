-- Migración incremental RF05: cuentas de acceso separadas de los clientes comerciales.
-- Ejecutar una sola vez después de verificar que DDL_KLAM.sql base ya existe.
-- No asigna cuentas a clientes anteriores ni cambia contraseñas del personal.
USE `klam`;

CREATE TABLE IF NOT EXISTS `cuenta_usuario` (
    `id_usuario` INT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `email` VARCHAR(120) NULL,
    `nombres` VARCHAR(150) NULL,
    `apellidos` VARCHAR(100) NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `id_paciente` INT NULL,
    `id_clinica` INT NULL,
    PRIMARY KEY (`id_usuario`),
    UNIQUE KEY `uq_cuenta_username` (`username`),
    KEY `idx_cuenta_paciente` (`id_paciente`),
    KEY `idx_cuenta_clinica` (`id_clinica`),
    CONSTRAINT `fk_cuenta_paciente` FOREIGN KEY (`id_paciente`)
        REFERENCES `paciente_particular` (`id_cliente`) ON DELETE RESTRICT,
    CONSTRAINT `fk_cuenta_clinica` FOREIGN KEY (`id_clinica`)
        REFERENCES `clinica_hospital` (`id_cliente`) ON DELETE RESTRICT,
    CONSTRAINT `chk_cuenta_un_cliente` CHECK
        ((`id_paciente` IS NOT NULL AND `id_clinica` IS NULL)
         OR (`id_paciente` IS NULL AND `id_clinica` IS NOT NULL))
);
