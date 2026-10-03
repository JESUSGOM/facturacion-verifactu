package es.jfgb.facturacion.verifactu.provider;

public enum EstadoResultadoEnvioVerifactu {

    /*
     * AEAT ha registrado correctamente
     * el registro de facturación.
     */
    ACEPTADO,

    /*
     * AEAT ha almacenado el registro,
     * pero devuelve errores o advertencias
     * que deben conservarse.
     */
    ACEPTADO_CON_ERRORES,

    /*
     * AEAT ha rechazado el registro.
     */
    RECHAZADO,

    /*
     * AEAT informa de que ya existe
     * un registro con el mismo identificador.
     *
     * No debe reenviarse automáticamente.
     * Será necesario reconciliarlo mediante
     * consulta antes de decidir el estado final.
     */
    REQUIERE_RECONCILIACION
}