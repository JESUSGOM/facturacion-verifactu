ALTER TABLE registros_facturacion
    DROP CHECK chk_registros_facturacion_estado;

ALTER TABLE registros_facturacion
    ADD CONSTRAINT chk_registros_facturacion_estado
    CHECK (
        estado_envio IN (
            'PENDIENTE',
            'ENVIANDO',
            'ACEPTADO',
            'RECHAZADO',
            'ERROR',
            'INCIERTO'
        )
    );