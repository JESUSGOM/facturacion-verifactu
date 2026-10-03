package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstadoEnvioVerifactuServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long REGISTRO_ID = 100L;
    private static final Integer NUMERO_INTENTO = 1;

    private RegistroFacturacionRepository registroRepository;
    private RegistroFacturacionIntentoRepository intentoRepository;

    private EstadoEnvioVerifactuService service;

    private RegistroFacturacion registro;
    private RegistroFacturacionIntento intento;

    @BeforeEach
    void setUp() {

        registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        intentoRepository =
                mock(
                        RegistroFacturacionIntentoRepository.class
                );

        registro =
                mock(
                        RegistroFacturacion.class
                );

        intento =
                mock(
                        RegistroFacturacionIntento.class
                );

        service =
                new EstadoEnvioVerifactuService(
                        registroRepository,
                        intentoRepository
                );

        when(
                registroRepository.findConBloqueo(
                        EMPRESA_ID,
                        REGISTRO_ID
                )
        ).thenReturn(
                Optional.of(registro)
        );

        when(
                registro.getIntentosEnvio()
        ).thenReturn(
                NUMERO_INTENTO
        );

        when(
                intentoRepository
                        .findByRegistroFacturacion_IdAndNumeroIntento(
                                REGISTRO_ID,
                                NUMERO_INTENTO
                        )
        ).thenReturn(
                Optional.of(intento)
        );
    }

    @Test
    void aceptadoTerminaComoAceptado() {

        ResultadoEnvioVerifactu resultado =
                crearResultado(
                        EstadoResultadoEnvioVerifactu.ACEPTADO
                );

        service.finalizarEnvio(
                EMPRESA_ID,
                REGISTRO_ID,
                "AEAT-PRUEBA",
                resultado
        );

        verify(registro).setEstadoEnvio(
                EstadoEnvioRegistro.ACEPTADO
        );

        verify(intento).setEstado(
                EstadoEnvioRegistro.ACEPTADO
        );
    }

    @Test
    void aceptadoConErroresTerminaComoAceptado() {

        ResultadoEnvioVerifactu resultado =
                crearResultado(
                        EstadoResultadoEnvioVerifactu
                                .ACEPTADO_CON_ERRORES
                );

        service.finalizarEnvio(
                EMPRESA_ID,
                REGISTRO_ID,
                "AEAT-PRUEBA",
                resultado
        );

        verify(registro).setEstadoEnvio(
                EstadoEnvioRegistro.ACEPTADO
        );

        verify(intento).setEstado(
                EstadoEnvioRegistro.ACEPTADO
        );
    }

    @Test
    void rechazadoTerminaComoRechazado() {

        ResultadoEnvioVerifactu resultado =
                crearResultado(
                        EstadoResultadoEnvioVerifactu.RECHAZADO
                );

        service.finalizarEnvio(
                EMPRESA_ID,
                REGISTRO_ID,
                "AEAT-PRUEBA",
                resultado
        );

        verify(registro).setEstadoEnvio(
                EstadoEnvioRegistro.RECHAZADO
        );

        verify(intento).setEstado(
                EstadoEnvioRegistro.RECHAZADO
        );
    }

    @Test
    void requiereReconciliacionTerminaComoIncierto() {

        ResultadoEnvioVerifactu resultado =
                crearResultado(
                        EstadoResultadoEnvioVerifactu
                                .REQUIERE_RECONCILIACION
                );

        service.finalizarEnvio(
                EMPRESA_ID,
                REGISTRO_ID,
                "AEAT-PRUEBA",
                resultado
        );

        verify(registro).setEstadoEnvio(
                EstadoEnvioRegistro.INCIERTO
        );

        verify(intento).setEstado(
                EstadoEnvioRegistro.INCIERTO
        );
    }

    private ResultadoEnvioVerifactu crearResultado(
            EstadoResultadoEnvioVerifactu estado) {

        return new ResultadoEnvioVerifactu(
                estado,
                "ID-PRUEBA",
                "<peticion/>",
                "<respuesta/>",
                "Mensaje de prueba"
        );
    }
}