-- ============================================================
-- Klam - DML corregido
-- Compatible con el DDL usando Concrete Table Inheritance
-- ============================================================

-- ------------------------------------------------------------
-- administrador
-- ------------------------------------------------------------
INSERT INTO `administrador`
(`id_usuario`, `username`, `password_hash`, `email`, `nombres`, `apellidos`,
 `rol`, `activo`, `id_admin`)
VALUES
    (1, 'admin.rojas',
     '$2y$10$examplehash0000000000000000000001',
     'crojas@bioklam.com',
     'Carlos',
     'Rojas Medina',
     'ADMINISTRADOR',
     1,
     1);


-- ------------------------------------------------------------
-- vendedor
-- ------------------------------------------------------------
INSERT INTO `vendedor`
(`id_usuario`, `username`, `password_hash`, `email`, `nombres`, `apellidos`,
 `rol`, `activo`, `id_vendedor`, `comision_acumulada`)
VALUES
    (2, 'vend.torres',
     '$2y$10$examplehash0000000000000000000002',
     'ltorres@bioklam.com',
     'Lucía',
     'Torres Vega',
     'VENDEDOR',
     1,
     1,
     1250.50),

    (3, 'vend.quispe',
     '$2y$10$examplehash0000000000000000000003',
     'mquispe@bioklam.com',
     'Miguel',
     'Quispe Huamán',
     'VENDEDOR',
     1,
     2,
     780.00);


-- ------------------------------------------------------------
-- tecnico_instrumentista
-- ------------------------------------------------------------
INSERT INTO `tecnico_instrumentista`
(`id_usuario`, `username`, `password_hash`, `email`, `nombres`, `apellidos`,
 `rol`, `activo`, `id_tecnico`, `especialidad`)
VALUES
    (4, 'tec.fernandez',
     '$2y$10$examplehash0000000000000000000004',
     'jfernandez@bioklam.com',
     'José',
     'Fernández Castro',
     'TECNICO_INSTRUMENTISTA',
     1,
     1,
     'Neurocirugía'),

    (5, 'tec.salazar',
     '$2y$10$examplehash0000000000000000000005',
     'psalazar@bioklam.com',
     'Paola',
     'Salazar Díaz',
     'TECNICO_INSTRUMENTISTA',
     1,
     2,
     'Cirugía general');


-- ------------------------------------------------------------
-- bandeja_instrumental
-- ------------------------------------------------------------
INSERT INTO `bandeja_instrumental`
(`id_bandeja`, `tipo`, `esterilizado`, `activo`)
VALUES
    (1, 'Bandeja de craneotomía', 1, 1),
    (2, 'Bandeja de microcirugía', 1, 1),
    (3, 'Bandeja de navegación estereotáxica', 0, 1);


-- ------------------------------------------------------------
-- consumible
-- ------------------------------------------------------------
INSERT INTO `consumible`
(`id_consumible`, `nombre_comercial`, `marca`, `medida`, `activo`)
VALUES
    (1, 'Sutura de nylon',   'Ethicon',  '4-0',      1),
    (2, 'Gasa estéril',      'Medline',  '10x10cm',  1),
    (3, 'Clip de aneurisma', 'Aesculap', 'Mediano',  1),
    (4, 'Cera para hueso',   'Ethicon',  '2.5g',     1);


-- ------------------------------------------------------------
-- bandeja_consumible
-- ------------------------------------------------------------
INSERT INTO `bandeja_consumible`
(`id_bandeja`, `id_consumible`, `cantidad_despachada`, `cantidad_consumida`)
VALUES
    (1, 1, 10, 6),
    (1, 3, 4,  2),
    (2, 2, 20, 15),
    (3, 4, 5,  1);


-- ------------------------------------------------------------
-- equipo
-- ------------------------------------------------------------
INSERT INTO `equipo`
(`id_equipo`, `nombre`, `categoria`, `disponible`, `activo`)
VALUES
    (1, 'Craneótomo Midas Rex',        'CRANEOTOMO', 1, 1),
    (2, 'Navegador StealthStation S8', 'NAVEGADOR',  1, 1),
    (3, 'Craneótomo Anspach eMax',     'CRANEOTOMO', 0, 1);


-- ------------------------------------------------------------
-- equipo_especificacion
-- ------------------------------------------------------------
INSERT INTO `equipo_especificacion`
(`id_equipo`, `clave`, `valor`)
VALUES
    (1, 'voltaje',    '110V'),
    (1, 'peso_kg',    '2.3'),
    (2, 'version_sw', '8.0.1'),
    (3, 'voltaje',    '220V');


-- ============================================================
-- CLIENTES
-- Cliente es abstracta: no existe INSERT a tabla cliente.
-- Los atributos heredados se guardan directamente en cada
-- tabla concreta.
-- ============================================================

