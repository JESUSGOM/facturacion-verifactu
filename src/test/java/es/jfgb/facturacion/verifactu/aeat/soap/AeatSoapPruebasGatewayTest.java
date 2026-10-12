package es.jfgb.facturacion.verifactu.aeat.soap;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoEnvioType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaRegFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.DatosPresentacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AeatSoapPruebasGatewayTest {

    private SfPortTypeVerifactu port;
    private AeatSoapPruebasGateway gateway;

    @BeforeEach
    void setUp() {

        port =
                mock(
                        SfPortTypeVerifactu.class
                );

        gateway =
                new AeatSoapPruebasGateway(
                        port,
                        () -> {}
                );
    }

    @Test
    void enviaPeticionBareYMapeaRespuestaCompleta() {

        CabeceraType cabeceraEntrada =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        CabeceraType cabeceraRespuesta =
                new CabeceraType();

        DatosPresentacionType datosPresentacion =
                new DatosPresentacionType();

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        RespuestaRegFactuSistemaFacturacionType respuesta =
                new RespuestaRegFactuSistemaFacturacionType();

        respuesta.setCSV(
                "CSV-PRUEBA"
        );

        respuesta.setDatosPresentacion(
                datosPresentacion
        );

        respuesta.setCabecera(
                cabeceraRespuesta
        );

        respuesta.setTiempoEsperaEnvio(
                "60"
        );

        respuesta.setEstadoEnvio(
                EstadoEnvioType.CORRECTO
        );

        respuesta
                .getRespuestaLinea()
                .add(
                        respuestaLinea
                );

        when(
                port.regFactuSistemaFacturacion(
                        any(
                                RegFactuSistemaFacturacion.class
                        )
                )
        ).thenReturn(
                respuesta
        );

        AeatSoapResultadoAlta resultado =
                gateway.enviarAlta(
                        cabeceraEntrada,
                        registroFactura
                );

        assertNotNull(
                resultado
        );

        assertEquals(
                "CSV-PRUEBA",
                resultado.csv()
        );

        assertSame(
                datosPresentacion,
                resultado.datosPresentacion()
        );

        assertSame(
                cabeceraRespuesta,
                resultado.cabecera()
        );

        assertEquals(
                "60",
                resultado.tiempoEsperaEnvio()
        );

        assertEquals(
                EstadoEnvioType.CORRECTO,
                resultado.estadoEnvio()
        );

        assertEquals(
                1,
                resultado.respuestasLinea().size()
        );

        assertSame(
                respuestaLinea,
                resultado.respuestasLinea().get(0)
        );

        ArgumentCaptor<RegFactuSistemaFacturacion> captor =
                ArgumentCaptor.forClass(
                        RegFactuSistemaFacturacion.class
                );

        verify(
                port
        ).regFactuSistemaFacturacion(
                captor.capture()
        );

        RegFactuSistemaFacturacion peticionEnviada =
                captor.getValue();

        assertSame(
                cabeceraEntrada,
                peticionEnviada.getCabecera()
        );

        assertEquals(
                1,
                peticionEnviada
                        .getRegistroFactura()
                        .size()
        );

        assertSame(
                registroFactura,
                peticionEnviada
                        .getRegistroFactura()
                        .get(0)
        );
    }

    @Test
    void respuestaSinLineasDevuelveListaVacia() {

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        RespuestaRegFactuSistemaFacturacionType respuesta =
                new RespuestaRegFactuSistemaFacturacionType();

        respuesta.setCabecera(
                new CabeceraType()
        );

        respuesta.setTiempoEsperaEnvio(
                "60"
        );

        respuesta.setEstadoEnvio(
                EstadoEnvioType.INCORRECTO
        );

        when(
                port.regFactuSistemaFacturacion(
                        any(
                                RegFactuSistemaFacturacion.class
                        )
                )
        ).thenReturn(
                respuesta
        );

        AeatSoapResultadoAlta resultado =
                gateway.enviarAlta(
                        cabecera,
                        registroFactura
                );

        assertNotNull(
                resultado
        );

        assertEquals(
                0,
                resultado.respuestasLinea().size()
        );

        assertEquals(
                EstadoEnvioType.INCORRECTO,
                resultado.estadoEnvio()
        );
    }

    @Test
    void respuestaNulaDevuelveNull() {

        CabeceraType cabecera =
                new CabeceraType();

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        when(
                port.regFactuSistemaFacturacion(
                        any(
                                RegFactuSistemaFacturacion.class
                        )
                )
        ).thenReturn(
                null
        );

        AeatSoapResultadoAlta resultado =
                gateway.enviarAlta(
                        cabecera,
                        registroFactura
                );

        assertNull(
                resultado
        );
    }

    @Test
    void rechazaCabeceraNula() {

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        assertThrows(
                IllegalArgumentException.class,
                () -> gateway.enviarAlta(
                        null,
                        registroFactura
                )
        );
    }

    @Test
    void rechazaRegistroFacturaNulo() {

        CabeceraType cabecera =
                new CabeceraType();

        assertThrows(
                IllegalArgumentException.class,
                () -> gateway.enviarAlta(
                        cabecera,
                        null
                )
        );
    }

    @Test
    void rechazaPuertoSoapNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new AeatSoapPruebasGateway(
                        null
                )
        );
    }
}