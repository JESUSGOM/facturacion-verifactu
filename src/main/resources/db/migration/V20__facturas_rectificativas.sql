-- ============================================================
-- V20 - Facturas rectificativas
--
-- Objetivos:
-- 1. Distinguir series ordinarias y rectificativas.
-- 2. Soportar facturas F1 y rectificativas R1-R4.
-- 3. Soportar rectificacion por sustitucion (S)
--    y por diferencias/incremental (I).
-- 4. Permitir una rectificativa sobre una o varias facturas.
-- 5. Conservar snapshot inmutable VERI*FACTU de las
--    facturas rectificadas.
-- 6. Admitir importes negativos exclusivamente mediante
--    el signo de las lineas rectificativas.
--
-- R5 queda fuera de esta version porque todavia no se
-- soportan facturas simplificadas F2.
-- ============================================================


-- ------------------------------------------------------------
-- 1. TIPO DE SERIE
-- ------------------------------------------------------------

ALTER TABLE series_facturacion
    ADD COLUMN tipo_serie VARCHAR(20)
        NOT NULL DEFAULT 'ORDINARIA'
        AFTER descripcion,

    ADD CONSTRAINT chk_series_facturacion_tipo
        CHECK (
            tipo_serie IN (
                'ORDINARIA',
                'RECTIFICATIVA'
            )
        );


-- ------------------------------------------------------------
-- 2. DATOS DE RECTIFICACION EN FACTURAS
--
-- Las facturas historicas quedan como F1.
-- ------------------------------------------------------------

ALTER TABLE facturas
    ADD COLUMN tipo_factura VARCHAR(2)
        NOT NULL DEFAULT 'F1'
        AFTER fecha_expedicion,

    ADD COLUMN tipo_rectificativa CHAR(1)
        NULL
        AFTER tipo_factura,

    ADD COLUMN motivo_rectificacion VARCHAR(500)
        NULL
        AFTER tipo_rectificativa,

    ADD COLUMN base_rectificada DECIMAL(15,2)
        NULL
        AFTER motivo_rectificacion,

    ADD COLUMN cuota_rectificada DECIMAL(15,2)
        NULL
        AFTER base_rectificada;


ALTER TABLE facturas
    ADD CONSTRAINT chk_facturas_tipo_factura
        CHECK (
            tipo_factura IN (
                'F1',
                'R1',
                'R2',
                'R3',
                'R4'
            )
        ),

    ADD CONSTRAINT chk_facturas_tipo_rectificativa
        CHECK (
            tipo_rectificativa IS NULL
            OR tipo_rectificativa IN ('S', 'I')
        );


-- ------------------------------------------------------------
-- 3. COHERENCIA DE LOS DATOS RECTIFICATIVOS
--
-- F1:
--   no contiene datos de rectificacion.
--
-- I:
--   rectificacion por diferencias.
--   No usa BaseRectificada/CuotaRectificada.
--
-- S:
--   rectificacion por sustitucion.
--   Conserva BaseRectificada/CuotaRectificada.
-- ------------------------------------------------------------

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
                tipo_factura IN ('R1','R2','R3','R4')
                AND tipo_rectificativa = 'I'
                AND motivo_rectificacion IS NOT NULL
                AND base_rectificada IS NULL
                AND cuota_rectificada IS NULL
            )
            OR
            (
                tipo_factura IN ('R1','R2','R3','R4')
                AND tipo_rectificativa = 'S'
                AND motivo_rectificacion IS NOT NULL
                AND base_rectificada IS NOT NULL
                AND cuota_rectificada IS NOT NULL
            )
        );


-- ------------------------------------------------------------
-- 4. TOTALES DE FACTURA
--
-- F1 mantiene la proteccion historica de importes >= 0.
--
-- R1-R4 pueden representar aumentos o disminuciones y,
-- por tanto, pueden contener importes con signo.
-- ------------------------------------------------------------

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
            tipo_factura IN ('R1','R2','R3','R4')
        );


-- ------------------------------------------------------------
-- 5. SIGNO DE LAS LINEAS
--
-- cantidad y precio_unitario continuan siendo positivos.
--
-- signo =  1 : importe positivo
-- signo = -1 : disminucion rectificativa
-- ------------------------------------------------------------

ALTER TABLE factura_lineas
    ADD COLUMN signo SMALLINT
        NOT NULL DEFAULT 1
        AFTER precio_unitario,

    ADD CONSTRAINT chk_factura_lineas_signo
        CHECK (
            signo IN (-1, 1)
        );


