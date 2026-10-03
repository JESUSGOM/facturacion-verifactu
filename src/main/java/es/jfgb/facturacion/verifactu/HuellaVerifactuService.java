package es.jfgb.facturacion.verifactu;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

@Service
public class HuellaVerifactuService {

    private static final DateTimeFormatter FECHA_FACTURA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd'T'HH:mm:ssXXX"
            );

    public String calcularAlta(
            String nifEmisor,
            String numSerieFactura,
            LocalDate fechaExpedicion,
            String tipoFactura,
            BigDecimal cuotaTotal,
            BigDecimal importeTotal,
            String huellaAnterior,
            OffsetDateTime fechaHoraGeneracion) {

        String cadena = construirCadenaAlta(
                nifEmisor,
                numSerieFactura,
                fechaExpedicion,
                tipoFactura,
                cuotaTotal,
                importeTotal,
                huellaAnterior,
                fechaHoraGeneracion
        );

        return calcularSha256(cadena);
    }


    public String formatearFechaHora(
            OffsetDateTime fechaHoraGeneracion) {

        return fechaHoraGeneracion.format(FECHA_HORA);
    }

    public String construirCadenaAlta(
            String nifEmisor,
            String numSerieFactura,
            LocalDate fechaExpedicion,
            String tipoFactura,
            BigDecimal cuotaTotal,
            BigDecimal importeTotal,
            String huellaAnterior,
            OffsetDateTime fechaHoraGeneracion) {

        return "IDEmisorFactura="
                + limpiar(nifEmisor)

                + "&NumSerieFactura="
                + limpiar(numSerieFactura)

                + "&FechaExpedicionFactura="
                + fechaExpedicion.format(FECHA_FACTURA)

                + "&TipoFactura="
                + limpiar(tipoFactura)

                + "&CuotaTotal="
                + importe(cuotaTotal)

                + "&ImporteTotal="
                + importe(importeTotal)

                + "&Huella="
                + limpiar(huellaAnterior)

                + "&FechaHoraHusoGenRegistro="
                + fechaHoraGeneracion.format(FECHA_HORA);
    }

    private String calcularSha256(String cadena) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    cadena.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of()
                    .withUpperCase()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "No está disponible el algoritmo SHA-256",
                    e
            );
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String importe(BigDecimal valor) {

        if (valor == null) {
            return "";
        }

        return valor
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

}