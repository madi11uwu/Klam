-- BIOKLAM - 10 administradores, 10 vendedores y 10 tecnicos instrumentistas. Contrasena de todos: Klam2026!
-- Re-ejecutable: no duplica usuarios existentes.

USE `klam`;

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
