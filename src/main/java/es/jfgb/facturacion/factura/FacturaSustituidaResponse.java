package es.jfgb.facturacion.factura;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaSustituidaResponse(

        Long facturaId,

        Long serieId,

        String serieCodigo,

        Long numero,

        LocalDate fechaExpedicion,

        BigDecimal baseImponible,

        BigDecimal cuotaImpuesto,

        BigDecimal total
) {
}
