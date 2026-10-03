package es.jfgb.facturacion.verifactu.provider;

public record ResultadoEnvioVerifactu(

        EstadoResultadoEnvioVerifactu estado,

        String identificadorExterno,

        String peticion,

        String respuesta,

        String mensaje

) {

    /*
     * Constructor de compatibilidad.
     *
     * Permite que los proveedores que todavía
     * utilizan el antiguo boolean sigan
     * compilando mientras hacemos la transición
     * al nuevo modelo de estados.
     */
    public ResultadoEnvioVerifactu(
            boolean aceptado,
            String identificadorExterno,
            String peticion,
            String respuesta,
            String mensaje) {

        this(
                aceptado
                        ? EstadoResultadoEnvioVerifactu.ACEPTADO
                        : EstadoResultadoEnvioVerifactu.RECHAZADO,
                identificadorExterno,
                peticion,
                respuesta,
                mensaje
        );
    }

    /*
     * Método de compatibilidad con el código
     * existente de EstadoEnvioVerifactuService.
     *
     * ACEPTADO_CON_ERRORES sigue significando
     * que AEAT ha almacenado el registro.
     */
    public boolean aceptado() {

        return estado
                == EstadoResultadoEnvioVerifactu.ACEPTADO
                ||
                estado
                        == EstadoResultadoEnvioVerifactu
                        .ACEPTADO_CON_ERRORES;
    }

    public boolean requiereReconciliacion() {

        return estado
                == EstadoResultadoEnvioVerifactu
                .REQUIERE_RECONCILIACION;
    }
}