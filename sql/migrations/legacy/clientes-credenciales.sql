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

-- =====================================================================
-- CUENTAS INICIALES: todo cliente debe tener credenciales (RF01).
-- Ejecutar DESPUÉS de DML_KLAM.sql. Se puede repetir: no duplica cuentas.
-- Un cliente nuevo recibe su cuenta automáticamente al registrarse:
--     usuario = DNI (paciente) o RUC (clínica), contraseña inicial = 'Klam' + ese número.
-- Aquí se crean las cuentas de los clientes que ya trae el DML.
-- =====================================================================

-- 1) Cuentas de demostración con nombre fácil de recordar (contraseña: cliente123)
INSERT INTO cuenta_usuario (username, password_hash, email, nombres, apellidos, activo, id_paciente)
SELECT 'juan.perez', 'cliente123', p.email_contacto, p.nombre, '', 1, p.id_cliente
  FROM paciente_particular p
 WHERE p.id_cliente = 1
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.id_paciente = p.id_cliente)
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.username = 'juan.perez');

INSERT INTO cuenta_usuario (username, password_hash, email, nombres, apellidos, activo, id_clinica)
SELECT 'sanfelipe', 'cliente123', h.email_contacto, h.nombre, '', 1, h.id_cliente
  FROM clinica_hospital h
 WHERE h.id_cliente = 2
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.id_clinica = h.id_cliente)
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.username = 'sanfelipe');

INSERT INTO cuenta_usuario (username, password_hash, email, nombres, apellidos, activo, id_clinica)
SELECT 'dosdemayo', 'cliente123', h.email_contacto, h.nombre, '', 1, h.id_cliente
  FROM clinica_hospital h
 WHERE h.id_cliente = 3
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.id_clinica = h.id_cliente)
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.username = 'dosdemayo');

-- 2) Cualquier otro cliente sin cuenta: usuario = DNI o RUC, contraseña = 'Klam' + ese número
INSERT INTO cuenta_usuario (username, password_hash, email, nombres, apellidos, activo, id_paciente)
SELECT p.dni, CONCAT('Klam', p.dni), p.email_contacto, p.nombre, '', 1, p.id_cliente
  FROM paciente_particular p
 WHERE NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.id_paciente = p.id_cliente)
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.username = p.dni);

INSERT INTO cuenta_usuario (username, password_hash, email, nombres, apellidos, activo, id_clinica)
SELECT h.ruc, CONCAT('Klam', h.ruc), h.email_contacto, h.nombre, '', 1, h.id_cliente
  FROM clinica_hospital h
 WHERE NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.id_clinica = h.id_cliente)
   AND NOT EXISTS (SELECT 1 FROM cuenta_usuario c WHERE c.username = h.ruc);

-- 3) Cuentas de demostración del personal: el DML trae contraseñas de relleno ("2y10$examplehash...").
--    Todas las contraseñas del sistema se guardan en texto plano (columna password_hash).
UPDATE administrador          SET password_hash = 'admin12345'  WHERE username = 'admin.rojas';
UPDATE vendedor               SET password_hash = 'vendedor123' WHERE username IN ('vend.torres', 'vend.quispe');
UPDATE tecnico_instrumentista SET password_hash = 'tecnico123'  WHERE username IN ('tec.fernandez', 'tec.salazar');
