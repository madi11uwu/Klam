USE `klam`;

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

INSERT INTO administrador
(username, password_hash, email, nombres, apellidos, rol, activo)
VALUES
    ('izegarra', 'pbkdf2-sha256$600000$9MQBiUz2MM7sV7OfBEQ+iw==$VoLvg0eWbexy6flIjhyx81S8uHRlDZi93dmzU1lDPHw=', 'izegarra@bioklam.com', 'Isabela', 'Zegarra Becerra', 'ADMINISTRADOR', 1),
    ('admin.mendoza', 'pbkdf2-sha256$600000$7uaXBZTfVn1zhUOlrTRyiw==$FRv40qZ+++kb/mBKA2m9fD6UNtdT+iDRKsuoWVbsLrY=', 'amendoza@bioklam.com', 'Ana', 'Mendoza Rios', 'ADMINISTRADOR', 1),
    ('admin.vargas', 'pbkdf2-sha256$600000$aP9bT+aTeIcb/vknvhdCoQ==$Sc0Bmeg1n6JCiwACg0Sx12Yv+9EqZctd0nYFsVuWgvs=', 'rvargas@bioklam.com', 'Ricardo', 'Vargas Paredes', 'ADMINISTRADOR', 1),
    ('admin.delgado', 'pbkdf2-sha256$600000$t3f1ie+pefNhtPNDFCTZpg==$O/ZWNhSnZQcZvOdOpfK2dRQ0iGmCVt1yFYvK63Q3Ghk=', 'pdelgado@bioklam.com', 'Patricia', 'Delgado Núñez', 'ADMINISTRADOR', 1),
    ('admin.gutierrez', 'pbkdf2-sha256$600000$OCzcoTn4E1uTyhctzcOR9Q==$Lt3IT9zBh/7oQUtVbfIkLnF7ec7e26JVWDDZ25ErGO0=', 'fgutierrez@bioklam.com', 'Fernando', 'Gutiérrez Lazo', 'ADMINISTRADOR', 1),
    ('admin.benavides', 'pbkdf2-sha256$600000$KU4SY4ZbrijRiRFtFOwMBA==$sBt1No57g+UBT9kGiLWjtFkHyh1HCuLYhp9AQ+0ejII=', 'cbenavides@bioklam.com', 'Claudia', 'Benavides Pinto', 'ADMINISTRADOR', 1),
    ('admin.montoya', 'pbkdf2-sha256$600000$MlzLA4Jyuqdw8qm/fnVOig==$bG/HQNDlcOculuC89MVzp8d1ei3620nwp/Xps1c3gdI=', 'jmontoya@bioklam.com', 'Javier', 'Montoya Ruiz', 'ADMINISTRADOR', 1),
    ('admin.espinoza', 'pbkdf2-sha256$600000$2MWCIw9+ESxcwXaA8bDrEA==$43PUbE4Yzsg5habZJX1NeTJQ93/+rteEsdTHeSr1jmo=', 'respinoza@bioklam.com', 'Rosa', 'Espinoza Cárdenas', 'ADMINISTRADOR', 1),
    ('admin.linares', 'pbkdf2-sha256$600000$VpWizvibyF1GL/ClUdl2Dw==$xLGPaiKgLtaKj28pOGVDASogf/ua9VHLNjNHo1HRr04=', 'alinares@bioklam.com', 'Álvaro', 'Linares Vidal', 'ADMINISTRADOR', 1),
    ('admin.tapia', 'pbkdf2-sha256$600000$QIdXfNcDcX530OccOcmx7g==$OYcPFHvdH19gDWF92rynngNv1P8KgkCl8CyuiFcBWqM=', 'gtapia@bioklam.com', 'Gabriela', 'Tapia Zúñiga', 'ADMINISTRADOR', 1),
    ('admin.valdivia', 'pbkdf2-sha256$600000$czsEiYSocNWxGrTxB6dHbw==$42X7aFngw7bhZW3SybsJwU1a2+HBWFdXJZ3Aa66viYI=', 'hvaldivia@bioklam.com', 'Héctor', 'Valdivia Mora', 'ADMINISTRADOR', 1)
ON DUPLICATE KEY UPDATE username = username;


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

