package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicioEnvioVerifactuTest {

    private EstadoEnvioVerifactuService estadoEnvioService;
    private VerifactuProvider provider;
    private ServicioEnvioVerifactu servicio;

    @BeforeEach
    void setUp() {

        estadoEnvioService =
                mock(
                        EstadoEnvioVerifactuService.class
                );

        provider =
                mock(
                        VerifactuProvider.class
                );

        /*
         * En los tests funcionales normales dejamos
         * el envío expresamente habilitado.
         */
        servicio =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        provider,
                        true
                );
    }

    @Test
    void bloqueaAntesDeModificarEstadoCuandoEnvioEstaDeshabilitado() {

        ServicioEnvioVerifactu servicioBloqueado =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        provider,
                        false
                );

        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () -> servicioBloqueado.enviarAlta(
                                1L,
                                13L
                        )
                );

        assertEquals(
                503,
                excepcion.getStatusCode().value()
        );

        /*
         * CRÍTICO:
         *
         * Si el seguro está activo no debe tocarse
         * ni el estado del registro ni el proveedor.
         */
        verifyNoInteractions(
                estadoEnvioService
        );

        verifyNoInteractions(
                provider
        );
    }

    @Test
    void finalizaCuandoElProveedorRespondeCorrectamente() {

        Long empresaId = 1L;
        Long registroId = 13L;

        RegistroFacturacion registro =
                new RegistroFacturacion();

        ResultadoEnvioVerifactu resultado =
                new ResultadoEnvioVerifactu(
                        true,
                        "EXT-PRUEBA",
                        "<peticion/>",
                        "<respuesta/>",
                        "Aceptado"
                );

        when(
                estadoEnvioService.iniciarEnvio(
                        empresaId,
                        registroId
                )
        ).thenReturn(
                registro
        );

        when(
                provider.getNombre()
        ).thenReturn(
                "AEAT-PRUEBAS"
        );

        when(
                provider.enviarAlta(
                        registro
                )
        ).thenReturn(
                resultado
        );

        servicio.enviarAlta(
                empresaId,
                registroId
        );

        verify(
                estadoEnvioService
        ).finalizarEnvio(
                empresaId,
                registroId,
                "AEAT-PRUEBAS",
                resultado
        );

        verify(
                estadoEnvioService,
                never()
        ).registrarError(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );

        verify(
                estadoEnvioService,
                never()
        ).registrarIncierto(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );
    }

    @Test
    void resultadoInciertoNoSeConvierteEnError() {

        Long empresaId = 1L;
        Long registroId = 13L;

        RegistroFacturacion registro =
                new RegistroFacturacion();

        EnvioVerifactuInciertoException fallo =
                new EnvioVerifactuInciertoException(
                        "No se conoce el resultado"
                );

        when(
                estadoEnvioService.iniciarEnvio(
                        empresaId,
                        registroId
                )
        ).thenReturn(
                registro
        );

        when(
                provider.getNombre()
        ).thenReturn(
                "AEAT-PRUEBAS"
        );

        when(
                provider.enviarAlta(
                        registro
                )
        ).thenThrow(
                fallo
        );

        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () -> servicio.enviarAlta(
                                empresaId,
                                registroId
                        )
                );

        assertEquals(
                502,
                excepcion.getStatusCode().value()
        );

        verify(
                estadoEnvioService
        ).registrarIncierto(
                empresaId,
                registroId,
                "AEAT-PRUEBAS",
                fallo
        );

        verify(
                estadoEnvioService,
                never()
        ).registrarError(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );

        verify(
                estadoEnvioService,
                never()
        ).finalizarEnvio(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );
    }

    @Test
    void errorSeguroSeRegistraComoError() {

        Long empresaId = 1L;
        Long registroId = 13L;

        RegistroFacturacion registro =
                new RegistroFacturacion();

        IllegalStateException fallo =
                new IllegalStateException(
                        "Fallo seguro de prueba"
                );

        when(
                estadoEnvioService.iniciarEnvio(
                        empresaId,
                        registroId
                )
        ).thenReturn(
                registro
        );

        when(
                provider.getNombre()
        ).thenReturn(
                "AEAT-PRUEBAS"
        );

        when(
                provider.enviarAlta(
                        registro
                )
        ).thenThrow(
                fallo
        );

        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () -> servicio.enviarAlta(
                                empresaId,
                                registroId
                        )
                );

        assertEquals(
                502,
                excepcion.getStatusCode().value()
        );

        verify(
                estadoEnvioService
        ).registrarError(
                empresaId,
                registroId,
                "AEAT-PRUEBAS",
                fallo
        );

        verify(
                estadoEnvioService,
                never()
        ).registrarIncierto(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );

        verify(
                estadoEnvioService,
                never()
        ).finalizarEnvio(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );
    }

    @Test
    void falloAlGuardarRespuestaNoSeConvierteEnErrorReenviable() {

        Long empresaId = 1L;
        Long registroId = 13L;

        RegistroFacturacion registro =
                new RegistroFacturacion();

        ResultadoEnvioVerifactu resultado =
                new ResultadoEnvioVerifactu(
                        true,
                        "EXT-PRUEBA",
                        "<peticion/>",
                        "<respuesta/>",
                        "Aceptado"
                );

        IllegalStateException falloPersistencia =
                new IllegalStateException(
                        "Fallo de persistencia simulado"
                );

        when(
                estadoEnvioService.iniciarEnvio(
                        empresaId,
                        registroId
                )
        ).thenReturn(
                registro
        );

        when(
                provider.getNombre()
        ).thenReturn(
                "AEAT-PRUEBAS"
        );

        when(
                provider.enviarAlta(
                        registro
                )
        ).thenReturn(
                resultado
        );

        doThrow(
                falloPersistencia
        ).when(
                estadoEnvioService
        ).finalizarEnvio(
                empresaId,
                registroId,
                "AEAT-PRUEBAS",
                resultado
        );

        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () -> servicio.enviarAlta(
                                empresaId,
                                registroId
                        )
                );

        assertEquals(
                500,
                excepcion.getStatusCode().value()
        );

        verify(
                provider
        ).enviarAlta(
                registro
        );

        verify(
                estadoEnvioService
        ).finalizarEnvio(
                empresaId,
                registroId,
                "AEAT-PRUEBAS",
                resultado
        );

        /*
         * CRÍTICO:
         *
         * El proveedor ya respondió.
         * Un fallo posterior al guardar en MySQL
         * NO puede marcarse como ERROR reenviable.
         */

        verify(
                estadoEnvioService,
                never()
        ).registrarError(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );

        verify(
                estadoEnvioService,
                never()
        ).registrarIncierto(
                anyLong(),
                anyLong(),
                anyString(),
                any()
        );
    }
}