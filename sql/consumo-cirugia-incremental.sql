-- Solo para bases existentes SIN cirugia_consumible_real.
-- Respaldar y verificar el esquema antes de ejecutar. No atribuye datos
-- antiguos de bandeja_consumible a cirugias: esa relacion no era persistente.
-- No ejecutar despues de DDL_KLAM.sql en una instalacion nueva.
USE `klam`;

CREATE TABLE IF NOT EXISTS `cirugia_consumible_real` (
    `id_cirugia` INT NOT NULL,
    `id_consumible` INT NOT NULL,
    `cantidad_despachada` INT NOT NULL,
    `cantidad_consumida` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id_cirugia`, `id_consumible`),
    CONSTRAINT `fk_ccr_cirugia` FOREIGN KEY (`id_cirugia`)
        REFERENCES `cirugia` (`id_cirugia`) ON DELETE RESTRICT,
    CONSTRAINT `fk_ccr_consumible` FOREIGN KEY (`id_consumible`)
        REFERENCES `consumible` (`id_consumible`) ON DELETE RESTRICT,
    CONSTRAINT `chk_ccr_despachada` CHECK (`cantidad_despachada` >= 0),
    CONSTRAINT `chk_ccr_consumida` CHECK (`cantidad_consumida` >= 0
        AND `cantidad_consumida` <= `cantidad_despachada`)
);
