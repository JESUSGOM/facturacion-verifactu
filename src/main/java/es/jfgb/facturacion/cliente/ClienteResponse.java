package es.jfgb.facturacion.cliente;

import java.time.Instant;

public record ClienteResponse(
        Long id,
        Long empresaId,
        String nif,
        String nombre,
        String nombreComercial,
        String direccion,
        String codigoPostal,
        String municipio,
        String provincia,
        String codigoPais,
        String telefono,
        String email,
        boolean activo,
        Instant fechaCreacion,
        Instant fechaModificacion
) {
}