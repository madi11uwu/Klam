CREATE SCHEMA IF NOT EXISTS `klam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;

USE `klam` ;

CREATE TABLE `administrador` (
             `id_usuario` INT NOT NULL AUTO_INCREMENT,
             `username` VARCHAR(50) NOT NULL,
             `password_hash` VARCHAR(255) NOT NULL,
             `email` VARCHAR(120) NOT NULL,
             `nombres` VARCHAR(100) NOT NULL,
             `apellidos` VARCHAR(100) NOT NULL,
             `rol` ENUM('ADMINISTRADOR') NOT NULL DEFAULT 'ADMINISTRADOR',
             `activo` TINYINT(1) NOT NULL DEFAULT 1,
             `id_admin` VARCHAR(20) NOT NULL,
             PRIMARY KEY (`id_usuario`),
             UNIQUE KEY `uq_administrador_username` (`username`),
             UNIQUE KEY `uq_administrador_email` (`email`),
             UNIQUE KEY `uq_administrador_id_admin` (`id_admin`)
);

CREATE TABLE `vendedor` (
        `id_usuario` INT NOT NULL AUTO_INCREMENT,
        `username` VARCHAR(50) NOT NULL,
        `password_hash` VARCHAR(255) NOT NULL,
        `email` VARCHAR(120) NOT NULL,
        `nombres` VARCHAR(100) NOT NULL,
        `apellidos` VARCHAR(100) NOT NULL,
        `rol` ENUM('VENDEDOR') NOT NULL DEFAULT 'VENDEDOR',
        `activo` TINYINT(1) NOT NULL DEFAULT 1,
        `id_vendedor` VARCHAR(20) NOT NULL,
        `comision_acumulada` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
        PRIMARY KEY (`id_usuario`),
        UNIQUE KEY `uq_vendedor_username` (`username`),
        UNIQUE KEY `uq_vendedor_email` (`email`),
        UNIQUE KEY `uq_vendedor_id_vendedor` (`id_vendedor`)
);

CREATE TABLE `tecnico_instrumentista` (
              `id_usuario` INT NOT NULL AUTO_INCREMENT,
              `username` VARCHAR(50) NOT NULL,
              `password_hash` VARCHAR(255) NOT NULL,
              `email` VARCHAR(120) NOT NULL,
              `nombres` VARCHAR(100) NOT NULL,
              `apellidos` VARCHAR(100) NOT NULL,
              `rol` ENUM('TECNICO_INSTRUMENTISTA') NOT NULL DEFAULT 'TECNICO_INSTRUMENTISTA',
              `activo` TINYINT(1) NOT NULL DEFAULT 1,
              `id_tecnico` VARCHAR(20) NOT NULL,
              `especialidad` VARCHAR(100) NULL,
              PRIMARY KEY (`id_usuario`),
              UNIQUE KEY `uq_tecnico_username` (`username`),
              UNIQUE KEY `uq_tecnico_email` (`email`),
              UNIQUE KEY `uq_tecnico_id_tecnico` (`id_tecnico`)
);

CREATE TABLE `bandeja_instrumental` (
                    `id_bandeja` INT NOT NULL AUTO_INCREMENT,
                    `tipo` VARCHAR(100) NOT NULL,
                    `esterilizado` TINYINT(1) NOT NULL DEFAULT 0,
                    `activo` TINYINT(1) NOT NULL DEFAULT 1,
                    PRIMARY KEY (`id_bandeja`)
);

CREATE TABLE `consumible` (
          `id_consumible` INT NOT NULL AUTO_INCREMENT,
          `nombre_comercial` VARCHAR(150) NOT NULL,
          `marca` VARCHAR(100) NULL,
          `medida` VARCHAR(50) NULL,
          `activo` TINYINT(1) NOT NULL DEFAULT 1,
          PRIMARY KEY (`id_consumible`)
);

CREATE TABLE `bandeja_consumible` (
                  `id_bandeja` INT NOT NULL,
                  `id_consumible` INT NOT NULL,
                  `cantidad_despachada` INT NOT NULL DEFAULT 0,
                  `cantidad_consumida` INT NOT NULL DEFAULT 0,
                  PRIMARY KEY (`id_bandeja`, `id_consumible`),
                  CONSTRAINT `fk_bandeja_consumible_bandeja`
                      FOREIGN KEY (`id_bandeja`) REFERENCES `bandeja_instrumental` (`id_bandeja`)
                          ON DELETE CASCADE ON UPDATE CASCADE,
                  CONSTRAINT `fk_bandeja_consumible_consumible`
                      FOREIGN KEY (`id_consumible`) REFERENCES `consumible` (`id_consumible`)
                          ON DELETE RESTRICT ON UPDATE CASCADE,
                  CONSTRAINT `chk_bandeja_despachada` CHECK (`cantidad_despachada` >= 0),
                  CONSTRAINT `chk_bandeja_consumida` CHECK (`cantidad_consumida` >= 0)
);

CREATE TABLE `equipo` (
      `id_equipo` INT NOT NULL AUTO_INCREMENT,
      `nombre` VARCHAR(150) NOT NULL,
      `categoria` ENUM('CRANEOTOMO','NAVEGADOR','OTRO') NOT NULL,
      `disponible` TINYINT(1) NOT NULL DEFAULT 1,
      `activo` TINYINT(1) NOT NULL DEFAULT 1,
      PRIMARY KEY (`id_equipo`)
);

CREATE TABLE `equipo_especificacion` (
                     `id_equipo` INT NOT NULL,
                     `clave` VARCHAR(50) NOT NULL,
                     `valor` VARCHAR(255) NULL,
                     PRIMARY KEY (`id_equipo`, `clave`),
                     CONSTRAINT `fk_especificacion_equipo`
                         FOREIGN KEY (`id_equipo`) REFERENCES `equipo` (`id_equipo`)
                             ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `clinica_hospital` (
                `id_cliente` INT NOT NULL,
                `nombre` VARCHAR(150) NOT NULL,
                `direccion` VARCHAR(200) NULL,
                `email_contacto` VARCHAR(120) NULL,
                `telefono` VARCHAR(20) NULL,
                `activo` TINYINT(1) NOT NULL DEFAULT 1,
                `ruc` CHAR(11) NOT NULL,
                `tiene_consignacion` TINYINT(1) NOT NULL DEFAULT 0,
                `periodo_credito` VARCHAR(30) NULL,
                PRIMARY KEY (`id_cliente`),
                UNIQUE KEY `uq_clinica_ruc` (`ruc`)
);

CREATE TABLE `paciente_particular` (
                   `id_cliente` INT NOT NULL,
                   `nombre` VARCHAR(150) NOT NULL,
                   `direccion` VARCHAR(200) NULL,
                   `email_contacto` VARCHAR(120) NULL,
                   `telefono` VARCHAR(20) NULL,
                   `activo` TINYINT(1) NOT NULL DEFAULT 1,
                   `dni` CHAR(8) NOT NULL,
                   `pago_confirmado` TINYINT(1) NOT NULL DEFAULT 0,
                   PRIMARY KEY (`id_cliente`),
                   UNIQUE KEY `uq_paciente_dni` (`dni`)
);

CREATE TABLE `cirugia` (
       `id_cirugia` INT NOT NULL AUTO_INCREMENT,
       `fecha_hora_inicio` DATETIME NOT NULL,
       `fecha_hora_fin` DATETIME NULL,
       `tipo_procedimiento` VARCHAR(150) NOT NULL,
       `doctor_nombre` VARCHAR(150) NULL,
       `motivo_cancelacion` VARCHAR(255) NULL,
       `estado` ENUM('PROGRAMADA','EN_PROCESO','FINALIZADA','CANCELADA')
NOT NULL DEFAULT 'PROGRAMADA',
       `id_equipo` INT NULL,
       `id_bandeja` INT NULL,
       `id_clinica_hospital` VARCHAR(50) NULL,
       `id_paciente_particular` VARCHAR(50) NULL,
       `activo` TINYINT(1) NOT NULL DEFAULT 1,
       PRIMARY KEY (`id_cirugia`),
       CONSTRAINT `fk_cirugia_equipo`
           FOREIGN KEY (`id_equipo`) REFERENCES `equipo` (`id_equipo`)
               ON DELETE SET NULL ON UPDATE CASCADE,
       CONSTRAINT `fk_cirugia_bandeja`
           FOREIGN KEY (`id_bandeja`) REFERENCES `bandeja_instrumental` (`id_bandeja`)
               ON DELETE SET NULL ON UPDATE CASCADE,
       CONSTRAINT `fk_cirugia_clinica`
           FOREIGN KEY (`id_clinica_hospital`) REFERENCES `clinica_hospital` (`id_cliente`)
               ON DELETE RESTRICT ON UPDATE CASCADE,
       CONSTRAINT `fk_cirugia_paciente`
           FOREIGN KEY (`id_paciente_particular`) REFERENCES `paciente_particular` (`id_cliente`)
               ON DELETE RESTRICT ON UPDATE CASCADE,
       CONSTRAINT `chk_cirugia_un_cliente` CHECK (
           (`id_clinica_hospital` IS NOT NULL AND `id_paciente_particular` IS NULL)
               OR
           (`id_clinica_hospital` IS NULL AND `id_paciente_particular` IS NOT NULL)
           )
);

CREATE TABLE `cotizacion` (
          `id_cotizacion` INT NOT NULL AUTO_INCREMENT,
          `id_cirugia` INT NOT NULL,
          `precio_pactado` DECIMAL(10,2) NOT NULL,
          `estado` ENUM('EMITIDA','ACEPTADA','RECHAZADA','VENCIDA')
NOT NULL DEFAULT 'EMITIDA',
          `fecha_emision` DATETIME NOT NULL,
          `activo` TINYINT(1) NOT NULL DEFAULT 1,
          PRIMARY KEY (`id_cotizacion`),
          CONSTRAINT `fk_cotizacion_cirugia`
              FOREIGN KEY (`id_cirugia`) REFERENCES `cirugia` (`id_cirugia`)
                  ON DELETE RESTRICT ON UPDATE CASCADE,
          CONSTRAINT `chk_cotizacion_precio` CHECK (`precio_pactado` >= 0)
);

CREATE TABLE `orden_compra` (
            `id_orden_compra` INT NOT NULL,
            `archivo_respaldo_path` VARCHAR(255) NOT NULL,
            `fecha_recepcion` DATETIME NOT NULL,
            `activo` TINYINT(1) NOT NULL DEFAULT 1,
            PRIMARY KEY (`id_orden_compra`)
);

CREATE TABLE `documento_ingreso` (
                 `id_documento` INT NOT NULL,
                 `tipo_documento` ENUM('DNI','RECETA_MEDICA','ORDEN_COMPRA') NOT NULL,
                 `archivo_path` VARCHAR(255) NOT NULL,
                 `estado_validacion` ENUM('PENDIENTE','VALIDADO','RECHAZADO')
NOT NULL DEFAULT 'PENDIENTE',
                 `fecha_carga` DATETIME NOT NULL,
                 `id_orden_compra` VARCHAR(50) NULL,
                 `activo` TINYINT(1) NOT NULL DEFAULT 1,
                 `id_administrador` INT NULL,
                 `id_vendedor` INT NULL,
                 `id_tecnico` INT NULL,
                 PRIMARY KEY (`id_documento`),
                 CONSTRAINT `fk_documento_ingreso_orden`
                     FOREIGN KEY (`id_orden_compra`) REFERENCES `orden_compra` (`id_orden_compra`)
                         ON DELETE SET NULL ON UPDATE CASCADE,
                 CONSTRAINT `fk_documento_ingreso_admin`
                     FOREIGN KEY (`id_administrador`) REFERENCES `administrador` (`id_usuario`)
                         ON DELETE RESTRICT ON UPDATE CASCADE,
                 CONSTRAINT `fk_documento_ingreso_vendedor`
                     FOREIGN KEY (`id_vendedor`) REFERENCES `vendedor` (`id_usuario`)
                         ON DELETE RESTRICT ON UPDATE CASCADE,
                 CONSTRAINT `fk_documento_ingreso_tecnico`
                     FOREIGN KEY (`id_tecnico`) REFERENCES `tecnico_instrumentista` (`id_usuario`)
                         ON DELETE RESTRICT ON UPDATE CASCADE,
                 CONSTRAINT `chk_documento_ingreso_un_usuario` CHECK (
                     (`id_administrador` IS NOT NULL)
                         + (`id_vendedor` IS NOT NULL)
                         + (`id_tecnico` IS NOT NULL) = 1
                     )
);

CREATE TABLE `factura` (
       `id_documento` INT NOT NULL AUTO_INCREMENT,
       `id_cirugia` INT NOT NULL,
       `fecha_emision` DATETIME NOT NULL,
       `monto_base` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       `tasa_igv` DECIMAL(5,4) NOT NULL DEFAULT 0.1800,
       `igv` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       `monto_total` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
       `estado_pago` ENUM('PENDIENTE','PAGADO','ANULADO')
NOT NULL DEFAULT 'PENDIENTE',
       `activo` TINYINT(1) NOT NULL DEFAULT 1,
       `ruc_receptor` CHAR(11) NOT NULL,
       PRIMARY KEY (`id_documento`),
       CONSTRAINT `fk_factura_cirugia`
           FOREIGN KEY (`id_cirugia`) REFERENCES `cirugia` (`id_cirugia`)
               ON DELETE RESTRICT ON UPDATE CASCADE,
       CONSTRAINT `chk_factura_montos` CHECK (
           `monto_base` >= 0 AND `tasa_igv` >= 0
               AND `igv` >= 0 AND `monto_total` >= 0
           )
);

CREATE TABLE `boleta` (
      `id_documento` INT NOT NULL AUTO_INCREMENT,
      `id_cirugia` INT NOT NULL,
      `fecha_emision` DATETIME NOT NULL,
      `monto_base` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
      `tasa_igv` DECIMAL(5,4) NOT NULL DEFAULT 0.1800,
      `igv` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
      `monto_total` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
      `estado_pago` ENUM('PENDIENTE','PAGADO','ANULADO')
NOT NULL DEFAULT 'PENDIENTE',
      `activo` TINYINT(1) NOT NULL DEFAULT 1,
      `dni_receptor` CHAR(8) NOT NULL,
      PRIMARY KEY (`id_documento`),
      CONSTRAINT `fk_boleta_cirugia`
          FOREIGN KEY (`id_cirugia`) REFERENCES `cirugia` (`id_cirugia`)
              ON DELETE RESTRICT ON UPDATE CASCADE,
      CONSTRAINT `chk_boleta_montos` CHECK (
          `monto_base` >= 0 AND `tasa_igv` >= 0
              AND `igv` >= 0 AND `monto_total` >= 0
          )
);

CREATE TABLE `linea_factura` (
             `id_linea` INT NOT NULL AUTO_INCREMENT,
             `id_documento` INT NOT NULL,
             `id_consumible` INT NULL,
             `item_referencia` VARCHAR(50) NULL,
             `descripcion` VARCHAR(150) NOT NULL,
             `cantidad` INT NOT NULL,
             `precio_unitario` DECIMAL(10,2) NOT NULL,
             PRIMARY KEY (`id_linea`),
             CONSTRAINT `fk_linea_factura_documento`
                 FOREIGN KEY (`id_documento`) REFERENCES `factura` (`id_documento`)
                     ON DELETE CASCADE ON UPDATE CASCADE,
             CONSTRAINT `fk_linea_factura_consumible`
                 FOREIGN KEY (`id_consumible`) REFERENCES `consumible` (`id_consumible`)
                     ON DELETE SET NULL ON UPDATE CASCADE,
             CHECK (`cantidad` > 0),
             CHECK (`precio_unitario` >= 0)
);

CREATE TABLE `linea_boleta` (
            `id_linea` INT NOT NULL AUTO_INCREMENT,
            `id_documento` INT NOT NULL,
            `id_consumible` INT NULL,
            `item_referencia` VARCHAR(50) NULL,
            `descripcion` VARCHAR(150) NOT NULL,
            `cantidad` INT NOT NULL,
            `precio_unitario` DECIMAL(10,2) NOT NULL,
            PRIMARY KEY (`id_linea`),
            CONSTRAINT `fk_linea_boleta_documento`
                FOREIGN KEY (`id_documento`) REFERENCES `boleta` (`id_documento`)
                    ON DELETE CASCADE ON UPDATE CASCADE,
            CONSTRAINT `fk_linea_boleta_consumible`
                FOREIGN KEY (`id_consumible`) REFERENCES `consumible` (`id_consumible`)
                    ON DELETE SET NULL ON UPDATE CASCADE,
            CHECK (`cantidad` > 0),
            CHECK (`precio_unitario` >= 0)
);

CREATE TABLE `nota_credito` (
            `id_nota_credito` INT NOT NULL AUTO_INCREMENT,
            `id_factura_original` INT NULL,
            `id_boleta_original` INT NULL,
            `motivo` VARCHAR(255) NOT NULL,
            `fecha_emision` DATETIME NOT NULL,
            `monto_total` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
            `activo` TINYINT(1) NOT NULL DEFAULT 1,
            PRIMARY KEY (`id_nota_credito`),
            CONSTRAINT `fk_nota_factura`
                FOREIGN KEY (`id_factura_original`) REFERENCES `factura` (`id_documento`)
                    ON DELETE RESTRICT ON UPDATE CASCADE,
            CONSTRAINT `fk_nota_boleta`
                FOREIGN KEY (`id_boleta_original`) REFERENCES `boleta` (`id_documento`)
                    ON DELETE RESTRICT ON UPDATE CASCADE,
            CONSTRAINT `chk_nota_un_documento` CHECK (
                (`id_factura_original` IS NOT NULL AND `id_boleta_original` IS NULL)
                    OR
                (`id_factura_original` IS NULL AND `id_boleta_original` IS NOT NULL)
                ),
            CHECK (`monto_total` >= 0)
);

CREATE TABLE `linea_nota_credito` (
                  `id_linea` INT NOT NULL AUTO_INCREMENT,
                  `id_nota_credito` INT NOT NULL,
                  `id_consumible` INT NULL,
                  `item_referencia` VARCHAR(50) NULL,
                  `descripcion` VARCHAR(150) NOT NULL,
                  `cantidad` INT NOT NULL,
                  `precio_unitario` DECIMAL(10,2) NOT NULL,
                  `motivo_devolucion` VARCHAR(255) NULL,
                  PRIMARY KEY (`id_linea`),
                  CONSTRAINT `fk_linea_nota_credito_nota`
                      FOREIGN KEY (`id_nota_credito`) REFERENCES `nota_credito` (`id_nota_credito`)
                          ON DELETE CASCADE ON UPDATE CASCADE,
                  CONSTRAINT `fk_linea_nota_credito_consumible`
                      FOREIGN KEY (`id_consumible`) REFERENCES `consumible` (`id_consumible`)
                          ON DELETE SET NULL ON UPDATE CASCADE,
                  CHECK (`cantidad` > 0),
                  CHECK (`precio_unitario` >= 0)
);

CREATE TABLE `linea_orden_compra` (
                  `id_linea` INT NOT NULL AUTO_INCREMENT,
                  `id_orden_compra` VARCHAR(50) NOT NULL,
                  `id_consumible` INT NULL,
                  `item_referencia` VARCHAR(50) NULL,
                  `descripcion` VARCHAR(150) NOT NULL,
                  `cantidad` INT NOT NULL,
                  `precio_unitario` DECIMAL(10,2) NOT NULL,
                  PRIMARY KEY (`id_linea`),
                  CONSTRAINT `fk_linea_orden_compra_orden`
                      FOREIGN KEY (`id_orden_compra`) REFERENCES `orden_compra` (`id_orden_compra`)
                          ON DELETE CASCADE ON UPDATE CASCADE,
                  CONSTRAINT `fk_linea_orden_compra_consumible`
                      FOREIGN KEY (`id_consumible`) REFERENCES `consumible` (`id_consumible`)
                          ON DELETE SET NULL ON UPDATE CASCADE,
                  CHECK (`cantidad` > 0),
                  CHECK (`precio_unitario` >= 0)
);

CREATE TABLE `notificacion` (
            `id_notificacion` INT NOT NULL,
            `fecha_hora` DATETIME NOT NULL,
            `titulo` VARCHAR(150) NOT NULL,
            `mensaje` VARCHAR(500) NULL,
            `estado_leida` TINYINT(1) NOT NULL DEFAULT 0,
            `id_administrador` INT NULL,
            `id_vendedor` INT NULL,
            `id_tecnico` INT NULL,
            PRIMARY KEY (`id_notificacion`),
            CONSTRAINT `fk_notificacion_admin`
                FOREIGN KEY (`id_administrador`) REFERENCES `administrador` (`id_usuario`)
                    ON DELETE CASCADE ON UPDATE CASCADE,
            CONSTRAINT `fk_notificacion_vendedor`
                FOREIGN KEY (`id_vendedor`) REFERENCES `vendedor` (`id_usuario`)
                    ON DELETE CASCADE ON UPDATE CASCADE,
            CONSTRAINT `fk_notificacion_tecnico`
                FOREIGN KEY (`id_tecnico`) REFERENCES `tecnico_instrumentista` (`id_usuario`)
                    ON DELETE CASCADE ON UPDATE CASCADE,
            CONSTRAINT `chk_notificacion_un_usuario` CHECK (
                (`id_administrador` IS NOT NULL)
                    + (`id_vendedor` IS NOT NULL)
                    + (`id_tecnico` IS NOT NULL) = 1
                )
);
