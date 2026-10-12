package es.jfgb.facturacion.verifactu.aeat.security;

import java.net.URI;

public final class AeatEndpointPreproduccionValidator {

    public static final String ENDPOINT_PRE =
            "https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/"
            + "SistemaFacturacion/VerifactuSOAP";

    private AeatEndpointPreproduccionValidator() {
    }

    public static void validar(String endpoint) {

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException(
                    "No se ha configurado el endpoint AEAT PRE"
            );
        }

        final URI uri;

        try {
            uri = URI.create(endpoint);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "El endpoint AEAT tiene una URI invalida",
                    e
            );
        }

        if (!uri.isAbsolute()
                || !ENDPOINT_PRE.equals(endpoint)) {

            throw new IllegalStateException(
                    "Endpoint AEAT no autorizado para preproduccion"
            );
        }
    }
}
