
DROP TABLE IF EXISTS tecnico_instrumentista;
DROP TABLE IF EXISTS vendedor;
DROP TABLE IF EXISTS administrador;
DROP TABLE IF EXISTS usuario;

-- Tabla Padre: usuario

CREATE TABLE usuario (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    email          VARCHAR(100) NOT NULL,
    nombres        VARCHAR(100) NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    rol            VARCHAR(30)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT uq_usuario_email    UNIQUE (email)
);


-- Tabla hija: administrador

CREATE TABLE administrador (
    id_usuario INT NOT NULL,
    id_admin   VARCHAR(30) NOT NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_administrador_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON DELETE CASCADE,
    CONSTRAINT uq_administrador_id_admin UNIQUE (id_admin)
);


-- Tabla hija: vendedor

CREATE TABLE vendedor (
    id_usuario          INT NOT NULL,
    id_vendedor         VARCHAR(30) NOT NULL,
    comision_acumulada  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_vendedor_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON DELETE CASCADE,
    CONSTRAINT uq_vendedor_id_vendedor UNIQUE (id_vendedor)
);


-- Tabla hija: tecnico_instrumentista

CREATE TABLE tecnico_instrumentista (
    id_usuario   INT NOT NULL,
    id_tecnico   VARCHAR(30) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_tecnico_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON DELETE CASCADE,
    CONSTRAINT uq_tecnico_id_tecnico UNIQUE (id_tecnico)
);

-- Datos de prueba

-- 1) Un administrador
INSERT INTO usuario (username, password_hash, email, nombres, apellidos, rol, activo)
VALUES ('jparras', 'HASH_PLACEHOLDER_1', 'j.parra@bioklam.com', 'Jose Felipe', 'Parra Salazar', 'ADMINISTRADOR', TRUE);

INSERT INTO administrador (id_usuario, id_admin)
VALUES (LAST_INSERT_ID(), 'ADM-001');

-- 2) Un vendedor
INSERT INTO usuario (username, password_hash, email, nombres, apellidos, rol, activo)
VALUES ('pedrito777', 'HASH_PLACEHOLDER_2', 'p.perez@bioklam.com', 'Pedro Pablo', 'Perez Pereira', 'VENDEDOR', TRUE);

INSERT INTO vendedor (id_usuario, id_vendedor, comision_acumulada)
VALUES (LAST_INSERT_ID(), 'VEN-001', 0.00);

-- 3) Un técnico instrumentista
INSERT INTO usuario (username, password_hash, email, nombres, apellidos, rol, activo)
VALUES ('dianaTA', 'HASH_PLACEHOLDER_3', 'd.torres@bioklam.com', 'Diana Ingrid', 'Torres Arias', 'TECNICO_INSTRUMENTISTA', TRUE);

INSERT INTO tecnico_instrumentista (id_usuario, id_tecnico, especialidad)
VALUES (LAST_INSERT_ID(), 'TEC-001', 'Neurocirugia');

-- 4) Un usuario desactivado (para probar el eliminado lógico / filtros por estado)
INSERT INTO usuario (username, password_hash, email, nombres, apellidos, rol, activo)
VALUES ('camposM', 'HASH_PLACEHOLDER_4', 'c.campos@bioklam.com', 'Carlos Austin', 'Campos Mendoza', 'VENDEDOR', FALSE);

INSERT INTO vendedor (id_usuario, id_vendedor, comision_acumulada)
VALUES (LAST_INSERT_ID(), 'VEN-002', 150.50);

-- Consultas de verificación

SELECT u.*, a.id_admin
FROM usuario u
JOIN administrador a ON u.id_usuario = a.id_usuario;

SELECT u.*, v.id_vendedor, v.comision_acumulada
FROM usuario u
JOIN vendedor v ON u.id_usuario = v.id_usuario
WHERE u.activo = TRUE;

-- Todos los usuarios (de cualquier rol) que están inactivos
SELECT id_usuario, username, rol, activo
FROM usuario
WHERE activo = FALSE;
