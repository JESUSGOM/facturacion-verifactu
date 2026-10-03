package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoEnvioType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AeatVerifactuProviderTest {

    private AeatSolicitudAltaXmlBuilder
            solicitudAltaXmlBuilder;

    private AeatSoapGateway
            soapGateway;

    private AeatRespuestaAltaXmlBuilder
            respuestaAltaXmlBuilder;

    private AeatRespuestaAltaValidator
            respuestaAltaValidator;

    private AeatResultadoEnvioMapper
            resultadoEnvioMapper;

    private AeatVerifactuProvider
            provider;

    @BeforeEach
    void setUp() {

        solicitudAltaXmlBuilder =
                mock(
                        AeatSolicitudAltaXmlBuilder.class
                );

        soapGateway =
                mock(
                        AeatSoapGateway.class
                );

        respuestaAltaXmlBuilder =
                mock(
                        AeatRespuestaAltaXmlBuilder.class
                );

        respuestaAltaValidator =
                mock(
                        AeatRespuestaAltaValidator.class
                );

        resultadoEnvioMapper =
                mock(
                        AeatResultadoEnvioMapper.class
                );

        provider =
                new AeatVerifactuProvider(
                        solicitudAltaXmlBuilder,
                        soapGateway,
                        respuestaAltaXmlBuilder,
                        respuestaAltaValidator,
                        resultadoEnvioMapper
                );
    }

    @Test
    void procesaCorrectamenteUnaRespuestaConUnaLinea() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RegFactuSistemaFacturacion peticion =
                crearPeticion(
                        cabecera,
                        registroFactura
                );

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.CORRECTO
        );

        AeatSoapResultadoAlta resultadoSoap =
                new AeatSoapResultadoAlta(
                        "CSV-PRUEBA",
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of(
                                respuestaLinea
                        )
                );

        ResultadoEnvioVerifactu resultadoEsperado =
                new ResultadoEnvioVerifactu(
                        EstadoResultadoEnvioVerifactu.ACEPTADO,
                        "CSV-PRUEBA",
                        "<peticion/>",
                        "<respuesta/>",
                        "Aceptado"
                );

        prepararPeticion(
                registro,
                peticion
        );

        when(
                soapGateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenReturn(
                resultadoSoap
        );

        when(
                respuestaAltaXmlBuilder.generarXml(
                        resultadoSoap
                )
        ).thenReturn(
                "<respuesta/>"
        );

        when(
                respuestaAltaValidator.esCoherente(
                        resultadoSoap
                )
        ).thenReturn(
                true
        );

        when(
                resultadoEnvioMapper.convertir(
                        "CSV-PRUEBA",
                        "<peticion/>",
                        "<respuesta/>",
                        respuestaLinea
                )
        ).thenReturn(
                resultadoEsperado
        );

        ResultadoEnvioVerifactu resultado =
                provider.enviarAlta(
                        registro
                );

        assertSame(
                resultadoEsperado,
                resultado
        );

        verify(
                respuestaAltaValidator
        ).esCoherente(
                resultadoSoap
        );

        verify(
                resultadoEnvioMapper
        ).convertir(
                "CSV-PRUEBA",
                "<peticion/>",
                "<respuesta/>",
                respuestaLinea
        );
    }

    @Test
    void respuestaIncoherenteRequiereReconciliacion() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RegFactuSistemaFacturacion peticion =
                crearPeticion(
                        cabecera,
                        registroFactura
                );

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.CORRECTO
        );

        AeatSoapResultadoAlta resultadoSoap =
                new AeatSoapResultadoAlta(
                        "CSV-INCOHERENTE",
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.INCORRECTO,
                        List.of(
                                respuestaLinea
                        )
                );

        prepararPeticion(
                registro,
                peticion
        );

        when(
                soapGateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenReturn(
                resultadoSoap
        );

        when(
                respuestaAltaXmlBuilder.generarXml(
                        resultadoSoap
                )
        ).thenReturn(
                "<respuesta/>"
        );

        when(
                respuestaAltaValidator.esCoherente(
                        resultadoSoap
                )
        ).thenReturn(
                false
        );

        ResultadoEnvioVerifactu resultado =
                provider.enviarAlta(
                        registro
                );

        assertEquals(
                EstadoResultadoEnvioVerifactu
                        .REQUIERE_RECONCILIACION,
                resultado.estado()
        );

        assertEquals(
                "CSV-INCOHERENTE",
                resultado.identificadorExterno()
        );

        verify(
                resultadoEnvioMapper,
                never()
        ).convertir(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void respuestaSinLineasRequiereReconciliacion() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RegFactuSistemaFacturacion peticion =
                crearPeticion(
                        cabecera,
                        registroFactura
                );

        AeatSoapResultadoAlta resultadoSoap =
                new AeatSoapResultadoAlta(
                        "CSV-SIN-LINEAS",
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.INCORRECTO,
                        List.of()
                );

        prepararPeticion(
                registro,
                peticion
        );

        when(
                soapGateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenReturn(
                resultadoSoap
        );

        when(
                respuestaAltaXmlBuilder.generarXml(
                        resultadoSoap
                )
        ).thenReturn(
                "<respuesta/>"
        );

        ResultadoEnvioVerifactu resultado =
                provider.enviarAlta(
                        registro
                );

        assertEquals(
                EstadoResultadoEnvioVerifactu
                        .REQUIERE_RECONCILIACION,
                resultado.estado()
        );

        verify(
                respuestaAltaValidator,
                never()
        ).esCoherente(
                any()
        );

        verify(
                resultadoEnvioMapper,
                never()
        ).convertir(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void respuestaConMasDeUnaLineaRequiereReconciliacion() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RegFactuSistemaFacturacion peticion =
                crearPeticion(
                        cabecera,
                        registroFactura
                );

        AeatSoapResultadoAlta resultadoSoap =
                new AeatSoapResultadoAlta(
                        "CSV-DOS-LINEAS",
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.PARCIALMENTE_CORRECTO,
                        List.of(
                                new RespuestaExpedidaType(),
                                new RespuestaExpedidaType()
                        )
                );

        prepararPeticion(
                registro,
                peticion
        );

        when(
                soapGateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenReturn(
                resultadoSoap
        );

        when(
                respuestaAltaXmlBuilder.generarXml(
                        resultadoSoap
                )
        ).thenReturn(
                "<respuesta/>"
        );

        ResultadoEnvioVerifactu resultado =
                provider.enviarAlta(
                        registro
                );

        assertEquals(
                EstadoResultadoEnvioVerifactu
                        .REQUIERE_RECONCILIACION,
                resultado.estado()
        );

        verify(
                respuestaAltaValidator,
                never()
        ).esCoherente(
                any()
        );

        verify(
                resultadoEnvioMapper,
                never()
        ).convertir(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void falloDuranteLaComunicacionSeConsideraIncierto() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RegFactuSistemaFacturacion peticion =
                crearPeticion(
                        cabecera,
                        registroFactura
                );

        prepararPeticion(
                registro,
                peticion
        );

        when(
                soapGateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenThrow(
                new IllegalStateException(
                        "Fallo SOAP simulado"
                )
        );

        assertThrows(
                EnvioVerifactuInciertoException.class,
                () -> provider.enviarAlta(
                        registro
                )
        );

        verify(
                resultadoEnvioMapper,
                never()
        ).convertir(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void respuestaSoapNulaSeConsideraIncierta() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RegFactuSistemaFacturacion peticion =
                crearPeticion(
                        cabecera,
                        registroFactura
                );

        prepararPeticion(
                registro,
                peticion
        );

        when(
                soapGateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenReturn(
                null
        );

        assertThrows(
                EnvioVerifactuInciertoException.class,
                () -> provider.enviarAlta(
                        registro
                )
        );

        verify(
                respuestaAltaXmlBuilder,
                never()
        ).generarXml(
                any()
        );
    }

    @Test
    void falloAlGenerarXmlDeRespuestaSeConsideraIncierto() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RegFactuSistemaFacturacion peticion =
                crearPeticion(
                        cabecera,
                        registroFactura
                );

        AeatSoapResultadoAlta resultadoSoap =
                new AeatSoapResultadoAlta(
                        "CSV-PRUEBA",
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of(
                                new RespuestaExpedidaType()
                        )
                );

        prepararPeticion(
                registro,
                peticion
        );

        when(
                soapGateway.enviarAlta(
                        cabecera,
                        registroFactura
                )
        ).thenReturn(
                resultadoSoap
        );

        when(
                respuestaAltaXmlBuilder.generarXml(
                        resultadoSoap
                )
        ).thenThrow(
                new IllegalStateException(
                        "No se pudo serializar"
                )
        );

        assertThrows(
                EnvioVerifactuInciertoException.class,
                () -> provider.enviarAlta(
                        registro
                )
        );
    }

    @Test
    void peticionConNumeroIncorrectoDeRegistrosNoLlamaAlGateway() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        RegFactuSistemaFacturacion peticion =
                new RegFactuSistemaFacturacion();

        peticion.setCabecera(
                new CabeceraType()
        );

        when(
                solicitudAltaXmlBuilder.construir(
                        registro
                )
        ).thenReturn(
                peticion
        );

        when(
                solicitudAltaXmlBuilder.generarXml(
                        registro
                )
        ).thenReturn(
                "<peticion/>"
        );

        assertThrows(
                IllegalStateException.class,
                () -> provider.enviarAlta(
                        registro
                )
        );

        verify(
                soapGateway,
                never()
        ).enviarAlta(
                any(),
                any()
        );
    }

    private RegFactuSistemaFacturacion crearPeticion(
            CabeceraType cabecera,
            RegistroFacturaType registroFactura) {

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

        return peticion;
    }

    private void prepararPeticion(
            RegistroFacturacion registro,
            RegFactuSistemaFacturacion peticion) {

        when(
                solicitudAltaXmlBuilder.construir(
                        registro
                )
        ).thenReturn(
                peticion
        );

        when(
                solicitudAltaXmlBuilder.generarXml(
                        registro
                )
        ).thenReturn(
                "<peticion/>"
        );
    }
}