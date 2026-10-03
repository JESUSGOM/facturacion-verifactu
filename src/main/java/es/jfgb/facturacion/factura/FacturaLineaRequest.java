package es.jfgb.facturacion.factura;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FacturaLineaRequest(

        @NotNull
        Long productoId,

        @NotNull
        @DecimalMin(value = "0.0001")
        @Digits(integer = 11, fraction = 4)
        BigDecimal cantidad
) {
}