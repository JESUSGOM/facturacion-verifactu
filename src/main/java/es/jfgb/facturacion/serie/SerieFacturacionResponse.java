package es.jfgb.facturacion.serie;

import java.time.Instant;

public record SerieFacturacionResponse(
        Long id,
        Long empresaId,
        String codigo,
        String descripcion,
        TipoSerieFacturacion tipoSerie,
        Long ultimoNumero,
        boolean activa,
        Instant fechaCreacion,
        Instant fechaModificacion
) {
}