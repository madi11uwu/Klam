-- =========================================================
-- MÓDULO DE CLIENTES (Rol 3)
-- =========================================================

-- 1. Tabla para Clínica / Hospital
CREATE TABLE IF NOT EXISTS clinica_hospital (
    -- Atributos heredados de la clase abstracta Cliente
                                                id_cliente VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(200),
    email_contacto VARCHAR(100),
    telefono VARCHAR(20),
    estado BOOLEAN DEFAULT TRUE, -- Eliminación lógica (TRUE = Activo, FALSE = Inactivo)

-- Atributos propios de ClinicaHospital
    ruc VARCHAR(20) NOT NULL UNIQUE,
    tiene_consignacion BOOLEAN DEFAULT FALSE,
    periodo_credito VARCHAR(50)
    );

-- 2. Tabla para Paciente Particular
CREATE TABLE IF NOT EXISTS paciente_particular (
    -- Atributos heredados de la clase abstracta Cliente
                                                   id_cliente VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(200),
    email_contacto VARCHAR(100),
    telefono VARCHAR(20),
    estado BOOLEAN DEFAULT TRUE, -- Eliminación lógica (TRUE = Activo, FALSE = Inactivo)

-- Atributos propios de PacienteParticular
    dni VARCHAR(20) NOT NULL UNIQUE,
    pago_confirmado BOOLEAN DEFAULT FALSE
    );