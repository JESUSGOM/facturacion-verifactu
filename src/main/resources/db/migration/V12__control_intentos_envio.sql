ALTER TABLE registros_facturacion
    ADD COLUMN intentos_envio INT NOT NULL DEFAULT 0
    AFTER estado_envio,

    ADD COLUMN fecha_ultimo_intento TIMESTAMP NULL DEFAULT NULL
    AFTER intentos_envio,

    ADD CONSTRAINT chk_registros_facturacion_intentos
        CHECK (intentos_envio >= 0);