-- ------------------------------------------------------------
-- paciente_particular
-- ------------------------------------------------------------
INSERT INTO `paciente_particular`
(`id_cliente`, `nombre`, `direccion`, `email_contacto`, `telefono`, `activo`,
 `dni`, `pago_confirmado`)
VALUES
    (1,
     'Juan Pérez Alarcón',
     'Av. Javier Prado 1234, San Isidro',
     'juan.perez@gmail.com',
     '987654321',
     1,
     '45678912',
     1);


-- ------------------------------------------------------------
-- clinica_hospital
-- ------------------------------------------------------------
INSERT INTO `clinica_hospital`
(`id_cliente`, `nombre`, `direccion`, `email_contacto`, `telefono`, `activo`,
 `ruc`, `tiene_consignacion`, `periodo_credito`)
VALUES
    (2,
     'Clínica San Felipe',
     'Av. Gregorio Escobedo 650, Jesús María',
     'contacto@sanfelipe.com.pe',
     '014632424',
     1,
     '20100123456',
     1,
     '30 días'),

    (3,
     'Hospital Nacional Dos de Mayo',
     'Av. Grau 13, Cercado de Lima',
     'logistica@hdosdemayo.gob.pe',
     '014284700',
     1,
     '20131312955',
     0,
     '60 días');


-- ------------------------------------------------------------
-- orden_compra
-- ------------------------------------------------------------
INSERT INTO `orden_compra`
(`id_orden_compra`, `archivo_respaldo_path`, `fecha_recepcion`, `activo`)
VALUES
    (1,
     '/docs/oc/OC-2026-001.pdf',
     '2026-08-10 09:15:00',
     1);


-- ============================================================
-- DOCUMENTOS DE INGRESO
-- UsuarioPlataforma es abstracta: se usa la FK concreta que
-- corresponda y las demás quedan en NULL.
-- ============================================================

INSERT INTO `documento_ingreso`
(`id_documento`, `tipo_documento`, `archivo_path`, `estado_validacion`,
 `fecha_carga`, `id_orden_compra`, `activo`,
 `id_administrador`, `id_vendedor`, `id_tecnico`)
VALUES
    (1,
     'DNI',
     '/docs/ingreso/dni_juanperez.pdf',
     'VALIDADO',
     '2026-08-01 10:00:00',
     NULL,
     1,
     1, NULL, NULL),

    (2,
     'ORDEN_COMPRA',
     '/docs/ingreso/oc_sanfelipe.pdf',
     'VALIDADO',
     '2026-08-09 16:30:00',
     1,
     1,
     1, NULL, NULL),

    (3,
     'RECETA_MEDICA',
     '/docs/ingreso/receta_2demayo.pdf',
     'PENDIENTE',
     '2026-08-12 08:45:00',
     NULL,
     1,
     NULL, 2, NULL);


-- ============================================================
-- CIRUGIAS
-- Cliente es abstracta: cada cirugía referencia exactamente
-- una clase concreta de Cliente.
-- ============================================================

INSERT INTO `cirugia`
(`id_cirugia`, `fecha_hora_inicio`, `fecha_hora_fin`,
 `tipo_procedimiento`, `doctor_nombre`, `motivo_cancelacion`,
 `estado`, `id_equipo`, `id_bandeja`,
 `id_clinica_hospital`, `id_paciente_particular`, `activo`)
VALUES
    (1,
     '2026-08-15 08:00:00',
     '2026-08-15 11:30:00',
     'Craneotomía por tumor',
     'Dr. Alberto Núñez',
     NULL,
     'FINALIZADA',
     1,
     1,
     NULL,
     1,
     1),

    (2,
     '2026-08-20 14:00:00',
     NULL,
     'Clipaje de aneurisma',
     'Dra. Silvia Ramos',
     NULL,
     'PROGRAMADA',
     2,
     3,
     2,
     NULL,
     1),

    (3,
     '2026-08-22 09:00:00',
     NULL,
     'Descompresión microvascular',
     'Dr. Raúl Ochoa',
     'Paciente no se presentó',
     'CANCELADA',
     NULL,
     NULL,
     3,
     NULL,
     1);


-- ------------------------------------------------------------
-- cotizacion
-- ------------------------------------------------------------
INSERT INTO `cotizacion`
(`id_cotizacion`, `id_cirugia`, `precio_pactado`,
 `estado`, `fecha_emision`, `activo`)
VALUES
    (1, 1, 8500.00,  'ACEPTADA', '2026-08-05 12:00:00', 1),
    (2, 2, 12300.00, 'EMITIDA',  '2026-08-16 10:00:00', 1);


