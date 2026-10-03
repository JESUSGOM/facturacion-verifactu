package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.EstadoRegFactuType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.IndicadorPaginacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RegistroRespuestaConsultaRegFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.ResultadoConsultaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.IDFacturaExpedidaType;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatConsultaRegistroInterpreterTest {

    private static final String NIF =
            "B12345678";

    private static final String NUMERO_SERIE =
            "F2026-000123";

    private static final String FECHA =
            "03-10-2026";

    private final AeatConsultaRegistroInterpreter interpreter =
            new AeatConsultaRegistroInterpreter();

    @Test
    void encuentraUnaUnicaFacturaEnRespuestaCompleta() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                respuestaConDatos();

        RegistroRespuestaConsultaRegFacturacionType registro =
                registro(
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        EstadoRegFactuType estado =
                new EstadoRegFactuType();

        EstadoRegistroType estadoAeat =
                EstadoRegistroType
                        .values()[0];

        estado.setEstadoRegistro(
                estadoAeat
        );

        estado.setCodigoErrorRegistro(
                BigInteger.valueOf(
                        123
                )
        );

        estado.setDescripcionErrorRegistro(
                " Aviso de prueba "
        );

        registro.setEstadoRegistro(
                estado
        );

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registro
                );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.ENCONTRADO,
                resultado.estado()
        );

        assertEquals(
                estadoAeat.name(),
                resultado.estadoRegistroAeat()
        );

        assertEquals(
                "123",
                resultado.codigoErrorRegistro()
        );

        assertEquals(
                "Aviso de prueba",
                resultado.descripcionErrorRegistro()
        );

        assertEquals(
                1,
                resultado.numeroCoincidencias()
        );

        assertTrue(
                resultado.esConcluyente()
        );
    }

    @Test
    void sinDatosYRespuestaCompletaSignificaNoEncontrado() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        respuesta.setResultadoConsulta(
                ResultadoConsultaType.SIN_DATOS
        );

        respuesta.setIndicadorPaginacion(
                IndicadorPaginacionType.N
        );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.NO_ENCONTRADO,
                resultado.estado()
        );

        assertEquals(
                0,
                resultado.numeroCoincidencias()
        );

        assertNull(
                resultado.estadoRegistroAeat()
        );

        assertTrue(
                resultado.esConcluyente()
        );
    }

    @Test
    void conDatosPeroFacturaDistintaSignificaNoEncontrado() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                respuestaConDatos();

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registro(
                                NIF,
                                "OTRA-FACTURA",
                                FECHA
                        )
                );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.NO_ENCONTRADO,
                resultado.estado()
        );
    }

    @Test
    void dosCoincidenciasExactasSonAmbiguas() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                respuestaConDatos();

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registro(
                                NIF,
                                NUMERO_SERIE,
                                FECHA
                        )
                );

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registro(
                                NIF,
                                NUMERO_SERIE,
                                FECHA
                        )
                );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.AMBIGUO,
                resultado.estado()
        );

        assertEquals(
                2,
                resultado.numeroCoincidencias()
        );

        assertFalse(
                resultado.esConcluyente()
        );
    }

    @Test
    void respuestaPaginadaNuncaSeConsideraDefinitiva() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                respuestaConDatos();

        respuesta.setIndicadorPaginacion(
                IndicadorPaginacionType.S
        );

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registro(
                                NIF,
                                NUMERO_SERIE,
                                FECHA
                        )
                );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.RESPUESTA_NO_CONCLUYENTE,
                resultado.estado()
        );

        assertFalse(
                resultado.esConcluyente()
        );
    }

    @Test
    void sinDatosConRegistrosEsContradictorio() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        respuesta.setResultadoConsulta(
                ResultadoConsultaType.SIN_DATOS
        );

        respuesta.setIndicadorPaginacion(
                IndicadorPaginacionType.N
        );

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registro(
                                NIF,
                                NUMERO_SERIE,
                                FECHA
                        )
                );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.RESPUESTA_NO_CONCLUYENTE,
                resultado.estado()
        );
    }

    @Test
    void facturaEncontradaSinEstadoAeatNoEsConcluyente() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                respuestaConDatos();

        respuesta
                .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                .add(
                        registro(
                                NIF,
                                NUMERO_SERIE,
                                FECHA
                        )
                );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.RESPUESTA_NO_CONCLUYENTE,
                resultado.estado()
        );

        assertEquals(
                1,
                resultado.numeroCoincidencias()
        );
    }

    @Test
    void respuestaNulaNoEsConcluyente() {

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        null,
                        NIF,
                        NUMERO_SERIE,
                        FECHA
                );

        assertEquals(
                EstadoConsultaRegistroAeat.RESPUESTA_NO_CONCLUYENTE,
                resultado.estado()
        );

        assertFalse(
                resultado.esConcluyente()
        );
    }

    private RespuestaConsultaFactuSistemaFacturacionType
    respuestaConDatos() {

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        respuesta.setResultadoConsulta(
                ResultadoConsultaType.CON_DATOS
        );

        respuesta.setIndicadorPaginacion(
                IndicadorPaginacionType.N
        );

        return respuesta;
    }

    private RegistroRespuestaConsultaRegFacturacionType registro(
            String nif,
            String numeroSerie,
            String fecha) {

        IDFacturaExpedidaType idFactura =
                new IDFacturaExpedidaType();

        idFactura.setIDEmisorFactura(
                nif
        );

        idFactura.setNumSerieFactura(
                numeroSerie
        );

        idFactura.setFechaExpedicionFactura(
                fecha
        );

        RegistroRespuestaConsultaRegFacturacionType registro =
                new RegistroRespuestaConsultaRegFacturacionType();

        registro.setIDFactura(
                idFactura
        );

        return registro;
    }
}