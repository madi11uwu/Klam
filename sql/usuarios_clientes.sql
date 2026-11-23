-- BIOKLAM - paciente 10 y cuentas de acceso de pacientes particulares. Contrasena: Klam2026!
-- Re-ejecutable.

USE `klam`;

INSERT INTO paciente_particular
(id_cliente, nombre, direccion, email_contacto, telefono, activo, dni, pago_confirmado)
VALUES (10, 'Carla Medina', 'Dirección ficticia 10', 'paciente10@example.test', '000000010', 1, '90000010', 0)
ON DUPLICATE KEY UPDATE nombre = nombre;

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
