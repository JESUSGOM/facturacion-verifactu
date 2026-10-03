package es.jfgb.facturacion.verifactu;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record RegistroFacturacionResponse(

        Long id,
        Long empresaId,
        Long facturaId,

        TipoRegistroFacturacion tipoRegistro,

        String nifEmisor,

        String serie,
        Long numero,
        String numSerieFactura,

        LocalDate fechaExpedicion,
        String tipoFactura,

        BigDecimal cuotaTotal,
        BigDecimal importeTotal,

        Long registroAnteriorId,
        String huellaAnterior,

        String fechaHoraGeneracion,
        String huella,

        EstadoEnvioRegistro estadoEnvio,

        String proveedor,
        String identificadorExterno,
        Integer intentosEnvio,
        Instant fechaUltimoIntento,
        Instant fechaEnvio,
        Instant fechaCreacion

) {
}