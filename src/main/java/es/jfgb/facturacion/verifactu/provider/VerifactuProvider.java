package es.jfgb.facturacion.verifactu.provider;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;

public interface VerifactuProvider {

    String getNombre();

    /**
     * Indica que este proveedor representa deliberadamente
     * un modo en el que cualquier envío debe quedar bloqueado.
     *
     * Por defecto los proveedores normales no bloquean.
     */
    default boolean bloqueaEnvio() {
        return false;
    }

    ResultadoEnvioVerifactu enviarAlta(
            RegistroFacturacion registro);
}