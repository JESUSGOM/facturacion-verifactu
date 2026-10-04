-- ============================================================
-- V18 - Snapshot historico del emisor en la factura
--
-- Las facturas BORRADOR mantienen estos campos a NULL.
-- Se rellenan en el momento de la emision.
--
-- Las facturas ya emitidas se inicializan con los datos
-- actuales de su empresa para preservar el comportamiento
-- existente antes de esta migracion.
-- ============================================================

ALTER TABLE facturas
    ADD COLUMN empresa_nif VARCHAR(20) NULL,
    ADD COLUMN empresa_razon_social VARCHAR(150) NULL,
    ADD COLUMN empresa_nombre_comercial VARCHAR(150) NULL,
    ADD COLUMN empresa_direccion VARCHAR(200) NULL,
    ADD COLUMN empresa_codigo_postal VARCHAR(10) NULL,
    ADD COLUMN empresa_municipio VARCHAR(100) NULL,
    ADD COLUMN empresa_provincia VARCHAR(100) NULL,
    ADD COLUMN empresa_codigo_pais CHAR(2) NULL;

UPDATE facturas f
INNER JOIN empresas e
        ON e.id = f.empresa_id
SET
    f.empresa_nif = e.nif,
    f.empresa_razon_social = e.razon_social,
    f.empresa_nombre_comercial = e.nombre_comercial,
    f.empresa_direccion = e.direccion,
    f.empresa_codigo_postal = e.codigo_postal,
    f.empresa_municipio = e.municipio,
    f.empresa_provincia = e.provincia,
    f.empresa_codigo_pais = e.codigo_pais
WHERE f.estado = 'EMITIDA';