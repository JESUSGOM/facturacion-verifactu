package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        properties = {

                /*
                 * Datos mínimos necesarios para arrancar
                 * el contexto de pruebas.
                 */
                "verifactu.sistema.nombre-razon=Gombeth Software Solutions Test",
                "verifactu.sistema.nif=TESTSIF001",
                "verifactu.sistema.nombre-sistema=Gombeth Factura",
                "verifactu.sistema.id-sistema=GF",
                "verifactu.sistema.version=0.0.1-SNAPSHOT",
                "verifactu.sistema.solo-verifactu=S",
                "verifactu.sistema.multi-ot=S",

                /*
                 * Evitamos que el scheduler automático
                 * tenga ocasión de ejecutarse durante este test.
                 */
                "verifactu.envios.recuperacion-initial-delay-ms=3600000"

                /*
                 * IMPORTANTE:
                 *
                 * NO definimos aquí:
                 *
                 * verifactu.envio-habilitado
                 *
                 * Queremos probar el valor REAL resuelto
                 * desde application.properties + entorno.
                 */
        }
)
class VerifactuEnvioBloqueadoPorDefectoIT {

    @Autowired
    private ServicioEnvioVerifactu servicioEnvioVerifactu;

    @Test
    void envioEstaBloqueadoPorDefecto() {

        /*
         * Usamos IDs negativos deliberadamente.
         *
         * Si el bloqueo funciona correctamente,
         * ni siquiera se consultará el registro.
         */

        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                servicioEnvioVerifactu.enviarAlta(
                                        -1L,
                                        -1L
                                )
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                excepcion.getStatusCode()
        );

        assertEquals(
                503,
                excepcion.getStatusCode().value()
        );

        System.out.println(
                "VERIFACTU_DEFAULT_BLOQUEADO=true"
        );

        System.out.println(
                "VERIFACTU_DEFAULT_HTTP_STATUS="
                        + excepcion.getStatusCode().value()
        );

        System.out.println(
                "VERIFACTU_DEFAULT_AEAT=false"
        );
    }
}