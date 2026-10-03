package es.jfgb.facturacion.verifactu.aeat.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatPreproduccionTlsHandshakeIT {

    private static final String HOST =
            "prewww1.aeat.es";

    private static final int PORT =
            443;

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void realizaHandshakeTlsConPreproduccionAeat()
            throws Exception {

        String huella =
                System.getenv(
                        "VERIFACTU_AEAT_CERT_SHA256"
                );

        AeatCertificadoWindowsSelector selector =
                new AeatCertificadoWindowsSelector(
                        huella
                );

        AeatSslContextFactory sslContextFactory =
                new AeatSslContextFactory(
                        selector
                );

        SSLContext sslContext =
                sslContextFactory.crearSslContext();

        try (SSLSocket socket =
                     (SSLSocket) sslContext
                             .getSocketFactory()
                             .createSocket()) {

            socket.connect(
                    new InetSocketAddress(
                            HOST,
                            PORT
                    ),
                    15000
            );

            socket.setSoTimeout(
                    15000
            );

            SSLParameters parameters =
                    socket.getSSLParameters();

            /*
             * Activa la comprobación del nombre del servidor
             * igual que en una conexión HTTPS normal.
             */
            parameters.setEndpointIdentificationAlgorithm(
                    "HTTPS"
            );

            parameters.setServerNames(
                    List.of(
                            new SNIHostName(
                                    HOST
                            )
                    )
            );

            socket.setSSLParameters(
                    parameters
            );

            /*
             * Esta es la única operación de red.
             *
             * No se construye XML VERI*FACTU.
             * No se invoca RegFactu.
             * No se envía ninguna factura.
             */
            socket.startHandshake();

            SSLSession session =
                    socket.getSession();

            assertTrue(
                    session.isValid()
            );

            assertNotNull(
                    session.getPeerPrincipal()
            );

            Certificate[] certificadosLocales =
                    session.getLocalCertificates();

            boolean certificadoClientePresentado =
                    certificadosLocales != null
                            && certificadosLocales.length > 0;

            System.out.println(
                    "AEAT_TLS_OK=true"
            );

            System.out.println(
                    "AEAT_TLS_PROTOCOLO="
                            + session.getProtocol()
            );

            System.out.println(
                    "AEAT_CERTIFICADO_CLIENTE_PRESENTADO="
                            + certificadoClientePresentado
            );
        }
    }
}