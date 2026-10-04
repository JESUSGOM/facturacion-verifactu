package es.jfgb.facturacion.verifactu.provider;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;

public interface VerifactuProvider {

    String getNombre();

    /**
     * Indica que este proveedor representa deliberadamente
     * un modo en el que cualquier envio debe quedar bloqueado.
     */
    default boolean bloqueaEnvio() {
        return false;
    }

    ResultadoEnvioVerifactu enviarAlta(
            RegistroFacturacion registro);

    /**
     * La implementacion real AEAT se incorporara
     * expresamente en un paso posterior.
     *
     * Mientras tanto, un proveedor que no implemente
     * ANULACION queda bloqueado localmente.
     */
    default ResultadoEnvioVerifactu enviarAnulacion(
            RegistroFacturacion registro) {

        throw new IllegalStateException(
                "El proveedor VERI*FACTU "
                        + getNombre()
                        + " no admite todavia registros "
                        + "de ANULACION"
        );
    }
}