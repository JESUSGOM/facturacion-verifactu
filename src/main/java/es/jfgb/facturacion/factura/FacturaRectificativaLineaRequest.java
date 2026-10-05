package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.empresa.TipoImpuesto;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FacturaRectificativaLineaRequest(

        Long productoId,

        @NotBlank
        @Size(max = 500)
        String descripcion,

        @NotNull
        @DecimalMin(value = "0.0001")
        @Digits(integer = 11, fraction = 4)
        BigDecimal cantidad,

        @NotNull
        @DecimalMin(value = "0.0000")
        @Digits(integer = 11, fraction = 4)
        BigDecimal precioUnitario,

        @NotNull
        TipoImpuesto tipoImpuesto,

        @NotNull
        @DecimalMin(value = "0.00")
        @DecimalMax(value = "100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal porcentajeImpuesto,

        @NotNull
        Integer signo
) {
}