-- ============================================================
-- V28 - Permite series de facturacion simplificadas
--
-- V20 creo chk_series_facturacion_tipo admitiendo:
--   ORDINARIA
--   RECTIFICATIVA
--
-- Se amplia la restriccion para soportar tambien:
--   SIMPLIFICADA
--
-- Necesario para las facturas F2.
-- ============================================================

ALTER TABLE series_facturacion
    DROP CHECK chk_series_facturacion_tipo;

ALTER TABLE series_facturacion
    ADD CONSTRAINT chk_series_facturacion_tipo
        CHECK (
            tipo_serie IN (
                'ORDINARIA',
                'SIMPLIFICADA',
                'RECTIFICATIVA'
            )
        );