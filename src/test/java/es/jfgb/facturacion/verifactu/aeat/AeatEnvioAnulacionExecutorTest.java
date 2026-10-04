package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoEnvioType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroFacturacionAnulacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AeatEnvioAnulacionExecutorTest {

    @Test
    void procesaAnulacionUsandoSoapMockSinRed() {

        AeatSolicitudAnulacionXmlBuilder builder =
                mock(
                        AeatSolicitudAnulacionXmlBuilder.class
                );

        AeatSoapGateway gateway =
                mock(
                        AeatSoapGateway.class
                );

        AeatRespuestaAltaXmlBuilder respuestaBuilder =
                mock(
                        AeatRespuestaAltaXmlBuilder.class
                );

        AeatRespuestaAltaValidator validator =
                mock(
                        AeatRespuestaAltaValidator.class
                );

        AeatResultadoEnvioMapper resultadoMapper =
                mock(
                        AeatResultadoEnvioMapper.class
                );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        registroFactura.setRegistroAnulacion(
                new RegistroFacturacionAnulacionType()
        );

        RegFactuSistemaFacturacion peticion =
                new RegFactuSistemaFacturacion();

        peticion.setCabecera(
                cabecera
        );

        peticion
                .getRegistroFactura()
                .add(
                        registroFactura
                );

        RespuestaExpedidaType linea =
                new RespuestaExpedidaType();

        linea.setEstadoRegistro(
                EstadoRegistroType.CORRECTO
        );

        AeatSoapResultadoAlta resultadoSoap =
                new AeatSoapResultadoAlta(
                        "CSV-ANULACION-PRUEBA",
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of(
                                linea
                        )
                );

        ResultadoEnvioVerifactu esperado =
                new ResultadoEnvioVerifactu(
                        EstadoResultadoEnvioVerifactu.ACEPTADO,
                        "CSV-ANULACION-PRUEBA",
                        "<anulacion/>",
                        "<respuesta/>",
                        "Aceptado"
                );

        when(
                builder.construir(
                        registro
                )
        ).thenReturn(
                peticion
        );

        when(
                builder.generarXml(
                        registro
                )
        ).thenReturn(
                "<anulacion/>"
        );

        when(
                gateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenReturn(
                resultadoSoap
        );

        when(
                respuestaBuilder.generarXml(
                        resultadoSoap
                )
        ).thenReturn(
                "<respuesta/>"
        );

        when(
                validator.esCoherente(
                        resultadoSoap
                )
        ).thenReturn(
                true
        );

        when(
                resultadoMapper.convertir(
                        "CSV-ANULACION-PRUEBA",
                        "<anulacion/>",
                        "<respuesta/>",
                        linea
                )
        ).thenReturn(
                esperado
        );

        AeatEnvioAnulacionExecutor executor =
                new AeatEnvioAnulacionExecutor(
                        builder,
                        gateway,
                        respuestaBuilder,
                        validator,
                        resultadoMapper
                );

        ResultadoEnvioVerifactu resultado =
                executor.enviar(
                        registro
                );

        assertSame(
                esperado,
                resultado
        );

        verify(
                gateway
        ).enviarAlta(
                cabecera,
                registroFactura
        );

        /*
         * Es un mock.
         * No existe ningun puerto SOAP real en este test.
         */
    }

    @Test
    void providerConConstructorAntiguoMantieneAnulacionBloqueada() {

        AeatSolicitudAltaXmlBuilder altaBuilder =
                mock(
                        AeatSolicitudAltaXmlBuilder.class
                );

        AeatSoapGateway gateway =
                mock(
                        AeatSoapGateway.class
                );

        AeatRespuestaAltaXmlBuilder respuestaBuilder =
                mock(
                        AeatRespuestaAltaXmlBuilder.class
                );

        AeatRespuestaAltaValidator validator =
                mock(
                        AeatRespuestaAltaValidator.class
                );

        AeatResultadoEnvioMapper resultadoMapper =
                mock(
                        AeatResultadoEnvioMapper.class
                );

        AeatVerifactuProvider provider =
                new AeatVerifactuProvider(
                        altaBuilder,
                        gateway,
                        respuestaBuilder,
                        validator,
                        resultadoMapper
                );

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                provider.enviarAnulacion(
                                        new RegistroFacturacion()
                                )
                );

        assertTrue(
                ex.getMessage()
                        .contains(
                                "todavia no esta habilitado"
                        )
        );

        /*
         * El bloqueo sucede antes de llegar al gateway.
         */
        verifyNoInteractions(
                gateway
        );
    }

    @Test
    void providerPreparadoDelegaEnExecutor() {

        AeatSolicitudAltaXmlBuilder altaBuilder =
                mock(
                        AeatSolicitudAltaXmlBuilder.class
                );

        AeatSoapGateway gateway =
                mock(
                        AeatSoapGateway.class
                );

        AeatRespuestaAltaXmlBuilder respuestaBuilder =
                mock(
                        AeatRespuestaAltaXmlBuilder.class
                );

        AeatRespuestaAltaValidator validator =
                mock(
                        AeatRespuestaAltaValidator.class
                );

        AeatResultadoEnvioMapper resultadoMapper =
                mock(
                        AeatResultadoEnvioMapper.class
                );

        AeatEnvioAnulacionExecutor executor =
                mock(
                        AeatEnvioAnulacionExecutor.class
                );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        ResultadoEnvioVerifactu esperado =
                new ResultadoEnvioVerifactu(
                        EstadoResultadoEnvioVerifactu.ACEPTADO,
                        "SIM",
                        "<a/>",
                        "<r/>",
                        "Aceptado"
                );

        when(
                executor.enviar(
                        registro
                )
        ).thenReturn(
                esperado
        );

        AeatVerifactuProvider provider =
                new AeatVerifactuProvider(
                        altaBuilder,
                        gateway,
                        respuestaBuilder,
                        validator,
                        resultadoMapper,
                        executor
                );

        ResultadoEnvioVerifactu resultado =
                provider.enviarAnulacion(
                        registro
                );

        assertSame(
                esperado,
                resultado
        );

        verify(
                executor
        ).enviar(
                registro
        );

        /*
         * El provider no llama directamente al gateway:
         * delega exclusivamente en el executor.
         */
        verifyNoInteractions(
                gateway
        );
    }
}