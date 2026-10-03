CREATE TABLE empresas (
  id BIGINT NOT NULL AUTO_INCREMENT,

  nif VARCHAR(20) NOT NULL,
  razon_social VARCHAR(150) NOT NULL,
  nombre_comercial VARCHAR(150),

  direccion VARCHAR(200),
  codigo_postal VARCHAR(10),
  municipio VARCHAR(100),
  provincia VARCHAR(100),
  codigo_pais CHAR(2) NOT NULL DEFAULT 'ES',

  telefono VARCHAR(30),
  email VARCHAR(150),

  tipo_impuesto VARCHAR(10) NOT NULL,

  activa BOOLEAN NOT NULL DEFAULT TRUE,

  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP NULL DEFAULT NULL,

  PRIMARY KEY (id),

  CONSTRAINT uk_empresas_nif UNIQUE (nif),

  CONSTRAINT chk_empresas_tipo_impuesto
      CHECK (tipo_impuesto IN ('IVA', 'IGIC', 'IPSI'))
);