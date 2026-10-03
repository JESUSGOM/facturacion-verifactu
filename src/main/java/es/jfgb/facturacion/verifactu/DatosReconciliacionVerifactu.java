package es.jfgb.facturacion.verifactu;

import java.time.LocalDate;

public record DatosReconciliacionVerifactu(

        String nombreRazonEmisor,

        String nifEmisor,

        String numSerieFactura,

        LocalDate fechaExpedicion) {
}