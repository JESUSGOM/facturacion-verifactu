CREATE TABLE registro_facturacion_intentos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    registro_facturacion_id BIGINT NOT NULL,
    numero_intento INT NOT NULL,
    estado VARCHAR(20) NOT NULL,

    proveedor VARCHAR(50) NULL,
    identificador_externo VARCHAR(255) NULL,

    peticion LONGTEXT NULL,
    respuesta LONGTEXT NULL,
    error LONGTEXT NULL,

    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_registro_intento_registro
        FOREIGN KEY (registro_facturacion_id)
        REFERENCES registros_facturacion (id),

    CONSTRAINT uk_registro_intento_numero
        UNIQUE (registro_facturacion_id, numero_intento),

    CONSTRAINT chk_registro_intento_numero
        CHECK (numero_intento > 0)
);