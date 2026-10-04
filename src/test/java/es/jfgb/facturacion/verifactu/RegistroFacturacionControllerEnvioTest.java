package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistroFacturacionControllerEnvioTest {

    @Test
    void registroAltaUtilizaEnviarAlta() {

        RegistroFacturacionService service =
                mock(
                        RegistroFacturacionService.class
                );

        ServicioEnvioVerifactu servicioEnvio =
                mock(
                        ServicioEnvioVerifactu.class
                );

        RegistroFacturacionIntentoService intentoService =
                mock(
                        RegistroFacturacionIntentoService.class
                );

        RegistroFacturacionResponse antes =
                mock(
                        RegistroFacturacionResponse.class
                );

        RegistroFacturacionResponse despues =
                mock(
                        RegistroFacturacionResponse.class
                );

        when(
                antes.tipoRegistro()
        ).thenReturn(
                TipoRegistroFacturacion.ALTA
        );

        when(
                service.obtener(
                        1L,
                        30L
                )
        ).thenReturn(
                antes,
                despues
        );

        RegistroFacturacionController controller =
                new RegistroFacturacionController(
                        service,
                        servicioEnvio,
                        intentoService
                );

        RegistroFacturacionResponse resultado =
                controller.enviar(
                        1L,
                        30L
                );

        assertSame(
                despues,
                resultado
        );

        verify(
                servicioEnvio
        ).enviarAlta(
                1L,
                30L
        );

        verify(
                servicioEnvio,
                never()
        ).enviarAnulacion(
                anyLong(),
                anyLong()
        );
    }

    @Test
    void registroAnulacionUtilizaEnviarAnulacion() {

        RegistroFacturacionService service =
                mock(
                        RegistroFacturacionService.class
                );

        ServicioEnvioVerifactu servicioEnvio =
                mock(
                        ServicioEnvioVerifactu.class
                );

        RegistroFacturacionIntentoService intentoService =
                mock(
                        RegistroFacturacionIntentoService.class
                );

        RegistroFacturacionResponse antes =
                mock(
                        RegistroFacturacionResponse.class
                );

        RegistroFacturacionResponse despues =
                mock(
                        RegistroFacturacionResponse.class
                );

        when(
                antes.tipoRegistro()
        ).thenReturn(
                TipoRegistroFacturacion.ANULACION
        );

        when(
                service.obtener(
                        1L,
                        31L
                )
        ).thenReturn(
                antes,
                despues
        );

        RegistroFacturacionController controller =
                new RegistroFacturacionController(
                        service,
                        servicioEnvio,
                        intentoService
                );

        RegistroFacturacionResponse resultado =
                controller.enviar(
                        1L,
                        31L
                );

        assertSame(
                despues,
                resultado
        );

        verify(
                servicioEnvio
        ).enviarAnulacion(
                1L,
                31L
        );

        verify(
                servicioEnvio,
                never()
        ).enviarAlta(
                anyLong(),
                anyLong()
        );
    }

    @Test
    void registroSinTipoSeRechazaSinIntentarEnvio() {

        RegistroFacturacionService service =
                mock(
                        RegistroFacturacionService.class
                );

        ServicioEnvioVerifactu servicioEnvio =
                mock(
                        ServicioEnvioVerifactu.class
                );

        RegistroFacturacionIntentoService intentoService =
                mock(
                        RegistroFacturacionIntentoService.class
                );

        RegistroFacturacionResponse registro =
                mock(
                        RegistroFacturacionResponse.class
                );

        when(
                registro.tipoRegistro()
        ).thenReturn(
                null
        );

        when(
                service.obtener(
                        1L,
                        99L
                )
        ).thenReturn(
                registro
        );

        RegistroFacturacionController controller =
                new RegistroFacturacionController(
                        service,
                        servicioEnvio,
                        intentoService
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                controller.enviar(
                                        1L,
                                        99L
                                )
                );

        assertEquals(
                409,
                ex.getStatusCode().value()
        );

        verifyNoInteractions(
                servicioEnvio
        );
    }
}