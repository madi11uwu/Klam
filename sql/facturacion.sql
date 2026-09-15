-- =====================================================================
-- KlamControl - BIOKLAM
-- Modulo: Documentacion y Finanzas  (Rol 6)
-- Contenido: cotizacion, documentos de facturacion, notas de credito
--            y sus lineas de detalle, mas registros de prueba.
-- =====================================================================
--
-- DEPENDENCIAS EXTERNAS
--   Este script asume que ya existen las tablas de otros modulos:
--     - cirugia(id_cirugia)        -> modulo de agenda y operaciones
--     - consumible(id_consumible)  -> modulo de inventario
--   Ejecutar despues de los scripts de esos modulos.
--   Si necesita ejecutarlo de forma aislada, comente las restricciones
--   marcadas como [FK EXTERNA] y los INSERT de la seccion de pruebas.
--   Los INSERT de prueba no dependen de correlativos concretos: las
--   referencias a cirugia y consumible se resuelven por posicion en la
--   seccion 7.
--
-- CONVENCIONES
--   - Nombres de tablas y columnas en snake_case singular.
--   - Clave primaria: id_<entidad> INT AUTO_INCREMENT.
--   - Eliminacion logica mediante la columna activo.
--   - Restricciones nombradas fk_<tabla>_<referencia>.
-- =====================================================================


-- =====================================================================
-- 1. LIMPIEZA (orden inverso al de creacion para no romper las FK)
-- =====================================================================

DROP TABLE IF EXISTS linea_nota_credito;
DROP TABLE IF EXISTS linea_boleta;
DROP TABLE IF EXISTS linea_factura;
DROP TABLE IF EXISTS nota_credito;
DROP TABLE IF EXISTS boleta;
DROP TABLE IF EXISTS factura;
DROP TABLE IF EXISTS documento_facturacion;
DROP TABLE IF EXISTS cotizacion;


-- =====================================================================
-- 2. COTIZACION
--    Documento previo y estimado. No tiene lineas de detalle:
--    el diagrama de clases solo le define un precio pactado.
-- =====================================================================

CREATE TABLE cotizacion (
    id_cotizacion   INT AUTO_INCREMENT PRIMARY KEY,
    id_cirugia      INT NOT NULL,
    precio_pactado  DECIMAL(10,2) NOT NULL,
    estado          ENUM('EMITIDA', 'ACEPTADA', 'RECHAZADA', 'VENCIDA')
                        NOT NULL DEFAULT 'EMITIDA',
    fecha_emision   DATETIME NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,

    -- [FK EXTERNA]
    CONSTRAINT fk_cotizacion_cirugia
        FOREIGN KEY (id_cirugia) REFERENCES cirugia(id_cirugia)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT ck_cotizacion_precio CHECK (precio_pactado >= 0)
);


-- =====================================================================
-- 3. DOCUMENTO DE FACTURACION  (tabla padre)
--    Concentra los atributos comunes de factura y boleta.
--    tipo_documento actua como discriminador.
--    El receptor se guarda congelado en la tabla hija (RUC o DNI),
--    no como clave foranea al cliente: un comprobante conserva la
--    identidad tributaria con la que fue emitido.
-- =====================================================================

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

    -- [FK EXTERNA]
    CONSTRAINT fk_documento_facturacion_cirugia
        FOREIGN KEY (id_cirugia) REFERENCES cirugia(id_cirugia)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT ck_documento_montos CHECK (monto_base >= 0 AND monto_total >= 0)
);


-- =====================================================================
-- 4. FACTURA Y BOLETA  (tablas hijas, comparten la PK del padre)
-- =====================================================================

