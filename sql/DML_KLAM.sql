-- ============================================================
-- BIOKLAM - datos exclusivamente demostrativos para una instalacion nueva.
-- No ejecutar sobre una base con datos. Las claves de personal son marcadores
-- invalidos: antes de iniciar sesion deben aprovisionarse hashes seguros.
-- No se crean cuentas de cliente automaticamente al registrar clientes.
-- ============================================================

USE `klam`;
-- administrador

INSERT INTO administrador
(id_usuario, username, password_hash, email, nombres, apellidos, rol, activo)
VALUES
    (1, 'admin.rojas',
     'pbkdf2-sha256$600000$g+7HUqedKQGQwrg9cQcPAg==$VJb2Hy2G1/yJ4U4W3d8c289hdYI3j4qGOtdwDNiyB4w=',
     'crojas@bioklam.com',
     'Carlos',
     'Rojas Medina',
     'ADMINISTRADOR',
     1);


-- administradores adicionales (contrasena de todos: Klam2026!)
INSERT INTO administrador
(username, password_hash, email, nombres, apellidos, rol, activo)
VALUES
    ('admin.mendoza', 'pbkdf2-sha256$600000$GC8kfSUiFXYkIt2XFPw1QQ==$D89tuPnKqn+/d/TJoBanasxOmIAu7xjUmMOH6jMKI/4=', 'amendoza@bioklam.com', 'Ana', 'Mendoza Rios', 'ADMINISTRADOR', 1),
    ('admin.vargas', 'pbkdf2-sha256$600000$YnvAOdfUus+N5HFgtwa+6Q==$TL4l3Uel1I9Z1JyR+sXkJo6Yh8/qV8u1gfDGXTLF818=', 'rvargas@bioklam.com', 'Ricardo', 'Vargas Paredes', 'ADMINISTRADOR', 1),
    ('admin.delgado', 'pbkdf2-sha256$600000$jNdD9jPQx9cK80Jn2Uu/7w==$MvGcfDHdHZPskEgU2TTMcYknOsqmUFTPnCj3flyTE58=', 'pdelgado@bioklam.com', 'Patricia', 'Delgado Núñez', 'ADMINISTRADOR', 1),
    ('admin.gutierrez', 'pbkdf2-sha256$600000$LoXGF/Mx51ZvHMsysW3IVQ==$oOtRur0dDPI2OS5QiDNjgcCjFiXtHB7SM5ZE7GPj8vk=', 'fgutierrez@bioklam.com', 'Fernando', 'Gutiérrez Lazo', 'ADMINISTRADOR', 1),
    ('admin.benavides', 'pbkdf2-sha256$600000$07ydLl891KrQ/3uqrYNJzw==$Enaz3SSXTfElewBBbO0iXcQlIMsEPlmncAbcKJNyD5o=', 'cbenavides@bioklam.com', 'Claudia', 'Benavides Pinto', 'ADMINISTRADOR', 1),
    ('admin.montoya', 'pbkdf2-sha256$600000$bWROvCnAO/pcuAEyZELdxA==$gPkrOgMbsV0gAI0zhtQuZyg+jBxxEuCXKwzLHlNONss=', 'jmontoya@bioklam.com', 'Javier', 'Montoya Ruiz', 'ADMINISTRADOR', 1),
    ('admin.espinoza', 'pbkdf2-sha256$600000$RQPYghwsC7obaowhXBBz/Q==$p1PCBIUPPYt61ZsWNUSvNnMOsgigjboUYvmoEQeuACw=', 'respinoza@bioklam.com', 'Rosa', 'Espinoza Cárdenas', 'ADMINISTRADOR', 1),
    ('admin.linares', 'pbkdf2-sha256$600000$Q9uUmc+4rcUeHxhgzlHRuQ==$DNCcc1J8OyGR/NBrYcnZDzBumafzfiHpuaz9CJHRVtU=', 'alinares@bioklam.com', 'Álvaro', 'Linares Vidal', 'ADMINISTRADOR', 1),
    ('admin.tapia', 'pbkdf2-sha256$600000$Qq/KL5xSicozjfgdRo2rLg==$yGYK1LguhzBMVm88D/E5yhIUF3/FzuREIBq2kpinTzs=', 'gtapia@bioklam.com', 'Gabriela', 'Tapia Zúñiga', 'ADMINISTRADOR', 1),
    ('admin.valdivia', 'pbkdf2-sha256$600000$RHCTdcJpHcARKoq2h8GPzA==$OztIBWlQBPDIjayBIeLjpF4cvpshRxnAinVgelEzUfI=', 'hvaldivia@bioklam.com', 'Héctor', 'Valdivia Mora', 'ADMINISTRADOR', 1)
ON DUPLICATE KEY UPDATE username = username;

