-- ============================================================
-- V17 - Numero de instalacion VERI*FACTU por empresa
--
-- Cada facturacion gestionada por un SIF multiempresa debe
-- disponer de su propio identificador de instalacion.
--
-- Se crea nullable para conservar las empresas historicas.
-- La aplicacion exigira el dato antes de generar nuevos
-- registros fiscales VERI*FACTU.
-- ============================================================

ALTER TABLE empresas
    ADD COLUMN verifactu_numero_instalacion VARCHAR(100) NULL;