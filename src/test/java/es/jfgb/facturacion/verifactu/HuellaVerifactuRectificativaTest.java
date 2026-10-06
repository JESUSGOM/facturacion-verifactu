package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuellaVerifactuRectificativaTest {

    private final HuellaVerifactuService servicio =
            new HuellaVerifactuService();

    private static final String HUELLA_ANTERIOR =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                    + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    @Test
    void calculaHuellaR1PorDiferencias() {

        LocalDate fecha =
                LocalDate.of(
                        2026,
                        10,
                        5
                );

        OffsetDateTime fechaHora =
                OffsetDateTime.parse(
                        "2026-10-05T15:00:00+01:00"
                );

        String cadenaEsperada =
                "IDEmisorFactura=B12345678"
                        + "&NumSerieFactura=RECT-I-1"
                        + "&FechaExpedicionFactura=05-10-2026"
                        + "&TipoFactura=R1"
                        + "&CuotaTotal=-1.40"
                        + "&ImporteTotal=-21.40"
                        + "&Huella="
                        + HUELLA_ANTERIOR
                        + "&FechaHoraHusoGenRegistro="
                        + "2026-10-05T15:00:00+01:00";

        String cadena =
                servicio.construirCadenaAlta(
                        "B12345678",
                        "RECT-I-1",
                        fecha,
                        "R1",
                        new BigDecimal("-1.40"),
                        new BigDecimal("-21.40"),
                        HUELLA_ANTERIOR,
                        fechaHora
                );

        assertEquals(
                cadenaEsperada,
                cadena
        );

        String huella =
                servicio.calcularAlta(
                        "B12345678",
                        "RECT-I-1",
                        fecha,
                        "R1",
                        new BigDecimal("-1.40"),
                        new BigDecimal("-21.40"),
                        HUELLA_ANTERIOR,
                        fechaHora
                );

        assertEquals(
                "9CEBFB4180B3D3DAB502ABDA834061F6"
                        + "1206F3B1B16F6FCB768A74B8C13EEA2D",
                huella
        );
    }

    @Test
    void calculaHuellaR1PorSustitucion() {

        LocalDate fecha =
                LocalDate.of(
                        2026,
                        10,
                        5
                );

        OffsetDateTime fechaHora =
                OffsetDateTime.parse(
                        "2026-10-05T15:00:00+01:00"
                );

        String cadenaEsperada =
                "IDEmisorFactura=B12345678"
                        + "&NumSerieFactura=RECT-S-1"
                        + "&FechaExpedicionFactura=05-10-2026"
                        + "&TipoFactura=R1"
                        + "&CuotaTotal=5.60"
                        + "&ImporteTotal=85.60"
                        + "&Huella="
                        + HUELLA_ANTERIOR
                        + "&FechaHoraHusoGenRegistro="
                        + "2026-10-05T15:00:00+01:00";

        String cadena =
                servicio.construirCadenaAlta(
                        "B12345678",
                        "RECT-S-1",
                        fecha,
                        "R1",
                        new BigDecimal("5.60"),
                        new BigDecimal("85.60"),
                        HUELLA_ANTERIOR,
                        fechaHora
                );

        assertEquals(
                cadenaEsperada,
                cadena
        );

        String huella =
                servicio.calcularAlta(
                        "B12345678",
                        "RECT-S-1",
                        fecha,
                        "R1",
                        new BigDecimal("5.60"),
                        new BigDecimal("85.60"),
                        HUELLA_ANTERIOR,
                        fechaHora
                );

        assertEquals(
                "78CF00286CB585DF5179B3723CFDB4A2"
                        + "801C1B34E2D5FAD0C38778549B954F9A",
                huella
        );
    }
}