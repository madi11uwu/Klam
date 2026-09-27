-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - ROL 4: CLIENTES
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS insertar_clinica_hospital;
DROP PROCEDURE IF EXISTS modificar_clinica_hospital;
DROP PROCEDURE IF EXISTS eliminar_clinica_hospital;
DROP PROCEDURE IF EXISTS listar_clinicas_hospitales;
DROP PROCEDURE IF EXISTS buscar_clinica_hospital_por_id;
DROP PROCEDURE IF EXISTS buscar_clinica_hospital_por_ruc;
DROP PROCEDURE IF EXISTS insertar_paciente_particular;
DROP PROCEDURE IF EXISTS modificar_paciente_particular;
DROP PROCEDURE IF EXISTS eliminar_paciente_particular;
DROP PROCEDURE IF EXISTS listar_pacientes_particulares;
DROP PROCEDURE IF EXISTS buscar_paciente_particular_por_id;
DROP PROCEDURE IF EXISTS buscar_paciente_particular_por_dni;

DELIMITER $$

-- ---------------------------------------------------------
-- CLINICA_HOSPITAL  (ROL 4)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_clinica_hospital(
    OUT p_id INT,
    IN  p_nombre VARCHAR(150),
    IN  p_direccion VARCHAR(200),
    IN  p_email_contacto VARCHAR(120),
    IN  p_telefono VARCHAR(20),
    IN  p_ruc CHAR(11),
    IN  p_tiene_consignacion TINYINT,
    IN  p_periodo_credito VARCHAR(30),
    IN  p_activo TINYINT
)
BEGIN
    -- id_cliente no es AUTO_INCREMENT: se genera el siguiente id
    SELECT COALESCE(MAX(id_cliente), 0) + 1 INTO p_id FROM clinica_hospital;

    INSERT INTO clinica_hospital (id_cliente, nombre, direccion, email_contacto, telefono, ruc, tiene_consignacion, periodo_credito, activo)
    VALUES (p_id, p_nombre, p_direccion, p_email_contacto, p_telefono, p_ruc, p_tiene_consignacion, p_periodo_credito, p_activo);
END$$

CREATE PROCEDURE modificar_clinica_hospital(
    IN p_id INT,
    IN p_nombre VARCHAR(150),
    IN p_direccion VARCHAR(200),
    IN p_email_contacto VARCHAR(120),
    IN p_telefono VARCHAR(20),
    IN p_ruc CHAR(11),
    IN p_tiene_consignacion TINYINT,
    IN p_periodo_credito VARCHAR(30),
    IN p_activo TINYINT
)
BEGIN
    UPDATE clinica_hospital
       SET nombre = p_nombre,
           direccion = p_direccion,
           email_contacto = p_email_contacto,
           telefono = p_telefono,
           ruc = p_ruc,
           tiene_consignacion = p_tiene_consignacion,
           periodo_credito = p_periodo_credito,
           activo = p_activo
     WHERE id_cliente = p_id;
END$$

CREATE PROCEDURE eliminar_clinica_hospital(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE clinica_hospital
       SET activo = 0
     WHERE id_cliente = p_id;
END$$

CREATE PROCEDURE listar_clinicas_hospitales()
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, ruc, tiene_consignacion, periodo_credito,
           activo
      FROM clinica_hospital
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_clinica_hospital_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, ruc, tiene_consignacion, periodo_credito,
           activo
      FROM clinica_hospital
     WHERE id_cliente = p_id;
END$$

CREATE PROCEDURE buscar_clinica_hospital_por_ruc(
    IN p_ruc CHAR(11)
)
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, ruc, tiene_consignacion, periodo_credito,
           activo
      FROM clinica_hospital
     WHERE ruc = p_ruc;
END$$

-- ---------------------------------------------------------
-- PACIENTE_PARTICULAR  (ROL 4)
-- ---------------------------------------------------------
CREATE PROCEDURE insertar_paciente_particular(
    OUT p_id INT,
    IN  p_nombre VARCHAR(150),
    IN  p_direccion VARCHAR(200),
    IN  p_email_contacto VARCHAR(120),
    IN  p_telefono VARCHAR(20),
    IN  p_dni CHAR(8),
    IN  p_pago_confirmado TINYINT,
    IN  p_activo TINYINT
)
BEGIN
    -- id_cliente no es AUTO_INCREMENT: se genera el siguiente id
    SELECT COALESCE(MAX(id_cliente), 0) + 1 INTO p_id FROM paciente_particular;

    INSERT INTO paciente_particular (id_cliente, nombre, direccion, email_contacto, telefono, dni, pago_confirmado, activo)
    VALUES (p_id, p_nombre, p_direccion, p_email_contacto, p_telefono, p_dni, p_pago_confirmado, p_activo);
END$$

CREATE PROCEDURE modificar_paciente_particular(
    IN p_id INT,
    IN p_nombre VARCHAR(150),
    IN p_direccion VARCHAR(200),
    IN p_email_contacto VARCHAR(120),
    IN p_telefono VARCHAR(20),
    IN p_dni CHAR(8),
    IN p_pago_confirmado TINYINT,
    IN p_activo TINYINT
)
BEGIN
    UPDATE paciente_particular
       SET nombre = p_nombre,
           direccion = p_direccion,
           email_contacto = p_email_contacto,
           telefono = p_telefono,
           dni = p_dni,
           pago_confirmado = p_pago_confirmado,
           activo = p_activo
     WHERE id_cliente = p_id;
END$$

CREATE PROCEDURE eliminar_paciente_particular(
    IN p_id INT
)
BEGIN
    -- Baja logica
    UPDATE paciente_particular
       SET activo = 0
     WHERE id_cliente = p_id;
END$$

CREATE PROCEDURE listar_pacientes_particulares()
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, dni, pago_confirmado, activo
      FROM paciente_particular
     WHERE activo = 1;
END$$

CREATE PROCEDURE buscar_paciente_particular_por_id(
    IN p_id INT
)
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, dni, pago_confirmado, activo
      FROM paciente_particular
     WHERE id_cliente = p_id;
END$$

CREATE PROCEDURE buscar_paciente_particular_por_dni(
    IN p_dni CHAR(8)
)
BEGIN
    SELECT id_cliente, nombre, direccion, email_contacto,
           telefono, dni, pago_confirmado, activo
      FROM paciente_particular
     WHERE dni = p_dni;
END$$

DELIMITER ;
