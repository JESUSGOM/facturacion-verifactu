-- ============================================================
-- V24 - Permite factura rectificativa R5
--
-- V20 ya está aplicada y no debe modificarse.
--
-- Se amplían los CHECK existentes para admitir R5:
--   - tipo de factura
--   - coherencia de datos rectificativos
--   - importes con signo
-- ============================================================

ALTER TABLE facturas
    DROP CHECK chk_facturas_tipo_factura;

ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_tipo_factura
        CHECK (
            tipo_factura IN (
                'F1',
                'R1',
                'R2',
                'R3',
                'R4',
                'R5'
            )
        );


ALTER TABLE facturas
    DROP CHECK chk_facturas_datos_rectificacion;

ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_datos_rectificacion
        CHECK (
            (
                tipo_factura = 'F1'
                AND tipo_rectificativa IS NULL
                AND motivo_rectificacion IS NULL
                AND base_rectificada IS NULL
                AND cuota_rectificada IS NULL
            )
            OR
            (
                tipo_factura IN ('R1','R2','R3','R4','R5')
                AND tipo_rectificativa = 'I'
                AND motivo_rectificacion IS NOT NULL
                AND base_rectificada IS NULL
                AND cuota_rectificada IS NULL
            )
            OR
            (
                tipo_factura IN ('R1','R2','R3','R4','R5')
                AND tipo_rectificativa = 'S'
                AND motivo_rectificacion IS NOT NULL
                AND base_rectificada IS NOT NULL
                AND cuota_rectificada IS NOT NULL
            )
        );


ALTER TABLE facturas
    DROP CHECK chk_facturas_totales;

ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_totales
        CHECK (
            (
                tipo_factura = 'F1'
                AND base_imponible >= 0
                AND cuota_impuesto >= 0
                AND total >= 0
            )
            OR
            tipo_factura IN ('R1','R2','R3','R4','R5')
        );