package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.factura.Factura;
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

        Factura factura =
                mock(
                        Factura.class
                );

        when(
                factura.getId()
        ).thenReturn(
                10L
        );

        registro.setFactura(
                factura
        );

        RegistroFacturacion altaOriginal =
                new RegistroFacturacion();

        altaOriginal.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        altaOriginal.setEstadoEnvio(
                EstadoEnvioRegistro.ACEPTADO
        );

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

        when(
                registroRepository.findAltaOriginalConBloqueo(
                        10L,
                        1L,
                        TipoRegistroFacturacion.ALTA
                )
        ).thenReturn(
                Optional.of(
                        altaOriginal
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

    @Test
    void bloqueaAnulacionMientrasAltaOriginalNoEsteAceptado() {

        EstadoEnvioRegistro[] estadosBloqueados = {
                EstadoEnvioRegistro.PENDIENTE,
                EstadoEnvioRegistro.ENVIANDO,
                EstadoEnvioRegistro.RECHAZADO,
                EstadoEnvioRegistro.ERROR,
                EstadoEnvioRegistro.INCIERTO
        };

        for (EstadoEnvioRegistro estadoAlta : estadosBloqueados) {

            RegistroFacturacionRepository registroRepository =
                    mock(RegistroFacturacionRepository.class);

            RegistroFacturacionIntentoRepository intentoRepository =
                    mock(RegistroFacturacionIntentoRepository.class);

            EstadoEnvioVerifactuService service =
                    new EstadoEnvioVerifactuService(
                            registroRepository,
                            intentoRepository
                    );

            Factura factura =
                    mock(Factura.class);

            when(
                    factura.getId()
            ).thenReturn(
                    10L
            );

            RegistroFacturacion anulacion =
                    new RegistroFacturacion();

            anulacion.setFactura(
                    factura
            );

            anulacion.setTipoRegistro(
                    TipoRegistroFacturacion.ANULACION
            );

            anulacion.setEstadoEnvio(
                    EstadoEnvioRegistro.PENDIENTE
            );

            anulacion.setIntentosEnvio(
                    0
            );

            RegistroFacturacion altaOriginal =
                    new RegistroFacturacion();

            altaOriginal.setTipoRegistro(
                    TipoRegistroFacturacion.ALTA
            );

            altaOriginal.setEstadoEnvio(
                    estadoAlta
            );

            when(
                    registroRepository.findConBloqueo(
                            1L,
                            99L
                    )
            ).thenReturn(
                    Optional.of(anulacion)
            );

            when(
                    registroRepository.findAltaOriginalConBloqueo(
                            10L,
                            1L,
                            TipoRegistroFacturacion.ALTA
                    )
            ).thenReturn(
                    Optional.of(altaOriginal)
            );

            ResponseStatusException ex =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> service.iniciarEnvio(
                                    1L,
                                    99L,
                                    TipoRegistroFacturacion.ANULACION
                            ),
                            "Debe bloquearse con ALTA " + estadoAlta
                    );

            assertEquals(
                    409,
                    ex.getStatusCode().value()
            );

            assertEquals(
                    EstadoEnvioRegistro.PENDIENTE,
                    anulacion.getEstadoEnvio()
            );

            assertEquals(
                    0,
                    anulacion.getIntentosEnvio()
            );

            assertNull(
                    anulacion.getFechaUltimoIntento()
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

    @Test
    void bloqueaAnulacionSiNoExisteAltaOriginal() {

        RegistroFacturacionRepository registroRepository =
                mock(RegistroFacturacionRepository.class);

        RegistroFacturacionIntentoRepository intentoRepository =
                mock(RegistroFacturacionIntentoRepository.class);

        EstadoEnvioVerifactuService service =
                new EstadoEnvioVerifactuService(
                        registroRepository,
                        intentoRepository
                );

        Factura factura =
                mock(Factura.class);

        when(
                factura.getId()
        ).thenReturn(
                10L
        );

        RegistroFacturacion anulacion =
                new RegistroFacturacion();

        anulacion.setFactura(
                factura
        );

        anulacion.setTipoRegistro(
                TipoRegistroFacturacion.ANULACION
        );

        anulacion.setEstadoEnvio(
                EstadoEnvioRegistro.PENDIENTE
        );

        anulacion.setIntentosEnvio(
                0
        );

        when(
                registroRepository.findConBloqueo(
                        1L,
                        99L
                )
        ).thenReturn(
                Optional.of(anulacion)
        );

        when(
                registroRepository.findAltaOriginalConBloqueo(
                        10L,
                        1L,
                        TipoRegistroFacturacion.ALTA
                )
        ).thenReturn(
                Optional.empty()
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.iniciarEnvio(
                                1L,
                                99L,
                                TipoRegistroFacturacion.ANULACION
                        )
                );

        assertEquals(
                409,
                ex.getStatusCode().value()
        );

        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                anulacion.getEstadoEnvio()
        );

        assertEquals(
                0,
                anulacion.getIntentosEnvio()
        );

        assertNull(
                anulacion.getFechaUltimoIntento()
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
