package es.jfgb.facturacion.verifactu.provider;

/**
 * Indica que una operación de envío puede haber sido
 * procesada por el proveedor, pero no se ha podido
 * determinar con certeza su resultado.
 *
 * En este caso el registro NO debe reenviarse
 * automáticamente.
 */
public class EnvioVerifactuInciertoException
        extends RuntimeException {

    public EnvioVerifactuInciertoException(
            String message) {

        super(message);
    }

    public EnvioVerifactuInciertoException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}