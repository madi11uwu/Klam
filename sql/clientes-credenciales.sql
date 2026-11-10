-- =========================================================
-- CREDENCIALES (RF01): todo cliente es un usuario de la plataforma + cuentas de demostración del personal
-- Ejecutar DESPUES de DDL_KLAM.sql / clientes-procedimientos.sql / DML_KLAM.sql.
-- =========================================================
USE `klam`;

-- Solo si la base YA existía sin estas columnas (con DDL_KLAM.sql actualizado no hace falta).
-- MySQL no admite ADD COLUMN IF NOT EXISTS: ejecutar una sola vez.
-- ALTER TABLE clinica_hospital   ADD COLUMN username VARCHAR(50) NULL, ADD COLUMN password_hash VARCHAR(255) NULL, ADD UNIQUE KEY uq_clinica_username (username);
-- ALTER TABLE paciente_particular ADD COLUMN username VARCHAR(50) NULL, ADD COLUMN password_hash VARCHAR(255) NULL, ADD UNIQUE KEY uq_paciente_username (username);

DROP PROCEDURE IF EXISTS buscar_clinica_hospital_por_username;
DROP PROCEDURE IF EXISTS buscar_paciente_particular_por_username;
DROP PROCEDURE IF EXISTS asignar_credenciales_clinica_hospital;
DROP PROCEDURE IF EXISTS asignar_credenciales_paciente_particular;

DELIMITER $$

-- Mismas columnas que buscar_*_por_id, más username y password_hash
CREATE PROCEDURE buscar_clinica_hospital_por_username(IN p_username VARCHAR(50))
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, ruc, tiene_consignacion, periodo_credito,
           activo, username, password_hash
      FROM clinica_hospital
     WHERE username = p_username;
END$$

CREATE PROCEDURE buscar_paciente_particular_por_username(IN p_username VARCHAR(50))
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, dni, pago_confirmado,
           activo, username, password_hash
      FROM paciente_particular
     WHERE username = p_username;
END$$

CREATE PROCEDURE asignar_credenciales_clinica_hospital(
    IN p_id INT, IN p_username VARCHAR(50), IN p_password_hash VARCHAR(255))
BEGIN
    UPDATE clinica_hospital SET username = p_username, password_hash = p_password_hash
     WHERE id_cliente = p_id;
END$$

CREATE PROCEDURE asignar_credenciales_paciente_particular(
    IN p_id INT, IN p_username VARCHAR(50), IN p_password_hash VARCHAR(255))
BEGIN
    UPDATE paciente_particular SET username = p_username, password_hash = p_password_hash
     WHERE id_cliente = p_id;
END$$

DELIMITER ;

-- Usuarios de demostración (contraseña = texto plano, igual que administrador/vendedor/técnico hoy)
UPDATE paciente_particular SET username = 'juan.perez',   password_hash = 'cliente123' WHERE id_cliente = 1;
UPDATE clinica_hospital    SET username = 'sanfelipe',    password_hash = 'cliente123' WHERE id_cliente = 2;
UPDATE clinica_hospital    SET username = 'dosdemayo',    password_hash = 'cliente123' WHERE id_cliente = 3;

-- Cuentas de demostración del personal: el DML trae contraseñas de relleno ("2y10$examplehash...") con las
-- que no se puede ingresar. Todas las contraseñas del sistema se guardan en texto plano (columna password_hash).
UPDATE administrador          SET password_hash = 'admin12345'    WHERE username = 'admin.rojas';
UPDATE vendedor               SET password_hash = 'vendedor123'   WHERE username IN ('vend.torres', 'vend.quispe');
UPDATE tecnico_instrumentista SET password_hash = 'tecnico123'    WHERE username IN ('tec.fernandez', 'tec.salazar');
