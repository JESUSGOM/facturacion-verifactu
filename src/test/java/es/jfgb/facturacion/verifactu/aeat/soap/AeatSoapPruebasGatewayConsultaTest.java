package es.jfgb.facturacion.verifactu.aeat.soap;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AeatSoapPruebasGatewayConsultaTest {

    @Test
    void delegaLaConsultaEnElPuertoSoap() {

        SfPortTypeVerifactu port =
                mock(
                        SfPortTypeVerifactu.class
                );

        ConsultaFactuSistemaFacturacionType consulta =
                new ConsultaFactuSistemaFacturacionType();

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        when(
                port.consultaFactuSistemaFacturacion(
                        consulta
                )
        ).thenReturn(
                respuesta
        );

        AeatSoapPruebasGateway gateway =
                new AeatSoapPruebasGateway(
                        port
                );

        RespuestaConsultaFactuSistemaFacturacionType resultado =
                gateway.consultar(
                        consulta
                );

        assertSame(
                respuesta,
                resultado
        );

        verify(
                port
        ).consultaFactuSistemaFacturacion(
                consulta
        );

        verifyNoMoreInteractions(
                port
        );
    }

    @Test
    void rechazaUnaConsultaNulaAntesDeLlegarAlPuertoSoap() {

        SfPortTypeVerifactu port =
                mock(
                        SfPortTypeVerifactu.class
                );

        AeatSoapPruebasGateway gateway =
                new AeatSoapPruebasGateway(
                        port
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> gateway.consultar(
                        null
                )
        );

        verifyNoMoreInteractions(
                port
        );
    }
}