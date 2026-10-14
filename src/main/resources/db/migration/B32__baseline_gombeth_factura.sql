-- ============================================================
-- B32 - Baseline Gombeth Factura (estado equivalente a V32)
--
-- Generada a partir de la estructura real de jfgb_facturacion
-- (MySQL 8.0.37) y de sus CHECK CONSTRAINTS.
--
-- Uso:
--   * Esquemas NUEVOS: Flyway puede partir directamente de B32.
--   * Esquemas existentes con V1-V32 aplicadas: B32 se ignora.
--
-- No contiene:
--   * datos
--   * flyway_schema_history
--   * CREATE/DROP DATABASE
--   * reparación del historial
--
-- No modificar V1-V32 para acomodar esta baseline.
-- ============================================================

-- ============================================================
-- 1. TABLAS
-- ============================================================

CREATE TABLE `clientes` (

  `id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `nif` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nombre` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `nombre_comercial` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `direccion` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `codigo_postal` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `municipio` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `provincia` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `codigo_pais` char(2) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ES',
  `telefono` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `empresas` (

  `id` bigint NOT NULL,
  `nif` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `razon_social` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `nombre_comercial` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `direccion` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `codigo_postal` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `municipio` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `provincia` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `codigo_pais` char(2) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ES',
  `telefono` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tipo_impuesto` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT NULL,
  `verifactu_numero_instalacion` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `facturas` (

  `id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `serie_id` bigint NOT NULL,
  `estado` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'BORRADOR',
  `numero` bigint DEFAULT NULL,
  `fecha_expedicion` date DEFAULT NULL,
  `tipo_factura` varchar(2) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'F1',
  `tipo_rectificativa` enum('I','S') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `motivo_rectificacion` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `base_rectificada` decimal(15,2) DEFAULT NULL,
  `cuota_rectificada` decimal(15,2) DEFAULT NULL,
  `observaciones` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `base_imponible` decimal(15,2) NOT NULL DEFAULT '0.00',
  `cuota_impuesto` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total` decimal(15,2) NOT NULL DEFAULT '0.00',
  `cliente_nif` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cliente_nombre` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cliente_direccion` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cliente_codigo_postal` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cliente_municipio` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cliente_provincia` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cliente_codigo_pais` char(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT NULL,
  `fecha_emision` timestamp NULL DEFAULT NULL,
  `empresa_nif` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `empresa_razon_social` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `empresa_nombre_comercial` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `empresa_direccion` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `empresa_codigo_postal` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `empresa_municipio` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `empresa_provincia` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `empresa_codigo_pais` char(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fecha_anulacion` timestamp(6) NULL DEFAULT NULL,
  `motivo_anulacion` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `facturas_rectificadas` (

  `id` bigint NOT NULL,
  `factura_rectificativa_id` bigint NOT NULL,
  `factura_rectificada_id` bigint NOT NULL,
  `orden` int UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `facturas_sustituidas` (

  `id` bigint NOT NULL,
  `factura_sustitutiva_id` bigint NOT NULL,
  `factura_sustituida_id` bigint NOT NULL,
  `orden` int UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `factura_lineas` (

  `id` bigint NOT NULL,
  `factura_id` bigint NOT NULL,
  `producto_id` bigint DEFAULT NULL,
  `descripcion` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  `cantidad` decimal(15,4) NOT NULL,
  `precio_unitario` decimal(15,4) NOT NULL,
  `signo` int NOT NULL DEFAULT '1',
  `tipo_impuesto` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `porcentaje_impuesto` decimal(5,2) NOT NULL,
  `base_imponible` decimal(15,2) NOT NULL,
  `cuota_impuesto` decimal(15,2) NOT NULL,
  `total` decimal(15,2) NOT NULL,
  `orden` int NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `productos` (

  `id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `codigo` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tipo` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `nombre` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `descripcion` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `precio` decimal(15,4) NOT NULL,
  `tipo_impuesto` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `porcentaje_impuesto` decimal(5,2) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `registros_facturacion` (

  `id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `factura_id` bigint NOT NULL,
  `tipo_registro` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `nif_emisor` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `serie` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `numero` bigint NOT NULL,
  `num_serie_factura` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `fecha_expedicion` date NOT NULL,
  `tipo_factura` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `tipo_rectificativa` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `base_rectificada` decimal(15,2) DEFAULT NULL,
  `cuota_rectificada` decimal(15,2) DEFAULT NULL,
  `cuota_total` decimal(15,2) NOT NULL,
  `importe_total` decimal(15,2) NOT NULL,
  `registro_anterior_id` bigint DEFAULT NULL,
  `huella_anterior` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fecha_hora_generacion` varchar(35) COLLATE utf8mb4_general_ci NOT NULL,
  `huella` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `estado_envio` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDIENTE',
  `intentos_envio` int NOT NULL DEFAULT '0',
  `fecha_ultimo_intento` timestamp NULL DEFAULT NULL,
  `proveedor` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `identificador_externo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `peticion` longtext COLLATE utf8mb4_general_ci,
  `respuesta` longtext COLLATE utf8mb4_general_ci,
  `fecha_envio` timestamp NULL DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `id_version_aeat` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nombre_razon_emisor` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `descripcion_operacion` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `destinatario_nombre` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `destinatario_nif` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `destinatario_codigo_pais` char(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `destinatario_tipo_identificacion` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `destinatario_id_otro` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tipo_huella` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_nombre_razon` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_nif` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_nombre` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_id` varchar(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_version` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_numero_instalacion` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_solo_verifactu` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_multi_ot` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sistema_multiples_ot` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `registros_facturacion_desgloses` (

  `id` bigint NOT NULL,
  `registro_facturacion_id` bigint NOT NULL,
  `orden` smallint UNSIGNED NOT NULL,
  `impuesto` varchar(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `clave_regimen` varchar(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `calificacion_operacion` varchar(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `operacion_exenta` varchar(2) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tipo_impositivo` decimal(5,2) DEFAULT NULL,
  `base_imponible_o_importe_no_sujeto` decimal(15,2) NOT NULL,
  `base_imponible_a_coste` decimal(15,2) DEFAULT NULL,
  `cuota_repercutida` decimal(15,2) DEFAULT NULL,
  `tipo_recargo_equivalencia` decimal(5,2) DEFAULT NULL,
  `cuota_recargo_equivalencia` decimal(15,2) DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `registros_facturacion_rectificadas` (

  `id` bigint NOT NULL,
  `registro_facturacion_id` bigint NOT NULL,
  `factura_rectificada_id` bigint DEFAULT NULL,
  `orden` smallint UNSIGNED NOT NULL,
  `nif_emisor` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `num_serie_factura` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `fecha_expedicion` date NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `registros_facturacion_sustituidas` (

  `id` bigint NOT NULL,
  `registro_facturacion_id` bigint NOT NULL,
  `factura_sustituida_id` bigint DEFAULT NULL,
  `orden` smallint UNSIGNED NOT NULL,
  `nif_emisor` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `num_serie_factura` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `fecha_expedicion` date NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `registro_facturacion_intentos` (

  `id` bigint NOT NULL,
  `registro_facturacion_id` bigint NOT NULL,
  `numero_intento` int NOT NULL,
  `estado` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `proveedor` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `identificador_externo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `peticion` longtext COLLATE utf8mb4_general_ci,
  `respuesta` longtext COLLATE utf8mb4_general_ci,
  `error` longtext COLLATE utf8mb4_general_ci,
  `fecha_inicio` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_fin` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `series_facturacion` (

  `id` bigint NOT NULL,
  `empresa_id` bigint NOT NULL,
  `codigo` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `descripcion` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `tipo_serie` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ORDINARIA',
  `ultimo_numero` bigint NOT NULL DEFAULT '0',
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_modificacion` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- 2. CLAVES PRIMARIAS, UNICAS E INDICES
-- ============================================================

ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_clientes_empresa_nif` (`empresa_id`,`nif`),
  ADD KEY `idx_clientes_empresa` (`empresa_id`);

ALTER TABLE `empresas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_empresas_nif` (`nif`);

ALTER TABLE `facturas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_facturas_serie_numero` (`serie_id`,`numero`),
  ADD KEY `idx_facturas_empresa` (`empresa_id`),
  ADD KEY `idx_facturas_cliente` (`cliente_id`),
  ADD KEY `idx_facturas_serie` (`serie_id`);

ALTER TABLE `facturas_rectificadas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_facturas_rectificadas_facturas` (`factura_rectificativa_id`,`factura_rectificada_id`),
  ADD UNIQUE KEY `uk_facturas_rectificadas_orden` (`factura_rectificativa_id`,`orden`),
  ADD KEY `idx_facturas_rectificadas_original` (`factura_rectificada_id`);

ALTER TABLE `facturas_sustituidas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_facturas_sustituidas_facturas` (`factura_sustitutiva_id`,`factura_sustituida_id`),
  ADD UNIQUE KEY `uk_facturas_sustituidas_orden` (`factura_sustitutiva_id`,`orden`),
  ADD KEY `idx_facturas_sustituidas_original` (`factura_sustituida_id`);

ALTER TABLE `factura_lineas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_factura_lineas_producto` (`producto_id`),
  ADD KEY `idx_factura_lineas_factura` (`factura_id`);

ALTER TABLE `productos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_productos_empresa_codigo` (`empresa_id`,`codigo`),
  ADD KEY `idx_productos_empresa` (`empresa_id`);

ALTER TABLE `registros_facturacion`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_registros_facturacion_huella` (`huella`),
  ADD UNIQUE KEY `uk_registros_facturacion_factura_tipo` (`factura_id`,`tipo_registro`),
  ADD KEY `fk_registros_facturacion_anterior` (`registro_anterior_id`),
  ADD KEY `idx_registros_facturacion_empresa` (`empresa_id`),
  ADD KEY `idx_registros_facturacion_factura` (`factura_id`),
  ADD KEY `idx_registros_facturacion_empresa_id` (`empresa_id`,`id`);

ALTER TABLE `registros_facturacion_desgloses`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_reg_fact_desglose_orden` (`registro_facturacion_id`,`orden`);

ALTER TABLE `registros_facturacion_rectificadas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_reg_fact_rect_orden` (`registro_facturacion_id`,`orden`),
  ADD UNIQUE KEY `uk_reg_fact_rect_factura` (`registro_facturacion_id`,`num_serie_factura`,`fecha_expedicion`);

ALTER TABLE `registros_facturacion_sustituidas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_reg_fact_sust_orden` (`registro_facturacion_id`,`orden`),
  ADD UNIQUE KEY `uk_reg_fact_sust_factura` (`registro_facturacion_id`,`num_serie_factura`,`fecha_expedicion`),
  ADD KEY `idx_reg_fact_sust_factura_id` (`factura_sustituida_id`);

ALTER TABLE `registro_facturacion_intentos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_registro_intento_numero` (`registro_facturacion_id`,`numero_intento`);

ALTER TABLE `series_facturacion`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_series_facturacion_empresa_codigo` (`empresa_id`,`codigo`),
  ADD KEY `idx_series_facturacion_empresa` (`empresa_id`);

-- ============================================================
-- 3. AUTO_INCREMENT
-- ============================================================

ALTER TABLE `clientes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `empresas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `facturas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `facturas_rectificadas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `facturas_sustituidas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `factura_lineas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `productos`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `registros_facturacion`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `registros_facturacion_desgloses`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `registros_facturacion_rectificadas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `registros_facturacion_sustituidas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `registro_facturacion_intentos`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

ALTER TABLE `series_facturacion`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

-- ============================================================
-- 4. CLAVES FORANEAS
-- ============================================================

ALTER TABLE `clientes`
  ADD CONSTRAINT `fk_clientes_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`) ON DELETE RESTRICT;

ALTER TABLE `facturas`
  ADD CONSTRAINT `fk_facturas_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  ADD CONSTRAINT `fk_facturas_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`),
  ADD CONSTRAINT `fk_facturas_serie` FOREIGN KEY (`serie_id`) REFERENCES `series_facturacion` (`id`);

ALTER TABLE `facturas_rectificadas`
  ADD CONSTRAINT `fk_facturas_rectificadas_original` FOREIGN KEY (`factura_rectificada_id`) REFERENCES `facturas` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_facturas_rectificadas_rectificativa` FOREIGN KEY (`factura_rectificativa_id`) REFERENCES `facturas` (`id`) ON DELETE RESTRICT;

ALTER TABLE `facturas_sustituidas`
  ADD CONSTRAINT `fk_facturas_sustituidas_original` FOREIGN KEY (`factura_sustituida_id`) REFERENCES `facturas` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_facturas_sustituidas_sustitutiva` FOREIGN KEY (`factura_sustitutiva_id`) REFERENCES `facturas` (`id`) ON DELETE RESTRICT;

ALTER TABLE `factura_lineas`
  ADD CONSTRAINT `fk_factura_lineas_factura` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_factura_lineas_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`);

ALTER TABLE `productos`
  ADD CONSTRAINT `fk_productos_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`);

ALTER TABLE `registros_facturacion`
  ADD CONSTRAINT `fk_registros_facturacion_anterior` FOREIGN KEY (`registro_anterior_id`) REFERENCES `registros_facturacion` (`id`),
  ADD CONSTRAINT `fk_registros_facturacion_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`),
  ADD CONSTRAINT `fk_registros_facturacion_factura` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`);

ALTER TABLE `registros_facturacion_desgloses`
  ADD CONSTRAINT `fk_reg_fact_desglose_registro` FOREIGN KEY (`registro_facturacion_id`) REFERENCES `registros_facturacion` (`id`) ON DELETE RESTRICT;

ALTER TABLE `registros_facturacion_rectificadas`
  ADD CONSTRAINT `fk_reg_fact_rect_registro` FOREIGN KEY (`registro_facturacion_id`) REFERENCES `registros_facturacion` (`id`);

ALTER TABLE `registros_facturacion_sustituidas`
  ADD CONSTRAINT `fk_reg_fact_sust_registro` FOREIGN KEY (`registro_facturacion_id`) REFERENCES `registros_facturacion` (`id`);

ALTER TABLE `registro_facturacion_intentos`
  ADD CONSTRAINT `fk_registro_intento_registro` FOREIGN KEY (`registro_facturacion_id`) REFERENCES `registros_facturacion` (`id`);

ALTER TABLE `series_facturacion`
  ADD CONSTRAINT `fk_series_facturacion_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresas` (`id`);

-- ============================================================
-- 5. CHECK CONSTRAINTS (28)
-- ============================================================

ALTER TABLE `empresas` ADD CONSTRAINT `chk_empresas_tipo_impuesto` CHECK ((`tipo_impuesto` in (_utf8mb4'IVA',_utf8mb4'IGIC',_utf8mb4'IPSI')));

ALTER TABLE `factura_lineas` ADD CONSTRAINT `chk_factura_lineas_cantidad` CHECK ((`cantidad` > 0));

ALTER TABLE `factura_lineas` ADD CONSTRAINT `chk_factura_lineas_porcentaje` CHECK (((`porcentaje_impuesto` >= 0) and (`porcentaje_impuesto` <= 100)));

ALTER TABLE `factura_lineas` ADD CONSTRAINT `chk_factura_lineas_precio` CHECK ((`precio_unitario` >= 0));

ALTER TABLE `factura_lineas` ADD CONSTRAINT `chk_factura_lineas_signo` CHECK ((`signo` in (-(1),1)));

ALTER TABLE `factura_lineas` ADD CONSTRAINT `chk_factura_lineas_tipo_impuesto` CHECK ((`tipo_impuesto` in (_utf8mb4'IVA',_utf8mb4'IGIC',_utf8mb4'IPSI')));

ALTER TABLE `factura_lineas` ADD CONSTRAINT `chk_factura_lineas_totales` CHECK ((((`signo` = 1) and (`base_imponible` >= 0) and (`cuota_impuesto` >= 0) and (`total` >= 0)) or ((`signo` = -(1)) and (`base_imponible` <= 0) and (`cuota_impuesto` <= 0) and (`total` <= 0))));

ALTER TABLE `facturas` ADD CONSTRAINT `chk_facturas_datos_rectificacion` CHECK ((((`tipo_factura` in (_utf8mb4'F1',_utf8mb4'F2',_utf8mb4'F3')) and (`tipo_rectificativa` is null) and (`motivo_rectificacion` is null) and (`base_rectificada` is null) and (`cuota_rectificada` is null)) or ((`tipo_factura` in (_utf8mb4'R1',_utf8mb4'R2',_utf8mb4'R3',_utf8mb4'R4',_utf8mb4'R5')) and (`tipo_rectificativa` = _utf8mb4'I') and (`motivo_rectificacion` is not null) and (`base_rectificada` is null) and (`cuota_rectificada` is null)) or ((`tipo_factura` in (_utf8mb4'R1',_utf8mb4'R2',_utf8mb4'R3',_utf8mb4'R4',_utf8mb4'R5')) and (`tipo_rectificativa` = _utf8mb4'S') and (`motivo_rectificacion` is not null) and (`base_rectificada` is not null) and (`cuota_rectificada` is not null))));

ALTER TABLE `facturas` ADD CONSTRAINT `chk_facturas_estado` CHECK ((`estado` in (_utf8mb4'BORRADOR',_utf8mb4'EMITIDA',_utf8mb4'ANULADA')));

ALTER TABLE `facturas` ADD CONSTRAINT `chk_facturas_tipo_factura` CHECK ((`tipo_factura` in (_utf8mb4'F1',_utf8mb4'F2',_utf8mb4'F3',_utf8mb4'R1',_utf8mb4'R2',_utf8mb4'R3',_utf8mb4'R4',_utf8mb4'R5')));

ALTER TABLE `facturas` ADD CONSTRAINT `chk_facturas_tipo_rectificativa` CHECK (((`tipo_rectificativa` is null) or (`tipo_rectificativa` in (_utf8mb4'S',_utf8mb4'I'))));

ALTER TABLE `facturas` ADD CONSTRAINT `chk_facturas_totales` CHECK ((((`tipo_factura` in (_utf8mb4'F1',_utf8mb4'F2',_utf8mb4'F3')) and (`base_imponible` >= 0) and (`cuota_impuesto` >= 0) and (`total` >= 0)) or (`tipo_factura` in (_utf8mb4'R1',_utf8mb4'R2',_utf8mb4'R3',_utf8mb4'R4',_utf8mb4'R5'))));

ALTER TABLE `facturas_rectificadas` ADD CONSTRAINT `chk_facturas_rectificadas_distintas` CHECK ((`factura_rectificativa_id` <> `factura_rectificada_id`));

ALTER TABLE `facturas_rectificadas` ADD CONSTRAINT `chk_facturas_rectificadas_orden` CHECK ((`orden` between 1 and 1000));

ALTER TABLE `facturas_sustituidas` ADD CONSTRAINT `chk_facturas_sustituidas_distintas` CHECK ((`factura_sustitutiva_id` <> `factura_sustituida_id`));

ALTER TABLE `facturas_sustituidas` ADD CONSTRAINT `chk_facturas_sustituidas_orden` CHECK ((`orden` between 1 and 1000));

ALTER TABLE `productos` ADD CONSTRAINT `chk_productos_porcentaje_impuesto` CHECK (((`porcentaje_impuesto` >= 0) and (`porcentaje_impuesto` <= 100)));

ALTER TABLE `productos` ADD CONSTRAINT `chk_productos_precio` CHECK ((`precio` >= 0));

ALTER TABLE `productos` ADD CONSTRAINT `chk_productos_tipo` CHECK ((`tipo` in (_utf8mb4'PRODUCTO',_utf8mb4'SERVICIO')));

ALTER TABLE `productos` ADD CONSTRAINT `chk_productos_tipo_impuesto` CHECK ((`tipo_impuesto` in (_utf8mb4'IVA',_utf8mb4'IGIC',_utf8mb4'IPSI')));

ALTER TABLE `registro_facturacion_intentos` ADD CONSTRAINT `chk_registro_intento_numero` CHECK ((`numero_intento` > 0));

ALTER TABLE `registros_facturacion` ADD CONSTRAINT `chk_registros_facturacion_estado` CHECK ((`estado_envio` in (_utf8mb4'PENDIENTE',_utf8mb4'ENVIANDO',_utf8mb4'ACEPTADO',_utf8mb4'RECHAZADO',_utf8mb4'ERROR',_utf8mb4'INCIERTO')));

ALTER TABLE `registros_facturacion` ADD CONSTRAINT `chk_registros_facturacion_intentos` CHECK ((`intentos_envio` >= 0));

ALTER TABLE `registros_facturacion` ADD CONSTRAINT `chk_registros_facturacion_tipo` CHECK ((`tipo_registro` in (_utf8mb4'ALTA',_utf8mb4'ANULACION')));

ALTER TABLE `registros_facturacion` ADD CONSTRAINT `chk_registros_tipo_rectificativa` CHECK (((`tipo_rectificativa` is null) or (`tipo_rectificativa` in (_utf8mb4'S',_utf8mb4'I'))));

ALTER TABLE `registros_facturacion_sustituidas` ADD CONSTRAINT `chk_reg_fact_sust_orden` CHECK ((`orden` between 1 and 1000));

ALTER TABLE `series_facturacion` ADD CONSTRAINT `chk_series_facturacion_tipo` CHECK ((`tipo_serie` in (_utf8mb4'ORDINARIA',_utf8mb4'SIMPLIFICADA',_utf8mb4'RECTIFICATIVA')));

ALTER TABLE `series_facturacion` ADD CONSTRAINT `chk_series_facturacion_ultimo_numero` CHECK ((`ultimo_numero` >= 0));