-- vendedor

INSERT INTO vendedor
(id_usuario, username, password_hash, email, nombres, apellidos, rol, activo, comision_acumulada)
VALUES
    (2, 'vend.torres',
     'pbkdf2-sha256$600000$Z/Ylq7vHuL6zTNNfq+IDKw==$QDUaRy15j4QI+PDwjLgzMj9BSpGXeVPsW3Rw3uvd+XQ=',
     'ltorres@bioklam.com',
     'Lucía',
     'Torres Vega',
     'VENDEDOR',
     1,
     1250.50),

    (3, 'vend.quispe',
     'pbkdf2-sha256$600000$hTur049uWPg2LGHCHqshEQ==$H2C7dimUmZnjhnzUxnu+IX2xaVKTDBM/nBj9Gwtu8uA=',
     'mquispe@bioklam.com',
     'Miguel',
     'Quispe Huamán',
     'VENDEDOR',
     1,
     780.00);


-- vendedores adicionales (contrasena de todos: Klam2026!)
INSERT INTO vendedor
(username, password_hash, email, nombres, apellidos, rol, activo, comision_acumulada)
VALUES
    ('vend.castillo', 'pbkdf2-sha256$600000$KrTqCeIeh/fW5Bt7rl3jig==$1PUc4ABUwYf3ua1fFOUm7/zY0gcGURRSJf7WYk+ebKs=', 'scastillo@bioklam.com', 'Sofía', 'Castillo Ramos', 'VENDEDOR', 1, 0.00),
    ('vend.huaman', 'pbkdf2-sha256$600000$q5prhKj06bT6z7a2kMA6Ig==$GOHWheGUCnRJDVXFZuw7DzaNoiG3OqLSg9qxJrn+Beg=', 'dhuaman@bioklam.com', 'Diego', 'Huamán Flores', 'VENDEDOR', 1, 0.00),
    ('vend.salas', 'pbkdf2-sha256$600000$7IcqlA3ArvQlafphXsfBRg==$XEVGzZiJCaIjEGuBHn7FImHbfU2SwGTm5QX6/foJ2qE=', 'vsalas@bioklam.com', 'Valeria', 'Salas Ortega', 'VENDEDOR', 1, 0.00),
    ('vend.aguirre', 'pbkdf2-sha256$600000$lrzvFSarSznqkwIMHtAG/g==$TGXW4LsoHSjerx7RjdcEvU+C1Ln1cxk5wgAyMf2s3Fs=', 'raguirre@bioklam.com', 'Renzo', 'Aguirre Palma', 'VENDEDOR', 1, 0.00),
    ('vend.quiroz', 'pbkdf2-sha256$600000$6hJ92eu645vMXxtCXqTUUQ==$WLghPvQJr1sujhOTWLnXovlw0jBGApnrSdFFoVoNscU=', 'cquiroz@bioklam.com', 'Camila', 'Quiroz Beltrán', 'VENDEDOR', 1, 0.00),
    ('vend.ccori', 'pbkdf2-sha256$600000$cnasKjnyyz6tMPU9Vpz3qg==$4r+VetLzQa7mGhpsoqrx7zaeW7NG1zkPHsP71H59D/M=', 'mccori@bioklam.com', 'Mateo', 'Ccori Mamani', 'VENDEDOR', 1, 0.00),
    ('vend.rivas', 'pbkdf2-sha256$600000$ezyFgjGRqhaTKHVYqXy0rg==$kNf/k+yndknEfGM725hJplToLCQvck/ddND6jQt1/XY=', 'drivas@bioklam.com', 'Daniela', 'Rivas Cornejo', 'VENDEDOR', 1, 0.00),
    ('vend.ledesma', 'pbkdf2-sha256$600000$5FbYwH486Z7HgemkGqyh6A==$RdNFeivvD3i3G2vEnkC0YSUR2wnYyLrc4vDWfdwvLII=', 'bledesma@bioklam.com', 'Bruno', 'Ledesma Arce', 'VENDEDOR', 1, 0.00),
    ('vend.pacheco', 'pbkdf2-sha256$600000$g4Uc6ex09E6A6hOGREQ7Sg==$nj0M1/LfMFWo7P1jDN6FL4WZt+kuqKg9RrUr73X56Uk=', 'fpacheco@bioklam.com', 'Fiorella', 'Pacheco Gamarra', 'VENDEDOR', 1, 0.00),
    ('vend.neyra', 'pbkdf2-sha256$600000$yx2pdR8q6AFcfplsOlbhDA==$Xpfl7lLiFOsLFdYG4puYKL5a9zq7X+wAY2TNqu5mJlM=', 'jneyra@bioklam.com', 'Joaquín', 'Neyra Bustamante', 'VENDEDOR', 1, 0.00)
