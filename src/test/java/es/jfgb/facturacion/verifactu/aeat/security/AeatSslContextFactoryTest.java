package es.jfgb.facturacion.verifactu.aeat.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AeatSslContextFactoryTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void creaSslContextUsandoSoloElCertificadoConfigurado() {

        String huella =
                System.getenv(
                        "VERIFACTU_AEAT_CERT_SHA256"
                );

        AeatCertificadoWindowsSelector selector =
                new AeatCertificadoWindowsSelector(
                        huella
                );

        AeatCertificadoWindowsSelector.CertificadoSeleccionado seleccionado =
                selector.seleccionar();

        AeatSslContextFactory factory =
                new AeatSslContextFactory(
                        selector
                );

        X509ExtendedKeyManager keyManager =
                factory.crearKeyManagerSeleccionado();

        assertNotNull(
                keyManager
        );

        assertNotNull(
                keyManager.getPrivateKey(
                        seleccionado.alias()
                )
        );

        X509Certificate[] cadena =
                keyManager.getCertificateChain(
                        seleccionado.alias()
                );

        assertNotNull(
                cadena
        );

        X509Certificate certificado =
                cadena[0];

        String algoritmo =
                certificado
                        .getPublicKey()
                        .getAlgorithm();

        String aliasElegido =
                keyManager.chooseClientAlias(
                        new String[]{algoritmo},
                        null,
                        null
                );

        assertEquals(
                seleccionado.alias(),
                aliasElegido
        );

        assertNull(
                keyManager.chooseClientAlias(
                        new String[]{"ALGORITMO_INEXISTENTE"},
                        null,
                        null
                )
        );

        assertNull(
                keyManager.getPrivateKey(
                        "otro-certificado"
                )
        );

        SSLContext sslContext =
                factory.crearSslContext();

        assertNotNull(
                sslContext
        );

        assertNotNull(
                sslContext.getSocketFactory()
        );

        assertEquals(
                "TLS",
                sslContext.getProtocol()
        );
    }
}