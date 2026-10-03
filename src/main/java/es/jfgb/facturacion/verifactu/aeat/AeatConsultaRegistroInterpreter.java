package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.EstadoRegFactuType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.IndicadorPaginacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RegistroRespuestaConsultaRegFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.ResultadoConsultaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.IDFacturaExpedidaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AeatConsultaRegistroInterpreter {

    public ResultadoConsultaRegistroAeat interpretar(
            RespuestaConsultaFactuSistemaFacturacionType respuesta,
            String nifEmisorEsperado,
            String numeroSerieEsperado,
            String fechaExpedicionEsperada) {

        validarTexto(
                nifEmisorEsperado,
                "El NIF esperado es obligatorio"
        );

        validarTexto(
                numeroSerieEsperado,
                "El numero o serie esperado es obligatorio"
        );

        validarTexto(
                fechaExpedicionEsperada,
                "La fecha de expedicion esperada es obligatoria"
        );

        /*
         * Una ausencia total de respuesta nunca puede utilizarse
         * para modificar el estado fiscal local.
         */
        if (respuesta == null) {
            return ResultadoConsultaRegistroAeat
                    .noConcluyente(
                            0
                    );
        }

        ResultadoConsultaType resultadoConsulta =
                respuesta.getResultadoConsulta();

        IndicadorPaginacionType indicadorPaginacion =
                respuesta.getIndicadorPaginacion();

        if (resultadoConsulta == null
                || indicadorPaginacion == null) {

            return ResultadoConsultaRegistroAeat
                    .noConcluyente(
                            0
                    );
        }

        List<RegistroRespuestaConsultaRegFacturacionType> registros =
                respuesta
                        .getRegistroRespuestaConsultaFactuSistemaFacturacion();

        /*
         * SIN_DATOS solo es definitivo cuando:
         *
         * - no existen registros en la respuesta;
         * - AEAT indica que no hay más páginas.
         */
        if (resultadoConsulta
                == ResultadoConsultaType.SIN_DATOS) {

            if (!registros.isEmpty()
                    || indicadorPaginacion
                    == IndicadorPaginacionType.S) {

                return ResultadoConsultaRegistroAeat
                        .noConcluyente(
                                registros.size()
                        );
            }

            return ResultadoConsultaRegistroAeat
                    .noEncontrado();
        }

        if (resultadoConsulta
                != ResultadoConsultaType.CON_DATOS) {

            return ResultadoConsultaRegistroAeat
                    .noConcluyente(
                            registros.size()
                    );
        }

        List<RegistroRespuestaConsultaRegFacturacionType>
                coincidencias =
                new ArrayList<>();

        for (RegistroRespuestaConsultaRegFacturacionType registro
                : registros) {

            if (coincide(
                    registro,
                    nifEmisorEsperado,
                    numeroSerieEsperado,
                    fechaExpedicionEsperada
            )) {

                coincidencias.add(
                        registro
                );
            }
        }

        /*
         * Si existen más páginas no tomamos una decisión
         * definitiva, aunque esta página contenga una coincidencia.
         *
         * Es deliberadamente conservador.
         */
        if (indicadorPaginacion
                == IndicadorPaginacionType.S) {

            return ResultadoConsultaRegistroAeat
                    .noConcluyente(
                            coincidencias.size()
                    );
        }

        if (coincidencias.isEmpty()) {

            return ResultadoConsultaRegistroAeat
                    .noEncontrado();
        }

        if (coincidencias.size() > 1) {

            return ResultadoConsultaRegistroAeat
                    .ambiguo(
                            coincidencias.size()
                    );
        }

        RegistroRespuestaConsultaRegFacturacionType registro =
                coincidencias.get(0);

        EstadoRegFactuType estado =
                registro.getEstadoRegistro();

        /*
         * Hemos localizado la factura, pero necesitamos además
         * un estado AEAT válido para considerar la respuesta
         * utilizable en reconciliación.
         */
        if (estado == null
                || estado.getEstadoRegistro() == null) {

            return ResultadoConsultaRegistroAeat
                    .noConcluyente(
                            1
                    );
        }

        String estadoRegistroAeat =
                estado
                        .getEstadoRegistro()
                        .name();

        String codigoError =
                estado.getCodigoErrorRegistro() == null
                        ? null
                        : estado
                                .getCodigoErrorRegistro()
                                .toString();

        String descripcionError =
                normalizar(
                        estado.getDescripcionErrorRegistro()
                );

        return ResultadoConsultaRegistroAeat
                .encontrado(
                        estadoRegistroAeat,
                        codigoError,
                        descripcionError
                );
    }

    private boolean coincide(
            RegistroRespuestaConsultaRegFacturacionType registro,
            String nifEmisorEsperado,
            String numeroSerieEsperado,
            String fechaExpedicionEsperada) {

        if (registro == null) {
            return false;
        }

        IDFacturaExpedidaType idFactura =
                registro.getIDFactura();

        if (idFactura == null) {
            return false;
        }

        return iguales(
                idFactura.getIDEmisorFactura(),
                nifEmisorEsperado
        )
                && iguales(
                idFactura.getNumSerieFactura(),
                numeroSerieEsperado
        )
                && iguales(
                idFactura.getFechaExpedicionFactura(),
                fechaExpedicionEsperada
        );
    }

    private boolean iguales(
            String izquierda,
            String derecha) {

        String valorIzquierda =
                normalizar(
                        izquierda
                );

        String valorDerecha =
                normalizar(
                        derecha
                );

        return valorIzquierda != null
                && valorIzquierda.equals(
                valorDerecha
        );
    }

    private String normalizar(
            String valor) {

        if (valor == null
                || valor.isBlank()) {

            return null;
        }

        return valor.trim();
    }

    private void validarTexto(
            String valor,
            String mensaje) {

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }
}