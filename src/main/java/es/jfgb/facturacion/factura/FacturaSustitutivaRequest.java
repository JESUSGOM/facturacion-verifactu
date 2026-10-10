package es.jfgb.facturacion.factura;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FacturaSustitutivaRequest(

        @NotNull
        Long clienteId,

        @NotNull
        Long serieId,

        @Size(max = 1000)
        List<@NotNull Long> facturasSustituidasIds,

        @Size(max = 1000)
        String observaciones,

        @NotEmpty
        List<@Valid FacturaLineaRequest> lineas
) {
}
