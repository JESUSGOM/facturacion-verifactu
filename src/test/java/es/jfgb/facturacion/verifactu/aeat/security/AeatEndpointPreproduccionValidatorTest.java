package es.jfgb.facturacion.verifactu.aeat.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AeatEndpointPreproduccionValidatorTest {

    @Test
    void aceptaEndpointPreproduccion() {
        assertDoesNotThrow(() ->
                AeatEndpointPreproduccionValidator.validar(
                        AeatEndpointPreproduccionValidator.ENDPOINT_PRE
                )
        );
    }

    @Test
    void rechazaProduccion() {
        rechazar(
                "https://www1.agenciatributaria.gob.es/"
                + "wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP"
        );
    }

    @Test
    void rechazaHttp() {
        rechazar(
                AeatEndpointPreproduccionValidator.ENDPOINT_PRE
                        .replace("https:", "http:")
        );
    }

    @Test
    void rechazaServidorParecido() {
        rechazar(
                "https://prewww1.aeat.es.ejemplo.com/"
                + "wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP"
        );
    }

    @Test
    void rechazaPuertoAlternativo() {
        rechazar(
                AeatEndpointPreproduccionValidator.ENDPOINT_PRE
                        .replace("aeat.es/", "aeat.es:8443/")
        );
    }

    @Test
    void rechazaQuery() {
        rechazar(
                AeatEndpointPreproduccionValidator.ENDPOINT_PRE
                        + "?modo=prueba"
        );
    }

    @Test
    void rechazaFragmento() {
        rechazar(
                AeatEndpointPreproduccionValidator.ENDPOINT_PRE
                        + "#otro"
        );
    }

    @Test
    void rechazaCredenciales() {
        rechazar(
                AeatEndpointPreproduccionValidator.ENDPOINT_PRE
                        .replace("https://", "https://usuario:clave@")
        );
    }

    @Test
    void rechazaRutaDiferente() {
        rechazar(
                AeatEndpointPreproduccionValidator.ENDPOINT_PRE
                        .replace("VerifactuSOAP", "RequerimientoSOAP")
        );
    }

    @Test
    void rechazaDireccionNulaOVacia() {
        rechazar(null);
        rechazar("");
        rechazar("   ");
    }

    private void rechazar(String endpoint) {
        assertThrows(
                IllegalStateException.class,
                () -> AeatEndpointPreproduccionValidator.validar(endpoint)
        );
    }
}