INSERT INTO vendedor
(username, password_hash, email, nombres, apellidos, rol, activo, comision_acumulada)
VALUES
    ('mobregon', 'pbkdf2-sha256$600000$6UhkskO3QRzZyamTLD8DLw==$EzrgNhpn92dJ8OMBtVugdtUdKNwimRtoRgsPVsM7G7c=', 'mobregon@bioklam.com', 'Madison', 'Obregón Cruz', 'VENDEDOR', 1, 0.00),
    ('jparra', 'pbkdf2-sha256$600000$MSjqcqrAIv11auQbXRh7HA==$RaO2yvjjg4dsVLT6P1hm6hLX6zumvXobGpcZq4xmJ/0=', 'jparra@bioklam.com', 'José Felipe Sebastián', 'Parra Salazar', 'VENDEDOR', 1, 0.00),
    ('vend.castillo', 'pbkdf2-sha256$600000$zgW5w5p8PlyrgVndzIwNxQ==$APWgVirUvHjwMG+8Itr4KhhKVMuZqRKGtKO2kjVuAo4=', 'scastillo@bioklam.com', 'Sofía', 'Castillo Ramos', 'VENDEDOR', 1, 0.00),
    ('vend.huaman', 'pbkdf2-sha256$600000$p4KS0YxMR3/7tu5AUhT/MA==$czTq/zRO07Fh/PQNuydULLX1IRrVQ1/FJB7VsBVjJNE=', 'dhuaman@bioklam.com', 'Diego', 'Huamán Flores', 'VENDEDOR', 1, 0.00),
    ('vend.salas', 'pbkdf2-sha256$600000$B+Suc3a/oOzw516pg/wRhQ==$n/I2dYFUkktEhHkHZDLbgFdj+MtdTnE6eukalhLBC1o=', 'vsalas@bioklam.com', 'Valeria', 'Salas Ortega', 'VENDEDOR', 1, 0.00),
    ('vend.aguirre', 'pbkdf2-sha256$600000$mKLE9cmOTvlmdeID0Yx6zA==$pVgDwHEVPvLJtB9Gy27nu8gYd02UY1Dus6W/ephuHOo=', 'raguirre@bioklam.com', 'Renzo', 'Aguirre Palma', 'VENDEDOR', 1, 0.00),
    ('vend.quiroz', 'pbkdf2-sha256$600000$nNqxZWUvZRrNZx3v32jFVA==$8j5XzBJD1d7cyTfuW64rkgfW2j2uRgFxGbu9NejeVAI=', 'cquiroz@bioklam.com', 'Camila', 'Quiroz Beltrán', 'VENDEDOR', 1, 0.00),
    ('vend.ccori', 'pbkdf2-sha256$600000$e4FZarVQbRqCcE0VLFWzdA==$Ne81Rn5msSXhp2O99tLJaFvZLzWM29jtejHChUjH0yA=', 'mccori@bioklam.com', 'Mateo', 'Ccori Mamani', 'VENDEDOR', 1, 0.00),
    ('vend.rivas', 'pbkdf2-sha256$600000$qybNODmprjJCvoIcORBsxA==$y8jp8Y6UbOk2bH181gutkEK35IQxU5+wELXChEDblXw=', 'drivas@bioklam.com', 'Daniela', 'Rivas Cornejo', 'VENDEDOR', 1, 0.00),
    ('vend.ledesma', 'pbkdf2-sha256$600000$yX9xc4A4dAWJEp2Y5/nsMw==$fBYtEuS4L4udjAAYXkNISH4h8taftp/F5DqtaMF+SBM=', 'bledesma@bioklam.com', 'Bruno', 'Ledesma Arce', 'VENDEDOR', 1, 0.00),
    ('vend.pacheco', 'pbkdf2-sha256$600000$282sT7qt6/ZZ5iKty71q5g==$56M+8DxASdGR5Kz60VofhNP4PSciiVwF0yI1q2J5Jy0=', 'fpacheco@bioklam.com', 'Fiorella', 'Pacheco Gamarra', 'VENDEDOR', 1, 0.00),
    ('vend.neyra', 'pbkdf2-sha256$600000$arUqqp64vHNw0V+K3WlIDQ==$1NEbNYZ7ulBzoku/HIhCFrGL0g0ZDiVqFF1k02h6ZQ4=', 'jneyra@bioklam.com', 'Joaquín', 'Neyra Bustamante', 'VENDEDOR', 1, 0.00)
ON DUPLICATE KEY UPDATE username = username;

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

