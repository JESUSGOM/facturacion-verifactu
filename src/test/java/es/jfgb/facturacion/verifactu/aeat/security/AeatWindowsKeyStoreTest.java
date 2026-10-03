package es.jfgb.facturacion.verifactu.aeat.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatWindowsKeyStoreTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void puedeUsarLaClavePrivadaDelCertificadoSeleccionado() throws Exception {

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

        KeyStore keyStore =
                KeyStore.getInstance(
                        "Windows-MY"
                );

        keyStore.load(
                null,
                null
        );

        assertTrue(
                keyStore.isKeyEntry(
                        seleccionado.alias()
                )
        );

        Key key =
                keyStore.getKey(
                        seleccionado.alias(),
                        null
                );

        assertNotNull(
                key
        );

        assertInstanceOf(
                PrivateKey.class,
                key
        );

        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm()
                );

        keyManagerFactory.init(
                keyStore,
                null
        );

        boolean encontradoEnKeyManager =
                false;

        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {

            if (!(keyManager instanceof X509KeyManager x509KeyManager)) {
                continue;
            }

            PrivateKey privateKey =
                    x509KeyManager.getPrivateKey(
                            seleccionado.alias()
                    );

            if (privateKey != null) {
                encontradoEnKeyManager = true;
                break;
            }
        }

        assertTrue(
                encontradoEnKeyManager,
                "El KeyManager debe poder acceder a la clave privada del certificado seleccionado"
        );
    }
}