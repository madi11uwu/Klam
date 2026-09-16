-- =====================================================================
-- KlamControl - BIOKLAM
-- Esquema fisico completo, generado a partir de las clases actuales de
-- klam-modelo (todos los roles).
--
-- USO EN MYSQL WORKBENCH
--   1. Abrir el script y ejecutarlo (rayo) sobre el servidor local.
--   2. Database > Reverse Engineer..., elegir el esquema klam y
--      seleccionar todas las tablas: Workbench dibuja el diagrama fisico
--      con sus relaciones.
--   Alternativa sin ejecutar nada: File > Import > Reverse Engineer
--   MySQL Create Script... y elegir este archivo.
--
-- CONVENCIONES
--   - Tablas y columnas en snake_case singular.
--   - Un ENUM de MySQL por cada enum de Java, con los mismos valores.
--   - activo BOOLEAN = eliminacion logica (no borra, desactiva).
--   - Los Map<K,V> de Java se modelan como tablas de relacion.
--   - La herencia de Java se modela con una tabla por clase, unidas por
--     la clave primaria del padre.
-- =====================================================================

DROP SCHEMA IF EXISTS klam;
CREATE SCHEMA klam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE klam;


-- =====================================================================
-- 1. USUARIOS Y PERMISOS   (UsuarioPlataforma y sus tres subclases)
-- =====================================================================

