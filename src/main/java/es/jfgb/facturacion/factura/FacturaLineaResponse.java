package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.empresa.TipoImpuesto;

import java.math.BigDecimal;

public record FacturaLineaResponse(
        Long id,
        Long productoId,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        Integer signo,
        TipoImpuesto tipoImpuesto,
        BigDecimal porcentajeImpuesto,
        BigDecimal baseImponible,
        BigDecimal cuotaImpuesto,
        BigDecimal total,
        Integer orden
) {
}
