-- ============================================================
-- V16 - Snapshot fiscal completo para VERI*FACTU
--
-- Objetivos:
-- 1. Conservar en el registro de facturación los datos fiscales
--    necesarios para reconstruir exactamente el registro AEAT.
-- 2. No depender de modificaciones posteriores de Empresa,
--    Cliente, Factura o líneas de factura.
-- 3. Mantener intactos los registros históricos existentes.
-- 4. Permitir varios desgloses fiscales por registro.
--
-- IMPORTANTE:
-- Las nuevas columnas del registro se crean inicialmente NULL
-- para no alterar ni invalidar los registros históricos V1-V15.
-- ============================================================


-- ------------------------------------------------------------
-- 1. Ampliación del snapshot principal del registro
-- ------------------------------------------------------------

ALTER TABLE registros_facturacion

    ADD COLUMN id_version_aeat VARCHAR(10) NULL,

    ADD COLUMN nombre_razon_emisor VARCHAR(120) NULL,

    ADD COLUMN descripcion_operacion VARCHAR(500) NULL,

    ADD COLUMN destinatario_nombre VARCHAR(120) NULL,

    ADD COLUMN destinatario_nif VARCHAR(20) NULL,

    ADD COLUMN destinatario_codigo_pais CHAR(2) NULL,

    ADD COLUMN destinatario_tipo_identificacion VARCHAR(10) NULL,

    ADD COLUMN destinatario_id_otro VARCHAR(100) NULL,

    ADD COLUMN tipo_huella VARCHAR(10) NULL,

    ADD COLUMN sistema_nombre_razon VARCHAR(120) NULL,

    ADD COLUMN sistema_nif VARCHAR(20) NULL,

    ADD COLUMN sistema_nombre VARCHAR(30) NULL,

    ADD COLUMN sistema_id VARCHAR(2) NULL,

    ADD COLUMN sistema_version VARCHAR(50) NULL,

    ADD COLUMN sistema_numero_instalacion VARCHAR(100) NULL,

    ADD COLUMN sistema_solo_verifactu CHAR(1) NULL,

    ADD COLUMN sistema_multi_ot CHAR(1) NULL,

    ADD COLUMN sistema_multiples_ot CHAR(1) NULL;


-- ------------------------------------------------------------
-- 2. Desgloses fiscales inmutables del registro
--
-- No se guarda una fila por línea de factura necesariamente.
-- Se guardará una fila por cada desglose fiscal resultante:
--
--   impuesto
--   régimen
--   calificación
--   tipo impositivo
--   base
--   cuota
--
-- Por ejemplo, una factura con IGIC 7 % e IGIC 3 % tendrá
-- dos registros de desglose.
-- ------------------------------------------------------------

CREATE TABLE registros_facturacion_desgloses (

                                                 id BIGINT NOT NULL AUTO_INCREMENT,

                                                 registro_facturacion_id BIGINT NOT NULL,

                                                 orden SMALLINT UNSIGNED NOT NULL,

                                                 impuesto VARCHAR(2) NULL,

                                                 clave_regimen VARCHAR(2) NULL,

                                                 calificacion_operacion VARCHAR(2) NULL,

                                                 operacion_exenta VARCHAR(2) NULL,

                                                 tipo_impositivo DECIMAL(5,2) NULL,

                                                 base_imponible_o_importe_no_sujeto DECIMAL(15,2) NOT NULL,

                                                 base_imponible_a_coste DECIMAL(15,2) NULL,

                                                 cuota_repercutida DECIMAL(15,2) NULL,

                                                 tipo_recargo_equivalencia DECIMAL(5,2) NULL,

                                                 cuota_recargo_equivalencia DECIMAL(15,2) NULL,

                                                 fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                 PRIMARY KEY (id),

                                                 CONSTRAINT uk_reg_fact_desglose_orden
                                                     UNIQUE (registro_facturacion_id, orden),

                                                 CONSTRAINT fk_reg_fact_desglose_registro
                                                     FOREIGN KEY (registro_facturacion_id)
                                                         REFERENCES registros_facturacion (id)
                                                         ON DELETE RESTRICT

) ENGINE=InnoDB;