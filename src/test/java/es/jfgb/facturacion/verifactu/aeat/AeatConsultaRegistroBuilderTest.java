package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.LRFiltroRegFacturacionType;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AeatConsultaRegistroBuilderTest {

    private final AeatConsultaRegistroBuilder builder =
            new AeatConsultaRegistroBuilder();

    @Test
    void construyeConsultaDeUnaFacturaPorPeriodoYNumeroSerie() {

        ConsultaFactuSistemaFacturacionType consulta =
                builder.construir(
                        "Empresa de Prueba SL",
                        "B12345678",
                        "F2026-000123",
                        YearMonth.of(
                                2026,
                                10
                        )
                );

        assertNotNull(
                consulta
        );

        assertNotNull(
                consulta.getCabecera()
        );

        assertEquals(
                "1.0",
                consulta
                        .getCabecera()
                        .getIDVersion()
        );

        assertNotNull(
                consulta
                        .getCabecera()
                        .getObligadoEmision()
        );

        assertEquals(
                "Empresa de Prueba SL",
                consulta
                        .getCabecera()
                        .getObligadoEmision()
                        .getNombreRazon()
        );

        assertEquals(
                "B12345678",
                consulta
                        .getCabecera()
                        .getObligadoEmision()
                        .getNIF()
        );

        LRFiltroRegFacturacionType filtro =
                consulta.getFiltroConsulta();

        assertNotNull(
                filtro
        );

        assertNotNull(
                filtro.getPeriodoImputacion()
        );

        assertEquals(
                "2026",
                filtro
                        .getPeriodoImputacion()
                        .getEjercicio()
        );

        assertEquals(
                "10",
                filtro
                        .getPeriodoImputacion()
                        .getPeriodo()
        );

        assertEquals(
                "F2026-000123",
                filtro.getNumSerieFactura()
        );

        /*
         * Confirmamos que este builder no amplia
         * accidentalmente la busqueda.
         */
        assertNull(
                filtro.getContraparte()
        );

        assertNull(
                filtro.getFechaExpedicionFactura()
        );

        assertNull(
                filtro.getSistemaInformatico()
        );

        assertNull(
                filtro.getRefExterna()
        );

        assertNull(
                filtro.getClavePaginacion()
        );
    }

    @Test
    void normalizaEspaciosDeLosDatosDeIdentificacion() {

        ConsultaFactuSistemaFacturacionType consulta =
                builder.construir(
                        "  Empresa de Prueba SL  ",
                        "  B12345678  ",
                        "  SERIE-001  ",
                        YearMonth.of(
                                2026,
                                1
                        )
                );

        assertEquals(
                "Empresa de Prueba SL",
                consulta
                        .getCabecera()
                        .getObligadoEmision()
                        .getNombreRazon()
        );

        assertEquals(
                "B12345678",
                consulta
                        .getCabecera()
                        .getObligadoEmision()
                        .getNIF()
        );

        assertEquals(
                "SERIE-001",
                consulta
                        .getFiltroConsulta()
                        .getNumSerieFactura()
        );

        assertEquals(
                "01",
                consulta
                        .getFiltroConsulta()
                        .getPeriodoImputacion()
                        .getPeriodo()
        );
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {

        YearMonth periodo =
                YearMonth.of(
                        2026,
                        10
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.construir(
                        null,
                        "B12345678",
                        "F-1",
                        periodo
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.construir(
                        "Empresa",
                        " ",
                        "F-1",
                        periodo
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.construir(
                        "Empresa",
                        "B12345678",
                        "",
                        periodo
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.construir(
                        "Empresa",
                        "B12345678",
                        "F-1",
                        null
                )
        );
    }
}