package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.jfgb.facturacion.verifactu.DatosReconciliacionVerifactu;
import es.jfgb.facturacion.verifactu.EstadoReconciliacionVerifactuService;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Orquestador de reconciliacion AEAT.
 *
 * No abre ninguna transaccion alrededor de la llamada SOAP.
 *
 * Solo se registra cuando esta activo exclusivamente
 * el perfil real de preproduccion AEAT.
 */
@Service
@Profile("aeat-pre & !dev")
public class ServicioReconciliacionVerifactu {

    private static final DateTimeFormatter
            FORMATO_FECHA_AEAT =
            DateTimeFormatter.ofPattern(
                    "dd-MM-yyyy"
            );

    private final EstadoReconciliacionVerifactuService estadoService;
    private final AeatConsultaRegistroBuilder consultaBuilder;
    private final AeatSoapGateway soapGateway;
    private final AeatConsultaRegistroInterpreter interpreter;

    public ServicioReconciliacionVerifactu(
            EstadoReconciliacionVerifactuService estadoService,
            AeatConsultaRegistroBuilder consultaBuilder,
            AeatSoapGateway soapGateway,
            AeatConsultaRegistroInterpreter interpreter) {

        this.estadoService = estadoService;
        this.consultaBuilder = consultaBuilder;
        this.soapGateway = soapGateway;
        this.interpreter = interpreter;
    }

    public ResultadoConsultaRegistroAeat reconciliar(
            Long empresaId,
            Long registroId) {

        /*
         * Primera transaccion:
         * valida el estado y toma un snapshot de consulta.
         */
        DatosReconciliacionVerifactu datos =
                estadoService.prepararConsulta(
                        empresaId,
                        registroId
                );

        YearMonth periodo =
                YearMonth.from(
                        datos.fechaExpedicion()
                );

        ConsultaFactuSistemaFacturacionType consulta =
                consultaBuilder.construir(
                        datos.nombreRazonEmisor(),
                        datos.nifEmisor(),
                        datos.numSerieFactura(),
                        periodo
                );

        /*
         * LLAMADA EXTERNA.
         *
         * No existe una transaccion de base de datos abierta
         * por este orquestador.
         */
        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                soapGateway.consultar(
                        consulta
                );

        ResultadoConsultaRegistroAeat resultado =
                interpreter.interpretar(
                        respuesta,
                        datos.nifEmisor(),
                        datos.numSerieFactura(),
                        datos.fechaExpedicion()
                                .format(
                                        FORMATO_FECHA_AEAT
                                )
                );

        /*
         * Criterio deliberadamente conservador:
         *
         * Solo resolvemos automaticamente como ACEPTADO
         * cuando AEAT contiene exactamente la factura y
         * su estado significa que permanece almacenada.
         *
         * NO_ENCONTRADO, AMBIGUO, ANULADA o una respuesta
         * no concluyente mantienen INCIERTO.
         */
        if (resultado.estado()
                == EstadoConsultaRegistroAeat.ENCONTRADO
                && esEstadoAceptado(
                resultado.estadoRegistroAeat()
        )) {

            estadoService
                    .marcarAceptadoSiSigueIncierto(
                            empresaId,
                            registroId
                    );
        }

        return resultado;
    }

    private boolean esEstadoAceptado(
            String estadoAeat) {

        if (estadoAeat == null
                || estadoAeat.isBlank()) {

            return false;
        }

        String normalizado =
                estadoAeat
                        .trim()
                        .replace(
                                "_",
                                ""
                        )
                        .toUpperCase(
                                Locale.ROOT
                        );

        return normalizado.equals(
                "CORRECTA"
        )
                || normalizado.equals(
                "ACEPTADACONERRORES"
        );
    }
}