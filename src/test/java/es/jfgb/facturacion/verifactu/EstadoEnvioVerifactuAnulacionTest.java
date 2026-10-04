package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EstadoEnvioVerifactuAnulacionTest {

    @Test
    void iniciaAnulacionYLaMarcaEnviando() {

        RegistroFacturacionRepository registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        RegistroFacturacionIntentoRepository intentoRepository =
                mock(
                        RegistroFacturacionIntentoRepository.class
                );

        EstadoEnvioVerifactuService service =
                new EstadoEnvioVerifactuService(
                        registroRepository,
                        intentoRepository
                );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ANULACION
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.PENDIENTE
        );

        registro.setIntentosEnvio(
                0
        );

        when(
                registroRepository.findConBloqueo(
                        1L,
                        99L
                )
        ).thenReturn(
                Optional.of(
                        registro
                )
        );

        RegistroFacturacion resultado =
                service.iniciarEnvio(
                        1L,
                        99L,
                        TipoRegistroFacturacion.ANULACION
                );

        assertSame(
                registro,
                resultado
        );

        assertEquals(
                EstadoEnvioRegistro.ENVIANDO,
                registro.getEstadoEnvio()
        );

        assertEquals(
                1,
                registro.getIntentosEnvio()
        );

        assertNotNull(
                registro.getFechaUltimoIntento()
        );

        verify(
                registroRepository
        ).saveAndFlush(
                registro
        );

        verify(
                intentoRepository
        ).saveAndFlush(
                any(
                        RegistroFacturacionIntento.class
                )
        );
    }

    @Test
    void tipoIncorrectoSeRechazaAntesDeModificarEstado() {

        RegistroFacturacionRepository registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        RegistroFacturacionIntentoRepository intentoRepository =
                mock(
                        RegistroFacturacionIntentoRepository.class
                );

        EstadoEnvioVerifactuService service =
                new EstadoEnvioVerifactuService(
                        registroRepository,
                        intentoRepository
                );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ANULACION
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.PENDIENTE
        );

        when(
                registroRepository.findConBloqueo(
                        1L,
                        99L
                )
        ).thenReturn(
                Optional.of(
                        registro
                )
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.iniciarEnvio(
                                        1L,
                                        99L,
                                        TipoRegistroFacturacion.ALTA
                                )
                );

        assertEquals(
                400,
                ex.getStatusCode().value()
        );

        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                registro.getEstadoEnvio()
        );

        verify(
                registroRepository,
                never()
        ).saveAndFlush(
                any()
        );

        verifyNoInteractions(
                intentoRepository
        );
    }
}