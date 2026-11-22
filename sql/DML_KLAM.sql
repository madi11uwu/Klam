-- ============================================================
-- BIOKLAM - datos exclusivamente demostrativos para una instalacion nueva.
-- No ejecutar sobre una base con datos. Todas las cuentas de demostracion
-- utilizan BioKlam2026!; cambiarla antes de usar una instalacion real.
-- No se crean cuentas de cliente automaticamente al registrar clientes.
-- ============================================================

USE `klam`;
-- administrador

INSERT INTO administrador
(id_usuario, username, password_hash, email, nombres, apellidos, rol, activo)
VALUES
    (1, 'admin.rojas',
     'pbkdf2-sha256$600000$t5crEQdzTQyfSOtL8LD6uw==$TJaYB/lLMO3Dad9VWbWFTQFkRFLlrEP9/CZr9nnvEfw=',
     'crojas@bioklam.com',
     'Carlos',
     'Rojas Medina',
     'ADMINISTRADOR',
     1);

-- vendedor

INSERT INTO vendedor
(id_usuario, username, password_hash, email, nombres, apellidos, rol, activo, comision_acumulada)
VALUES
    (2, 'vend.torres',
     'pbkdf2-sha256$600000$1jM7aEO/PRp5Fcxp95HgmQ==$JivBlAFV3c0UagKNpUkz0NVq/2M+gnmCLI9MfTbytzw=',
     'ltorres@bioklam.com',
     'Lucía',
     'Torres Vega',
     'VENDEDOR',
     1,
     1250.50),

    (3, 'vend.quispe',
     'pbkdf2-sha256$600000$BZCk0rEF6fII02buR10toQ==$4gv/19Ylye797A4eVXD1bEZBaxPgaEsHVjHP2AkgwXo=',
     'mquispe@bioklam.com',
     'Miguel',
     'Quispe Huamán',
     'VENDEDOR',
     1,
     780.00);
-- tecnico_instrumentista

INSERT INTO tecnico_instrumentista
(id_usuario, username, password_hash, email, nombres, apellidos, rol, activo, especialidad)
VALUES
    (4, 'tec.fernandez',
     'pbkdf2-sha256$600000$zLnK3alcvwY7Rs4mb65W1Q==$RhQwczDUPSbtE1h5r0NernVIhc2tD6o7UzvJQxhayEs=',
     'jfernandez@bioklam.com',
     'José',
     'Fernández Castro',
     'TECNICO_INSTRUMENTISTA',
     1,
     'Neurocirugía'),

    (5, 'tec.salazar',
     'pbkdf2-sha256$600000$uefwonF9fmE6XJaXIXalqQ==$X9EWplDkm0xSPojhy+C71xL5tkXRCOZBAZVVrmFZZw0=',
     'psalazar@bioklam.com',
     'Paola',
     'Salazar Díaz',
     'TECNICO_INSTRUMENTISTA',
     1,
     'Cirugía general');
-- Catalogo de procedimientos: cráneo (bandeja corta) y columna (bandeja larga).
-- Los procedimientos se identifican por bandeja_instrumental.procedimiento.

INSERT INTO bandeja_instrumental
(id_bandeja, tipo, procedimiento, esterilizado, activo)
VALUES
    (1, 'Bandeja de craneotomía', 'Cirugía de cráneo', 1, 1),
    (2, 'Bandeja de microcirugía', 'Cirugía de cráneo', 1, 1),
    (3, 'Bandeja de navegación estereotáxica', 'Cirugía de cráneo', 0, 1),
    (4, 'Bandeja larga de columna', 'Cirugía de columna', 1, 1),
    (5, 'Bandeja larga de vértebras', 'Cirugía de columna', 1, 1);

-- consumible

INSERT INTO consumible
(id_consumible, nombre_comercial, marca, medida, activo)
VALUES
    (1, 'Sutura de nylon',   'Ethicon',  '4-0',      1),
    (2, 'Gasa estéril',      'Medline',  '10x10cm',  1),
    (3, 'Clip de aneurisma', 'Aesculap', 'Mediano',  1),
    (4, 'Cera para hueso',   'Ethicon',  '2.5g',     1),
    (5, 'Tornillo pedicular', 'Demostración', '6 mm', 1);

-- Composicion estandar reutilizable: no representa despacho ni consumo real.
INSERT INTO bandeja_consumible_estandar
(id_bandeja, id_consumible, cantidad_estandar)
VALUES
    (1, 1, 10), (1, 3, 4),
    (2, 2, 20), (3, 4, 5),
    (4, 1, 8), (4, 2, 20), (4, 5, 6),
    (5, 2, 16), (5, 5, 8);

-- Datos historicos de demostracion de despacho/consumo, separados del estandar.

