package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoEnvioType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatRespuestaAltaValidatorTest {

    private AeatRespuestaAltaValidator validator;

    @BeforeEach
    void setUp() {

        validator =
                new AeatRespuestaAltaValidator();
    }

    @Test
    void correctoConGlobalCorrectoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                crearResultado(
                        EstadoEnvioType.CORRECTO,
                        EstadoRegistroType.CORRECTO
                );

        assertTrue(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void aceptadoConErroresConGlobalParcialEsCoherente() {

        AeatSoapResultadoAlta resultado =
                crearResultado(
                        EstadoEnvioType.PARCIALMENTE_CORRECTO,
                        EstadoRegistroType.ACEPTADO_CON_ERRORES
                );

        assertTrue(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void incorrectoConGlobalIncorrectoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                crearResultado(
                        EstadoEnvioType.INCORRECTO,
                        EstadoRegistroType.INCORRECTO
                );

        assertTrue(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void correctoConGlobalIncorrectoNoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                crearResultado(
                        EstadoEnvioType.INCORRECTO,
                        EstadoRegistroType.CORRECTO
                );

        assertFalse(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void aceptadoConErroresConGlobalCorrectoNoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                crearResultado(
                        EstadoEnvioType.CORRECTO,
                        EstadoRegistroType.ACEPTADO_CON_ERRORES
                );

        assertFalse(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void incorrectoConGlobalParcialNoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                crearResultado(
                        EstadoEnvioType.PARCIALMENTE_CORRECTO,
                        EstadoRegistroType.INCORRECTO
                );

        assertFalse(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void resultadoNuloNoEsCoherente() {

        assertFalse(
                validator.esCoherente(
                        null
                )
        );
    }

    @Test
    void estadoGlobalNuloNoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        new CabeceraType(),
                        "60",
                        null,
                        List.of(
                                crearRespuesta(
                                        EstadoRegistroType.CORRECTO
                                )
                        )
                );

        assertFalse(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void sinLineasNoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        new CabeceraType(),
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of()
                );

        assertFalse(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void variasLineasNoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        new CabeceraType(),
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of(
                                crearRespuesta(
                                        EstadoRegistroType.CORRECTO
                                ),
                                crearRespuesta(
                                        EstadoRegistroType.CORRECTO
                                )
                        )
                );

        assertFalse(
                validator.esCoherente(
                        resultado
                )
        );
    }

    @Test
    void estadoDeLineaNuloNoEsCoherente() {

        AeatSoapResultadoAlta resultado =
                new AeatSoapResultadoAlta(
                        null,
                        null,
                        new CabeceraType(),
                        "60",
                        EstadoEnvioType.CORRECTO,
                        List.of(
                                new RespuestaExpedidaType()
                        )
                );

        assertFalse(
                validator.esCoherente(
                        resultado
                )
        );
    }

    private AeatSoapResultadoAlta crearResultado(
            EstadoEnvioType estadoGlobal,
            EstadoRegistroType estadoRegistro) {

        return new AeatSoapResultadoAlta(
                "CSV-PRUEBA",
                null,
                new CabeceraType(),
                "60",
                estadoGlobal,
                List.of(
                        crearRespuesta(
                                estadoRegistro
                        )
                )
        );
    }

    private RespuestaExpedidaType crearRespuesta(
            EstadoRegistroType estadoRegistro) {

        RespuestaExpedidaType respuesta =
                new RespuestaExpedidaType();

        respuesta.setEstadoRegistro(
                estadoRegistro
        );

        return respuesta;
    }
}