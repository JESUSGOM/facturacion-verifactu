package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicioEnvioVerifactuAnulacionTest {

    @Test
    void enviaAnulacionPorElMetodoEspecificoDelProvider() {

        EstadoEnvioVerifactuService estadoEnvioService =
                mock(
                        EstadoEnvioVerifactuService.class
                );

        VerifactuProvider provider =
                mock(
                        VerifactuProvider.class
                );

        ServicioEnvioVerifactu service =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        provider,
                        true
                );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        ResultadoEnvioVerifactu resultado =
                new ResultadoEnvioVerifactu(
                        true,
                        "DEV-ANULACION-99",
                        "<anulacion/>",
                        "<respuesta/>",
                        "Aceptado"
                );

        when(
                estadoEnvioService.iniciarEnvio(
                        1L,
                        99L,
                        TipoRegistroFacturacion.ANULACION
                )
        ).thenReturn(
                registro
        );

        when(
                provider.getNombre()
        ).thenReturn(
                "DESARROLLO"
        );

        when(
                provider.enviarAnulacion(
                        registro
                )
        ).thenReturn(
                resultado
        );

        service.enviarAnulacion(
                1L,
                99L
        );

        verify(
                provider
        ).enviarAnulacion(
                registro
        );

        verify(
                provider,
                never()
        ).enviarAlta(
                any()
        );

        verify(
                estadoEnvioService
        ).finalizarEnvio(
                1L,
                99L,
                "DESARROLLO",
                resultado
        );
    }

    @Test
    void anulacionBloqueadaNoTocaEstadoNiProvider() {

        EstadoEnvioVerifactuService estadoEnvioService =
                mock(
                        EstadoEnvioVerifactuService.class
                );

        VerifactuProvider provider =
                mock(
                        VerifactuProvider.class
                );

        ServicioEnvioVerifactu service =
                new ServicioEnvioVerifactu(
                        estadoEnvioService,
                        provider,
                        false
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.enviarAnulacion(
                                        1L,
                                        99L
                                )
                );

        assertEquals(
                503,
                ex.getStatusCode().value()
        );

        verifyNoInteractions(
                estadoEnvioService
        );

        verifyNoInteractions(
                provider
        );
    }
}