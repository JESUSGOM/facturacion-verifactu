package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstadoReconciliacionVerifactuServiceTest {

    @Test
    void preparaSnapshotDeUnRegistroIncierto() {

        RegistroFacturacionRepository registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        RegistroFacturacionIntentoRepository intentoRepository =
                mock(
                        RegistroFacturacionIntentoRepository.class
                );

        RegistroFacturacion registro =
                registroIncierto();

        when(
                registroRepository.findConBloqueo(
                        1L,
                        10L
                )
        ).thenReturn(
                Optional.of(
                        registro
                )
        );

        EstadoReconciliacionVerifactuService service =
                new EstadoReconciliacionVerifactuService(
                        registroRepository,
                        intentoRepository
                );

        DatosReconciliacionVerifactu datos =
                service.prepararConsulta(
                        1L,
                        10L
                );

        assertEquals(
                "Empresa de Prueba SL",
                datos.nombreRazonEmisor()
        );

        assertEquals(
                "B12345678",
                datos.nifEmisor()
        );

        assertEquals(
                "F2026-000123",
                datos.numSerieFactura()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        10,
                        3
                ),
                datos.fechaExpedicion()
        );
    }

    @Test
    void reconciliacionAceptadaActualizaRegistroEIntento() {

        RegistroFacturacionRepository registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        RegistroFacturacionIntentoRepository intentoRepository =
                mock(
                        RegistroFacturacionIntentoRepository.class
                );

        RegistroFacturacion registro =
                registroIncierto();

        registro.setIntentosEnvio(
                1
        );

        RegistroFacturacionIntento intento =
                new RegistroFacturacionIntento();

        intento.setEstado(
                EstadoEnvioRegistro.INCIERTO
        );

        when(
                registroRepository.findConBloqueo(
                        1L,
                        10L
                )
        ).thenReturn(
                Optional.of(
                        registro
                )
        );

        when(
                intentoRepository
                        .findByRegistroFacturacion_IdAndNumeroIntento(
                                10L,
                                1
                        )
        ).thenReturn(
                Optional.of(
                        intento
                )
        );

        EstadoReconciliacionVerifactuService service =
                new EstadoReconciliacionVerifactuService(
                        registroRepository,
                        intentoRepository
                );

        boolean actualizado =
                service.marcarAceptadoSiSigueIncierto(
                        1L,
                        10L
                );

        assertTrue(
                actualizado
        );

        assertEquals(
                EstadoEnvioRegistro.ACEPTADO,
                registro.getEstadoEnvio()
        );

        assertEquals(
                EstadoEnvioRegistro.ACEPTADO,
                intento.getEstado()
        );

        verify(
                intentoRepository
        ).saveAndFlush(
                intento
        );

        verify(
                registroRepository
        ).saveAndFlush(
                registro
        );
    }

    @Test
    void noSobrescribeRegistroQueYaDejoDeSerIncierto() {

        RegistroFacturacionRepository registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        RegistroFacturacionIntentoRepository intentoRepository =
                mock(
                        RegistroFacturacionIntentoRepository.class
                );

        RegistroFacturacion registro =
                registroIncierto();

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.ACEPTADO
        );

        when(
                registroRepository.findConBloqueo(
                        1L,
                        10L
                )
        ).thenReturn(
                Optional.of(
                        registro
                )
        );

        EstadoReconciliacionVerifactuService service =
                new EstadoReconciliacionVerifactuService(
                        registroRepository,
                        intentoRepository
                );

        boolean actualizado =
                service.marcarAceptadoSiSigueIncierto(
                        1L,
                        10L
                );

        assertFalse(
                actualizado
        );

        verify(
                intentoRepository,
                never()
        ).saveAndFlush(
                org.mockito.ArgumentMatchers.any()
        );

        verify(
                registroRepository,
                never()
        ).saveAndFlush(
                org.mockito.ArgumentMatchers.any()
        );
    }

    private RegistroFacturacion registroIncierto() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.INCIERTO
        );

        registro.setNombreRazonEmisor(
                "Empresa de Prueba SL"
        );

        registro.setNifEmisor(
                "B12345678"
        );

        registro.setNumSerieFactura(
                "F2026-000123"
        );

        registro.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        3
                )
        );

        return registro;
    }
}