package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroDuplicadoType;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AeatRespuestaAltaInterpreterTest {

    private AeatRespuestaAltaInterpreter interpreter;

    @BeforeEach
    void setUp() {

        interpreter =
                new AeatRespuestaAltaInterpreter();
    }

    @Test
    void correctoSeInterpretaComoAceptado() {

        RespuestaExpedidaType respuesta =
                new RespuestaExpedidaType();

        respuesta.setEstadoRegistro(
                EstadoRegistroType.CORRECTO
        );

        assertEquals(
                EstadoResultadoEnvioVerifactu.ACEPTADO,
                interpreter.interpretar(respuesta)
        );
    }

    @Test
    void aceptadoConErroresSeInterpretaComoAceptadoConErrores() {

        RespuestaExpedidaType respuesta =
                new RespuestaExpedidaType();

        respuesta.setEstadoRegistro(
                EstadoRegistroType.ACEPTADO_CON_ERRORES
        );

        assertEquals(
                EstadoResultadoEnvioVerifactu
                        .ACEPTADO_CON_ERRORES,
                interpreter.interpretar(respuesta)
        );
    }

    @Test
    void incorrectoSinDuplicadoSeInterpretaComoRechazado() {

        RespuestaExpedidaType respuesta =
                new RespuestaExpedidaType();

        respuesta.setEstadoRegistro(
                EstadoRegistroType.INCORRECTO
        );

        assertEquals(
                EstadoResultadoEnvioVerifactu.RECHAZADO,
                interpreter.interpretar(respuesta)
        );
    }

    @Test
    void incorrectoConDuplicadoRequiereReconciliacion() {

        RespuestaExpedidaType respuesta =
                new RespuestaExpedidaType();

        respuesta.setEstadoRegistro(
                EstadoRegistroType.INCORRECTO
        );

        respuesta.setRegistroDuplicado(
                new RegistroDuplicadoType()
        );

        assertEquals(
                EstadoResultadoEnvioVerifactu
                        .REQUIERE_RECONCILIACION,
                interpreter.interpretar(respuesta)
        );
    }
}