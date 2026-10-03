package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.EstadoReconciliacionVerifactuService;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ServicioReconciliacionVerifactuBloqueadoTest {

    @Test
    void bloqueaAntesDeBaseDeDatosYAntesDeSoap() {

        EstadoReconciliacionVerifactuService estadoService =
                mock(
                        EstadoReconciliacionVerifactuService.class
                );

        AeatConsultaRegistroBuilder builder =
                mock(
                        AeatConsultaRegistroBuilder.class
                );

        AeatSoapGateway gateway =
                mock(
                        AeatSoapGateway.class
                );

        AeatConsultaRegistroInterpreter interpreter =
                mock(
                        AeatConsultaRegistroInterpreter.class
                );

        ServicioReconciliacionVerifactu service =
                new ServicioReconciliacionVerifactu(
                        estadoService,
                        builder,
                        gateway,
                        interpreter,
                        false
                );

        ResponseStatusException excepcion =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.reconciliar(
                                1L,
                                10L
                        )
                );

        assertEquals(
                503,
                excepcion.getStatusCode().value()
        );

        /*
         * El seguro debe actuar antes de cualquier efecto.
         */
        verifyNoInteractions(
                estadoService
        );

        verifyNoInteractions(
                builder
        );

        verifyNoInteractions(
                gateway
        );

        verifyNoInteractions(
                interpreter
        );

        System.out.println(
                "VERIFACTU_CONSULTA_BLOQUEADA=true"
        );

        System.out.println(
                "VERIFACTU_CONSULTA_HTTP_STATUS=503"
        );

        System.out.println(
                "VERIFACTU_CONSULTA_BD=false"
        );

        System.out.println(
                "VERIFACTU_CONSULTA_SOAP=false"
        );

        System.out.println(
                "VERIFACTU_CONSULTA_AEAT=false"
        );
    }
}