INSERT INTO bandeja_consumible
(id_bandeja, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES
    (1, 1, 10, 6),
    (1, 3, 4,  2),
    (2, 2, 20, 15),
    (3, 4, 5,  1);

-- equipo

INSERT INTO equipo
(id_equipo, nombre, categoria, disponible, activo)
VALUES
    (1, 'Craneótomo Midas Rex',        'CRANEOTOMO', 1, 1),
    (2, 'Navegador StealthStation S8', 'NAVEGADOR',  1, 1),
    (3, 'Craneótomo Anspach eMax',     'CRANEOTOMO', 0, 1);

-- equipo_especificacion

INSERT INTO equipo_especificacion
(id_equipo, clave, valor)
VALUES
    (1, 'marca',      'Medtronic'),
    (1, 'voltaje',    '110V'),
    (1, 'peso_kg',    '2.3'),
    (2, 'marca',      'Medtronic'),
    (2, 'version_sw', '8.0.1'),
    (3, 'marca',      'DePuy Synthes'),
    (3, 'voltaje',    '220V');

-- ============================================================
-- CLIENTES
-- ============================================================

-- La tabla tiene una sola columna nombre: contiene nombre y apellido completos.
-- DNI, telefono, correo y direccion son valores sinteticos de laboratorio.

INSERT INTO paciente_particular
(id_cliente, nombre, direccion, email_contacto, telefono, activo, dni, pago_confirmado)
VALUES
    (1, 'Eric Huiza', 'Dirección ficticia 01', 'paciente01@example.test', '000000001', 1, '90000001', 1),
    (2, 'Freddy Paz', 'Dirección ficticia 02', 'paciente02@example.test', '000000002', 1, '90000002', 0),
    (3, 'Rony Cueva', 'Dirección ficticia 03', 'paciente03@example.test', '000000003', 1, '90000003', 0),
    (4, 'Erasmo Gómez', 'Dirección ficticia 04', 'paciente04@example.test', '000000004', 1, '90000004', 0),
    (5, 'David Allasi', 'Dirección ficticia 05', 'paciente05@example.test', '000000005', 1, '90000005', 0),
    (6, 'José Luis Corcuera', 'Dirección ficticia 06', 'paciente06@example.test', '000000006', 1, '90000006', 0),
    (7, 'Miguel Guanira', 'Dirección ficticia 07', 'paciente07@example.test', '000000007', 1, '90000007', 0),
    (8, 'Viktor Khlebnikov', 'Dirección ficticia 08', 'paciente08@example.test', '000000008', 1, '90000008', 0),
    (9, 'Luis Muroya', 'Dirección ficticia 09', 'paciente09@example.test', '000000009', 1, '90000009', 0);

-- clinica_hospital

INSERT INTO clinica_hospital
(id_cliente, nombre, direccion, email_contacto, telefono, activo, ruc, tiene_consignacion, periodo_credito)
VALUES
    (2,
     'Clínica Académica Norte',
     'Dirección ficticia clínica 01',
     'clinica01@example.test',
     '000000010',
     1,
     '20900000001',
     1,
     '30 días'),

    (3,
     'Hospital Simulado Sur',
     'Dirección ficticia hospital 02',
     'hospital02@example.test',
     '000000011',
     1,
     '20900000002',
     0,
     '60 días'),
    -- Instituciones reales; solo el nombre y el RUC son datos institucionales.
    -- Los accesos asociados más abajo son exclusivamente de laboratorio.
    (4, 'Clínica San Felipe', NULL, NULL, NULL, 1, '20100129841', 0, NULL),
    (5, 'Clínica Internacional', NULL, NULL, NULL, 1, '20100054184', 0, NULL),
    (6, 'Clínica Auna Vallesur', NULL, NULL, NULL, 1, '20454135432', 0, NULL),
    (7, 'Clínica Auna Bellavista', NULL, NULL, NULL, 1, '20100251176', 0, NULL),
    (8, 'Hospital Nacional Dos de Mayo', NULL, NULL, NULL, 1, '20160388570', 0, NULL),
    (9, 'Hospital Nacional Arzobispo Loayza', NULL, NULL, NULL, 1, '20154996991', 0, NULL),
    (10, 'Hospital Cayetano Heredia', NULL, NULL, NULL, 1, '20154528971', 0, NULL),
    (11, 'Hospital San Juan de Dios de Pisco', NULL, NULL, NULL, 1, '20452578949', 0, NULL);

-- Cuentas de representantes FICTICIOS para probar el acceso de clínicas.
-- No implican autorización de las instituciones reales. Contraseña de laboratorio:
-- BioKlam2026! (PBKDF2-SHA256 con sal diferente por cuenta).
-- El registro comercial y la habilitación de acceso siguen siendo operaciones separadas.
INSERT INTO cuenta_usuario
(username, password_hash, email, nombres, apellidos, activo, id_paciente, id_clinica)
VALUES
    ('lab.clinica.norte',
     'pbkdf2-sha256$600000$Kl/lylWb/4oK7UhzFV3chw==$WvpRqz18vlI0mLVe9VOhOdWsfwLoSvQijCdHXioxWRA=',
     'acceso.norte@example.test', 'Usuario', 'de laboratorio', 1, NULL, 2),
    ('lab.hospital.sur',
     'pbkdf2-sha256$600000$SZiaFXdPWSUW+j22Fn1s7w==$OHmstkqoCRZ1F7TSUa4SR6ot951vvxxL9+rfCov6q1Y=',
     'acceso.sur@example.test', 'Usuario', 'de laboratorio', 1, NULL, 3),
    ('lab.clinica.sanfelipe',
     'pbkdf2-sha256$600000$Z/2TB4Wlm2vC4MGxf75ruQ==$azaArPZwFEjA6OlcJEuoK1tDDppFZbq3HjLuWp1J2xM=',
     'acceso.sanfelipe@example.test', 'Usuario', 'de laboratorio', 1, NULL, 4),
    ('lab.clinica.internacional',
     'pbkdf2-sha256$600000$QuO9dUXZknDsbj8ZBwM7zQ==$+2ILqxRDPbYazNEfwpqVQjnI3kyo/vwfdDZM9Suos/A=',
     'acceso.internacional@example.test', 'Usuario', 'de laboratorio', 1, NULL, 5),
    ('lab.clinica.vallesur',
     'pbkdf2-sha256$600000$ZpzsWBNoo6Vv1sGHEzFxcg==$NUF7/ykxCMSgg785ygX2/ePNSGS8tvupkHIAPqggeUY=',
     'acceso.vallesur@example.test', 'Usuario', 'de laboratorio', 1, NULL, 6),
    ('lab.clinica.bellavista',
     'pbkdf2-sha256$600000$+m25ECrKNcEPGmkA6goEXA==$eGA3Zz1VFMNax19lpz1vthKXHULjzCLm2go3ZYTxsBY=',
     'acceso.bellavista@example.test', 'Usuario', 'de laboratorio', 1, NULL, 7),
    ('lab.hospital.dosdemayo',
     'pbkdf2-sha256$600000$FUifYl1sM3V4jWmxAo3iRg==$ndJwxMJ4MnWouOyMe1jsVS7KMsPNlASi5r9oyPdBmpY=',
     'acceso.dosdemayo@example.test', 'Usuario', 'de laboratorio', 1, NULL, 8),
    ('lab.hospital.loayza',
     'pbkdf2-sha256$600000$NjX1+oSNOAT3DWaKwLDNLA==$paeer5sBa0SKakQNGYJ8Ji6rpFa+gJ/Tscxj099Ljpw=',
     'acceso.loayza@example.test', 'Usuario', 'de laboratorio', 1, NULL, 9),
    ('lab.hospital.cayetano',
     'pbkdf2-sha256$600000$/HiTDbI9CrtgZXUtotm2Qg==$UyYZ5i1pTySFKdbUnb1DoTuREBm8G3h5Str2nVoh5Os=',
     'acceso.cayetano@example.test', 'Usuario', 'de laboratorio', 1, NULL, 10),
    ('lab.hospital.pisco',
     'pbkdf2-sha256$600000$JYqv0xju8qEDRtrpheFjAQ==$apYqgfUSt4PLdyEEQAj8SswDwKwAv0BARAWhnije8/o=',
     'acceso.pisco@example.test', 'Usuario', 'de laboratorio', 1, NULL, 11);
-- orden_compra

INSERT INTO orden_compra
(id_orden_compra, archivo_respaldo_path, fecha_recepcion, activo)
VALUES
    (1,
     '/docs/oc/OC-2026-001.pdf',
     '2026-08-10 09:15:00',
     1);

-- ============================================================
-- DOCUMENTOS DE INGRESO
-- ============================================================
INSERT INTO documento_ingreso
(id_documento, tipo_documento, archivo_path, estado_validacion,
 fecha_carga, id_orden_compra, activo,
 id_administrador, id_vendedor, id_tecnico)
VALUES
    (1,
     'DNI',
     '/docs/ingreso/demo_dni_eric.pdf',
     'VALIDADO',
     '2026-08-01 10:00:00',
     NULL,
     1,
     1, NULL, NULL),

    (2,
     'ORDEN_COMPRA',
     '/docs/ingreso/demo_oc_clinica.pdf',
     'VALIDADO',
     '2026-08-09 16:30:00',
     1,
     1,
     1, NULL, NULL),

    (3,
     'RECETA_MEDICA',
     '/docs/ingreso/demo_receta_hospital.pdf',
     'PENDIENTE',
     '2026-08-12 08:45:00',
     NULL,
     1,
     NULL, 2, NULL);
-- ============================================================
-- CIRUGIAS
-- ============================================================
INSERT INTO cirugia
(id_cirugia, fecha_hora_inicio, fecha_hora_fin,
 tipo_procedimiento, doctor_nombre, diagnostico, paciente_nombre, paciente_documento, motivo_cancelacion,
 estado, id_equipo, id_bandeja,
 id_clinica_hospital, id_paciente_particular, activo)
VALUES
    (1,
     '2026-08-15 08:00:00',
     '2026-08-15 11:30:00',
     'Cirugía de cráneo',
     'Dr. Alberto Núñez',
     'Tumor cerebral frontal',
     'Eric Huiza',
     '90000001',
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
     'Cirugía de cráneo',
     'Dra. Silvia Ramos',
     'Aneurisma de la arteria comunicante anterior',
     'Freddy Paz',
     '90000002',
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
     'Cirugía de cráneo',
     'Dr. Raúl Ochoa',
     'Neuralgia del trigémino',
     'Rony Cueva',
     '90000003',
     'Paciente no se presentó',
     'CANCELADA',
     NULL,
     NULL,
     3,
     NULL,
     1),

    -- Ejemplos de historial y bitacora que antes estaban en rol2_historial_auditoria.sql.
    (4, '2026-11-20 11:30:00', NULL, 'Cirugía de cráneo',
     'Dra. Silvia Ramos', '[DEMO R2] Lesión craneal de prueba',
     'Eric Huiza', '90000001', NULL, 'PROGRAMADA',
     NULL, 1, NULL, 1, 1),
    (5, '2026-11-12 08:00:00', NULL, 'Cirugía de cráneo',
     'Dr. Alberto Núñez', '[DEMO R2] Diagnóstico de prueba',
     'Eric Huiza', '90000001', 'Paciente no apto', 'CANCELADA',
     NULL, 1, NULL, 1, 1);

-- Personalizacion prevista de una cirugia: no modifica la plantilla.
INSERT INTO cirugia_consumible_previsto
(id_cirugia, id_consumible, cantidad_prevista)
VALUES (1, 1, 10), (1, 3, 4), (2, 4, 5);

-- Resultado final de la demostracion RF10/RF16. La bitacora conserva los
-- pasos previos; no se ejecutan UPDATE ni DELETE sobre datos de prueba.
INSERT INTO auditoria_cirugia
(id_auditoria, id_cirugia, fecha_hora, accion, responsable, detalle,
 valores_anteriores, valores_posteriores)
VALUES
    (1, 4, '2026-11-10 09:15:00', 'CREAR', 'Carlos Rojas Medina',
     'Alta de programación', NULL,
     'estado=PROGRAMADA; fechaHoraInicio=2026-11-20T10:00'),
    (2, 4, '2026-11-10 09:40:00', 'EDITAR', 'Carlos Rojas Medina',
     'Cambio de fecha y médico',
     'fechaHoraInicio=2026-11-20T10:00; doctorNombre=Dr. Alberto Núñez',
     'fechaHoraInicio=2026-11-20T11:30; doctorNombre=Dra. Silvia Ramos'),
    (3, 5, '2026-11-10 09:20:00', 'CREAR', 'Carlos Rojas Medina',
     'Alta de programación', NULL, 'estado=PROGRAMADA'),
    (4, 5, '2026-11-10 10:05:00', 'CANCELAR', 'Carlos Rojas Medina',
     'Cirugía cancelada: Paciente no apto',
     'estado=PROGRAMADA', 'estado=CANCELADA; motivoCancelacion=Paciente no apto');
-- cotizacion

INSERT INTO cotizacion
(id_cotizacion, id_cirugia, precio_pactado, estado, fecha_emision, activo)
VALUES
    (1, 1, 8500.00,  'ACEPTADA', '2026-08-05 12:00:00', 1),
    (2, 2, 12300.00, 'EMITIDA',  '2026-08-16 10:00:00', 1);

-- ============================================================
-- ============================================================

-- linea_orden_compra

INSERT INTO linea_orden_compra
(id_linea, id_orden_compra, id_consumible,
 item_referencia, descripcion, cantidad, precio_unitario)
VALUES
    (1, 1, 2, NULL, 'Gasa estéril 10x10cm', 50, 5.50),
    (2, 1, 4, NULL, 'Cera para hueso 2.5g', 10, 35.00);

-- ============================================================
-- NOTIFICACIONES
-- ============================================================
INSERT INTO notificacion
(id_notificacion, fecha_hora, titulo, mensaje,
 estado_leida, id_administrador, id_vendedor, id_tecnico)
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
