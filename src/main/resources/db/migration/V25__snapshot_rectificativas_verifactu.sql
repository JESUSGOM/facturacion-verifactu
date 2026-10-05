-- ============================================================
-- V25 - Snapshot VERI*FACTU para facturas rectificativas
--
-- V1-V24 ya estan aplicadas y NO deben modificarse.
--
-- Añade:
--
-- 1. Datos propios de la rectificacion al registro fiscal:
--      tipo_rectificativa
--      base_rectificada
--      cuota_rectificada
--
-- 2. Snapshot independiente de las facturas rectificadas:
--      orden
--      identificador local original
--      numero/serie fiscal
--      fecha de expedicion
--
-- Los datos fiscales del snapshot no dependen posteriormente
-- de cambios en la factura original.
-- ============================================================

ALTER TABLE registros_facturacion

    ADD COLUMN tipo_rectificativa VARCHAR(1) NULL
        AFTER tipo_factura,

    ADD COLUMN base_rectificada DECIMAL(15,2) NULL
        AFTER tipo_rectificativa,

    ADD COLUMN cuota_rectificada DECIMAL(15,2) NULL
        AFTER base_rectificada;

ALTER TABLE registros_facturacion
    ADD CONSTRAINT chk_registros_tipo_rectificativa
        CHECK (
            tipo_rectificativa IS NULL
            OR tipo_rectificativa IN ('S','I')
        );


-- ============================================================
-- Facturas originales identificadas en una rectificativa
-- ============================================================

CREATE TABLE registros_facturacion_rectificadas (

    id BIGINT NOT NULL AUTO_INCREMENT,

    registro_facturacion_id BIGINT NOT NULL,

    /*
     * Referencia interna para trazabilidad local.
     * El dato fiscal independiente es num_serie_factura +
     * fecha_expedicion.
     */
    factura_rectificada_id BIGINT NULL,

    orden SMALLINT UNSIGNED NOT NULL,

    num_serie_factura VARCHAR(60) NOT NULL,

    fecha_expedicion DATE NOT NULL,

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_reg_fact_rect_registro
        FOREIGN KEY (registro_facturacion_id)
            REFERENCES registros_facturacion(id),

    CONSTRAINT uk_reg_fact_rect_orden
        UNIQUE (
            registro_facturacion_id,
            orden
        ),

    CONSTRAINT uk_reg_fact_rect_factura
        UNIQUE (
            registro_facturacion_id,
            num_serie_factura,
            fecha_expedicion
        )
);