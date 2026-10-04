package es.jfgb.facturacion.verifactu.provider;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Proveedor seguro por defecto.
 *
 * No realiza ninguna comunicacion externa.
 */
@Component
@Profile("!dev & !aeat-pre")
public class VerifactuProviderDeshabilitado
        implements VerifactuProvider {

    @Override
    public String getNombre() {
        return "DESHABILITADO";
    }

    @Override
    public boolean bloqueaEnvio() {
        return true;
    }

    @Override
    public ResultadoEnvioVerifactu enviarAlta(
            RegistroFacturacion registro) {

        throw bloqueado();
    }

    @Override
    public ResultadoEnvioVerifactu enviarAnulacion(
            RegistroFacturacion registro) {

        throw bloqueado();
    }

    private IllegalStateException bloqueado() {

        return new IllegalStateException(
                "No existe ningun proveedor VERI*FACTU "
                        + "habilitado para el perfil actual"
        );
    }
}