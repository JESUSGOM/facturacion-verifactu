-- ============================================================
-- V30 - Relacion entre factura sustitutiva F3 y F2 sustituidas
--
-- V1-V29 ya estan aplicadas y NO deben modificarse.
--
-- Una factura F3 puede sustituir una o varias facturas
-- simplificadas F2.
--
-- Esta tabla representa la relacion funcional entre facturas.
-- El snapshot inmutable utilizado por VERI*FACTU se incorporara
-- posteriormente sobre el registro de facturacion.
-- ============================================================

CREATE TABLE facturas_sustituidas (

    id BIGINT NOT NULL AUTO_INCREMENT,

    factura_sustitutiva_id BIGINT NOT NULL,

    factura_sustituida_id BIGINT NOT NULL,

    orden INT UNSIGNED NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_facturas_sustituidas_facturas
        UNIQUE (
            factura_sustitutiva_id,
            factura_sustituida_id
        ),

    CONSTRAINT uk_facturas_sustituidas_orden
        UNIQUE (
            factura_sustitutiva_id,
            orden
        ),

    CONSTRAINT fk_facturas_sustituidas_sustitutiva
        FOREIGN KEY (factura_sustitutiva_id)
            REFERENCES facturas(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_facturas_sustituidas_original
        FOREIGN KEY (factura_sustituida_id)
            REFERENCES facturas(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_facturas_sustituidas_distintas
        CHECK (
            factura_sustitutiva_id
                <> factura_sustituida_id
        ),

    CONSTRAINT chk_facturas_sustituidas_orden
        CHECK (
            orden BETWEEN 1 AND 1000
        )

) ENGINE=InnoDB;


CREATE INDEX idx_facturas_sustituidas_original
    ON facturas_sustituidas (
        factura_sustituida_id
    );
