package es.jfgb.facturacion.verifactu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class RecuperacionEnviosVerifactuScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    RecuperacionEnviosVerifactuScheduler.class
            );

    private final RecuperacionEnviosVerifactuService recuperacionService;

    public RecuperacionEnviosVerifactuScheduler(
            RecuperacionEnviosVerifactuService recuperacionService) {

        this.recuperacionService = recuperacionService;
    }

    @Scheduled(
            fixedDelayString =
                    "${verifactu.envios.recuperacion-intervalo-ms:60000}",
            initialDelayString =
                    "${verifactu.envios.recuperacion-initial-delay-ms:60000}"
    )
    public void ejecutarRecuperacion() {

        try {

            int recuperados =
                    recuperacionService
                            .recuperarEnviosAntiguos();

            if (recuperados > 0) {

                log.warn(
                        "Recuperación VERI*FACTU: "
                                + "{} registro(s) ENVIANDO "
                                + "marcado(s) como INCIERTO",
                        recuperados
                );
            }

        } catch (Exception e) {

            /*
             * Un fallo puntual de la recuperación no debe
             * detener las ejecuciones futuras del scheduler.
             */
            log.error(
                    "Error durante la recuperación automática "
                            + "de envíos VERI*FACTU",
                    e
            );
        }
    }
}