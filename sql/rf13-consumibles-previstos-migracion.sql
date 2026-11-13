-- RF13: separar la configuración estándar y la previsión por cirugía del despacho real.
-- Ejecutar una vez antes de desplegar los DAO actualizados. No altera filas históricas.
CREATE TABLE IF NOT EXISTS bandeja_consumible_estandar (
    id_bandeja INT NOT NULL,
    id_consumible INT NOT NULL,
    cantidad_estandar INT NOT NULL,
    PRIMARY KEY (id_bandeja, id_consumible),
    CONSTRAINT fk_bce_bandeja FOREIGN KEY (id_bandeja) REFERENCES bandeja_instrumental(id_bandeja),
    CONSTRAINT fk_bce_consumible FOREIGN KEY (id_consumible) REFERENCES consumible(id_consumible),
    CONSTRAINT chk_bce_cantidad CHECK (cantidad_estandar > 0)
);

-- Los valores que el catálogo anterior llamaba estándar se copian una sola vez.
-- La clave primaria evita sobrescribir plantillas modificadas después de migrar.
INSERT INTO bandeja_consumible_estandar (id_bandeja, id_consumible, cantidad_estandar)
SELECT id_bandeja, id_consumible, cantidad_despachada
FROM bandeja_consumible
WHERE cantidad_despachada > 0
ON DUPLICATE KEY UPDATE cantidad_estandar = bandeja_consumible_estandar.cantidad_estandar;

CREATE TABLE IF NOT EXISTS cirugia_consumible_previsto (
    id_cirugia INT NOT NULL,
    id_consumible INT NOT NULL,
    cantidad_prevista INT NOT NULL,
    PRIMARY KEY (id_cirugia, id_consumible),
    CONSTRAINT fk_ccp_cirugia FOREIGN KEY (id_cirugia) REFERENCES cirugia(id_cirugia),
    CONSTRAINT fk_ccp_consumible FOREIGN KEY (id_consumible) REFERENCES consumible(id_consumible),
    CONSTRAINT chk_ccp_cantidad CHECK (cantidad_prevista > 0)
);
