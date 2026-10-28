CREATE SCHEMA IF NOT EXISTS `klam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;

USE `klam`;

CREATE TABLE IF NOT EXISTS `administrador` (
                                 `id_usuario` INT NOT NULL AUTO_INCREMENT,
                                 `username` VARCHAR(50) NOT NULL,
                                 `password_hash` VARCHAR(255) NOT NULL,
                                 `email` VARCHAR(120) NOT NULL,
                                 `nombres` VARCHAR(100) NOT NULL,
                                 `apellidos` VARCHAR(100) NOT NULL,
                                 `rol` ENUM('ADMINISTRADOR') NOT NULL DEFAULT 'ADMINISTRADOR',
                                 `activo` TINYINT(1) NOT NULL DEFAULT 1,
                                 PRIMARY KEY (`id_usuario`),
                                 UNIQUE KEY `uq_administrador_username` (`username`),
                                 UNIQUE KEY `uq_administrador_email` (`email`)
);

CREATE TABLE IF NOT EXISTS `vendedor` (
                            `id_usuario` INT NOT NULL AUTO_INCREMENT,
                            `username` VARCHAR(50) NOT NULL,
                            `password_hash` VARCHAR(255) NOT NULL,
                            `email` VARCHAR(120) NOT NULL,
                            `nombres` VARCHAR(100) NOT NULL,
                            `apellidos` VARCHAR(100) NOT NULL,
                            `rol` ENUM('VENDEDOR') NOT NULL DEFAULT 'VENDEDOR',
                            `activo` TINYINT(1) NOT NULL DEFAULT 1,
                            `comision_acumulada` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                            PRIMARY KEY (`id_usuario`),
                            UNIQUE KEY `uq_vendedor_username` (`username`),
                            UNIQUE KEY `uq_vendedor_email` (`email`)
);

CREATE TABLE IF NOT EXISTS `tecnico_instrumentista` (
                                          `id_usuario` INT NOT NULL AUTO_INCREMENT,
                                          `username` VARCHAR(50) NOT NULL,
                                          `password_hash` VARCHAR(255) NOT NULL,
                                          `email` VARCHAR(120) NOT NULL,
                                          `nombres` VARCHAR(100) NOT NULL,
                                          `apellidos` VARCHAR(100) NOT NULL,
                                          `rol` ENUM('TECNICO_INSTRUMENTISTA') NOT NULL DEFAULT 'TECNICO_INSTRUMENTISTA',
                                          `activo` TINYINT(1) NOT NULL DEFAULT 1,
                                          `especialidad` VARCHAR(100) NULL,
                                          PRIMARY KEY (`id_usuario`),
                                          UNIQUE KEY `uq_tecnico_username` (`username`),
                                          UNIQUE KEY `uq_tecnico_email` (`email`)
);

CREATE TABLE IF NOT EXISTS `bandeja_instrumental` (
                    `id_bandeja` INT NOT NULL AUTO_INCREMENT,
                    `tipo` VARCHAR(100) NOT NULL,
                    `procedimiento` VARCHAR(100) NULL,
                    `esterilizado` TINYINT(1) NOT NULL DEFAULT 0,
                    `activo` TINYINT(1) NOT NULL DEFAULT 1,
                    PRIMARY KEY (`id_bandeja`),
                    KEY `idx_bandeja_procedimiento` (`procedimiento`)
);

CREATE TABLE IF NOT EXISTS `consumible` (
          `id_consumible` INT NOT NULL AUTO_INCREMENT,
          `nombre_comercial` VARCHAR(150) NOT NULL,
          `marca` VARCHAR(100) NULL,
          `medida` VARCHAR(50) NULL,
          `activo` TINYINT(1) NOT NULL DEFAULT 1,
          PRIMARY KEY (`id_consumible`)
);

CREATE TABLE IF NOT EXISTS `bandeja_consumible` (
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
                  CONSTRAINT `chk_bandeja_consumida` CHECK (`cantidad_consumida` >= 0),
                  CONSTRAINT `chk_bandeja_consumo_vs_despacho` CHECK (`cantidad_consumida` <= `cantidad_despachada`)
);

CREATE TABLE IF NOT EXISTS `equipo` (
      `id_equipo` INT NOT NULL AUTO_INCREMENT,
      `nombre` VARCHAR(150) NOT NULL,
      `categoria` ENUM('CRANEOTOMO','NAVEGADOR','MICROSCOPIO','OTRO') NOT NULL,
      `disponible` TINYINT(1) NOT NULL DEFAULT 1,
      `activo` TINYINT(1) NOT NULL DEFAULT 1,
      PRIMARY KEY (`id_equipo`)
);

CREATE TABLE IF NOT EXISTS `equipo_especificacion` (
                     `id_equipo` INT NOT NULL,
                     `clave` VARCHAR(50) NOT NULL,
                     `valor` VARCHAR(255) NULL,
                     PRIMARY KEY (`id_equipo`, `clave`),
                     CONSTRAINT `fk_especificacion_equipo`
                         FOREIGN KEY (`id_equipo`) REFERENCES `equipo` (`id_equipo`)
                             ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `clinica_hospital` (
                `id_cliente` INT NOT NULL AUTO_INCREMENT,
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

CREATE TABLE IF NOT EXISTS `paciente_particular` (
                   `id_cliente` INT NOT NULL AUTO_INCREMENT,
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

-- Solicitud de cirugia (RF05, RF07, RF08). No tiene clase Java propia: se maneja
-- desde CirugiaBL/CirugiaDAO. El solicitante es una clinica/hospital O un paciente
-- particular (los ids de ambas tablas se solapan, por eso hay dos columnas).
CREATE TABLE IF NOT EXISTS `solicitud_cirugia` (
       `id_solicitud` INT NOT NULL AUTO_INCREMENT,
       `id_clinica_solicitante` INT NULL,
       `id_paciente_solicitante` INT NULL,
       `id_clinica_hospital` INT NOT NULL,
       `paciente_nombre` VARCHAR(150) NOT NULL,
       `diagnostico` VARCHAR(300) NOT NULL,
       `tipo_procedimiento` VARCHAR(100) NOT NULL,
       `doctor_nombre` VARCHAR(100) NOT NULL,
       `fecha_hora_solicitada` DATETIME NOT NULL,
       `observaciones` VARCHAR(500) NULL,
       `estado_solicitud` ENUM('POR_REVISAR','DOCUMENTACION_INCOMPLETA','RECHAZADA','APROBADA')
NOT NULL DEFAULT 'POR_REVISAR',
       `motivo_rechazo` VARCHAR(200) NULL,
       `fecha_registro` DATETIME NOT NULL,
       `fecha_revision` DATETIME NULL,
       `activo` TINYINT(1) NOT NULL DEFAULT 1,
       PRIMARY KEY (`id_solicitud`),
       KEY `idx_solicitud_estado` (`estado_solicitud`),
       KEY `idx_solicitud_clinica_solicitante` (`id_clinica_solicitante`),
       KEY `idx_solicitud_paciente_solicitante` (`id_paciente_solicitante`),
       KEY `idx_solicitud_destino` (`id_clinica_hospital`),
       KEY `idx_solicitud_fecha` (`fecha_hora_solicitada`),
       CONSTRAINT `fk_solicitud_clinica_solicitante`
           FOREIGN KEY (`id_clinica_solicitante`) REFERENCES `clinica_hospital` (`id_cliente`)
               ON DELETE RESTRICT ON UPDATE RESTRICT,
       CONSTRAINT `fk_solicitud_paciente_solicitante`
           FOREIGN KEY (`id_paciente_solicitante`) REFERENCES `paciente_particular` (`id_cliente`)
               ON DELETE RESTRICT ON UPDATE RESTRICT,
       CONSTRAINT `fk_solicitud_clinica_destino`
           FOREIGN KEY (`id_clinica_hospital`) REFERENCES `clinica_hospital` (`id_cliente`)
               ON DELETE RESTRICT ON UPDATE RESTRICT,
       CONSTRAINT `chk_solicitud_un_solicitante` CHECK (
           (`id_clinica_solicitante` IS NOT NULL AND `id_paciente_solicitante` IS NULL)
               OR
           (`id_clinica_solicitante` IS NULL AND `id_paciente_solicitante` IS NOT NULL)
           ),
       CONSTRAINT `chk_solicitud_motivo_rechazo` CHECK (
           `estado_solicitud` <> 'RECHAZADA'
               OR (`motivo_rechazo` IS NOT NULL AND CHAR_LENGTH(TRIM(`motivo_rechazo`)) > 0)
           )
);

CREATE TABLE IF NOT EXISTS `cirugia` (
       `id_cirugia` INT NOT NULL AUTO_INCREMENT,
       `fecha_hora_inicio` DATETIME NOT NULL,
       `fecha_hora_fin` DATETIME NULL,
       `tipo_procedimiento` VARCHAR(100) NOT NULL,
       `doctor_nombre` VARCHAR(100) NOT NULL,
       `diagnostico` VARCHAR(300) NOT NULL,
       `motivo_cancelacion` VARCHAR(200) NULL,
       `estado` ENUM('PROGRAMADA','EN_PROCESO','FINALIZADA','CANCELADA')
NOT NULL DEFAULT 'PROGRAMADA',
       `id_equipo` INT NULL,
       `id_bandeja` INT NULL,
       `id_clinica_hospital` INT NULL,
       `id_paciente_particular` INT NULL,
       `id_solicitud_origen` INT NULL,
       `consumo_completo` TINYINT(1) NOT NULL DEFAULT 0,
       `activo` TINYINT(1) NOT NULL DEFAULT 1,
       -- una solicitud solo puede tener UNA cirugia vigente (activa y no cancelada)
       `solicitud_vigente` INT GENERATED ALWAYS AS (
           CASE WHEN `activo` = 1 AND `estado` <> 'CANCELADA' THEN `id_solicitud_origen` ELSE NULL END
           ) STORED,
       PRIMARY KEY (`id_cirugia`),
       -- una bandeja fisica no puede estar en dos cirugias (varios NULL permitidos)
       UNIQUE KEY `uq_cirugia_bandeja` (`id_bandeja`),
       UNIQUE KEY `uq_cirugia_solicitud_vigente` (`solicitud_vigente`),
       KEY `idx_cirugia_fecha_inicio` (`fecha_hora_inicio`),
       KEY `idx_cirugia_estado` (`estado`),
       KEY `idx_cirugia_equipo` (`id_equipo`, `fecha_hora_inicio`),
       KEY `idx_cirugia_clinica` (`id_clinica_hospital`),
       KEY `idx_cirugia_paciente` (`id_paciente_particular`),
       KEY `idx_cirugia_solicitud_origen` (`id_solicitud_origen`),
       CONSTRAINT `fk_cirugia_equipo`
           FOREIGN KEY (`id_equipo`) REFERENCES `equipo` (`id_equipo`)
               ON DELETE SET NULL ON UPDATE CASCADE,
       CONSTRAINT `fk_cirugia_bandeja`
           FOREIGN KEY (`id_bandeja`) REFERENCES `bandeja_instrumental` (`id_bandeja`)
               ON DELETE SET NULL ON UPDATE CASCADE,
       CONSTRAINT `fk_cirugia_clinica`
           FOREIGN KEY (`id_clinica_hospital`) REFERENCES `clinica_hospital` (`id_cliente`)
               ON DELETE RESTRICT ON UPDATE RESTRICT,
       CONSTRAINT `fk_cirugia_paciente`
           FOREIGN KEY (`id_paciente_particular`) REFERENCES `paciente_particular` (`id_cliente`)
               ON DELETE RESTRICT ON UPDATE RESTRICT,
       CONSTRAINT `fk_cirugia_solicitud_origen`
           FOREIGN KEY (`id_solicitud_origen`) REFERENCES `solicitud_cirugia` (`id_solicitud`)
               ON DELETE RESTRICT ON UPDATE RESTRICT,
       CONSTRAINT `chk_cirugia_un_cliente` CHECK (
           (`id_clinica_hospital` IS NOT NULL AND `id_paciente_particular` IS NULL)
               OR
           (`id_clinica_hospital` IS NULL AND `id_paciente_particular` IS NOT NULL)
           ),
       CONSTRAINT `chk_cirugia_fechas` CHECK (
           `fecha_hora_fin` IS NULL OR `fecha_hora_fin` > `fecha_hora_inicio`
           ),
       CONSTRAINT `chk_cirugia_finalizada_con_fin` CHECK (
           `estado` <> 'FINALIZADA' OR `fecha_hora_fin` IS NOT NULL
           ),
       CONSTRAINT `chk_cirugia_cancelada_con_motivo` CHECK (
           `estado` <> 'CANCELADA'
               OR (`motivo_cancelacion` IS NOT NULL AND CHAR_LENGTH(TRIM(`motivo_cancelacion`)) > 0)
           )
);

CREATE TABLE IF NOT EXISTS `cotizacion` (
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

CREATE TABLE IF NOT EXISTS `orden_compra` (
            `id_orden_compra` INT NOT NULL AUTO_INCREMENT,
            `archivo_respaldo_path` VARCHAR(255) NOT NULL,
            `fecha_recepcion` DATETIME NOT NULL,
            `activo` TINYINT(1) NOT NULL DEFAULT 1,
            PRIMARY KEY (`id_orden_compra`)
);

-- Un documento rechazado NUNCA se borra: la resubida inserta una nueva version
-- (mismo origen y tipo, version + 1), de modo que el historial queda completo.
CREATE TABLE IF NOT EXISTS `documento_ingreso` (
                 `id_documento` INT NOT NULL AUTO_INCREMENT,
                 `tipo_documento` ENUM('DNI','RECETA_MEDICA','ORDEN_COMPRA','COMPROBANTE_PAGO') NOT NULL,
                 `archivo_path` VARCHAR(255) NOT NULL,
                 `estado_validacion` ENUM('PENDIENTE','VALIDADO','RECHAZADO')
NOT NULL DEFAULT 'PENDIENTE',
                 `fecha_carga` DATETIME NOT NULL,
                 `fecha_validacion` DATETIME NULL,
                 `motivo_rechazo` VARCHAR(200) NULL,
                 `version` INT NOT NULL DEFAULT 1,
                 `responsable` VARCHAR(150) NULL,
                 `id_solicitud` INT NULL,
                 `id_cirugia` INT NULL,
                 `id_orden_compra` INT NULL,
                 `activo` TINYINT(1) NOT NULL DEFAULT 1,
                 `id_administrador` INT NULL,
                 `id_vendedor` INT NULL,
                 `id_tecnico` INT NULL,
                 PRIMARY KEY (`id_documento`),
                 UNIQUE KEY `uq_documento_solicitud_version` (`id_solicitud`, `tipo_documento`, `version`),
                 UNIQUE KEY `uq_documento_cirugia_version` (`id_cirugia`, `tipo_documento`, `version`),
                 KEY `idx_documento_solicitud` (`id_solicitud`, `tipo_documento`),
                 KEY `idx_documento_cirugia` (`id_cirugia`, `tipo_documento`),
                 KEY `idx_documento_estado` (`estado_validacion`),
                 CONSTRAINT `fk_documento_ingreso_orden`
                     FOREIGN KEY (`id_orden_compra`) REFERENCES `orden_compra` (`id_orden_compra`)
                         ON DELETE SET NULL ON UPDATE CASCADE,
                 CONSTRAINT `fk_documento_ingreso_solicitud`
                     FOREIGN KEY (`id_solicitud`) REFERENCES `solicitud_cirugia` (`id_solicitud`)
                         ON DELETE RESTRICT ON UPDATE RESTRICT,
                 CONSTRAINT `fk_documento_ingreso_cirugia`
                     FOREIGN KEY (`id_cirugia`) REFERENCES `cirugia` (`id_cirugia`)
                         ON DELETE RESTRICT ON UPDATE RESTRICT,
                 CONSTRAINT `fk_documento_ingreso_admin`
                     FOREIGN KEY (`id_administrador`) REFERENCES `administrador` (`id_usuario`)
                         ON DELETE RESTRICT ON UPDATE RESTRICT,
                 CONSTRAINT `fk_documento_ingreso_vendedor`
                     FOREIGN KEY (`id_vendedor`) REFERENCES `vendedor` (`id_usuario`)
                         ON DELETE RESTRICT ON UPDATE RESTRICT,
                 CONSTRAINT `fk_documento_ingreso_tecnico`
                     FOREIGN KEY (`id_tecnico`) REFERENCES `tecnico_instrumentista` (`id_usuario`)
                         ON DELETE RESTRICT ON UPDATE RESTRICT,
                 CONSTRAINT `chk_documento_version` CHECK (`version` >= 1),
                 -- los clientes no tienen usuario en la plataforma: a lo sumo un usuario
                 -- y, en su lugar, el nombre del responsable como texto
                 CONSTRAINT `chk_documento_ingreso_un_usuario` CHECK (
                     (`id_administrador` IS NOT NULL)
                         + (`id_vendedor` IS NOT NULL)
                         + (`id_tecnico` IS NOT NULL) <= 1
                     )
);

CREATE TABLE IF NOT EXISTS `linea_orden_compra` (
                  `id_linea` INT NOT NULL AUTO_INCREMENT,
                  `id_orden_compra` INT NOT NULL,
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

CREATE TABLE IF NOT EXISTS `notificacion` (
            `id_notificacion` INT NOT NULL AUTO_INCREMENT,
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
                    ON DELETE RESTRICT ON UPDATE RESTRICT,
            CONSTRAINT `fk_notificacion_vendedor`
                FOREIGN KEY (`id_vendedor`) REFERENCES `vendedor` (`id_usuario`)
                    ON DELETE RESTRICT ON UPDATE RESTRICT,
            CONSTRAINT `fk_notificacion_tecnico`
                FOREIGN KEY (`id_tecnico`) REFERENCES `tecnico_instrumentista` (`id_usuario`)
                    ON DELETE RESTRICT ON UPDATE RESTRICT,
            CONSTRAINT `chk_notificacion_un_usuario` CHECK (
                (`id_administrador` IS NOT NULL)
                    + (`id_vendedor` IS NOT NULL)
                    + (`id_tecnico` IS NOT NULL) = 1
                )
);

-- Despacho y retorno (RF14). Sin clase Java: se maneja desde EquipoBL/EquipoDAO.
CREATE TABLE IF NOT EXISTS `despacho` (
            `id_despacho` INT NOT NULL AUTO_INCREMENT,
            `codigo` VARCHAR(20) NOT NULL,
            `id_cirugia` INT NOT NULL,
            `id_equipo` INT NULL,
            `id_bandeja` INT NULL,
            `fecha_salida` DATETIME NOT NULL,
            `fecha_retorno` DATETIME NULL,
            `estado` ENUM('DESPACHADO','RETORNADO') NOT NULL DEFAULT 'DESPACHADO',
            `activo` TINYINT(1) NOT NULL DEFAULT 1,
            -- un solo despacho pendiente de retorno por cirugia y por equipo
            `pendiente_cirugia` INT GENERATED ALWAYS AS (
                CASE WHEN `fecha_retorno` IS NULL AND `activo` = 1 THEN `id_cirugia` ELSE NULL END
                ) STORED,
            `pendiente_equipo` INT GENERATED ALWAYS AS (
                CASE WHEN `fecha_retorno` IS NULL AND `activo` = 1 THEN `id_equipo` ELSE NULL END
                ) STORED,
            PRIMARY KEY (`id_despacho`),
            UNIQUE KEY `uq_despacho_codigo` (`codigo`),
            UNIQUE KEY `uq_despacho_pendiente_cirugia` (`pendiente_cirugia`),
            UNIQUE KEY `uq_despacho_pendiente_equipo` (`pendiente_equipo`),
            KEY `idx_despacho_cirugia` (`id_cirugia`),
            KEY `idx_despacho_equipo` (`id_equipo`),
            KEY `idx_despacho_pendientes` (`fecha_retorno`, `estado`),
            CONSTRAINT `fk_despacho_cirugia`
                FOREIGN KEY (`id_cirugia`) REFERENCES `cirugia` (`id_cirugia`)
                    ON DELETE RESTRICT ON UPDATE RESTRICT,
            CONSTRAINT `fk_despacho_equipo`
                FOREIGN KEY (`id_equipo`) REFERENCES `equipo` (`id_equipo`)
                    ON DELETE RESTRICT ON UPDATE RESTRICT,
            CONSTRAINT `fk_despacho_bandeja`
                FOREIGN KEY (`id_bandeja`) REFERENCES `bandeja_instrumental` (`id_bandeja`)
                    ON DELETE RESTRICT ON UPDATE RESTRICT,
            CONSTRAINT `chk_despacho_recurso` CHECK (`id_equipo` IS NOT NULL OR `id_bandeja` IS NOT NULL),
            CONSTRAINT `chk_despacho_fechas` CHECK (`fecha_retorno` IS NULL OR `fecha_retorno` >= `fecha_salida`),
            CONSTRAINT `chk_despacho_estado` CHECK (
                (`estado` = 'RETORNADO') = (`fecha_retorno` IS NOT NULL)
                )
);

-- Bitacora de auditoria de cirugias (RF16). Sin clase Java: se maneja desde
-- CirugiaBL/CirugiaDAO. Los valores anterior/posterior se guardan como lineas campo=valor.
CREATE TABLE IF NOT EXISTS `auditoria_cirugia` (
            `id_auditoria` INT NOT NULL AUTO_INCREMENT,
            `id_cirugia` INT NOT NULL,
            `fecha_hora` DATETIME NOT NULL,
            `accion` VARCHAR(40) NOT NULL,
            `responsable` VARCHAR(150) NOT NULL,
            `detalle` VARCHAR(500) NULL,
            `valores_anteriores` TEXT NULL,
            `valores_posteriores` TEXT NULL,
            PRIMARY KEY (`id_auditoria`),
            KEY `idx_auditoria_cirugia_fecha` (`id_cirugia`, `fecha_hora`),
            KEY `idx_auditoria_fecha` (`fecha_hora`),
            KEY `idx_auditoria_accion` (`accion`),
            CONSTRAINT `fk_auditoria_cirugia`
                FOREIGN KEY (`id_cirugia`) REFERENCES `cirugia` (`id_cirugia`)
                    ON DELETE RESTRICT ON UPDATE RESTRICT
);
