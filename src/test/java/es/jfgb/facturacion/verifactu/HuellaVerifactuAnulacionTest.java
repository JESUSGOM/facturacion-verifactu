package es.jfgb.facturacion.verifactu;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuellaVerifactuAnulacionTest {

    @Test
    void calculaHuellaAnulacionSegunEjemploOficialAeat() {

        HuellaVerifactuService service =
                new HuellaVerifactuService();

        String huellaAnterior =
                "F7B94CFD8924EDFF273501B01EE5153E4CE8F259766F88CF6ACB8935802A2B97";

        OffsetDateTime fechaHora =
                OffsetDateTime.parse(
                        "2024-01-01T19:20:40+01:00"
                );

        String cadena =
                service.construirCadenaAnulacion(
                        "89890001K",
                        "12345679/G34",
                        LocalDate.of(
                                2024,
                                1,
                                1
                        ),
                        huellaAnterior,
                        fechaHora
                );

        assertEquals(
                "IDEmisorFacturaAnulada=89890001K"
                        + "&NumSerieFacturaAnulada=12345679/G34"
                        + "&FechaExpedicionFacturaAnulada=01-01-2024"
                        + "&Huella="
                        + huellaAnterior
                        + "&FechaHoraHusoGenRegistro="
                        + "2024-01-01T19:20:40+01:00",
                cadena
        );

        String huella =
                service.calcularAnulacion(
                        "89890001K",
                        "12345679/G34",
                        LocalDate.of(
                                2024,
                                1,
                                1
                        ),
                        huellaAnterior,
                        fechaHora
                );

        assertEquals(
                "177547C0D57AC74748561D054A9CEC14B4C4EA23D1BEFD6F2E69E3A388F90C68",
                huella
        );
    }
}