ALTER TABLE factura_lineas
    DROP CHECK chk_factura_lineas_totales;

ALTER TABLE factura_lineas
    ADD CONSTRAINT chk_factura_lineas_totales
        CHECK (
            (
                signo = 1
                AND base_imponible >= 0
                AND cuota_impuesto >= 0
                AND total >= 0
            )
            OR
            (
                signo = -1
                AND base_imponible <= 0
                AND cuota_impuesto <= 0
                AND total <= 0
            )
        );


-- ------------------------------------------------------------
-- 6. FACTURAS ORIGINALES RECTIFICADAS
--
-- Una factura rectificativa puede referenciar varias
-- facturas originales.
-- ------------------------------------------------------------

CREATE TABLE facturas_rectificadas (

    id BIGINT NOT NULL AUTO_INCREMENT,

    factura_rectificativa_id BIGINT NOT NULL,
    factura_rectificada_id BIGINT NOT NULL,

    orden SMALLINT UNSIGNED NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_facturas_rectificadas_facturas
        UNIQUE (
            factura_rectificativa_id,
            factura_rectificada_id
        ),

    CONSTRAINT uk_facturas_rectificadas_orden
        UNIQUE (
            factura_rectificativa_id,
            orden
        ),

    CONSTRAINT fk_facturas_rectificadas_rectificativa
        FOREIGN KEY (factura_rectificativa_id)
            REFERENCES facturas(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_facturas_rectificadas_original
        FOREIGN KEY (factura_rectificada_id)
            REFERENCES facturas(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_facturas_rectificadas_distintas
        CHECK (
            factura_rectificativa_id
                <> factura_rectificada_id
        ),

    CONSTRAINT chk_facturas_rectificadas_orden
        CHECK (
            orden BETWEEN 1 AND 1000
        )

) ENGINE=InnoDB;


CREATE INDEX idx_facturas_rectificadas_original
    ON facturas_rectificadas (
        factura_rectificada_id
    );


-- ------------------------------------------------------------
-- 7. SNAPSHOT RECTIFICATIVO DEL REGISTRO VERI*FACTU
-- ------------------------------------------------------------

ALTER TABLE registros_facturacion

    ADD COLUMN tipo_rectificativa CHAR(1)
        NULL
        AFTER tipo_factura,

    ADD COLUMN base_rectificada DECIMAL(15,2)
        NULL
        AFTER importe_total,

    ADD COLUMN cuota_rectificada DECIMAL(15,2)
        NULL
        AFTER base_rectificada,

    ADD CONSTRAINT chk_registros_tipo_rectificativa
        CHECK (
            tipo_rectificativa IS NULL
            OR tipo_rectificativa IN ('S', 'I')
        );


-- ------------------------------------------------------------
-- 8. SNAPSHOT DE LAS FACTURAS RECTIFICADAS
--
-- No dependemos de cambios posteriores en las entidades
-- de factura para reconstruir el XML VERI*FACTU.
-- ------------------------------------------------------------

CREATE TABLE registros_facturacion_rectificadas (

    id BIGINT NOT NULL AUTO_INCREMENT,

    registro_facturacion_id BIGINT NOT NULL,

    factura_rectificada_id BIGINT NOT NULL,

    orden SMALLINT UNSIGNED NOT NULL,

    nif_emisor VARCHAR(20) NOT NULL,

    num_serie_factura VARCHAR(60) NOT NULL,

    fecha_expedicion DATE NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_reg_fact_rectificadas_orden
        UNIQUE (
            registro_facturacion_id,
            orden
        ),

    CONSTRAINT uk_reg_fact_rectificadas_factura
        UNIQUE (
            registro_facturacion_id,
            factura_rectificada_id
        ),

    CONSTRAINT fk_reg_fact_rectificadas_registro
        FOREIGN KEY (registro_facturacion_id)
            REFERENCES registros_facturacion(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_reg_fact_rectificadas_factura
        FOREIGN KEY (factura_rectificada_id)
            REFERENCES facturas(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_reg_fact_rectificadas_orden
        CHECK (
            orden BETWEEN 1 AND 1000
        )

) ENGINE=InnoDB;


CREATE INDEX idx_reg_fact_rectificadas_factura
    ON registros_facturacion_rectificadas (
        factura_rectificada_id
    );