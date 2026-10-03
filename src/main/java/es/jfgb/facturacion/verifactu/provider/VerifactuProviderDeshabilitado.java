package es.jfgb.facturacion.verifactu.provider;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Proveedor seguro por defecto.
 *
 * Se utiliza únicamente cuando no están activos
 * ni el perfil de desarrollo ni el de preproducción AEAT.
 *
 * Este proveedor NO realiza ninguna comunicación externa.
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

        throw new IllegalStateException(
                "No existe ningún proveedor VERI*FACTU "
                        + "habilitado para el perfil actual"
        );
    }
}