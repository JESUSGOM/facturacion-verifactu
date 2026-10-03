package es.jfgb.facturacion.verifactu.aeat.config;

import es.jfgb.facturacion.verifactu.ServicioEnvioVerifactu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("aeat-pre")
class AeatPreproduccionEnvioBloqueadoIT {

    @Autowired
    private ServicioEnvioVerifactu servicioEnvioVerifactu;

    @Autowired
    private Environment environment;

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void aeatPreArrancaConEnvioBloqueadoPorDefecto() {

        Boolean envioHabilitado =
                environment.getProperty(
                        "verifactu.envio-habilitado",
                        Boolean.class,
                        Boolean.TRUE
                );

        assertFalse(
                envioHabilitado
        );

        /*
         * IDs deliberadamente inexistentes.
         *
         * El seguro debe actuar ANTES de que se intente
         * buscar, bloquear o modificar ningún registro.
         */
        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () -> servicioEnvioVerifactu.enviarAlta(
                                -999999L,
                                -999999L
                        )
                );

        assertEquals(
                503,
                excepcion.getStatusCode().value()
        );

        assertTrue(
                excepcion.getReason() != null
                        && excepcion.getReason()
                        .contains("deshabilitado")
        );

        System.out.println(
                "AEAT_PRE_ENVIO_HABILITADO="
                        + envioHabilitado
        );

        System.out.println(
                "AEAT_PRE_ENVIO_BLOQUEADO=true"
        );

        System.out.println(
                "AEAT_PRE_HTTP_STATUS="
                        + excepcion
                        .getStatusCode()
                        .value()
        );
    }
}