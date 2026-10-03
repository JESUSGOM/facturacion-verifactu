package es.jfgb.facturacion.factura;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record FacturaResponse(
        Long id,
        Long empresaId,
        Long clienteId,
        Long serieId,
        String serieCodigo,

        EstadoFactura estado,
        Long numero,
        LocalDate fechaExpedicion,

        String observaciones,

        BigDecimal baseImponible,
        BigDecimal cuotaImpuesto,
        BigDecimal total,

        String clienteNif,
        String clienteNombre,
        String clienteDireccion,
        String clienteCodigoPostal,
        String clienteMunicipio,
        String clienteProvincia,
        String clienteCodigoPais,

        Instant fechaCreacion,
        Instant fechaModificacion,
        Instant fechaEmision,

        List<FacturaLineaResponse> lineas
) {
}