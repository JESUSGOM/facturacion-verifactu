package es.jfgb.facturacion.factura;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnulacionFacturaRequest(

        @NotBlank(
                message = "Debe indicar el motivo de la anulacion"
        )
        @Size(
                max = 500,
                message = "El motivo no puede superar 500 caracteres"
        )
        String motivo,

        @AssertTrue(
                message = "Debe confirmar que no procede una factura rectificativa"
        )
        boolean confirmarQueNoProcedeRectificativa

) {
}