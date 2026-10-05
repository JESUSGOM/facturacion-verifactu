-- ============================================================
-- V26 - NIF del emisor en snapshot de facturas rectificadas
--
-- V25 ya esta aplicada y NO debe modificarse.
--
-- IDFacturaRectificada necesita conservar:
--
--   IDEmisorFactura
--   NumSerieFactura
--   FechaExpedicionFactura
--
-- Se toma el NIF historico almacenado en la factura original,
-- no el valor actual de la tabla empresas.
-- ============================================================

ALTER TABLE registros_facturacion_rectificadas
    ADD COLUMN nif_emisor VARCHAR(20) NULL
        AFTER orden;

UPDATE registros_facturacion_rectificadas r
INNER JOIN facturas f
        ON f.id = r.factura_rectificada_id
SET r.nif_emisor = f.empresa_nif
WHERE r.nif_emisor IS NULL;

ALTER TABLE registros_facturacion_rectificadas
    MODIFY COLUMN nif_emisor VARCHAR(20) NOT NULL;