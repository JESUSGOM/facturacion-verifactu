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

        TipoFactura tipoFactura,
        TipoRectificativa tipoRectificativa,
        String motivoRectificacion,
        BigDecimal baseRectificada,
        BigDecimal cuotaRectificada,

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

        Instant fechaAnulacion,
        String motivoAnulacion,

        List<FacturaRectificadaResponse> facturasRectificadas,

        List<FacturaSustituidaResponse> facturasSustituidas,

        List<FacturaLineaResponse> lineas
) {

    /**
     * Constructor de compatibilidad anterior a F3.
     *
     * Mantiene compatibles los consumidores que ya enviaban
     * la lista de facturas rectificadas.
     */
    public FacturaResponse(

            Long id,
            Long empresaId,
            Long clienteId,
            Long serieId,
            String serieCodigo,

            EstadoFactura estado,
            Long numero,
            LocalDate fechaExpedicion,

            TipoFactura tipoFactura,
            TipoRectificativa tipoRectificativa,
            String motivoRectificacion,
            BigDecimal baseRectificada,
            BigDecimal cuotaRectificada,

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

            Instant fechaAnulacion,
            String motivoAnulacion,

            List<FacturaRectificadaResponse> facturasRectificadas,

            List<FacturaLineaResponse> lineas) {

        this(
                id,
                empresaId,
                clienteId,
                serieId,
                serieCodigo,

                estado,
                numero,
                fechaExpedicion,

                tipoFactura,
                tipoRectificativa,
                motivoRectificacion,
                baseRectificada,
                cuotaRectificada,

                observaciones,

                baseImponible,
                cuotaImpuesto,
                total,

                clienteNif,
                clienteNombre,
                clienteDireccion,
                clienteCodigoPostal,
                clienteMunicipio,
                clienteProvincia,
                clienteCodigoPais,

                fechaCreacion,
                fechaModificacion,
                fechaEmision,

                fechaAnulacion,
                motivoAnulacion,

                facturasRectificadas,

                List.of(),

                lineas
        );
    }

    /**
     * Constructor de compatibilidad para facturas ordinarias.
     *
     * Mantiene compatibles los tests y consumidores anteriores
     * a la incorporacion de facturas rectificativas.
     */
    public FacturaResponse(

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

            Instant fechaAnulacion,
            String motivoAnulacion,

            List<FacturaLineaResponse> lineas) {

        this(
                id,
                empresaId,
                clienteId,
                serieId,
                serieCodigo,

                estado,
                numero,
                fechaExpedicion,

                TipoFactura.F1,
                null,
                null,
                null,
                null,

                observaciones,

                baseImponible,
                cuotaImpuesto,
                total,

                clienteNif,
                clienteNombre,
                clienteDireccion,
                clienteCodigoPostal,
                clienteMunicipio,
                clienteProvincia,
                clienteCodigoPais,

                fechaCreacion,
                fechaModificacion,
                fechaEmision,

                fechaAnulacion,
                motivoAnulacion,

                List.of(),

                List.of(),

                lineas
        );
    }
}