INSERT INTO tecnico_instrumentista
(username, password_hash, email, nombres, apellidos, rol, activo, especialidad)
VALUES
    ('hcabello', 'pbkdf2-sha256$600000$LXxcOwmN5ylnQy/Yj5lrYA==$rkQssHZq5TF5g43gNEpvMyCfhUc1y+tqlSuuxhlbI2Q=', 'hcabello@bioklam.com', 'Halím', 'Cabello Mendoza', 'TECNICO_INSTRUMENTISTA', 1, 'Neurocirugía'),
    ('mromero', 'pbkdf2-sha256$600000$avsLQuQjE02cvdOsO82L8A==$7dgPI89YGJbq3rAOYCktox4bv+ZI39BfqIh726coPg4=', 'mromero@bioklam.com', 'María Fernanda', 'Romero Ilave', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía general'),
    ('tec.ramos', 'pbkdf2-sha256$600000$f8x6ADAqPmj8fmjAqZB2Ag==$4pk6lmxkAko02hVJ24hBHEYT+ZtvDfpkG9rCFT8108E=', 'aramos@bioklam.com', 'Andrés', 'Ramos Cueva', 'TECNICO_INSTRUMENTISTA', 1, 'Neurocirugía'),
    ('tec.chavez', 'pbkdf2-sha256$600000$gooAYRBlMzXx6XfFYof4sA==$cGjYEpeRlPlWMvnQLLTvH/wHxDBasWzz7upWlxPnFq8=', 'lchavez@bioklam.com', 'Lucero', 'Chávez Soto', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía general'),
    ('tec.paredes', 'pbkdf2-sha256$600000$j1/57mGKM/oOmVKSJeoLYg==$HlNcLErNLSMOIvX3s1CFX0N+k/cgwvbzAFWyeMQzKOE=', 'cparedes@bioklam.com', 'César', 'Paredes Luna', 'TECNICO_INSTRUMENTISTA', 1, 'Traumatología'),
    ('tec.ayala', 'pbkdf2-sha256$600000$iriK0AXNdfdK+Cuaf0YbUg==$fW1t6pzTUPL//xgz6UmxUrpr6/Eb8Z0AZd34oO+Tn6A=', 'mayala@bioklam.com', 'Milagros', 'Ayala Torre', 'TECNICO_INSTRUMENTISTA', 1, 'Neurocirugía'),
    ('tec.zavala', 'pbkdf2-sha256$600000$yKhSX9liSDZHQn788eUK5A==$KZSEuZFy4Xs8vpa7aJL5+Fo+voHv6D7b3R1Hv//+mpQ=', 'ozavala@bioklam.com', 'Oscar', 'Zavala Meza', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía de columna'),
    ('tec.villanueva', 'pbkdf2-sha256$600000$qekYAspVX79g5qbsiUj+Tw==$BfthGmItFPazY5vgwxk98LZAp65q+Qom7srrUherZjg=', 'kvillanueva@bioklam.com', 'Karen', 'Villanueva Roca', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía general'),
    ('tec.condori', 'pbkdf2-sha256$600000$z9QqEqM0PgLWW4L9h/ZNIw==$3LPAmjhsE5ll76L0ry1yofumZ+45VPXpgAzGKrBJRsI=', 'wcondori@bioklam.com', 'Walter', 'Condori Apaza', 'TECNICO_INSTRUMENTISTA', 1, 'Traumatología'),
    ('tec.rojas', 'pbkdf2-sha256$600000$W0Xm2YtOl0QRnIlnsAKShg==$Knitmp7vV4O2tVYNTHWWLEl3ETlqXqGtuyC90rVe8M4=', 'erojas@bioklam.com', 'Estefany', 'Rojas Camacho', 'TECNICO_INSTRUMENTISTA', 1, 'Neurocirugía'),
    ('tec.maguina', 'pbkdf2-sha256$600000$pCwh79IHtbrmC0OoVIvRtA==$lJhbbaTf+C50kFnKjLKSCaVkuSHsZ2hZzO8n3Rker3s=', 'lmaguina@bioklam.com', 'Luis', 'Maguiña Ponce', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía de columna'),
    ('tec.santos', 'pbkdf2-sha256$600000$RWu2+4qAJY3RvqubNGMLrA==$8KgF5u0tZFmPIYwNP0sh0wUqn6HoiqiM9nQu397OvO0=', 'nsantos@bioklam.com', 'Nataly', 'Santos Egoavil', 'TECNICO_INSTRUMENTISTA', 1, 'Cirugía general')
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO bandeja_instrumental
(id_bandeja, tipo, procedimiento, esterilizado, activo)
VALUES
    (1, 'Bandeja de craneotomía', 'Cirugía de cráneo', 1, 1),
    (2, 'Bandeja de microcirugía', 'Cirugía de cráneo', 1, 1),
    (3, 'Bandeja de navegación estereotáxica', 'Cirugía de cráneo', 0, 1),
    (4, 'Bandeja larga de columna', 'Cirugía de columna', 1, 1),
    (5, 'Bandeja larga de vértebras', 'Cirugía de columna', 1, 1);


INSERT INTO consumible
(id_consumible, nombre_comercial, marca, medida, activo)
VALUES
    (1, 'Sutura de nylon',   'Ethicon',  '4-0',      1),
    (2, 'Gasa estéril',      'Medline',  '10x10cm',  1),
    (3, 'Clip de aneurisma', 'Aesculap', 'Mediano',  1),
    (4, 'Cera para hueso',   'Ethicon',  '2.5g',     1),
    (5, 'Tornillo pedicular', 'Demostración', '6 mm', 1);

INSERT INTO bandeja_consumible_estandar
(id_bandeja, id_consumible, cantidad_estandar)
VALUES
    (1, 1, 10), (1, 3, 4),
    (2, 2, 20), (3, 4, 5),
    (4, 1, 8), (4, 2, 20), (4, 5, 6),
    (5, 2, 16), (5, 5, 8);


INSERT INTO bandeja_consumible
(id_bandeja, id_consumible, cantidad_despachada, cantidad_consumida)
VALUES
    (1, 1, 10, 6),
    (1, 3, 4,  2),
    (2, 2, 20, 15),
    (3, 4, 5,  1);


INSERT INTO equipo
(id_equipo, nombre, categoria, disponible, activo)
VALUES
    (1, 'Craneótomo Midas Rex',        'CRANEOTOMO', 1, 1),
    (2, 'Navegador StealthStation S8', 'NAVEGADOR',  1, 1),
    (3, 'Craneótomo Anspach eMax',     'CRANEOTOMO', 0, 1);


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

INSERT INTO cuenta_usuario
(username, password_hash, email, nombres, apellidos, activo, id_paciente)
SELECT 'mbazan', 'pbkdf2-sha256$600000$uLQ+b1ZpQ208bvoFOYy8tA==$LwtWWAHHuDxNFVUn9urcrmbutNn5+X+AauFMdYx2F3w=', 'mbazan@example.test', 'Miguel', 'Bazán Montenegro', 1, id_cliente
FROM paciente_particular WHERE dni = '90000001'
ON DUPLICATE KEY UPDATE username = cuenta_usuario.username;

INSERT INTO cuenta_usuario
(username, password_hash, email, nombres, apellidos, activo, id_paciente)
SELECT v.username, v.password_hash, v.email, v.nombres, v.apellidos, 1, p.id_cliente
FROM (
  SELECT 'fpaz' AS username, 'pbkdf2-sha256$600000$77xmSkZWu+RmyUvOLElP2g==$NIcAQIXP1mcn/GFfqyE6aI3xZurlNa+e+U3DiqmPEc4=' AS password_hash, 'fpaz@example.test' AS email, 'Freddy' AS nombres, 'Paz' AS apellidos, '90000002' AS dni
 UNION ALL
  SELECT 'rcueva' AS username, 'pbkdf2-sha256$600000$2p2XaQ6p42hgxVRiVLg48Q==$ggDzfH0cKRQe7ssaVrHTyc6MAomrRoOtV6EI6XEPNJ0=' AS password_hash, 'rcueva@example.test' AS email, 'Rony' AS nombres, 'Cueva' AS apellidos, '90000003' AS dni
 UNION ALL
  SELECT 'egomez' AS username, 'pbkdf2-sha256$600000$0H+qPEHilMvqzZ5kjxPebA==$nPg0gJpidnHLWZr/s0OR9fYQPwuyJfplYY/o3N0c8iE=' AS password_hash, 'egomez@example.test' AS email, 'Erasmo' AS nombres, 'Gómez' AS apellidos, '90000004' AS dni
 UNION ALL
  SELECT 'dallasi' AS username, 'pbkdf2-sha256$600000$geOF4dDPRPZAqwAqLTDoQQ==$7Pa4OgLtet+X1c4pWp6D6I+47scNmPu5/jxnQTe7uT4=' AS password_hash, 'dallasi@example.test' AS email, 'David' AS nombres, 'Allasi' AS apellidos, '90000005' AS dni
 UNION ALL
  SELECT 'jcorcuera' AS username, 'pbkdf2-sha256$600000$xG+g1JsH3yHkylsfWlkLjg==$QybER/YmZh6dba+I7U2nqdvZ+rvRiNJi2wVcNOgQjs4=' AS password_hash, 'jcorcuera@example.test' AS email, 'José Luis' AS nombres, 'Corcuera' AS apellidos, '90000006' AS dni
 UNION ALL
  SELECT 'mguanira' AS username, 'pbkdf2-sha256$600000$GJsc7/gnceVf1sJ+qcxWOA==$1hjx3EbB43WJl2kZuCVE1bkj4Xt86GJ5SCW9o4AQw0g=' AS password_hash, 'mguanira@example.test' AS email, 'Miguel' AS nombres, 'Guanira' AS apellidos, '90000007' AS dni
 UNION ALL
  SELECT 'vkhlebnikov' AS username, 'pbkdf2-sha256$600000$Ier0sRR0f2sOGWtpQVvLMg==$Mvu7TP0wL99X907Dkpsv35WQqz2KgrdB1lV49I2TPkM=' AS password_hash, 'vkhlebnikov@example.test' AS email, 'Viktor' AS nombres, 'Khlebnikov' AS apellidos, '90000008' AS dni
 UNION ALL
  SELECT 'lmuroya' AS username, 'pbkdf2-sha256$600000$UtndizVZHojds3ya2+EjrA==$mGRS2FMnwMVxAvESgQNnqSkScyovFA4tVCM7vjjlXoA=' AS password_hash, 'lmuroya@example.test' AS email, 'Luis' AS nombres, 'Muroya' AS apellidos, '90000009' AS dni
 UNION ALL
  SELECT 'cmedina' AS username, 'pbkdf2-sha256$600000$e/2eI+fDxRTflShuydI98A==$Li4gSVWUHlQ8qHYkGO/ME7tB/GvJQog8vYH/UxiX2wo=' AS password_hash, 'cmedina@example.test' AS email, 'Carla' AS nombres, 'Medina' AS apellidos, '90000010' AS dni
) v
JOIN paciente_particular p ON p.dni = v.dni
ON DUPLICATE KEY UPDATE username = cuenta_usuario.username;


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
    (4, 'Clínica San Felipe', NULL, NULL, NULL, 1, '20100129841', 0, NULL),
    (5, 'Clínica Internacional', NULL, NULL, NULL, 1, '20100054184', 0, NULL),
    (6, 'Clínica Auna Vallesur', NULL, NULL, NULL, 1, '20454135432', 0, NULL),
    (7, 'Clínica Auna Bellavista', NULL, NULL, NULL, 1, '20100251176', 0, NULL),
    (8, 'Hospital Nacional Dos de Mayo', NULL, NULL, NULL, 1, '20160388570', 0, NULL),
    (9, 'Hospital Nacional Arzobispo Loayza', NULL, NULL, NULL, 1, '20154996991', 0, NULL),
    (10, 'Hospital Cayetano Heredia', NULL, NULL, NULL, 1, '20154528971', 0, NULL),
    (11, 'Hospital San Juan de Dios de Pisco', NULL, NULL, NULL, 1, '20452578949', 0, NULL);

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

INSERT INTO orden_compra
(id_orden_compra, archivo_respaldo_path, fecha_recepcion, activo)
VALUES
    (1,
     '/docs/oc/OC-2026-001.pdf',
     '2026-08-10 09:15:00',
     1);

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

    (4, '2026-11-20 11:30:00', NULL, 'Cirugía de cráneo',
     'Dra. Silvia Ramos', '[DEMO R2] Lesión craneal de prueba',
     'Eric Huiza', '90000001', NULL, 'PROGRAMADA',
     NULL, 1, NULL, 1, 1),
    (5, '2026-11-12 08:00:00', NULL, 'Cirugía de cráneo',
     'Dr. Alberto Núñez', '[DEMO R2] Diagnóstico de prueba',
     'Eric Huiza', '90000001', 'Paciente no apto', 'CANCELADA',
     NULL, 1, NULL, 1, 1);

INSERT INTO cirugia_consumible_previsto
(id_cirugia, id_consumible, cantidad_prevista)
VALUES (1, 1, 10), (1, 3, 4), (2, 4, 5);

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

INSERT INTO cotizacion
(id_cotizacion, id_cirugia, precio_pactado, estado, fecha_emision, activo)
VALUES
    (1, 1, 8500.00,  'ACEPTADA', '2026-08-05 12:00:00', 1),
    (2, 2, 12300.00, 'EMITIDA',  '2026-08-16 10:00:00', 1);



INSERT INTO linea_orden_compra
(id_linea, id_orden_compra, id_consumible,
 item_referencia, descripcion, cantidad, precio_unitario)
VALUES
    (1, 1, 2, NULL, 'Gasa estéril 10x10cm', 50, 5.50),
    (2, 1, 4, NULL, 'Cera para hueso 2.5g', 10, 35.00);

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
