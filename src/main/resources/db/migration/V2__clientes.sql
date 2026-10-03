CREATE TABLE clientes (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          empresa_id BIGINT NOT NULL,

                          nif VARCHAR(30),
                          razon_social VARCHAR(150) NOT NULL,
                          nombre_comercial VARCHAR(150),

                          direccion VARCHAR(200),
                          codigo_postal VARCHAR(10),
                          municipio VARCHAR(100),
                          provincia VARCHAR(100),
                          codigo_pais CHAR(2) NOT NULL DEFAULT 'ES',

                          telefono VARCHAR(30),
                          email VARCHAR(150),

                          activo BOOLEAN NOT NULL DEFAULT TRUE,

                          fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          fecha_modificacion TIMESTAMP NULL DEFAULT NULL,

                          PRIMARY KEY (id),

                          CONSTRAINT fk_clientes_empresa
                              FOREIGN KEY (empresa_id)
                                  REFERENCES empresas (id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT uk_clientes_empresa_nif
                              UNIQUE (empresa_id, nif),

                          INDEX idx_clientes_empresa (empresa_id)
);