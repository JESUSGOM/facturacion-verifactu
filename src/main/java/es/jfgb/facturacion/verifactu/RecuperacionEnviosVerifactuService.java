package es.jfgb.facturacion.verifactu;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RecuperacionEnviosVerifactuService {

    private final RegistroFacturacionRepository registroRepository;
    private final EstadoEnvioVerifactuService estadoEnvioService;

    private final long minutosParaConsiderarIncierto;

    public RecuperacionEnviosVerifactuService(
            RegistroFacturacionRepository registroRepository,
            EstadoEnvioVerifactuService estadoEnvioService,
            @Value("${verifactu.envios.minutos-para-incierto:5}")
            long minutosParaConsiderarIncierto) {

        this.registroRepository = registroRepository;
        this.estadoEnvioService = estadoEnvioService;
        this.minutosParaConsiderarIncierto =
                minutosParaConsiderarIncierto;
    }

    public int recuperarEnviosAntiguos() {

        Instant limite =
                Instant.now().minus(
                        minutosParaConsiderarIncierto,
                        ChronoUnit.MINUTES
                );

        List<Long> candidatos =
                registroRepository
                        .findIdsPorEstadoYFechaUltimoIntentoAnteriorA(
                                EstadoEnvioRegistro.ENVIANDO,
                                limite
                        );

        int recuperados = 0;

        for (Long registroId : candidatos) {

            boolean cambiado =
                    estadoEnvioService
                            .marcarComoInciertoSiSigueAntiguo(
                                    registroId,
                                    limite
                            );

            if (cambiado) {
                recuperados++;
            }
        }

        return recuperados;
    }
}