-- ============================================================
-- FACTURACION
-- DocumentoFacturacion es abstracta: no existe INSERT a una
-- tabla documento_facturacion.
-- Factura y Boleta contienen directamente los atributos comunes.
-- ============================================================

-- ------------------------------------------------------------
-- boleta
-- ------------------------------------------------------------
INSERT INTO `boleta`
(`id_documento`, `id_cirugia`, `fecha_emision`,
 `monto_base`, `tasa_igv`, `igv`, `monto_total`,
 `estado_pago`, `activo`, `dni_receptor`)
VALUES
    (1,
     1,
     '2026-08-15 12:00:00',
     8500.00,
     0.1800,
     1530.00,
     10030.00,
     'PAGADO',
     1,
     '45678912');


-- ------------------------------------------------------------
-- factura
-- ------------------------------------------------------------
INSERT INTO `factura`
(`id_documento`, `id_cirugia`, `fecha_emision`,
 `monto_base`, `tasa_igv`, `igv`, `monto_total`,
 `estado_pago`, `activo`, `ruc_receptor`)
VALUES
    (2,
     1,
     '2026-08-15 12:05:00',
     1200.00,
     0.1800,
     216.00,
     1416.00,
     'PENDIENTE',
     1,
     '20100123456');


-- ------------------------------------------------------------
-- linea_boleta
-- ------------------------------------------------------------
INSERT INTO `linea_boleta`
(`id_linea`, `id_documento`, `id_consumible`,
 `item_referencia`, `descripcion`, `cantidad`, `precio_unitario`)
VALUES
    (1,
     1,
     NULL,
     'SRV-CIR',
     'Servicio de cirugía - Craneotomía por tumor',
     1,
     8500.00);


-- ------------------------------------------------------------
-- linea_factura
-- ------------------------------------------------------------
INSERT INTO `linea_factura`
(`id_linea`, `id_documento`, `id_consumible`,
 `item_referencia`, `descripcion`, `cantidad`, `precio_unitario`)
VALUES
    (1, 2, 1, NULL, 'Sutura de nylon 4-0',       6, 100.00),
    (2, 2, 3, NULL, 'Clip de aneurisma mediano', 2, 300.00);


-- ------------------------------------------------------------
-- nota_credito
-- La nota original del DML apuntaba al documento 2, que era
-- una FACTURA. Ahora se referencia directamente a factura.
-- ------------------------------------------------------------
INSERT INTO `nota_credito`
(`id_nota_credito`, `id_factura_original`, `id_boleta_original`,
 `motivo`, `fecha_emision`, `monto_total`, `activo`)
VALUES
    (1,
     2,
     NULL,
     'Devolución parcial de consumibles no utilizados',
     '2026-08-18 09:00:00',
     300.00,
     1);


-- ------------------------------------------------------------
-- linea_nota_credito
-- ------------------------------------------------------------
INSERT INTO `linea_nota_credito`
(`id_linea`, `id_nota_credito`, `id_consumible`,
 `item_referencia`, `descripcion`, `cantidad`,
 `precio_unitario`, `motivo_devolucion`)
VALUES
    (1,
     1,
     3,
     NULL,
     'Clip de aneurisma mediano',
     1,
     300.00,
     'Empaque no utilizado, devuelto sin abrir');


-- ------------------------------------------------------------
-- linea_orden_compra
-- ------------------------------------------------------------
INSERT INTO `linea_orden_compra`
(`id_linea`, `id_orden_compra`, `id_consumible`,
 `item_referencia`, `descripcion`, `cantidad`, `precio_unitario`)
VALUES
    (1, 'OC-2026-001', 2, NULL, 'Gasa estéril 10x10cm', 50, 5.50),
    (2, 'OC-2026-001', 4, NULL, 'Cera para hueso 2.5g', 10, 35.00);


-- ============================================================
-- NOTIFICACIONES
-- UsuarioPlataforma es abstracta: se indica el subtipo concreto.
-- El DML original no incluía un valor para mensaje; se deja NULL.
-- ============================================================

INSERT INTO `notificacion`
(`id_notificacion`, `fecha_hora`, `titulo`, `mensaje`,
 `estado_leida`, `id_administrador`, `id_vendedor`, `id_tecnico`)
VALUES
    (1,
     '2026-08-16 10:01:00',
     'Nueva solicitud de cotización',
     NULL,
     0,
     1, NULL, NULL),

    (2,
     '2026-08-12 08:46:00',
     'Nuevo ingreso de documento pendiente de validar',
     NULL,
     0,
     1, NULL, NULL),

    (3,
     '2026-08-15 12:05:00',
     'Nueva cirugía finalizada',
     NULL,
     1,
     NULL, 2, NULL);
