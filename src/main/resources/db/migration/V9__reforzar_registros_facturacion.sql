ALTER TABLE registros_facturacion
    ADD COLUMN num_serie_factura VARCHAR(60) NULL
        AFTER numero;

UPDATE registros_facturacion
SET num_serie_factura = CONCAT(serie, '-', numero)
WHERE num_serie_factura IS NULL;

ALTER TABLE registros_facturacion
    MODIFY COLUMN num_serie_factura VARCHAR(60) NOT NULL;

CREATE UNIQUE INDEX uk_registros_facturacion_factura_tipo
    ON registros_facturacion (factura_id, tipo_registro);