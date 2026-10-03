package es.jfgb.facturacion.verifactu.aeat.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AeatCertificadoWindowsSelectorTest {

    @Test
    void normalizaHuellaSha256() {

        String original =
                "aa:bb:cc:dd:ee:ff:00:11:"
                        + "22:33:44:55:66:77:88:99:"
                        + "aa:bb:cc:dd:ee:ff:00:11:"
                        + "22:33:44:55:66:77:88:99";

        String normalizada =
                AeatCertificadoWindowsSelector
                        .normalizarHuella(
                                original
                        );

        assertEquals(
                "AABBCCDDEEFF00112233445566778899"
                        + "AABBCCDDEEFF00112233445566778899",
                normalizada
        );
    }

    @Test
    void rechazaHuellaSha256Invalida() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new AeatCertificadoWindowsSelector(
                        "NO-ES-UNA-HUELLA"
                )
        );
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void seleccionaElCertificadoRealConfiguradoEnWindows() {

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

        assertNotNull(
                seleccionado
        );

        assertNotNull(
                seleccionado.certificado()
        );

        assertFalse(
                seleccionado.alias().isBlank()
        );

        assertEquals(
                AeatCertificadoWindowsSelector.normalizarHuella(
                        huella
                ),
                seleccionado.huellaSha256()
        );
    }
}