ON DUPLICATE KEY UPDATE username = username;
-- tecnico_instrumentista

INSERT INTO tecnico_instrumentista
(id_usuario, username, password_hash, email, nombres, apellidos, rol, activo, especialidad)
VALUES
    (4, 'tec.fernandez',
     'pbkdf2-sha256$600000$OBuOz0/y/qfQeuP5G5xKow==$KR03C/bScqZL9wir/Ucg+v2dYmUxrZ6MdkeeHZUMIK8=',
     'jfernandez@bioklam.com',
     'José',
     'Fernández Castro',
     'TECNICO_INSTRUMENTISTA',
     1,
     'Neurocirugía'),

    (5, 'tec.salazar',
     'pbkdf2-sha256$600000$JRdUMqnM2CpRG5NUTstwOw==$3Uy85jP7NaaR9TxXx7asDlxjFKv/JZScqlG9y880rTM=',
     'psalazar@bioklam.com',
     'Paola',
     'Salazar Díaz',
     'TECNICO_INSTRUMENTISTA',
     1,
     'Cirugía general');


-- tecnicos instrumentistas adicionales (contrasena de todos: Klam2026!)
INSERT INTO tecnico_instrumentista
(username, password_hash, email, nombres, apellidos, rol, activo, especialidad)
VALUES
    ('tec.ramos', 'pbkdf2-sha256$600000$byfO6bSa3IlNNG2Wr97IVw==$tSvrso2wpBCinOb4v5jgcMlepDHNsePJJW7c/CjAz0s=', 'aramos@bioklam.com', 'Andrés', 'Ramos Cueva', 'TECNICO_INSTRUMENTISTA', 1, 'Neurocirugía'),
    ('tec.chavez', 'pbkdf2-sha256$600000$j5HYPnUJi8O8xWGL2XsLzQ==$3QVKHjSJT4cdzk2p6/H7aNdl7ueNZRAbAPWKhnfS0Yg=', 'lchavez@bioklam.com', 'Lucero', 'Chávez Soto', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía general'),
    ('tec.paredes', 'pbkdf2-sha256$600000$8auJFrOXhPmG7X5YkzXziw==$dToJZpnMca9aq/DtWvLpQwh3O8gMyexnFcKKWtA7RsY=', 'cparedes@bioklam.com', 'César', 'Paredes Luna', 'TECNICO_INSTRUMENTISTA', 1, 'Traumatología'),
    ('tec.ayala', 'pbkdf2-sha256$600000$gg5xDZ1HHD3oQz5gdcE2ZQ==$nVItB5VQcLrOqR1LeIJyiohAK63TRGoTGu7ieFqJdzg=', 'mayala@bioklam.com', 'Milagros', 'Ayala Torre', 'TECNICO_INSTRUMENTISTA', 1, 'Neurocirugía'),
    ('tec.zavala', 'pbkdf2-sha256$600000$hLKyAakPhv7uKCg/y9/w8g==$VcAAR9IxRPdv8FBYtlmEq45Clt5L0T6FdLk/72M0Ma4=', 'ozavala@bioklam.com', 'Oscar', 'Zavala Meza', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía de columna'),
    ('tec.villanueva', 'pbkdf2-sha256$600000$8gW/VRYwSWdajWBjp7hu0Q==$zcEvMG6wD3MnxSJllRZ6lPAcYRNM0AWI8RXROtoBe9E=', 'kvillanueva@bioklam.com', 'Karen', 'Villanueva Roca', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía general'),
    ('tec.condori', 'pbkdf2-sha256$600000$Txs9g9KS6ifLuU1HzlrDQA==$U0U1g6TvDf06VPV0Kf6cKsfSw03GoD8Xa9jWaMrvkQY=', 'wcondori@bioklam.com', 'Walter', 'Condori Apaza', 'TECNICO_INSTRUMENTISTA', 1, 'Traumatología'),
    ('tec.rojas', 'pbkdf2-sha256$600000$4vkAR32Hzp4Wb4+5uA7xeA==$a1DhOuUBM0nq0R0707V5nyxUISs2daK6BksHz591BbY=', 'erojas@bioklam.com', 'Estefany', 'Rojas Camacho', 'TECNICO_INSTRUMENTISTA', 1, 'Neurocirugía'),
    ('tec.maguina', 'pbkdf2-sha256$600000$zHl6UmO/YUufZuem19xV9Q==$HY5X0i8bAvtMNafHsOjtmN3m6eNHakE/cSEV4dFVzBc=', 'lmaguina@bioklam.com', 'Luis', 'Maguiña Ponce', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía de columna'),
    ('tec.santos', 'pbkdf2-sha256$600000$h+nuOOFWz16uPZO5iJZmkQ==$XFIsgubDYOqCUNWPagiLHkqnhQfqfJ/fKAXi8BMB3PM=', 'nsantos@bioklam.com', 'Nataly', 'Santos Egoavil', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía general')
ON DUPLICATE KEY UPDATE username = username;
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
    (9, 'Luis Muroya', 'Dirección ficticia 09', 'paciente09@example.test', '000000009', 1, '90000009', 0),
    (10, 'Carla Medina', 'Dirección ficticia 10', 'paciente10@example.test', '000000010', 1, '90000010', 0);

-- cuentas de acceso de pacientes particulares (contrasena de todos: Klam2026!)
INSERT INTO cuenta_usuario
(username, password_hash, email, nombres, apellidos, activo, id_paciente)
SELECT v.username, v.password_hash, v.email, v.nombres, v.apellidos, 1, p.id_cliente
FROM (
  SELECT 'fpaz' AS username, 'pbkdf2-sha256$600000$Xa1fULJdVaGs/yoB0StEfw==$t1SvpETpBITIJs6vSk1XSxpMWrCKxEToq1a78zWflxQ=' AS password_hash, 'fpaz@example.test' AS email, 'Freddy' AS nombres, 'Paz' AS apellidos, '90000002' AS dni
 UNION ALL
  SELECT 'rcueva' AS username, 'pbkdf2-sha256$600000$foFIlK7S6XTqXNCnO1wWUQ==$VyqX9/7Sq+5Wz60u2lLic1G+4IT5HpIff38lpB3Jnrg=' AS password_hash, 'rcueva@example.test' AS email, 'Rony' AS nombres, 'Cueva' AS apellidos, '90000003' AS dni
 UNION ALL
  SELECT 'egomez' AS username, 'pbkdf2-sha256$600000$TFR0rBwM88U9xX/xpOqy4A==$ezBNmrdi7yswtgD52bl+6I2nZiAkwinOjuOwE+OuI7w=' AS password_hash, 'egomez@example.test' AS email, 'Erasmo' AS nombres, 'Gómez' AS apellidos, '90000004' AS dni
 UNION ALL
  SELECT 'dallasi' AS username, 'pbkdf2-sha256$600000$XJRna3w0kbB1RM3sv/2Zpg==$LPLaYEPSjvlAaFP207XXrr27eZMqe4ohSJVusHdPbk0=' AS password_hash, 'dallasi@example.test' AS email, 'David' AS nombres, 'Allasi' AS apellidos, '90000005' AS dni
 UNION ALL
  SELECT 'jcorcuera' AS username, 'pbkdf2-sha256$600000$o2kuDvGLu4gWNAtjgAfgVQ==$gbqsJduLO4m/q7SKB9B8YMEG3n1WOXSQbEfbur6ShCg=' AS password_hash, 'jcorcuera@example.test' AS email, 'José Luis' AS nombres, 'Corcuera' AS apellidos, '90000006' AS dni
 UNION ALL
  SELECT 'mguanira' AS username, 'pbkdf2-sha256$600000$VW/+x+pVXE5rRDQkIOYJGA==$kU/Rej+GJAxfSgBM3DgEct0qwot0A0lAPE0ilXRaK1M=' AS password_hash, 'mguanira@example.test' AS email, 'Miguel' AS nombres, 'Guanira' AS apellidos, '90000007' AS dni
 UNION ALL
  SELECT 'vkhlebnikov' AS username, 'pbkdf2-sha256$600000$mREujNbBSK5I6pBLD/9gWw==$sOTc73pjLgM9eSzq0RTJOZH5DWOQnskvAfMQrmzT6lI=' AS password_hash, 'vkhlebnikov@example.test' AS email, 'Viktor' AS nombres, 'Khlebnikov' AS apellidos, '90000008' AS dni
 UNION ALL
  SELECT 'lmuroya' AS username, 'pbkdf2-sha256$600000$LY7gKsNoW+5dyRmbDZYGOw==$KF/frI2zuPrgytyqSVVkcx7HkAaM5Mf6+Jp7G7L1yl0=' AS password_hash, 'lmuroya@example.test' AS email, 'Luis' AS nombres, 'Muroya' AS apellidos, '90000009' AS dni
 UNION ALL
  SELECT 'cmedina' AS username, 'pbkdf2-sha256$600000$s0JNT4jC8ni+HprbOF8Vxg==$o7dU9HjETSmwJC/jAreYlW+02jvjYzIdmW0AT04tk8E=' AS password_hash, 'cmedina@example.test' AS email, 'Carla' AS nombres, 'Medina' AS apellidos, '90000010' AS dni
) v
JOIN paciente_particular p ON p.dni = v.dni
ON DUPLICATE KEY UPDATE username = cuenta_usuario.username;

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
     '60 días');
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
