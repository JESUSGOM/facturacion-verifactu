package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoEnvioType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaRegFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.DatosPresentacionType;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatRespuestaAltaXmlBuilderTest {

    private AeatRespuestaAltaXmlBuilder builder;

    @BeforeEach
    void setUp() {

        builder =
                new AeatRespuestaAltaXmlBuilder();
    }

    @Test
    void construyeRespuestaCompleta() {

        CabeceraType cabecera =
                new CabeceraType();

        DatosPresentacionType datosPresentacion =
                new DatosPresentacionType();

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.CORRECTO
        );

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        "CSV-PRUEBA",
                        datosPresentacion,
                        cabecera,
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of(
                                respuestaLinea
                        )
                );

        RespuestaRegFactuSistemaFacturacionType respuesta =
                builder.construir(
                        resultado
                );

        assertEquals(
                "CSV-PRUEBA",
                respuesta.getCSV()
        );

        assertSame(
                datosPresentacion,
                respuesta.getDatosPresentacion()
        );

        assertSame(
                cabecera,
                respuesta.getCabecera()
        );

        assertEquals(
                "60",
                respuesta.getTiempoEsperaEnvio()
        );

        assertEquals(
                EstadoEnvioType.CORRECTO,
                respuesta.getEstadoEnvio()
        );

        assertEquals(
                1,
                respuesta.getRespuestaLinea().size()
        );

        assertSame(
                respuestaLinea,
                respuesta.getRespuestaLinea().get(0)
        );
    }

    @Test
    void generaXmlConLosDatosPrincipales() {

        CabeceraType cabecera =
                new CabeceraType();

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.CORRECTO
        );

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        "CSV-XML-PRUEBA",
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of(
                                respuestaLinea
                        )
                );

        String xml =
                builder.generarXml(
                        resultado
                );

        assertTrue(
                xml.contains(
                        "RespuestaRegFactuSistemaFacturacion"
                )
        );

        assertTrue(
                xml.contains(
                        "CSV-XML-PRUEBA"
                )
        );

        assertTrue(
                xml.contains(
                        "TiempoEsperaEnvio"
                )
        );

        assertTrue(
                xml.contains(
                        ">60<"
                )
        );

        assertTrue(
                xml.contains(
                        "EstadoEnvio"
                )
        );

        assertTrue(
                xml.contains(
                        "Correcto"
                )
        );

        assertTrue(
                xml.contains(
                        "RespuestaLinea"
                )
        );
    }

    @Test
    void admiteCsvYDatosPresentacionNulos() {

        CabeceraType cabecera =
                new CabeceraType();

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        cabecera,
                        "60",
                        EstadoEnvioType.INCORRECTO,
                        List.of()
                );

        RespuestaRegFactuSistemaFacturacionType respuesta =
                builder.construir(
                        resultado
                );

        assertEquals(
                null,
                respuesta.getCSV()
        );

        assertEquals(
                null,
                respuesta.getDatosPresentacion()
        );

        assertSame(
                cabecera,
                respuesta.getCabecera()
        );

        assertEquals(
                EstadoEnvioType.INCORRECTO,
                respuesta.getEstadoEnvio()
        );

        assertEquals(
                0,
                respuesta.getRespuestaLinea().size()
        );
    }

    @Test
    void rechazaResultadoNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.construir(
                        null
                )
        );
    }

    @Test
    void rechazaCabeceraNula() {

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        null,
                        "60",
                        EstadoEnvioType.INCORRECTO,
                        List.of()
                );

        assertThrows(
                IllegalStateException.class,
                () -> builder.construir(
                        resultado
                )
        );
    }

    @Test
    void rechazaTiempoEsperaNulo() {

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        new CabeceraType(),
                        null,
                        EstadoEnvioType.INCORRECTO,
                        List.of()
                );

        assertThrows(
                IllegalStateException.class,
                () -> builder.construir(
                        resultado
                )
        );
    }

    @Test
    void rechazaEstadoEnvioNulo() {

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        new CabeceraType(),
                        "60",
                        null,
                        List.of()
                );

        assertThrows(
                IllegalStateException.class,
                () -> builder.construir(
                        resultado
                )
        );
    }
}