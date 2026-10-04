package es.jfgb.facturacion.verifactu.provider;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class VerifactuProviderDesarrollo
        implements VerifactuProvider {

    private final boolean simularError;
    private final boolean simularIncierto;

    public VerifactuProviderDesarrollo(
            @Value("${verifactu.dev.simular-error:false}")
            boolean simularError,
            @Value("${verifactu.dev.simular-incierto:false}")
            boolean simularIncierto) {

        this.simularError = simularError;
        this.simularIncierto = simularIncierto;
    }

    @Override
    public String getNombre() {
        return "DESARROLLO";
    }

    @Override
    public ResultadoEnvioVerifactu enviarAlta(
            RegistroFacturacion registro) {

        return simular(
                registro,
                "ALTA"
        );
    }

    @Override
    public ResultadoEnvioVerifactu enviarAnulacion(
            RegistroFacturacion registro) {

        return simular(
                registro,
                "ANULACION"
        );
    }

    private ResultadoEnvioVerifactu simular(
            RegistroFacturacion registro,
            String tipo) {

        if (simularIncierto) {

            throw new EnvioVerifactuInciertoException(
                    "Resultado incierto simulado "
                            + "durante la comunicacion "
                            + "con el proveedor"
            );
        }

        if (simularError) {

            throw new IllegalStateException(
                    "Fallo de comunicacion simulado "
                            + "con el proveedor"
            );
        }

        String peticion =
                "SIMULACION "
                        + tipo
                        + " "
                        + registro.getNumSerieFactura();

        String respuesta =
                "Registro aceptado en modo desarrollo";

        return new ResultadoEnvioVerifactu(
                true,
                "DEV-" + registro.getId(),
                peticion,
                respuesta,
                "Aceptado"
        );
    }
}