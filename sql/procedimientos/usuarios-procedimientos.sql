-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS - ROL 1: USUARIOS, PERMISOS Y COMUNICACIONES
-- =========================================================
USE `klam`;

DROP PROCEDURE IF EXISTS insertar_administrador;
DROP PROCEDURE IF EXISTS modificar_administrador;
DROP PROCEDURE IF EXISTS eliminar_administrador;
DROP PROCEDURE IF EXISTS listar_administradores;
DROP PROCEDURE IF EXISTS buscar_administrador_por_id;
DROP PROCEDURE IF EXISTS buscar_administrador_por_username;
DROP PROCEDURE IF EXISTS buscar_administrador_por_email;

DROP PROCEDURE IF EXISTS insertar_vendedor;
DROP PROCEDURE IF EXISTS modificar_vendedor;
DROP PROCEDURE IF EXISTS eliminar_vendedor;
DROP PROCEDURE IF EXISTS listar_vendedores;
DROP PROCEDURE IF EXISTS buscar_vendedor_por_id;
DROP PROCEDURE IF EXISTS buscar_vendedor_por_username;
DROP PROCEDURE IF EXISTS buscar_vendedor_por_email;

DROP PROCEDURE IF EXISTS insertar_tecnico;
DROP PROCEDURE IF EXISTS modificar_tecnico;
DROP PROCEDURE IF EXISTS eliminar_tecnico;
DROP PROCEDURE IF EXISTS listar_tecnicos;
DROP PROCEDURE IF EXISTS buscar_tecnico_por_id;
DROP PROCEDURE IF EXISTS buscar_tecnico_por_username;
DROP PROCEDURE IF EXISTS buscar_tecnico_por_email;

DROP PROCEDURE IF EXISTS insertar_notificacion;
DROP PROCEDURE IF EXISTS modificar_notificacion;
DROP PROCEDURE IF EXISTS eliminar_notificacion;
DROP PROCEDURE IF EXISTS listar_notificaciones;
DROP PROCEDURE IF EXISTS buscar_notificacion_por_id;
DROP PROCEDURE IF EXISTS marcar_notificacion_leida;
DROP PROCEDURE IF EXISTS listar_notificaciones_por_destinatario;
DROP PROCEDURE IF EXISTS listar_notificaciones_no_leidas;

DELIMITER //
-- ADMINISTRADOR  (ROL 1)

CREATE PROCEDURE insertar_administrador(
    OUT p_id INT,
    IN  p_username VARCHAR(50),
    IN  p_password_hash VARCHAR(255),
    IN  p_email VARCHAR(120),
    IN  p_nombres VARCHAR(100),
    IN  p_apellidos VARCHAR(100),
    IN  p_activo TINYINT
)
BEGIN
INSERT INTO administrador (username, password_hash, email, nombres, apellidos, rol, activo)
VALUES (p_username, p_password_hash, p_email, p_nombres, p_apellidos, 'ADMINISTRADOR', p_activo);

SET p_id = LAST_INSERT_ID();
END //

CREATE PROCEDURE modificar_administrador(
    IN p_id INT,
    IN p_username VARCHAR(50),
    IN p_password_hash VARCHAR(255),
    IN p_email VARCHAR(120),
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_activo TINYINT
)
BEGIN
UPDATE administrador
SET username = p_username,
    password_hash = p_password_hash,
    email = p_email,
    nombres = p_nombres,
    apellidos = p_apellidos,
    activo = p_activo
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE eliminar_administrador(
    IN p_id INT
)
BEGIN
-- Baja logica
UPDATE administrador
SET activo = 0
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE listar_administradores()
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, activo
FROM administrador
WHERE activo = 1;
END //

