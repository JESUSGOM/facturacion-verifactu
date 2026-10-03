CREATE TABLE facturas (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          empresa_id BIGINT NOT NULL,
                          cliente_id BIGINT NOT NULL,
                          serie_id BIGINT NOT NULL,

                          estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',

                          numero BIGINT NULL,

                          fecha_expedicion DATE NULL,

                          observaciones VARCHAR(1000) NULL,

                          base_imponible DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                          cuota_impuesto DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                          total DECIMAL(15,2) NOT NULL DEFAULT 0.00,

                          cliente_nif VARCHAR(20) NULL,
                          cliente_nombre VARCHAR(150) NULL,
                          cliente_direccion VARCHAR(200) NULL,
                          cliente_codigo_postal VARCHAR(10) NULL,
                          cliente_municipio VARCHAR(100) NULL,
                          cliente_provincia VARCHAR(100) NULL,
                          cliente_codigo_pais CHAR(2) NULL,

                          fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          fecha_modificacion TIMESTAMP NULL DEFAULT NULL,
                          fecha_emision TIMESTAMP NULL DEFAULT NULL,

                          PRIMARY KEY (id),

                          CONSTRAINT fk_facturas_empresa
                              FOREIGN KEY (empresa_id)
                                  REFERENCES empresas(id),

                          CONSTRAINT fk_facturas_cliente
                              FOREIGN KEY (cliente_id)
                                  REFERENCES clientes(id),

                          CONSTRAINT fk_facturas_serie
                              FOREIGN KEY (serie_id)
                                  REFERENCES series_facturacion(id),

                          CONSTRAINT uk_facturas_serie_numero
                              UNIQUE (serie_id, numero),

                          CONSTRAINT chk_facturas_estado
                              CHECK (estado IN ('BORRADOR', 'EMITIDA', 'ANULADA')),

                          CONSTRAINT chk_facturas_totales
                              CHECK (
                                  base_imponible >= 0
                                      AND cuota_impuesto >= 0
                                      AND total >= 0
                                  )
);

CREATE INDEX idx_facturas_empresa
    ON facturas (empresa_id);

CREATE INDEX idx_facturas_cliente
    ON facturas (cliente_id);

CREATE INDEX idx_facturas_serie
    ON facturas (serie_id);


CREATE TABLE factura_lineas (
                                id BIGINT NOT NULL AUTO_INCREMENT,

                                factura_id BIGINT NOT NULL,
                                producto_id BIGINT NULL,

                                descripcion VARCHAR(500) NOT NULL,

                                cantidad DECIMAL(15,4) NOT NULL,
                                precio_unitario DECIMAL(15,4) NOT NULL,

                                tipo_impuesto VARCHAR(10) NOT NULL,
                                porcentaje_impuesto DECIMAL(5,2) NOT NULL,

                                base_imponible DECIMAL(15,2) NOT NULL,
                                cuota_impuesto DECIMAL(15,2) NOT NULL,
                                total DECIMAL(15,2) NOT NULL,

                                orden INT NOT NULL DEFAULT 1,

                                PRIMARY KEY (id),

                                CONSTRAINT fk_factura_lineas_factura
                                    FOREIGN KEY (factura_id)
                                        REFERENCES facturas(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_factura_lineas_producto
                                    FOREIGN KEY (producto_id)
                                        REFERENCES productos(id),

                                CONSTRAINT chk_factura_lineas_tipo_impuesto
                                    CHECK (tipo_impuesto IN ('IVA', 'IGIC', 'IPSI')),

                                CONSTRAINT chk_factura_lineas_cantidad
                                    CHECK (cantidad > 0),

                                CONSTRAINT chk_factura_lineas_precio
                                    CHECK (precio_unitario >= 0),

                                CONSTRAINT chk_factura_lineas_porcentaje
                                    CHECK (
                                        porcentaje_impuesto >= 0
                                            AND porcentaje_impuesto <= 100
                                        ),

                                CONSTRAINT chk_factura_lineas_totales
                                    CHECK (
                                        base_imponible >= 0
                                            AND cuota_impuesto >= 0
                                            AND total >= 0
                                        )
);

CREATE INDEX idx_factura_lineas_factura
    ON factura_lineas (factura_id);