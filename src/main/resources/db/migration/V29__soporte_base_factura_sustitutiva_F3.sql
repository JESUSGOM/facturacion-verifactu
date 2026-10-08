-- ============================================================
-- V29 - Soporte base para factura sustitutiva F3
--
-- V1-V28 ya estan aplicadas y NO deben modificarse.
--
-- Prepara el esquema para admitir:
--
--   F1  factura con destinatario identificado
--   F2  factura simplificada/sin destinatario identificado
--   F3  factura emitida en sustitucion de facturas simplificadas
--   R1-R5 facturas rectificativas
--
-- En esta migracion solo se habilita el tipo F3 y su coherencia
-- basica en la tabla facturas.
--
-- La relacion con las facturas simplificadas sustituidas y el
-- snapshot VERI*FACTU se incorporaran en migraciones posteriores.
-- ============================================================

ALTER TABLE facturas
    DROP CHECK chk_facturas_tipo_factura;

ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_tipo_factura
        CHECK (
            tipo_factura IN (
                             'F1',
                             'F2',
                             'F3',
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
                tipo_factura IN ('F1','F2','F3')
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
                tipo_factura IN ('F1','F2','F3')
                    AND base_imponible >= 0
                    AND cuota_impuesto >= 0
                    AND total >= 0
                )
                OR
            tipo_factura IN ('R1','R2','R3','R4','R5')
            );
