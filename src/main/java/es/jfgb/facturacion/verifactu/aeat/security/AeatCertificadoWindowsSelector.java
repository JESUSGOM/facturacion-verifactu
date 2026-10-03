package es.jfgb.facturacion.verifactu.aeat.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.Locale;

@Component
@Profile("aeat-pre & !dev")
public class AeatCertificadoWindowsSelector {

    private static final String WINDOWS_MY = "Windows-MY";

    private final String huellaSha256Esperada;

    public AeatCertificadoWindowsSelector(
            @Value("${verifactu.aeat.certificado.sha256:}")
            String huellaSha256) {

        this.huellaSha256Esperada =
                normalizarHuella(
                        huellaSha256
                );
    }

    public CertificadoSeleccionado seleccionar() {

        if (huellaSha256Esperada.isBlank()) {
            throw new IllegalStateException(
                    "No se ha configurado la huella SHA-256 del certificado AEAT"
            );
        }

        try {

            KeyStore keyStore =
                    KeyStore.getInstance(
                            WINDOWS_MY
                    );

            keyStore.load(
                    null,
                    null
            );

            Enumeration<String> aliases =
                    keyStore.aliases();

            while (aliases.hasMoreElements()) {

                String alias =
                        aliases.nextElement();

                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }

                Certificate certificado =
                        keyStore.getCertificate(
                                alias
                        );

                if (!(certificado instanceof X509Certificate x509)) {
                    continue;
                }

                String huellaActual =
                        calcularHuellaSha256(
                                x509
                        );

                if (!huellaSha256Esperada.equals(huellaActual)) {
                    continue;
                }

                x509.checkValidity();

                return new CertificadoSeleccionado(
                        alias,
                        x509,
                        huellaActual
                );
            }

            throw new IllegalStateException(
                    "No se ha encontrado en Windows-MY un certificado "
                            + "con clave privada que coincida con la huella SHA-256 configurada"
            );

        } catch (IllegalStateException e) {

            throw e;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo acceder o validar el certificado del almacén Windows-MY",
                    e
            );
        }
    }

    static String normalizarHuella(
            String huella) {

        if (huella == null || huella.isBlank()) {
            return "";
        }

        String normalizada =
                huella
                        .replace(":", "")
                        .replace("-", "")
                        .replaceAll("\\s+", "")
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!normalizada.matches("[0-9A-F]{64}")) {
            throw new IllegalArgumentException(
                    "La huella SHA-256 del certificado debe contener exactamente 64 caracteres hexadecimales"
            );
        }

        return normalizada;
    }

    private static String calcularHuellaSha256(
            X509Certificate certificado)
            throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance(
                        "SHA-256"
                );

        byte[] huella =
                digest.digest(
                        certificado.getEncoded()
                );

        return HexFormat
                .of()
                .withUpperCase()
                .formatHex(
                        huella
                );
    }

    public record CertificadoSeleccionado(
            String alias,
            X509Certificate certificado,
            String huellaSha256) {
    }
}