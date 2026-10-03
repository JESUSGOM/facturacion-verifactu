package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.LRFiltroRegFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraConsultaSf;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.ObligadoEmisionConsultaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PeriodoImputacionType;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
public class AeatConsultaRegistroBuilder {

    private static final String VERSION = "1.0";

    public ConsultaFactuSistemaFacturacionType construir(
            String nombreRazon,
            String nif,
            String numeroSerieFactura,
            YearMonth periodoImputacion) {

        validarTexto(
                nombreRazon,
                "El nombre o razon social del obligado es obligatorio"
        );

        validarTexto(
                nif,
                "El NIF del obligado es obligatorio"
        );

        validarTexto(
                numeroSerieFactura,
                "El numero o serie de factura es obligatorio"
        );

        if (periodoImputacion == null) {
            throw new IllegalArgumentException(
                    "El periodo de imputacion es obligatorio"
            );
        }

        /*
         * =====================================================
         * CABECERA
         * =====================================================
         */

        CabeceraConsultaSf cabecera =
                new CabeceraConsultaSf();

        cabecera.setIDVersion(
                VERSION
        );

        ObligadoEmisionConsultaType obligado =
                new ObligadoEmisionConsultaType();

        obligado.setNombreRazon(
                nombreRazon.trim()
        );

        obligado.setNIF(
                nif.trim()
        );

        cabecera.setObligadoEmision(
                obligado
        );

        /*
         * =====================================================
         * PERIODO DE IMPUTACION
         * =====================================================
         */

        PeriodoImputacionType periodo =
                new PeriodoImputacionType();

        periodo.setEjercicio(
                Integer.toString(
                        periodoImputacion.getYear()
                )
        );

        periodo.setPeriodo(
                String.format(
                        "%02d",
                        periodoImputacion.getMonthValue()
                )
        );

        /*
         * =====================================================
         * FILTRO DE FACTURA
         * =====================================================
         *
         * NumSerieFactura representa:
         *
         *     Serie + numero de factura del emisor
         *
         * No utilizamos de momento contraparte, fecha,
         * sistema informatico ni referencia externa.
         */

        LRFiltroRegFacturacionType filtro =
                new LRFiltroRegFacturacionType();

        filtro.setPeriodoImputacion(
                periodo
        );

        filtro.setNumSerieFactura(
                numeroSerieFactura.trim()
        );

        /*
         * =====================================================
         * PETICION
         * =====================================================
         */

        ConsultaFactuSistemaFacturacionType consulta =
                new ConsultaFactuSistemaFacturacionType();

        consulta.setCabecera(
                cabecera
        );

        consulta.setFiltroConsulta(
                filtro
        );

        return consulta;
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