CREATE TABLE series_facturacion (
                                    id BIGINT NOT NULL AUTO_INCREMENT,

                                    empresa_id BIGINT NOT NULL,

                                    codigo VARCHAR(30) NOT NULL,

                                    descripcion VARCHAR(150) NOT NULL,

                                    ultimo_numero BIGINT NOT NULL DEFAULT 0,

                                    activa BOOLEAN NOT NULL DEFAULT TRUE,

                                    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    fecha_modificacion TIMESTAMP NULL DEFAULT NULL,

                                    PRIMARY KEY (id),

                                    CONSTRAINT fk_series_facturacion_empresa
                                        FOREIGN KEY (empresa_id)
                                            REFERENCES empresas(id),

                                    CONSTRAINT uk_series_facturacion_empresa_codigo
                                        UNIQUE (empresa_id, codigo),

                                    CONSTRAINT chk_series_facturacion_ultimo_numero
                                        CHECK (ultimo_numero >= 0)
);

CREATE INDEX idx_series_facturacion_empresa
    ON series_facturacion (empresa_id);