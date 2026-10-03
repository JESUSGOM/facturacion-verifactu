package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuellaVerifactuServiceTest {

    @Test
    void debeCalcularLaHuellaDelEjemploOficialAeat() {

        HuellaVerifactuService servicio =
                new HuellaVerifactuService();

        String huella = servicio.calcularAlta(
                "89890001K",
                "12345678/G33",
                LocalDate.of(2024, 1, 1),
                "F1",
                new BigDecimal("12.35"),
                new BigDecimal("123.45"),
                null,
                OffsetDateTime.parse(
                        "2024-01-01T19:20:30+01:00"
                )
        );

        assertEquals(
                "3C464DAF61ACB827C65FDA19F352A4E3" +
                        "BDC2C640E9E9FC4CC058073F38F12F60",
                huella
        );
    }

    @Test
    void debeConservarLosSegundosAunqueSeanCero() {

        HuellaVerifactuService servicio =
                new HuellaVerifactuService();

        String fecha = servicio.formatearFechaHora(
                OffsetDateTime.parse(
                        "2026-10-01T07:45:00+01:00"
                )
        );

        assertEquals(
                "2026-10-01T07:45:00+01:00",
                fecha
        );
    }
}