package es.jfgb.facturacion.factura;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FacturaRectificativaRequest(

        @NotNull
        Long serieId,

        @NotNull
        TipoFactura tipoFactura,

        @NotNull
        TipoRectificativa tipoRectificativa,

        @NotNull
        @Size(min = 1, max = 500)
        String motivoRectificacion,

        @NotEmpty
        List<@NotNull Long> facturasRectificadasIds,

        @Size(max = 1000)
        String observaciones,

        @NotEmpty
        List<@Valid FacturaRectificativaLineaRequest> lineas
) {
}