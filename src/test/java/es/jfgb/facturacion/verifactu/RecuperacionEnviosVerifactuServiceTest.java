package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacionEnviosVerifactuServiceTest {

    @Mock
    private RegistroFacturacionRepository registroRepository;

    @Mock
    private EstadoEnvioVerifactuService estadoEnvioService;

    private RecuperacionEnviosVerifactuService service;

    @BeforeEach
    void setUp() {

        service =
                new RecuperacionEnviosVerifactuService(
                        registroRepository,
                        estadoEnvioService,
                        5
                );
    }

    @Test
    void sinCandidatosNoModificaNada() {

        when(
                registroRepository
                        .findIdsPorEstadoYFechaUltimoIntentoAnteriorA(
                                eq(EstadoEnvioRegistro.ENVIANDO),
                                any(Instant.class)
                        )
        ).thenReturn(
                List.of()
        );

        int recuperados =
                service.recuperarEnviosAntiguos();

        assertEquals(
                0,
                recuperados
        );

        verify(
                registroRepository
        ).findIdsPorEstadoYFechaUltimoIntentoAnteriorA(
                eq(EstadoEnvioRegistro.ENVIANDO),
                any(Instant.class)
        );

        verifyNoInteractions(
                estadoEnvioService
        );
    }

    @Test
    void procesaTodosLosCandidatosYCuentaSoloLosQueCambia() {

        when(
                registroRepository
                        .findIdsPorEstadoYFechaUltimoIntentoAnteriorA(
                                eq(EstadoEnvioRegistro.ENVIANDO),
                                any(Instant.class)
                        )
        ).thenReturn(
                List.of(
                        10L,
                        20L,
                        30L
                )
        );

        when(
                estadoEnvioService
                        .marcarComoInciertoSiSigueAntiguo(
                                eq(10L),
                                any(Instant.class)
                        )
        ).thenReturn(
                true
        );

        /*
         * Simula que el registro 20 dejó de estar ENVIANDO
         * entre la consulta de candidatos y la adquisición
         * del bloqueo pesimista.
         */
        when(
                estadoEnvioService
                        .marcarComoInciertoSiSigueAntiguo(
                                eq(20L),
                                any(Instant.class)
                        )
        ).thenReturn(
                false
        );

        when(
                estadoEnvioService
                        .marcarComoInciertoSiSigueAntiguo(
                                eq(30L),
                                any(Instant.class)
                        )
        ).thenReturn(
                true
        );

        int recuperados =
                service.recuperarEnviosAntiguos();

        assertEquals(
                2,
                recuperados
        );

        ArgumentCaptor<Instant> limiteCaptor =
                ArgumentCaptor.forClass(
                        Instant.class
                );

        verify(
                registroRepository
        ).findIdsPorEstadoYFechaUltimoIntentoAnteriorA(
                eq(EstadoEnvioRegistro.ENVIANDO),
                limiteCaptor.capture()
        );

        Instant limite =
                limiteCaptor.getValue();

        /*
         * Con configuración de 5 minutos,
         * el límite calculado debe estar aproximadamente
         * cinco minutos antes del momento actual.
         */
        Instant minimo =
                Instant.now()
                        .minus(
                                6,
                                ChronoUnit.MINUTES
                        );

        Instant maximo =
                Instant.now()
                        .minus(
                                4,
                                ChronoUnit.MINUTES
                        );

        assertTrue(
                limite.isAfter(minimo)
                        && limite.isBefore(maximo)
        );

        /*
         * El MISMO límite debe utilizarse al volver
         * a comprobar cada registro bajo bloqueo.
         */

        verify(
                estadoEnvioService
        ).marcarComoInciertoSiSigueAntiguo(
                10L,
                limite
        );

        verify(
                estadoEnvioService
        ).marcarComoInciertoSiSigueAntiguo(
                20L,
                limite
        );

        verify(
                estadoEnvioService
        ).marcarComoInciertoSiSigueAntiguo(
                30L,
                limite
        );

        verifyNoMoreInteractions(
                estadoEnvioService
        );
    }
}