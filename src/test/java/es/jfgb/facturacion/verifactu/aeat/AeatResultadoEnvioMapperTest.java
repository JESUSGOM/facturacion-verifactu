package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.EstadoRegistroSFType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroDuplicadoType;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatResultadoEnvioMapperTest {

    private AeatResultadoEnvioMapper mapper;

    @BeforeEach
    void setUp() {

        mapper =
                new AeatResultadoEnvioMapper(
                        new AeatRespuestaAltaInterpreter()
                );
    }

    @Test
    void conservaCsvPeticionRespuestaYError() {

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.ACEPTADO_CON_ERRORES
        );

        respuestaLinea.setCodigoErrorRegistro(
                BigInteger.valueOf(1234)
        );

        respuestaLinea.setDescripcionErrorRegistro(
                "Advertencia de prueba"
        );

        ResultadoEnvioVerifactu resultado =
                mapper.convertir(
                        "CSV-PRUEBA",
                        "<peticion/>",
                        "<respuesta/>",
                        respuestaLinea
                );

        assertEquals(
                EstadoResultadoEnvioVerifactu
                        .ACEPTADO_CON_ERRORES,
                resultado.estado()
        );

        assertEquals(
                "CSV-PRUEBA",
                resultado.identificadorExterno()
        );

        assertEquals(
                "<peticion/>",
                resultado.peticion()
        );

        assertEquals(
                "<respuesta/>",
                resultado.respuesta()
        );

        assertTrue(
                resultado.mensaje()
                        .contains("1234")
        );

        assertTrue(
                resultado.mensaje()
                        .contains("Advertencia de prueba")
        );
    }

    @Test
    void incorrectoSinDuplicadoQuedaRechazado() {

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.INCORRECTO
        );

        respuestaLinea.setCodigoErrorRegistro(
                BigInteger.valueOf(2001)
        );

        respuestaLinea.setDescripcionErrorRegistro(
                "Registro rechazado de prueba"
        );

        ResultadoEnvioVerifactu resultado =
                mapper.convertir(
                        null,
                        "<peticion/>",
                        "<respuesta/>",
                        respuestaLinea
                );

        assertEquals(
                EstadoResultadoEnvioVerifactu.RECHAZADO,
                resultado.estado()
        );

        assertEquals(
                null,
                resultado.identificadorExterno()
        );

        assertTrue(
                resultado.mensaje()
                        .contains("2001")
        );

        assertTrue(
                resultado.mensaje()
                        .contains(
                                "Registro rechazado de prueba"
                        )
        );
    }

    @Test
    void duplicadoConservaInformacionParaReconciliacion() {

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.INCORRECTO
        );

        respuestaLinea.setCodigoErrorRegistro(
                BigInteger.valueOf(3000)
        );

        respuestaLinea.setDescripcionErrorRegistro(
                "Duplicado de prueba"
        );

        RegistroDuplicadoType duplicado =
                new RegistroDuplicadoType();

        duplicado.setIdPeticionRegistroDuplicado(
                "PETICION-ANTERIOR"
        );

        duplicado.setEstadoRegistroDuplicado(
                EstadoRegistroSFType.CORRECTA
        );

        duplicado.setCodigoErrorRegistro(
                BigInteger.valueOf(99)
        );

        duplicado.setDescripcionErrorRegistro(
                "Información del registro anterior"
        );

        respuestaLinea.setRegistroDuplicado(
                duplicado
        );

        ResultadoEnvioVerifactu resultado =
                mapper.convertir(
                        "CSV-DUPLICADO",
                        "<peticion/>",
                        "<respuesta/>",
                        respuestaLinea
                );

        assertEquals(
                EstadoResultadoEnvioVerifactu
                        .REQUIERE_RECONCILIACION,
                resultado.estado()
        );

        assertEquals(
                "CSV-DUPLICADO",
                resultado.identificadorExterno()
        );

        assertTrue(
                resultado.mensaje()
                        .contains(
                                "PETICION-ANTERIOR"
                        )
        );

        assertTrue(
                resultado.mensaje()
                        .contains(
                                "Correcta"
                        )
        );

        assertTrue(
                resultado.mensaje()
                        .contains(
                                "Información del registro anterior"
                        )
        );
    }

    @Test
    void csvVacioSeConvierteEnNull() {

        RespuestaExpedidaType respuestaLinea =
                new RespuestaExpedidaType();

        respuestaLinea.setEstadoRegistro(
                EstadoRegistroType.CORRECTO
        );

        ResultadoEnvioVerifactu resultado =
                mapper.convertir(
                        "   ",
                        "<peticion/>",
                        "<respuesta/>",
                        respuestaLinea
                );

        assertEquals(
                EstadoResultadoEnvioVerifactu.ACEPTADO,
                resultado.estado()
        );

        assertEquals(
                null,
                resultado.identificadorExterno()
        );
    }
}