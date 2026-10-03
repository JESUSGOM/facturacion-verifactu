package es.jfgb.facturacion.producto;

import es.jfgb.facturacion.empresa.TipoImpuesto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductoResponse(
        Long id,
        Long empresaId,
        String codigo,
        TipoProducto tipo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        TipoImpuesto tipoImpuesto,
        BigDecimal porcentajeImpuesto,
        boolean activo,
        Instant fechaCreacion,
        Instant fechaModificacion
) {
}