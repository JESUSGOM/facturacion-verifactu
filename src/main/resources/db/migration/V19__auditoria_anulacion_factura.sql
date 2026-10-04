-- ============================================================
-- V19 - Auditoria de anulacion de factura
--
-- La anulacion es una operacion excepcional.
-- Conservamos fecha y motivo para trazabilidad.
-- ============================================================

ALTER TABLE facturas
    ADD COLUMN fecha_anulacion TIMESTAMP(6) NULL,
    ADD COLUMN motivo_anulacion VARCHAR(500) NULL;