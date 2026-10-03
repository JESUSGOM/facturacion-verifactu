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

        /*
         * Simula un caso en el que la petición pudo haber
         * llegado al proveedor, pero no conocemos con
         * certeza el resultado.
         *
         * Este caso debe terminar como INCIERTO.
         */
        if (simularIncierto) {

            throw new EnvioVerifactuInciertoException(
                    "Resultado incierto simulado "
                            + "durante la comunicación con el proveedor"
            );
        }

        /*
         * Simula un error considerado seguro.
         *
         * Este caso debe terminar como ERROR.
         */
        if (simularError) {

            throw new IllegalStateException(
                    "Fallo de comunicación simulado con el proveedor"
            );
        }

        String peticion =
                "SIMULACION ALTA "
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