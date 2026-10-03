package es.jfgb.facturacion.verifactu.aeat.security;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.LRFiltroRegFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraConsultaSf;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.ObligadoEmisionConsultaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PeriodoImputacionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AeatPreproduccionConsultaIT {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void realizaConsultaRealEnPreproduccionAeat() {

        String huella =
                System.getenv(
                        "VERIFACTU_AEAT_CERT_SHA256"
                );

        String nombreObligado =
                System.getenv(
                        "AEAT_TEST_OBLIGADO_NOMBRE"
                );

        String nifObligado =
                System.getenv(
                        "AEAT_TEST_OBLIGADO_NIF"
                );

        assertNotNull(
                nombreObligado,
                "Falta AEAT_TEST_OBLIGADO_NOMBRE"
        );

        assertFalse(
                nombreObligado.isBlank(),
                "AEAT_TEST_OBLIGADO_NOMBRE está vacío"
        );

        assertNotNull(
                nifObligado,
                "Falta AEAT_TEST_OBLIGADO_NIF"
        );

        assertFalse(
                nifObligado.isBlank(),
                "AEAT_TEST_OBLIGADO_NIF está vacío"
        );

        /*
         * Certificado cliente Windows-MY.
         */
        AeatCertificadoWindowsSelector selector =
                new AeatCertificadoWindowsSelector(
                        huella
                );

        AeatSslContextFactory sslContextFactory =
                new AeatSslContextFactory(
                        selector
                );

        AeatSoapTlsConfigurer tlsConfigurer =
                new AeatSoapTlsConfigurer(
                        sslContextFactory
                );

        /*
         * Puerto SOAP oficial de PREPRODUCCIÓN.
         */
        SfVerifactu servicio =
                new SfVerifactu();

        SfPortTypeVerifactu port =
                servicio.getSistemaVerifactuPruebas();

        tlsConfigurer.configurar(
                port
        );

        /*
         * Cabecera de consulta.
         */
        CabeceraConsultaSf cabecera =
                new CabeceraConsultaSf();

        cabecera.setIDVersion(
                "1.0"
        );

        ObligadoEmisionConsultaType obligado =
                new ObligadoEmisionConsultaType();

        obligado.setNombreRazon(
                nombreObligado
        );

        obligado.setNIF(
                nifObligado
        );

        cabecera.setObligadoEmision(
                obligado
        );

        /*
         * Consultamos el mes actual.
         *
         * No indicamos factura concreta ni contraparte.
         */
        YearMonth ahora =
                YearMonth.now();

        PeriodoImputacionType periodo =
                new PeriodoImputacionType();

        periodo.setEjercicio(
                Integer.toString(
                        ahora.getYear()
                )
        );

        periodo.setPeriodo(
                String.format(
                        "%02d",
                        ahora.getMonthValue()
                )
        );

        LRFiltroRegFacturacionType filtro =
                new LRFiltroRegFacturacionType();

        filtro.setPeriodoImputacion(
                periodo
        );

        ConsultaFactuSistemaFacturacionType consulta =
                new ConsultaFactuSistemaFacturacionType();

        consulta.setCabecera(
                cabecera
        );

        consulta.setFiltroConsulta(
                filtro
        );

        /*
         * PRIMERA llamada SOAP real de aplicación.
         *
         * Es ConsultaFactuSistemaFacturacion.
         *
         * NO se invoca RegFactuSistemaFacturacion.
         */
        RespuestaConsultaFactuSistemaFacturacionType respuesta =
                port.consultaFactuSistemaFacturacion(
                        consulta
                );

        assertNotNull(
                respuesta,
                "AEAT no ha devuelto una respuesta de consulta"
        );

        int numeroRegistros =
                respuesta
                        .getRegistroRespuestaConsultaFactuSistemaFacturacion()
                        .size();

        /*
         * No mostramos NIF ni nombre en consola.
         */
        System.out.println(
                "AEAT_CONSULTA_OK=true"
        );

        System.out.println(
                "AEAT_CONSULTA_EJERCICIO="
                        + periodo.getEjercicio()
        );

        System.out.println(
                "AEAT_CONSULTA_PERIODO="
                        + periodo.getPeriodo()
        );

        System.out.println(
                "AEAT_CONSULTA_NUMERO_REGISTROS="
                        + numeroRegistros
        );
    }
}