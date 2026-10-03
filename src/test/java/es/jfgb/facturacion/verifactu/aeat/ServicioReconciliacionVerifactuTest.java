package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.jfgb.facturacion.verifactu.DatosReconciliacionVerifactu;
import es.jfgb.facturacion.verifactu.EstadoReconciliacionVerifactuService;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioReconciliacionVerifactuTest {

    @Test
    void correctaResuelveInciertoComoAceptado() {

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

        DatosReconciliacionVerifactu datos =
                datos();

        ConsultaFactuSistemaFacturacionType consulta =
                new ConsultaFactuSistemaFacturacionType();

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        ResultadoConsultaRegistroAeat resultado =
                ResultadoConsultaRegistroAeat.encontrado(
                        "CORRECTA",
                        null,
                        null
                );

        when(
                estadoService.prepararConsulta(
                        1L,
                        10L
                )
        ).thenReturn(
                datos
        );

        when(
                builder.construir(
                        datos.nombreRazonEmisor(),
                        datos.nifEmisor(),
                        datos.numSerieFactura(),
                        YearMonth.of(
                                2026,
                                10
                        )
                )
        ).thenReturn(
                consulta
        );

        when(
                gateway.consultar(
                        consulta
                )
        ).thenReturn(
                respuesta
        );

        when(
                interpreter.interpretar(
                        respuesta,
                        "B12345678",
                        "F2026-000123",
                        "03-10-2026"
                )
        ).thenReturn(
                resultado
        );

        ServicioReconciliacionVerifactu service =
                new ServicioReconciliacionVerifactu(
                        estadoService,
                        builder,
                        gateway,
                        interpreter
                );

        ResultadoConsultaRegistroAeat obtenido =
                service.reconciliar(
                        1L,
                        10L
                );

        assertSame(
                resultado,
                obtenido
        );

        verify(
                estadoService
        ).marcarAceptadoSiSigueIncierto(
                1L,
                10L
        );
    }

    @Test
    void aceptadaConErroresTambienResuelveComoAceptado() {

        ejecutarYComprobarEstadoAceptado(
                "ACEPTADA_CON_ERRORES"
        );
    }

    @Test
    void noEncontradoNoCambiaElEstadoLocal() {

        comprobarQueNoResuelveAutomaticamente(
                ResultadoConsultaRegistroAeat
                        .noEncontrado()
        );
    }

    @Test
    void anuladaNoCambiaElEstadoLocal() {

        comprobarQueNoResuelveAutomaticamente(
                ResultadoConsultaRegistroAeat
                        .encontrado(
                                "ANULADA",
                                null,
                                null
                        )
        );
    }

    private void ejecutarYComprobarEstadoAceptado(
            String estadoAeat) {

        EstadoReconciliacionVerifactuService estadoService =
                mock(
                        EstadoReconciliacionVerifactuService.class
                );

        AeatConsultaRegistroBuilder builder =
                new AeatConsultaRegistroBuilder();

        AeatSoapGateway gateway =
                mock(
                        AeatSoapGateway.class
                );

        AeatConsultaRegistroInterpreter interpreter =
                mock(
                        AeatConsultaRegistroInterpreter.class
                );

        DatosReconciliacionVerifactu datos =
                datos();

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        when(
                estadoService.prepararConsulta(
                        1L,
                        10L
                )
        ).thenReturn(
                datos
        );

        when(
                gateway.consultar(
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(
                respuesta
        );

        when(
                interpreter.interpretar(
                        respuesta,
                        "B12345678",
                        "F2026-000123",
                        "03-10-2026"
                )
        ).thenReturn(
                ResultadoConsultaRegistroAeat.encontrado(
                        estadoAeat,
                        null,
                        null
                )
        );

        ServicioReconciliacionVerifactu service =
                new ServicioReconciliacionVerifactu(
                        estadoService,
                        builder,
                        gateway,
                        interpreter
                );

        service.reconciliar(
                1L,
                10L
        );

        verify(
                estadoService
        ).marcarAceptadoSiSigueIncierto(
                1L,
                10L
        );
    }

    private void comprobarQueNoResuelveAutomaticamente(
            ResultadoConsultaRegistroAeat resultado) {

        EstadoReconciliacionVerifactuService estadoService =
                mock(
                        EstadoReconciliacionVerifactuService.class
                );

        AeatConsultaRegistroBuilder builder =
                new AeatConsultaRegistroBuilder();

        AeatSoapGateway gateway =
                mock(
                        AeatSoapGateway.class
                );

        AeatConsultaRegistroInterpreter interpreter =
                mock(
                        AeatConsultaRegistroInterpreter.class
                );

        DatosReconciliacionVerifactu datos =
                datos();

        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                new RespuestaConsultaFactuSistemaFacturacionType();

        when(
                estadoService.prepararConsulta(
                        1L,
                        10L
                )
        ).thenReturn(
                datos
        );

        when(
                gateway.consultar(
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(
                respuesta
        );

        when(
                interpreter.interpretar(
                        respuesta,
                        "B12345678",
                        "F2026-000123",
                        "03-10-2026"
                )
        ).thenReturn(
                resultado
        );

        ServicioReconciliacionVerifactu service =
                new ServicioReconciliacionVerifactu(
                        estadoService,
                        builder,
                        gateway,
                        interpreter
                );

        service.reconciliar(
                1L,
                10L
        );

        verify(
                estadoService,
                never()
        ).marcarAceptadoSiSigueIncierto(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    private DatosReconciliacionVerifactu datos() {

        return new DatosReconciliacionVerifactu(
                "Empresa de Prueba SL",
                "B12345678",
                "F2026-000123",
                LocalDate.of(
                        2026,
                        10,
                        3
                )
        );
    }
}