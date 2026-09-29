USE `klam`;

-- Desactivar temporalmente la comprobación de llaves foráneas
SET FOREIGN_KEY_CHECKS = 0;

-- Eliminar tablas transaccionales y de detalle (hijas)
DROP TABLE IF EXISTS `notificacion`;
DROP TABLE IF EXISTS `linea_orden_compra`;
DROP TABLE IF EXISTS `linea_nota_credito`;
DROP TABLE IF EXISTS `nota_credito`;
DROP TABLE IF EXISTS `linea_boleta`;
DROP TABLE IF EXISTS `linea_factura`;
DROP TABLE IF EXISTS `boleta`;
DROP TABLE IF EXISTS `factura`;
DROP TABLE IF EXISTS `documento_ingreso`;
DROP TABLE IF EXISTS `orden_compra`;
DROP TABLE IF EXISTS `cotizacion`;

-- Eliminar tablas operativas intermedias
DROP TABLE IF EXISTS `cirugia`;
DROP TABLE IF EXISTS `bandeja_consumible`;
DROP TABLE IF EXISTS `equipo_especificacion`;

DROP TABLE IF EXISTS `paciente_particular`;
DROP TABLE IF EXISTS `clinica_hospital`;
DROP TABLE IF EXISTS `equipo`;
DROP TABLE IF EXISTS `consumible`;
DROP TABLE IF EXISTS `bandeja_instrumental`;

-- Eliminar tablas de usuarios
DROP TABLE IF EXISTS `tecnico_instrumentista`;
DROP TABLE IF EXISTS `vendedor`;
DROP TABLE IF EXISTS `administrador`;

-- Reactivar la comprobación de llaves foráneas
SET FOREIGN_KEY_CHECKS = 1;