CREATE PROCEDURE buscar_administrador_por_id(
    IN p_id INT
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, activo
FROM administrador
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE buscar_administrador_por_username(
    IN p_username VARCHAR(50)
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, activo
FROM administrador
WHERE username = p_username;
END //

CREATE PROCEDURE buscar_administrador_por_email(
    IN p_email VARCHAR(120)
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, activo
FROM administrador
WHERE email = p_email;
END //

-- VENDEDOR  (ROL 1)

CREATE PROCEDURE insertar_vendedor(
    OUT p_id INT,
    IN  p_username VARCHAR(50),
    IN  p_password_hash VARCHAR(255),
    IN  p_email VARCHAR(120),
    IN  p_nombres VARCHAR(100),
    IN  p_apellidos VARCHAR(100),
    IN  p_comision_acumulada DECIMAL(10,2),
    IN  p_activo TINYINT
)
BEGIN
INSERT INTO vendedor (username, password_hash, email, nombres, apellidos, rol, comision_acumulada, activo)
VALUES (p_username, p_password_hash, p_email, p_nombres, p_apellidos, 'VENDEDOR', p_comision_acumulada, p_activo);

SET p_id = LAST_INSERT_ID();
END //

CREATE PROCEDURE modificar_vendedor(
    IN p_id INT,
    IN p_username VARCHAR(50),
    IN p_password_hash VARCHAR(255),
    IN p_email VARCHAR(120),
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_comision_acumulada DECIMAL(10,2),
    IN p_activo TINYINT
)
BEGIN
UPDATE vendedor
SET username = p_username,
    password_hash = p_password_hash,
    email = p_email,
    nombres = p_nombres,
    apellidos = p_apellidos,
    comision_acumulada = p_comision_acumulada,
    activo = p_activo
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE eliminar_vendedor(
    IN p_id INT
)
BEGIN
-- Baja logica
UPDATE vendedor
SET activo = 0
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE listar_vendedores()
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, comision_acumulada,
       activo
FROM vendedor
WHERE activo = 1;
END //

CREATE PROCEDURE buscar_vendedor_por_id(
    IN p_id INT
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, comision_acumulada,
       activo
FROM vendedor
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE buscar_vendedor_por_username(
    IN p_username VARCHAR(50)
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, comision_acumulada,
       activo
FROM vendedor
WHERE username = p_username;
END //

CREATE PROCEDURE buscar_vendedor_por_email(
    IN p_email VARCHAR(120)
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, comision_acumulada,
       activo
FROM vendedor
WHERE email = p_email;
END //

-- TECNICO_INSTRUMENTISTA  (ROL 1)

CREATE PROCEDURE insertar_tecnico(
    OUT p_id INT,
    IN  p_username VARCHAR(50),
    IN  p_password_hash VARCHAR(255),
    IN  p_email VARCHAR(120),
    IN  p_nombres VARCHAR(100),
    IN  p_apellidos VARCHAR(100),
    IN  p_especialidad VARCHAR(100),
    IN  p_activo TINYINT
)
BEGIN
INSERT INTO tecnico_instrumentista (username, password_hash, email, nombres, apellidos, rol, especialidad, activo)
VALUES (p_username, p_password_hash, p_email, p_nombres, p_apellidos, 'TECNICO_INSTRUMENTISTA', p_especialidad, p_activo);

SET p_id = LAST_INSERT_ID();
END //

CREATE PROCEDURE modificar_tecnico(
    IN p_id INT,
    IN p_username VARCHAR(50),
    IN p_password_hash VARCHAR(255),
    IN p_email VARCHAR(120),
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_especialidad VARCHAR(100),
    IN p_activo TINYINT
)
BEGIN
UPDATE tecnico_instrumentista
SET username = p_username,
    password_hash = p_password_hash,
    email = p_email,
    nombres = p_nombres,
    apellidos = p_apellidos,
    especialidad = p_especialidad,
    activo = p_activo
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE eliminar_tecnico(
    IN p_id INT
)
BEGIN
-- Baja logica
UPDATE tecnico_instrumentista
SET activo = 0
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE listar_tecnicos()
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, especialidad,
       activo
FROM tecnico_instrumentista
WHERE activo = 1;
END //

CREATE PROCEDURE buscar_tecnico_por_id(
    IN p_id INT
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, especialidad,
       activo
FROM tecnico_instrumentista
WHERE id_usuario = p_id;
END //

CREATE PROCEDURE buscar_tecnico_por_username(
    IN p_username VARCHAR(50)
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, especialidad,
       activo
FROM tecnico_instrumentista
WHERE username = p_username;
END //

CREATE PROCEDURE buscar_tecnico_por_email(
    IN p_email VARCHAR(120)
)
BEGIN
SELECT id_usuario, username, password_hash, email,
       nombres, apellidos, rol, especialidad,
       activo
FROM tecnico_instrumentista
WHERE email = p_email;
END //

-- NOTIFICACION  (ROL 1)

CREATE PROCEDURE insertar_notificacion(
    OUT p_id INT,
    IN  p_fecha_hora DATETIME,
    IN  p_titulo VARCHAR(150),
    IN  p_mensaje VARCHAR(500),
    IN  p_estado_leida TINYINT,
    IN  p_id_administrador INT,
    IN  p_id_vendedor INT,
    IN  p_id_tecnico INT
)
BEGIN
-- id_notificacion no es AUTO_INCREMENT: se genera el siguiente id
SELECT COALESCE(MAX(id_notificacion), 0) + 1 INTO p_id FROM notificacion;

INSERT INTO notificacion (id_notificacion, fecha_hora, titulo, mensaje, estado_leida, id_administrador, id_vendedor, id_tecnico)
VALUES (p_id, p_fecha_hora, p_titulo, p_mensaje, p_estado_leida, p_id_administrador, p_id_vendedor, p_id_tecnico);
END //

CREATE PROCEDURE modificar_notificacion(
    IN p_id INT,
    IN p_fecha_hora DATETIME,
    IN p_titulo VARCHAR(150),
    IN p_mensaje VARCHAR(500),
    IN p_estado_leida TINYINT,
    IN p_id_administrador INT,
    IN p_id_vendedor INT,
    IN p_id_tecnico INT
)
BEGIN
UPDATE notificacion
SET fecha_hora = p_fecha_hora,
    titulo = p_titulo,
    mensaje = p_mensaje,
    estado_leida = p_estado_leida,
    id_administrador = p_id_administrador,
    id_vendedor = p_id_vendedor,
    id_tecnico = p_id_tecnico
WHERE id_notificacion = p_id;
END //

CREATE PROCEDURE eliminar_notificacion(
    IN p_id INT
)
BEGIN
DELETE FROM notificacion
WHERE id_notificacion = p_id;
END //

CREATE PROCEDURE listar_notificaciones()
BEGIN
SELECT id_notificacion, fecha_hora, titulo, mensaje,
       estado_leida, id_administrador, id_vendedor, id_tecnico
FROM notificacion
WHERE 1 = 1;
END //

CREATE PROCEDURE buscar_notificacion_por_id(
    IN p_id INT
)
BEGIN
SELECT id_notificacion, fecha_hora, titulo, mensaje,
       estado_leida, id_administrador, id_vendedor, id_tecnico
FROM notificacion
WHERE id_notificacion = p_id;
END //

CREATE PROCEDURE marcar_notificacion_leida(
    IN p_id INT,
    IN p_estado_leida TINYINT
)
BEGIN
UPDATE notificacion
SET estado_leida = p_estado_leida
WHERE id_notificacion = p_id;
END //

CREATE PROCEDURE listar_notificaciones_por_destinatario(
    IN p_id_administrador INT,
    IN p_id_vendedor INT,
    IN p_id_tecnico INT
)
BEGIN
-- Pasar solo uno de los tres ids; los otros van en NULL
SELECT id_notificacion, fecha_hora, titulo, mensaje,
       estado_leida, id_administrador, id_vendedor, id_tecnico
FROM notificacion
WHERE (id_administrador = p_id_administrador
    OR id_vendedor = p_id_vendedor
    OR id_tecnico = p_id_tecnico)
ORDER BY fecha_hora DESC;
END //

CREATE PROCEDURE listar_notificaciones_no_leidas()
BEGIN
SELECT id_notificacion, fecha_hora, titulo, mensaje,
       estado_leida, id_administrador, id_vendedor, id_tecnico
FROM notificacion
WHERE estado_leida = 0
ORDER BY fecha_hora DESC;
END //

DELIMITER ;