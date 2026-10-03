CREATE TABLE registros_facturacion (
                                       id BIGINT NOT NULL AUTO_INCREMENT,

                                       empresa_id BIGINT NOT NULL,
                                       factura_id BIGINT NOT NULL,

                                       tipo_registro VARCHAR(20) NOT NULL,

                                       nif_emisor VARCHAR(20) NOT NULL,

                                       serie VARCHAR(30) NOT NULL,
                                       numero BIGINT NOT NULL,

                                       fecha_expedicion DATE NOT NULL,

                                       tipo_factura VARCHAR(10) NOT NULL,

                                       cuota_total DECIMAL(15,2) NOT NULL,
                                       importe_total DECIMAL(15,2) NOT NULL,

                                       registro_anterior_id BIGINT NULL,
                                       huella_anterior VARCHAR(64) NULL,

                                       fecha_hora_generacion VARCHAR(35) NOT NULL,

                                       huella VARCHAR(64) NOT NULL,

                                       estado_envio VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',

                                       proveedor VARCHAR(50) NULL,
                                       identificador_externo VARCHAR(255) NULL,

                                       peticion LONGTEXT NULL,
                                       respuesta LONGTEXT NULL,

                                       fecha_envio TIMESTAMP NULL DEFAULT NULL,

                                       fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       PRIMARY KEY (id),

                                       CONSTRAINT fk_registros_facturacion_empresa
                                           FOREIGN KEY (empresa_id)
                                               REFERENCES empresas(id),

                                       CONSTRAINT fk_registros_facturacion_factura
                                           FOREIGN KEY (factura_id)
                                               REFERENCES facturas(id),

                                       CONSTRAINT fk_registros_facturacion_anterior
                                           FOREIGN KEY (registro_anterior_id)
                                               REFERENCES registros_facturacion(id),

                                       CONSTRAINT chk_registros_facturacion_tipo
                                           CHECK (tipo_registro IN ('ALTA', 'ANULACION')),

                                       CONSTRAINT chk_registros_facturacion_estado
                                           CHECK (
                                               estado_envio IN (
                                                                'PENDIENTE',
                                                                'ENVIANDO',
                                                                'ACEPTADO',
                                                                'RECHAZADO',
                                                                'ERROR'
                                                   )
                                               )
);

CREATE INDEX idx_registros_facturacion_empresa
    ON registros_facturacion (empresa_id);

CREATE INDEX idx_registros_facturacion_factura
    ON registros_facturacion (factura_id);

CREATE INDEX idx_registros_facturacion_empresa_id
    ON registros_facturacion (empresa_id, id);

CREATE UNIQUE INDEX uk_registros_facturacion_huella
    ON registros_facturacion (huella);