CREATE TABLE usuario_plataforma (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    email          VARCHAR(120) NOT NULL,
    nombres        VARCHAR(100) NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    -- En Java rol es un String con tres valores fijos; aqui se modela
    -- como ENUM. Si el equipo decide crear el enum RolUsuario, coincide.
    rol            ENUM('ADMINISTRADOR', 'VENDEDOR', 'TECNICO_INSTRUMENTISTA') NOT NULL,
    activo         BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE administrador (
    id_usuario  INT PRIMARY KEY,
    id_admin    VARCHAR(20) NOT NULL,

    CONSTRAINT fk_administrador_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario_plataforma (id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE vendedor (
    id_usuario          INT PRIMARY KEY,
    id_vendedor         VARCHAR(20) NOT NULL,
    comision_acumulada  DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    CONSTRAINT fk_vendedor_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario_plataforma (id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_vendedor_comision CHECK (comision_acumulada >= 0)
);

CREATE TABLE tecnico_instrumentista (
    id_usuario    INT PRIMARY KEY,
    id_tecnico    VARCHAR(20)  NOT NULL,
    especialidad  VARCHAR(100) NULL,

    CONSTRAINT fk_tecnico_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario_plataforma (id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- =====================================================================
-- 2. CLIENTES   (Cliente abstracta, ClinicaHospital, PacienteParticular)
-- =====================================================================

CREATE TABLE cliente (
    id_cliente      VARCHAR(50) PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    direccion       VARCHAR(200) NULL,
    email_contacto  VARCHAR(120) NULL,
    telefono        VARCHAR(20)  NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE clinica_hospital (
    id_cliente          VARCHAR(50) PRIMARY KEY,
    ruc                 CHAR(11) NOT NULL UNIQUE,
    tiene_consignacion  BOOLEAN NOT NULL DEFAULT FALSE,
    periodo_credito     VARCHAR(30) NULL,

    CONSTRAINT fk_clinica_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE paciente_particular (
    id_cliente       VARCHAR(50) PRIMARY KEY,
    dni              CHAR(8) NOT NULL UNIQUE,
    pago_confirmado  BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_paciente_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- =====================================================================
-- 3. INVENTARIO   (Equipo, Consumible, BandejaInstrumental)
-- =====================================================================

CREATE TABLE equipo (
    id_equipo   INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    categoria   ENUM('CRANEOTOMO', 'NAVEGADOR') NOT NULL,
    disponible  BOOLEAN NOT NULL DEFAULT TRUE,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

-- Map<String,Object> especificaciones de Equipo: una fila por entrada.
CREATE TABLE equipo_especificacion (
    id_equipo  INT NOT NULL,
    clave      VARCHAR(50)  NOT NULL,
    valor      VARCHAR(255) NULL,

    PRIMARY KEY (id_equipo, clave),
    CONSTRAINT fk_especificacion_equipo
        FOREIGN KEY (id_equipo) REFERENCES equipo (id_equipo)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE consumible (
    id_consumible     INT AUTO_INCREMENT PRIMARY KEY,
    nombre_comercial  VARCHAR(150) NOT NULL,
    marca             VARCHAR(100) NULL,
    medida            VARCHAR(50)  NULL,
    activo            BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE bandeja_instrumental (
    id_bandeja    INT AUTO_INCREMENT PRIMARY KEY,
    tipo          VARCHAR(100) NOT NULL,
    esterilizado  BOOLEAN NOT NULL DEFAULT FALSE,
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

-- Los dos Map<Consumible,Integer> de BandejaInstrumental en una sola
-- tabla: lo despachado y lo consumido son dos cantidades del mismo par.
CREATE TABLE bandeja_consumible (
    id_bandeja           INT NOT NULL,
    id_consumible        INT NOT NULL,
    cantidad_despachada  INT NOT NULL DEFAULT 0,
    cantidad_consumida   INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id_bandeja, id_consumible),
    CONSTRAINT fk_bandeja_consumible_bandeja
        FOREIGN KEY (id_bandeja) REFERENCES bandeja_instrumental (id_bandeja)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_bandeja_consumible_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible (id_consumible)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_bandeja_consumible_cantidades
        CHECK (cantidad_despachada >= 0 AND cantidad_consumida >= 0)
);


-- =====================================================================
-- 4. AGENDA Y OPERACIONES   (Cirugia)
-- =====================================================================

CREATE TABLE cirugia (
    id_cirugia          INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora_inicio   DATETIME NOT NULL,
    fecha_hora_fin      DATETIME NULL,
    tipo_procedimiento  VARCHAR(150) NOT NULL,
    doctor_nombre       VARCHAR(150) NULL,
    motivo_cancelacion  VARCHAR(255) NULL,
    -- estado = ciclo de vida de la cirugia
    estado              ENUM('PROGRAMADA', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA')
                            NOT NULL DEFAULT 'PROGRAMADA',
    id_equipo           INT NULL,
    id_bandeja          INT NULL,
    -- activo = eliminacion logica, independiente del estado
    activo              BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_cirugia_equipo
        FOREIGN KEY (id_equipo) REFERENCES equipo (id_equipo)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_cirugia_bandeja
        FOREIGN KEY (id_bandeja) REFERENCES bandeja_instrumental (id_bandeja)
        ON DELETE SET NULL ON UPDATE CASCADE
);


-- =====================================================================
-- 5. COMUNICACIONES   (Notificacion)
-- =====================================================================

CREATE TABLE notificacion (
    id_notificacion  VARCHAR(50) PRIMARY KEY,
    fecha_hora       DATETIME NOT NULL,
    titulo           VARCHAR(150) NOT NULL,
    estado_leida     BOOLEAN NOT NULL DEFAULT FALSE
);


-- =====================================================================
-- 6. GESTION DOCUMENTAL DE INGRESO   (OrdenCompra, DocumentoIngreso,
--    LineaOrdenCompra)
-- =====================================================================

CREATE TABLE orden_compra (
    id_orden_compra        VARCHAR(50) PRIMARY KEY,
    archivo_respaldo_path  VARCHAR(255) NOT NULL,
    fecha_recepcion        DATETIME NOT NULL,
    activo                 BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE documento_ingreso (
    id_documento       VARCHAR(50) PRIMARY KEY,
    tipo_documento     ENUM('DNI', 'RECETA_MEDICA', 'ORDEN_COMPRA') NOT NULL,
    archivo_path       VARCHAR(255) NOT NULL,
    estado_validacion  ENUM('PENDIENTE', 'VALIDADO', 'RECHAZADO')
                           NOT NULL DEFAULT 'PENDIENTE',
    fecha_carga        DATETIME NOT NULL,
    id_orden_compra    VARCHAR(50) NULL,
    activo             BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_documento_ingreso_orden
        FOREIGN KEY (id_orden_compra) REFERENCES orden_compra (id_orden_compra)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- LineaOrdenCompra hereda de LineaDocumento: lleva sus mismos campos.
CREATE TABLE linea_orden_compra (
    id_linea         INT AUTO_INCREMENT PRIMARY KEY,
    id_orden_compra  VARCHAR(50) NOT NULL,
    id_consumible    INT NULL,
    item_referencia  VARCHAR(50)  NULL,
    descripcion      VARCHAR(150) NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_linea_orden_compra_orden
        FOREIGN KEY (id_orden_compra) REFERENCES orden_compra (id_orden_compra)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_linea_orden_compra_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible (id_consumible)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT ck_linea_orden_compra_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_orden_compra_precio   CHECK (precio_unitario >= 0)
);


-- =====================================================================
-- 7. DOCUMENTACION Y FINANZAS   (Cotizacion, DocumentoFacturacion,
--    Factura, Boleta, NotaCredito y sus lineas)
-- =====================================================================

CREATE TABLE cotizacion (
    id_cotizacion   INT AUTO_INCREMENT PRIMARY KEY,
    id_cirugia      INT NOT NULL,
    precio_pactado  DECIMAL(10,2) NOT NULL,
    estado          ENUM('EMITIDA', 'ACEPTADA', 'RECHAZADA', 'VENCIDA')
                        NOT NULL DEFAULT 'EMITIDA',
    fecha_emision   DATETIME NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_cotizacion_cirugia
        FOREIGN KEY (id_cirugia) REFERENCES cirugia (id_cirugia)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_cotizacion_precio CHECK (precio_pactado >= 0)
);

-- Tabla padre de factura y boleta. tipo_documento actua como
-- discriminador. El receptor (RUC o DNI) se guarda congelado en la tabla
-- hija, no como FK al cliente: un comprobante conserva la identidad
-- tributaria con la que fue emitido.
CREATE TABLE documento_facturacion (
    id_documento    INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento  ENUM('FACTURA', 'BOLETA') NOT NULL,
    id_cirugia      INT NOT NULL,
    fecha_emision   DATETIME NOT NULL,
    monto_base      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tasa_igv        DECIMAL(5,4)  NOT NULL DEFAULT 0.1800,
    igv             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    monto_total     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado_pago     ENUM('PENDIENTE', 'PAGADO', 'ANULADO')
                        NOT NULL DEFAULT 'PENDIENTE',
    activo          BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_documento_facturacion_cirugia
        FOREIGN KEY (id_cirugia) REFERENCES cirugia (id_cirugia)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_documento_montos CHECK (monto_base >= 0 AND monto_total >= 0)
);

CREATE TABLE factura (
    id_documento  INT PRIMARY KEY,
    ruc_receptor  CHAR(11) NOT NULL,

    CONSTRAINT fk_factura_documento
        FOREIGN KEY (id_documento) REFERENCES documento_facturacion (id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE boleta (
    id_documento  INT PRIMARY KEY,
    dni_receptor  CHAR(8) NOT NULL,

    CONSTRAINT fk_boleta_documento
        FOREIGN KEY (id_documento) REFERENCES documento_facturacion (id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- NotaCredito no hereda de DocumentoFacturacion: solo implementa
-- Facturable. Referencia al documento que corrige.
CREATE TABLE nota_credito (
    id_nota_credito        INT AUTO_INCREMENT PRIMARY KEY,
    id_documento_original  INT NOT NULL,
    motivo                 VARCHAR(255) NOT NULL,
    fecha_emision          DATETIME NOT NULL,
    monto_total            DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo                 BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_nota_credito_documento
        FOREIGN KEY (id_documento_original)
        REFERENCES documento_facturacion (id_documento)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_nota_credito_monto CHECK (monto_total >= 0)
);

-- Lineas de detalle. Una tabla por tipo de documento, como en las
-- clases. El precio unitario se guarda en la linea y no se lee del
-- consumible: un comprobante emitido debe seguir cuadrando aunque el
-- precio de lista cambie despues.
CREATE TABLE linea_factura (
    id_linea         INT AUTO_INCREMENT PRIMARY KEY,
    id_documento     INT NOT NULL,
    id_consumible    INT NULL,
    item_referencia  VARCHAR(50)  NULL,
    descripcion      VARCHAR(150) NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_linea_factura_documento
        FOREIGN KEY (id_documento) REFERENCES factura (id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_linea_factura_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible (id_consumible)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT ck_linea_factura_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_factura_precio   CHECK (precio_unitario >= 0)
);

CREATE TABLE linea_boleta (
    id_linea         INT AUTO_INCREMENT PRIMARY KEY,
    id_documento     INT NOT NULL,
    id_consumible    INT NULL,
    item_referencia  VARCHAR(50)  NULL,
    descripcion      VARCHAR(150) NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_linea_boleta_documento
        FOREIGN KEY (id_documento) REFERENCES boleta (id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_linea_boleta_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible (id_consumible)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT ck_linea_boleta_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_boleta_precio   CHECK (precio_unitario >= 0)
);

CREATE TABLE linea_nota_credito (
    id_linea           INT AUTO_INCREMENT PRIMARY KEY,
    id_nota_credito    INT NOT NULL,
    id_consumible      INT NULL,
    item_referencia    VARCHAR(50)  NULL,
    descripcion        VARCHAR(150) NOT NULL,
    cantidad           INT NOT NULL,
    precio_unitario    DECIMAL(10,2) NOT NULL,
    motivo_devolucion  VARCHAR(255) NULL,

    CONSTRAINT fk_linea_nota_credito_nota
        FOREIGN KEY (id_nota_credito) REFERENCES nota_credito (id_nota_credito)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_linea_nota_credito_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible (id_consumible)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT ck_linea_nota_credito_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_nota_credito_precio   CHECK (precio_unitario >= 0)
);


-- =====================================================================
-- 8. COMPROBACION RAPIDA
-- =====================================================================

-- Las 25 tablas del esquema:
-- SHOW TABLES;

-- Todas las relaciones que Workbench dibujara como lineas:
-- SELECT table_name, column_name, referenced_table_name
-- FROM information_schema.key_column_usage
-- WHERE table_schema = 'klam' AND referenced_table_name IS NOT NULL
-- ORDER BY table_name;
