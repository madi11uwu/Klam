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
-- Representa el Map<String, Object> especificaciones
-- =========================
CREATE TABLE equipo_especificacion (
   id_especificacion INT AUTO_INCREMENT PRIMARY KEY,
   id_equipo VARCHAR(20) NOT NULL,
   clave VARCHAR(50) NOT NULL,
   valor TEXT NOT NULL,

   CONSTRAINT fk_especificacion_equipo
       FOREIGN KEY (id_equipo)
           REFERENCES equipo(id_equipo)
           ON DELETE CASCADE,

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
-- Representa List<Consumible>
-- =========================
CREATE TABLE bandeja_consumible (
    id_bandeja INT AUTO_INCREMENT NOT NULL,
    id_consumible INT NOT NULL,

    PRIMARY KEY (id_bandeja, id_consumible),

    CONSTRAINT fk_bandeja_consumible_bandeja
        FOREIGN KEY (id_bandeja)
            REFERENCES bandeja_instrumental(id_bandeja),

    CONSTRAINT fk_bandeja_consumible_consumible
        FOREIGN KEY (id_consumible)
            REFERENCES consumible(id_consumible)
);