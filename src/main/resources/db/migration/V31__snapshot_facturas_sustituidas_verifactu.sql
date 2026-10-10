-- ============================================================
-- V31 - Snapshot VERI*FACTU de facturas sustituidas por F3
--
-- V1-V30 ya estan definidas y NO deben modificarse.
--
-- Conserva de forma inmutable la identificacion fiscal de las
-- facturas simplificadas F2 sustituidas por una factura F3.
--
-- Para cada F2 identificada se conserva:
--
--   IDEmisorFactura
--   NumSerieFactura
--   FechaExpedicionFactura
--
-- factura_sustituida_id se mantiene unicamente como referencia
-- local de trazabilidad. Los datos fiscales independientes son
-- nif_emisor + num_serie_factura + fecha_expedicion.
-- ============================================================

CREATE TABLE registros_facturacion_sustituidas (

    id BIGINT NOT NULL AUTO_INCREMENT,

    registro_facturacion_id BIGINT NOT NULL,

    /*
     * Referencia interna para trazabilidad local.
     * No forma parte de la identificacion fiscal AEAT.
     */
    factura_sustituida_id BIGINT NULL,

    orden SMALLINT UNSIGNED NOT NULL,

    nif_emisor VARCHAR(20) NOT NULL,

    num_serie_factura VARCHAR(60) NOT NULL,

    fecha_expedicion DATE NOT NULL,

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_reg_fact_sust_registro
        FOREIGN KEY (registro_facturacion_id)
            REFERENCES registros_facturacion(id),

    CONSTRAINT uk_reg_fact_sust_orden
        UNIQUE (
            registro_facturacion_id,
            orden
        ),

    CONSTRAINT uk_reg_fact_sust_factura
        UNIQUE (
            registro_facturacion_id,
            num_serie_factura,
            fecha_expedicion
        ),

    CONSTRAINT chk_reg_fact_sust_orden
        CHECK (
            orden BETWEEN 1 AND 1000
        )

) ENGINE=InnoDB;

CREATE INDEX idx_reg_fact_sust_factura_id
    ON registros_facturacion_sustituidas (
        factura_sustituida_id
    );
