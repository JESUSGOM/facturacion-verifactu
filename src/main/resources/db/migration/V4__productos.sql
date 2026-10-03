CREATE TABLE productos (
                           id BIGINT NOT NULL AUTO_INCREMENT,

                           empresa_id BIGINT NOT NULL,

                           codigo VARCHAR(50),

                           tipo VARCHAR(20) NOT NULL,

                           nombre VARCHAR(150) NOT NULL,

                           descripcion VARCHAR(500),

                           precio DECIMAL(15,4) NOT NULL,

                           tipo_impuesto VARCHAR(10) NOT NULL,

                           porcentaje_impuesto DECIMAL(5,2) NOT NULL,

                           activo BOOLEAN NOT NULL DEFAULT TRUE,

                           fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           fecha_modificacion TIMESTAMP NULL DEFAULT NULL,

                           PRIMARY KEY (id),

                           CONSTRAINT fk_productos_empresa
                               FOREIGN KEY (empresa_id)
                                   REFERENCES empresas(id),

                           CONSTRAINT uk_productos_empresa_codigo
                               UNIQUE (empresa_id, codigo),

                           CONSTRAINT chk_productos_tipo
                               CHECK (tipo IN ('PRODUCTO', 'SERVICIO')),

                           CONSTRAINT chk_productos_tipo_impuesto
                               CHECK (tipo_impuesto IN ('IVA', 'IGIC', 'IPSI')),

                           CONSTRAINT chk_productos_precio
                               CHECK (precio >= 0),

                           CONSTRAINT chk_productos_porcentaje_impuesto
                               CHECK (
                                   porcentaje_impuesto >= 0
                                       AND porcentaje_impuesto <= 100
                                   )
);

CREATE INDEX idx_productos_empresa
    ON productos (empresa_id);