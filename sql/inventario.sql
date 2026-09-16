-- =========================================
-- BASE DE DATOS
-- =========================================

CREATE DATABASE IF NOT EXISTS klam;
USE klam;


-- =========================================
-- MÓDULO: AGENDA Y OPERACIONES - INVENTARIO
-- =========================================


-- =========================
-- TABLA: EQUIPO
-- =========================

CREATE TABLE equipo (
    id_equipo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);


-- =========================
-- TABLA: EQUIPO_ESPECIFICACION
-- Representa el Map<String, Object>
-- de especificaciones de Equipo
-- =========================

CREATE TABLE equipo_especificacion (
   id_especificacion INT AUTO_INCREMENT PRIMARY KEY,
   id_equipo INT NOT NULL,
   clave VARCHAR(50) NOT NULL,
   valor TEXT NOT NULL,

   CONSTRAINT fk_especificacion_equipo
       FOREIGN KEY (id_equipo)
           REFERENCES equipo(id_equipo),

   CONSTRAINT uq_equipo_clave
       UNIQUE (id_equipo, clave)
);


-- =========================
-- TABLA: CONSUMIBLE
-- =========================

CREATE TABLE consumible (
    id_consumible INT AUTO_INCREMENT PRIMARY KEY,
    nombre_comercial VARCHAR(100) NOT NULL,
    marca VARCHAR(100),
    medida VARCHAR(50)
);


-- =========================
-- TABLA: BANDEJA_INSTRUMENTAL
-- =========================

CREATE TABLE bandeja_instrumental (
  id_bandeja INT AUTO_INCREMENT PRIMARY KEY,
  tipo VARCHAR(100) NOT NULL,
  esterilizado BOOLEAN NOT NULL DEFAULT FALSE
);


-- =========================
-- TABLA INTERMEDIA:
-- BANDEJA_CONSUMIBLE
-- Relaciona cada bandeja con sus
-- consumibles y la cantidad requerida
-- =========================

CREATE TABLE bandeja_consumible (
    id_bandeja INT NOT NULL,
    id_consumible INT NOT NULL,
    cantidad INT NOT NULL,

    PRIMARY KEY (id_bandeja, id_consumible),

    CONSTRAINT fk_bandeja_consumible_bandeja
        FOREIGN KEY (id_bandeja)
            REFERENCES bandeja_instrumental(id_bandeja),

    CONSTRAINT fk_bandeja_consumible_consumible
        FOREIGN KEY (id_consumible)
            REFERENCES consumible(id_consumible),

    CONSTRAINT chk_cantidad_positiva
        CHECK (cantidad > 0)
);


-- =========================================
-- REGISTROS DE PRUEBA
-- =========================================


-- =========================
-- EQUIPOS
-- =========================

INSERT INTO equipo (
    nombre,
    categoria,
    disponible,
    activo
)
VALUES
    ('Craneotomo 01', 'CRANEOTOMO', TRUE, TRUE),
    ('Craneotomo 02', 'CRANEOTOMO', TRUE, TRUE),
    ('Neuronavegador 01', 'NAVEGADOR', TRUE, TRUE),
    ('Microscopio Quirurgico 01', 'MICROSCOPIO', TRUE, TRUE);


-- =========================
-- ESPECIFICACIONES DE EQUIPO
-- =========================

INSERT INTO equipo_especificacion (
    id_equipo,
    clave,
    valor
)
VALUES
    (1, 'marca', 'Stryker'),
    (1, 'modelo', 'System 8'),
    (1, 'velocidad_maxima', '75000 rpm'),

    (2, 'marca', 'Stryker'),
    (2, 'modelo', 'System 8'),

    (3, 'marca', 'Brainlab'),
    (3, 'tipo_navegacion', 'Neuronavegacion'),
    (3, 'pantalla', 'Tactil'),

    (4, 'marca', 'Zeiss'),
    (4, 'tipo', 'Microscopio quirurgico');


-- =========================
-- CONSUMIBLES
-- =========================

INSERT INTO consumible (
    nombre_comercial,
    marca,
    medida
)
VALUES
    ('Fresa cortante', 'Sin especificar', '70'),
    ('Fresa diamante', 'Sin especificar', '70'),
    ('Cuchilla quirurgica', 'Sin especificar', '70'),

    ('Fresa cortante', 'Sin especificar', '125'),
    ('Fresa diamante', 'Sin especificar', '125'),
    ('Cuchilla quirurgica', 'Sin especificar', '125'),

    ('Fresa cortante', 'Sin especificar', '150'),
    ('Fresa diamante', 'Sin especificar', '150'),
    ('Cuchilla quirurgica', 'Sin especificar', '150');


-- =========================
-- BANDEJAS INSTRUMENTALES
-- =========================

INSERT INTO bandeja_instrumental (
    tipo,
    esterilizado
)
VALUES
    ('CABEZA', TRUE),
    ('COLUMNA', TRUE);


-- =========================
-- PLANTILLA BANDEJA CABEZA
-- Consumibles de medida 70
-- =========================

INSERT INTO bandeja_consumible (
    id_bandeja,
    id_consumible,
    cantidad
)
VALUES
    (1, 1, 1),
    (1, 2, 1),
    (1, 3, 1);


-- =========================
-- PLANTILLA BANDEJA COLUMNA
-- Consumibles de medida 125 y 150
-- =========================

INSERT INTO bandeja_consumible (
    id_bandeja,
    id_consumible,
    cantidad
)
VALUES
    (2, 4, 1),
    (2, 5, 1),
    (2, 6, 1),
    (2, 7, 1),
    (2, 8, 1),
    (2, 9, 1);