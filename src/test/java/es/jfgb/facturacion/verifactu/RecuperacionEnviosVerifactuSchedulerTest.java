package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class RecuperacionEnviosVerifactuSchedulerTest {

    private RecuperacionEnviosVerifactuService recuperacionService;

    private RecuperacionEnviosVerifactuScheduler scheduler;

    @BeforeEach
    void setUp() {

        recuperacionService =
                mock(
                        RecuperacionEnviosVerifactuService.class
                );

        scheduler =
                new RecuperacionEnviosVerifactuScheduler(
                        recuperacionService
                );
    }

    @Test
    void ejecutaElServicioDeRecuperacion() {

        when(
                recuperacionService
                        .recuperarEnviosAntiguos()
        ).thenReturn(
                2
        );

        assertDoesNotThrow(
                () ->
                        scheduler.ejecutarRecuperacion()
        );

        verify(
                recuperacionService,
                times(1)
        ).recuperarEnviosAntiguos();
    }

    @Test
    void unaExcepcionNoImpideLaSiguienteEjecucion() {

        when(
                recuperacionService
                        .recuperarEnviosAntiguos()
        )
                .thenThrow(
                        new IllegalStateException(
                                "Fallo simulado de recuperación"
                        )
                )
                .thenReturn(
                        1
                );

        /*
         * Primera ejecución:
         * el servicio falla, pero el scheduler
         * debe capturar la excepción.
         */
        assertDoesNotThrow(
                () ->
                        scheduler.ejecutarRecuperacion()
        );

        /*
         * Segunda ejecución:
         * debe poder ejecutarse normalmente.
         */
        assertDoesNotThrow(
                () ->
                        scheduler.ejecutarRecuperacion()
        );

        verify(
                recuperacionService,
                times(2)
        ).recuperarEnviosAntiguos();
    }
}