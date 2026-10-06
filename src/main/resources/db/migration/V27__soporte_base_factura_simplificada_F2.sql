-- ============================================================
-- V27 - Soporte base para factura simplificada F2
--
-- Prepara el esquema para:
--   F1  factura ordinaria con destinatario identificado
--   F2  factura simplificada/sin destinatario identificado
--   R1-R5 facturas rectificativas
--
-- El circuito de creacion/emision de F2 se implementara
-- posteriormente.
-- ============================================================


-- ------------------------------------------------------------
-- 1. Una F2 puede no tener destinatario identificado.
-- ------------------------------------------------------------

ALTER TABLE facturas
    MODIFY COLUMN cliente_id BIGINT NULL;


-- ------------------------------------------------------------
-- 2. Admitir F2 como tipo de factura.
-- ------------------------------------------------------------

ALTER TABLE facturas
    DROP CHECK chk_facturas_tipo_factura;

ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_tipo_factura
        CHECK (
            tipo_factura IN (
                'F1',
                'F2',
                'R1',
                'R2',
                'R3',
                'R4',
                'R5'
            )
        );


-- ------------------------------------------------------------
-- 3. F1 y F2 no contienen datos propios de rectificacion.
-- ------------------------------------------------------------

ALTER TABLE facturas
    DROP CHECK chk_facturas_datos_rectificacion;

ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_datos_rectificacion
        CHECK (
            (
                tipo_factura IN ('F1','F2')
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


-- ------------------------------------------------------------
-- 4. F1 y F2 ordinarias mantienen importes no negativos.
-- ------------------------------------------------------------

ALTER TABLE facturas
    DROP CHECK chk_facturas_totales;

ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_totales
        CHECK (
            (
                tipo_factura IN ('F1','F2')
                AND base_imponible >= 0
                AND cuota_impuesto >= 0
                AND total >= 0
            )
            OR
            tipo_factura IN ('R1','R2','R3','R4','R5')
        );