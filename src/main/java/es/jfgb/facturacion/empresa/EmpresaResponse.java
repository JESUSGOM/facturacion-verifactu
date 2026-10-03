package es.jfgb.facturacion.empresa;

import java.time.Instant;

public record EmpresaResponse(
        Long id,
        String nif,
        String razonSocial,
        String nombreComercial,
        String direccion,
        String codigoPostal,
        String municipio,
        String provincia,
        String codigoPais,
        String telefono,
        String email,
        TipoImpuesto tipoImpuesto,
        boolean activa,
        Instant fechaCreacion,
        Instant fechaModificacion
) {
}