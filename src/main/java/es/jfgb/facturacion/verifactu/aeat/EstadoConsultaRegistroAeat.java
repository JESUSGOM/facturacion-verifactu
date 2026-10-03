package es.jfgb.facturacion.verifactu.aeat;

/**
 * Resultado funcional de intentar localizar un registro
 * concreto mediante la consulta AEAT.
 */
public enum EstadoConsultaRegistroAeat {

    /**
     * Existe exactamente una coincidencia inequívoca
     * en una respuesta completa.
     */
    ENCONTRADO,

    /**
     * La respuesta completa confirma que no existe
     * la factura buscada.
     */
    NO_ENCONTRADO,

    /**
     * Existen varias coincidencias para la misma
     * identidad fiscal de factura.
     */
    AMBIGUO,

    /**
     * La respuesta no permite tomar una decisión segura:
     * paginación, datos incompletos, contradicción, etc.
     */
    RESPUESTA_NO_CONCLUYENTE
}