CREATE TABLE factura (
    id_documento  INT PRIMARY KEY,
    ruc_receptor  CHAR(11) NOT NULL,

    CONSTRAINT fk_factura_documento
        FOREIGN KEY (id_documento) REFERENCES documento_facturacion(id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE boleta (
    id_documento  INT PRIMARY KEY,
    dni_receptor  CHAR(8) NOT NULL,

    CONSTRAINT fk_boleta_documento
        FOREIGN KEY (id_documento) REFERENCES documento_facturacion(id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE
);


-- =====================================================================
-- 5. NOTA DE CREDITO
--    No hereda de documento_facturacion (se respeta el diagrama de
--    clases presentado). Referencia el documento que corrige.
-- =====================================================================

CREATE TABLE nota_credito (
    id_nota_credito       INT AUTO_INCREMENT PRIMARY KEY,
    id_documento_original INT NOT NULL,
    motivo                VARCHAR(255) NOT NULL,
    fecha_emision         DATETIME NOT NULL,
    monto_total           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo                BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_nota_credito_documento
        FOREIGN KEY (id_documento_original)
        REFERENCES documento_facturacion(id_documento)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT ck_nota_credito_monto CHECK (monto_total >= 0)
);


-- =====================================================================
-- 6. LINEAS DE DETALLE
--    Una tabla por tipo de documento. El precio unitario se guarda
--    en la linea, no se lee del consumible: un comprobante emitido
--    debe seguir cuadrando aunque el precio de lista cambie despues.
-- =====================================================================

CREATE TABLE linea_factura (
    id_linea         INT AUTO_INCREMENT PRIMARY KEY,
    id_documento     INT NOT NULL,
    id_consumible    INT NULL,
    item_referencia  VARCHAR(50) NULL,
    descripcion      VARCHAR(150) NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_linea_factura_documento
        FOREIGN KEY (id_documento) REFERENCES factura(id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- [FK EXTERNA]
    CONSTRAINT fk_linea_factura_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible(id_consumible)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT ck_linea_factura_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_factura_precio   CHECK (precio_unitario >= 0)
);

CREATE TABLE linea_boleta (
    id_linea         INT AUTO_INCREMENT PRIMARY KEY,
    id_documento     INT NOT NULL,
    id_consumible    INT NULL,
    item_referencia  VARCHAR(50) NULL,
    descripcion      VARCHAR(150) NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_linea_boleta_documento
        FOREIGN KEY (id_documento) REFERENCES boleta(id_documento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- [FK EXTERNA]
    CONSTRAINT fk_linea_boleta_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible(id_consumible)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT ck_linea_boleta_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_boleta_precio   CHECK (precio_unitario >= 0)
);

CREATE TABLE linea_nota_credito (
    id_linea          INT AUTO_INCREMENT PRIMARY KEY,
    id_nota_credito   INT NOT NULL,
    id_consumible     INT NULL,
    item_referencia   VARCHAR(50) NULL,
    descripcion       VARCHAR(150) NOT NULL,
    cantidad          INT NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,
    motivo_devolucion VARCHAR(255) NULL,

    CONSTRAINT fk_linea_nota_credito_nota
        FOREIGN KEY (id_nota_credito) REFERENCES nota_credito(id_nota_credito)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- [FK EXTERNA]
    CONSTRAINT fk_linea_nota_credito_consumible
        FOREIGN KEY (id_consumible) REFERENCES consumible(id_consumible)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT ck_linea_nota_credito_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_linea_nota_credito_precio   CHECK (precio_unitario >= 0)
);


-- =====================================================================
-- 7. REGISTROS DE PRUEBA
--
--    Las filas de cirugia y consumible pertenecen a otros modulos, y no
--    se puede asumir que tengan los correlativos 1, 2 y 3. En lugar de
--    escribir esos numeros a mano, se resuelven por posicion: se toma la
--    primera, la segunda y la tercera fila existente, sea cual sea su id.
--    Asi el script sigue funcionando aunque los otros modulos cambien su
--    numeracion o su data de prueba.
--
--    Los id_documento e id_nota_credito SI se escriben directamente,
--    porque son de tablas que este mismo script acaba de crear vacias.
-- =====================================================================

-- --- Resolucion de referencias externas -------------------------------
SET @cirugia_1 = (SELECT id_cirugia FROM cirugia ORDER BY id_cirugia LIMIT 1 OFFSET 0);
SET @cirugia_2 = (SELECT id_cirugia FROM cirugia ORDER BY id_cirugia LIMIT 1 OFFSET 1);
SET @cirugia_3 = (SELECT id_cirugia FROM cirugia ORDER BY id_cirugia LIMIT 1 OFFSET 2);

SET @consumible_1 = (SELECT id_consumible FROM consumible ORDER BY id_consumible LIMIT 1 OFFSET 0);
SET @consumible_2 = (SELECT id_consumible FROM consumible ORDER BY id_consumible LIMIT 1 OFFSET 1);
SET @consumible_3 = (SELECT id_consumible FROM consumible ORDER BY id_consumible LIMIT 1 OFFSET 2);

-- Si alguna variable de cirugia queda en NULL, no hay suficientes
-- cirugias registradas y los INSERT siguientes fallaran. Verificar con:
--   SELECT @cirugia_1, @cirugia_2, @cirugia_3,
--          @consumible_1, @consumible_2, @consumible_3;

-- --- Cotizaciones -----------------------------------------------------
INSERT INTO cotizacion (id_cirugia, precio_pactado, estado, fecha_emision) VALUES
    (@cirugia_1, 4500.00, 'ACEPTADA',  '2026-09-01 10:30:00'),
    (@cirugia_2, 3200.00, 'EMITIDA',   '2026-09-03 09:15:00'),
    (@cirugia_3, 7800.00, 'RECHAZADA', '2026-09-05 16:40:00');

-- --- Factura a cliente institucional (clinica con RUC) ----------------
INSERT INTO documento_facturacion
    (tipo_documento, id_cirugia, fecha_emision, monto_base, tasa_igv, igv, monto_total, estado_pago)
VALUES
    ('FACTURA', @cirugia_1, '2026-09-02 18:00:00', 4500.00, 0.1800, 810.00, 5310.00, 'PENDIENTE');

INSERT INTO factura (id_documento, ruc_receptor) VALUES
    (LAST_INSERT_ID(), '20481234567');

INSERT INTO linea_factura
    (id_documento, id_consumible, item_referencia, descripcion, cantidad, precio_unitario)
VALUES
    (1, @consumible_1, 'CONS-001', 'Fresa de corte 4mm',   2, 1200.00),
    (1, @consumible_2, 'CONS-002', 'Esfera de navegacion', 1, 2100.00);

-- --- Boleta a paciente particular (con DNI) ---------------------------
INSERT INTO documento_facturacion
    (tipo_documento, id_cirugia, fecha_emision, monto_base, tasa_igv, igv, monto_total, estado_pago)
VALUES
    ('BOLETA', @cirugia_2, '2026-09-04 12:00:00', 3200.00, 0.1800, 576.00, 3776.00, 'PAGADO');

INSERT INTO boleta (id_documento, dni_receptor) VALUES
    (LAST_INSERT_ID(), '70123456');

INSERT INTO linea_boleta
    (id_documento, id_consumible, item_referencia, descripcion, cantidad, precio_unitario)
VALUES
    (2, @consumible_1, 'CONS-001', 'Fresa de corte 4mm',       1, 1200.00),
    (2, @consumible_3, 'CONS-003', 'Duramadre artificial 5x5', 2, 1000.00);

-- --- Nota de credito sobre la factura ---------------------------------
INSERT INTO nota_credito (id_documento_original, motivo, fecha_emision, monto_total) VALUES
    (1, 'Consumible devuelto sin usar tras la cirugia', '2026-09-06 11:20:00', 1200.00);

INSERT INTO linea_nota_credito
    (id_nota_credito, id_consumible, item_referencia, descripcion, cantidad, precio_unitario, motivo_devolucion)
VALUES
    (1, @consumible_1, 'CONS-001', 'Fresa de corte 4mm', 1, 1200.00, 'No utilizada durante el procedimiento');


-- =====================================================================
-- 8. CONSULTAS DE VERIFICACION
--    Utiles para demostrar en el laboratorio que cabecera y detalle
--    son coherentes. No forman parte del esquema.
-- =====================================================================

-- Cada documento con la suma real de sus lineas.
-- La suma de las lineas debe coincidir con monto_base.
-- SELECT d.id_documento, d.tipo_documento, d.monto_base,
--        COALESCE(SUM(l.cantidad * l.precio_unitario), 0) AS suma_lineas
-- FROM documento_facturacion d
-- LEFT JOIN linea_factura l ON l.id_documento = d.id_documento
-- WHERE d.tipo_documento = 'FACTURA'
-- GROUP BY d.id_documento, d.tipo_documento, d.monto_base;

-- Notas de credito con el documento que corrigen.
-- SELECT nc.id_nota_credito, nc.motivo, nc.monto_total,
--        d.tipo_documento, d.id_documento, d.monto_total AS total_original
-- FROM nota_credito nc
-- JOIN documento_facturacion d ON d.id_documento = nc.id_documento_original;