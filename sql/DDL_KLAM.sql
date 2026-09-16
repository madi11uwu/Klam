-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema klam
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema klam
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `klam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;

USE `klam` ;

-- -----------------------------------------------------
-- Table `klam`.`usuario_plataforma`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`usuario_plataforma` (
  `id_usuario` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `email` VARCHAR(120) NOT NULL,
  `nombres` VARCHAR(100) NOT NULL,
  `apellidos` VARCHAR(100) NOT NULL,
  `rol` ENUM('ADMINISTRADOR', 'VENDEDOR', 'TECNICO_INSTRUMENTISTA') NOT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_usuario`),
  UNIQUE INDEX `username` (`username` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`administrador`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`administrador` (
  `id_usuario` INT NOT NULL,
  `id_admin` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `fk_administrador_usuario`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `klam`.`usuario_plataforma` (`id_usuario`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`bandeja_instrumental`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`bandeja_instrumental` (
  `id_bandeja` INT NOT NULL AUTO_INCREMENT,
  `tipo` VARCHAR(100) NOT NULL,
  `esterilizado` TINYINT(1) NOT NULL DEFAULT '0',
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_bandeja`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`consumible`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`consumible` (
  `id_consumible` INT NOT NULL AUTO_INCREMENT,
  `nombre_comercial` VARCHAR(150) NOT NULL,
  `marca` VARCHAR(100) NULL DEFAULT NULL,
  `medida` VARCHAR(50) NULL DEFAULT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_consumible`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`bandeja_consumible`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`bandeja_consumible` (
  `id_bandeja` INT NOT NULL,
  `id_consumible` INT NOT NULL,
  `cantidad_despachada` INT NOT NULL DEFAULT '0',
  `cantidad_consumida` INT NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_bandeja`, `id_consumible`),
  INDEX `fk_bandeja_consumible_consumible` (`id_consumible` ASC) VISIBLE,
  CONSTRAINT `fk_bandeja_consumible_bandeja`
    FOREIGN KEY (`id_bandeja`)
    REFERENCES `klam`.`bandeja_instrumental` (`id_bandeja`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_bandeja_consumible_consumible`
    FOREIGN KEY (`id_consumible`)
    REFERENCES `klam`.`consumible` (`id_consumible`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`equipo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`equipo` (
  `id_equipo` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(150) NOT NULL,
  `categoria` ENUM('CRANEOTOMO', 'NAVEGADOR') NOT NULL,
  `disponible` TINYINT(1) NOT NULL DEFAULT '1',
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_equipo`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`cliente`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`cliente` (
  `id_cliente` VARCHAR(50) NOT NULL,
  `nombre` VARCHAR(150) NOT NULL,
  `direccion` VARCHAR(200) NULL DEFAULT NULL,
  `email_contacto` VARCHAR(120) NULL DEFAULT NULL,
  `telefono` VARCHAR(20) NULL DEFAULT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_cliente`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`cirugia`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`cirugia` (
  `id_cirugia` INT NOT NULL AUTO_INCREMENT,
  `fecha_hora_inicio` DATETIME NOT NULL,
  `fecha_hora_fin` DATETIME NULL DEFAULT NULL,
  `tipo_procedimiento` VARCHAR(150) NOT NULL,
  `doctor_nombre` VARCHAR(150) NULL DEFAULT NULL,
  `motivo_cancelacion` VARCHAR(255) NULL DEFAULT NULL,
  `estado` ENUM('PROGRAMADA', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA') NOT NULL DEFAULT 'PROGRAMADA',
  `id_equipo` INT NULL DEFAULT NULL,
  `id_bandeja` INT NULL DEFAULT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  `cliente_id_cliente` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id_cirugia`, `cliente_id_cliente`),
  INDEX `fk_cirugia_equipo` (`id_equipo` ASC) VISIBLE,
  INDEX `fk_cirugia_bandeja` (`id_bandeja` ASC) VISIBLE,
  INDEX `fk_cirugia_cliente1_idx` (`cliente_id_cliente` ASC) VISIBLE,
  CONSTRAINT `fk_cirugia_bandeja`
    FOREIGN KEY (`id_bandeja`)
    REFERENCES `klam`.`bandeja_instrumental` (`id_bandeja`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_cirugia_equipo`
    FOREIGN KEY (`id_equipo`)
    REFERENCES `klam`.`equipo` (`id_equipo`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_cirugia_cliente1`
    FOREIGN KEY (`cliente_id_cliente`)
    REFERENCES `klam`.`cliente` (`id_cliente`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`documento_facturacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`documento_facturacion` (
  `id_documento` INT NOT NULL AUTO_INCREMENT,
  `tipo_documento` ENUM('FACTURA', 'BOLETA') NOT NULL,
  `id_cirugia` INT NOT NULL,
  `fecha_emision` DATETIME NOT NULL,
  `monto_base` DECIMAL(10,2) NOT NULL DEFAULT '0.00',
  `tasa_igv` DECIMAL(5,4) NOT NULL DEFAULT '0.1800',
  `igv` DECIMAL(10,2) NOT NULL DEFAULT '0.00',
  `monto_total` DECIMAL(10,2) NOT NULL DEFAULT '0.00',
  `estado_pago` ENUM('PENDIENTE', 'PAGADO', 'ANULADO') NOT NULL DEFAULT 'PENDIENTE',
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_documento`),
  INDEX `fk_documento_facturacion_cirugia` (`id_cirugia` ASC) VISIBLE,
  CONSTRAINT `fk_documento_facturacion_cirugia`
    FOREIGN KEY (`id_cirugia`)
    REFERENCES `klam`.`cirugia` (`id_cirugia`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`boleta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`boleta` (
  `id_documento` INT NOT NULL,
  `dni_receptor` CHAR(8) NOT NULL,
  PRIMARY KEY (`id_documento`),
  CONSTRAINT `fk_boleta_documento`
    FOREIGN KEY (`id_documento`)
    REFERENCES `klam`.`documento_facturacion` (`id_documento`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`clinica_hospital`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`clinica_hospital` (
  `id_cliente` VARCHAR(50) NOT NULL,
  `ruc` CHAR(11) NOT NULL,
  `tiene_consignacion` TINYINT(1) NOT NULL DEFAULT '0',
  `periodo_credito` VARCHAR(30) NULL DEFAULT NULL,
  PRIMARY KEY (`id_cliente`),
  UNIQUE INDEX `ruc` (`ruc` ASC) VISIBLE,
  CONSTRAINT `fk_clinica_cliente`
    FOREIGN KEY (`id_cliente`)
    REFERENCES `klam`.`cliente` (`id_cliente`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`cotizacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`cotizacion` (
  `id_cotizacion` INT NOT NULL AUTO_INCREMENT,
  `id_cirugia` INT NOT NULL,
  `precio_pactado` DECIMAL(10,2) NOT NULL,
  `estado` ENUM('EMITIDA', 'ACEPTADA', 'RECHAZADA', 'VENCIDA') NOT NULL DEFAULT 'EMITIDA',
  `fecha_emision` DATETIME NOT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_cotizacion`),
  INDEX `fk_cotizacion_cirugia` (`id_cirugia` ASC) VISIBLE,
  CONSTRAINT `fk_cotizacion_cirugia`
    FOREIGN KEY (`id_cirugia`)
    REFERENCES `klam`.`cirugia` (`id_cirugia`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`orden_compra`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`orden_compra` (
  `id_orden_compra` VARCHAR(50) NOT NULL,
  `archivo_respaldo_path` VARCHAR(255) NOT NULL,
  `fecha_recepcion` DATETIME NOT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_orden_compra`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`documento_ingreso`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`documento_ingreso` (
  `id_documento` VARCHAR(50) NOT NULL,
  `tipo_documento` ENUM('DNI', 'RECETA_MEDICA', 'ORDEN_COMPRA') NOT NULL,
  `archivo_path` VARCHAR(255) NOT NULL,
  `estado_validacion` ENUM('PENDIENTE', 'VALIDADO', 'RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_carga` DATETIME NOT NULL,
  `id_orden_compra` VARCHAR(50) NULL DEFAULT NULL,
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  `usuario_plataforma_id_usuario` INT NOT NULL,
  PRIMARY KEY (`id_documento`),
  INDEX `fk_documento_ingreso_orden` (`id_orden_compra` ASC) VISIBLE,
  INDEX `fk_documento_ingreso_usuario_plataforma1_idx` (`usuario_plataforma_id_usuario` ASC) VISIBLE,
  CONSTRAINT `fk_documento_ingreso_orden`
    FOREIGN KEY (`id_orden_compra`)
    REFERENCES `klam`.`orden_compra` (`id_orden_compra`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_documento_ingreso_usuario_plataforma1`
    FOREIGN KEY (`usuario_plataforma_id_usuario`)
    REFERENCES `klam`.`usuario_plataforma` (`id_usuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`equipo_especificacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`equipo_especificacion` (
  `id_equipo` INT NOT NULL,
  `clave` VARCHAR(50) NOT NULL,
  `valor` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id_equipo`, `clave`),
  CONSTRAINT `fk_especificacion_equipo`
    FOREIGN KEY (`id_equipo`)
    REFERENCES `klam`.`equipo` (`id_equipo`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`factura`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`factura` (
  `id_documento` INT NOT NULL,
  `ruc_receptor` CHAR(11) NOT NULL,
  PRIMARY KEY (`id_documento`),
  CONSTRAINT `fk_factura_documento`
    FOREIGN KEY (`id_documento`)
    REFERENCES `klam`.`documento_facturacion` (`id_documento`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`linea_boleta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`linea_boleta` (
  `id_linea` INT NOT NULL AUTO_INCREMENT,
  `id_documento` INT NOT NULL,
  `id_consumible` INT NULL DEFAULT NULL,
  `item_referencia` VARCHAR(50) NULL DEFAULT NULL,
  `descripcion` VARCHAR(150) NOT NULL,
  `cantidad` INT NOT NULL,
  `precio_unitario` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id_linea`),
  INDEX `fk_linea_boleta_documento` (`id_documento` ASC) VISIBLE,
  INDEX `fk_linea_boleta_consumible` (`id_consumible` ASC) VISIBLE,
  CONSTRAINT `fk_linea_boleta_consumible`
    FOREIGN KEY (`id_consumible`)
    REFERENCES `klam`.`consumible` (`id_consumible`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_linea_boleta_documento`
    FOREIGN KEY (`id_documento`)
    REFERENCES `klam`.`boleta` (`id_documento`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`linea_factura`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`linea_factura` (
  `id_linea` INT NOT NULL AUTO_INCREMENT,
  `id_documento` INT NOT NULL,
  `id_consumible` INT NULL DEFAULT NULL,
  `item_referencia` VARCHAR(50) NULL DEFAULT NULL,
  `descripcion` VARCHAR(150) NOT NULL,
  `cantidad` INT NOT NULL,
  `precio_unitario` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id_linea`),
  INDEX `fk_linea_factura_documento` (`id_documento` ASC) VISIBLE,
  INDEX `fk_linea_factura_consumible` (`id_consumible` ASC) VISIBLE,
  CONSTRAINT `fk_linea_factura_consumible`
    FOREIGN KEY (`id_consumible`)
    REFERENCES `klam`.`consumible` (`id_consumible`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_linea_factura_documento`
    FOREIGN KEY (`id_documento`)
    REFERENCES `klam`.`factura` (`id_documento`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`nota_credito`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`nota_credito` (
  `id_nota_credito` INT NOT NULL AUTO_INCREMENT,
  `id_documento_original` INT NOT NULL,
  `motivo` VARCHAR(255) NOT NULL,
  `fecha_emision` DATETIME NOT NULL,
  `monto_total` DECIMAL(10,2) NOT NULL DEFAULT '0.00',
  `activo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_nota_credito`),
  INDEX `fk_nota_credito_documento` (`id_documento_original` ASC) VISIBLE,
  CONSTRAINT `fk_nota_credito_documento`
    FOREIGN KEY (`id_documento_original`)
    REFERENCES `klam`.`documento_facturacion` (`id_documento`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`linea_nota_credito`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`linea_nota_credito` (
  `id_linea` INT NOT NULL AUTO_INCREMENT,
  `id_nota_credito` INT NOT NULL,
  `id_consumible` INT NULL DEFAULT NULL,
  `item_referencia` VARCHAR(50) NULL DEFAULT NULL,
  `descripcion` VARCHAR(150) NOT NULL,
  `cantidad` INT NOT NULL,
  `precio_unitario` DECIMAL(10,2) NOT NULL,
  `motivo_devolucion` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id_linea`),
  INDEX `fk_linea_nota_credito_nota` (`id_nota_credito` ASC) VISIBLE,
  INDEX `fk_linea_nota_credito_consumible` (`id_consumible` ASC) VISIBLE,
  CONSTRAINT `fk_linea_nota_credito_consumible`
    FOREIGN KEY (`id_consumible`)
    REFERENCES `klam`.`consumible` (`id_consumible`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_linea_nota_credito_nota`
    FOREIGN KEY (`id_nota_credito`)
    REFERENCES `klam`.`nota_credito` (`id_nota_credito`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`linea_orden_compra`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`linea_orden_compra` (
  `id_linea` INT NOT NULL AUTO_INCREMENT,
  `id_orden_compra` VARCHAR(50) NOT NULL,
  `id_consumible` INT NULL DEFAULT NULL,
  `item_referencia` VARCHAR(50) NULL DEFAULT NULL,
  `descripcion` VARCHAR(150) NOT NULL,
  `cantidad` INT NOT NULL,
  `precio_unitario` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id_linea`),
  INDEX `fk_linea_orden_compra_orden` (`id_orden_compra` ASC) VISIBLE,
  INDEX `fk_linea_orden_compra_consumible` (`id_consumible` ASC) VISIBLE,
  CONSTRAINT `fk_linea_orden_compra_consumible`
    FOREIGN KEY (`id_consumible`)
    REFERENCES `klam`.`consumible` (`id_consumible`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_linea_orden_compra_orden`
    FOREIGN KEY (`id_orden_compra`)
    REFERENCES `klam`.`orden_compra` (`id_orden_compra`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`notificacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`notificacion` (
  `id_notificacion` VARCHAR(50) NOT NULL,
  `fecha_hora` DATETIME NOT NULL,
  `titulo` VARCHAR(150) NOT NULL,
  `estado_leida` TINYINT(1) NOT NULL DEFAULT '0',
  `usuario_plataforma_id_usuario` INT NOT NULL,
  PRIMARY KEY (`id_notificacion`),
  INDEX `fk_notificacion_usuario_plataforma1_idx` (`usuario_plataforma_id_usuario` ASC) VISIBLE,
  CONSTRAINT `fk_notificacion_usuario_plataforma1`
    FOREIGN KEY (`usuario_plataforma_id_usuario`)
    REFERENCES `klam`.`usuario_plataforma` (`id_usuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`paciente_particular`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`paciente_particular` (
  `id_cliente` VARCHAR(50) NOT NULL,
  `dni` CHAR(8) NOT NULL,
  `pago_confirmado` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_cliente`),
  UNIQUE INDEX `dni` (`dni` ASC) VISIBLE,
  CONSTRAINT `fk_paciente_cliente`
    FOREIGN KEY (`id_cliente`)
    REFERENCES `klam`.`cliente` (`id_cliente`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`tecnico_instrumentista`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`tecnico_instrumentista` (
  `id_usuario` INT NOT NULL,
  `id_tecnico` VARCHAR(20) NOT NULL,
  `especialidad` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `fk_tecnico_usuario`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `klam`.`usuario_plataforma` (`id_usuario`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `klam`.`vendedor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `klam`.`vendedor` (
  `id_usuario` INT NOT NULL,
  `id_vendedor` VARCHAR(20) NOT NULL,
  `comision_acumulada` DECIMAL(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `fk_vendedor_usuario`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `klam`.`usuario_plataforma` (`id_usuario`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
