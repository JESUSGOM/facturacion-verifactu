package es.jfgb.facturacion.verifactu.aeat.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Locale;

@Component
@Profile("aeat-pre & !dev")
public class AeatSslContextFactory {

    private static final String WINDOWS_MY = "Windows-MY";

    private final AeatCertificadoWindowsSelector certificadoSelector;

    public AeatSslContextFactory(
            AeatCertificadoWindowsSelector certificadoSelector) {

        this.certificadoSelector = certificadoSelector;
    }

    public SSLContext crearSslContext() {

        try {

            X509ExtendedKeyManager keyManager =
                    crearKeyManagerSeleccionado();

            SSLContext sslContext =
                    SSLContext.getInstance(
                            "TLS"
                    );

            /*
             * trustManagers = null:
             * Java utiliza la configuración de confianza
             * predeterminada del entorno.
             *
             * No estamos desactivando ninguna validación TLS.
             */
            sslContext.init(
                    new KeyManager[]{keyManager},
                    null,
                    null
            );

            return sslContext;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo crear el SSLContext para la comunicación con AEAT",
                    e
            );
        }
    }

    X509ExtendedKeyManager crearKeyManagerSeleccionado() {

        try {

            AeatCertificadoWindowsSelector.CertificadoSeleccionado seleccionado =
                    certificadoSelector.seleccionar();

            KeyStore keyStore =
                    KeyStore.getInstance(
                            WINDOWS_MY
                    );

            keyStore.load(
                    null,
                    null
            );

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(
                            KeyManagerFactory.getDefaultAlgorithm()
                    );

            keyManagerFactory.init(
                    keyStore,
                    null
            );

            X509KeyManager keyManagerBase =
                    buscarX509KeyManager(
                            keyManagerFactory.getKeyManagers()
                    );

            if (keyManagerBase.getPrivateKey(
                    seleccionado.alias()) == null) {

                throw new IllegalStateException(
                        "El certificado seleccionado no tiene una clave privada accesible"
                );
            }

            return new KeyManagerCertificadoFijo(
                    keyManagerBase,
                    seleccionado.alias()
            );

        } catch (IllegalStateException e) {

            throw e;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo preparar el certificado cliente para AEAT",
                    e
            );
        }
    }

    private static X509KeyManager buscarX509KeyManager(
            KeyManager[] keyManagers) {

        for (KeyManager keyManager : keyManagers) {

            if (keyManager instanceof X509KeyManager x509KeyManager) {
                return x509KeyManager;
            }
        }

        throw new IllegalStateException(
                "No se ha encontrado un X509KeyManager compatible con Windows-MY"
        );
    }

    private static final class KeyManagerCertificadoFijo
            extends X509ExtendedKeyManager {

        private final X509KeyManager delegate;
        private final String alias;

        private KeyManagerCertificadoFijo(
                X509KeyManager delegate,
                String alias) {

            this.delegate = delegate;
            this.alias = alias;
        }

        @Override
        public String[] getClientAliases(
                String keyType,
                Principal[] issuers) {

            if (esCompatibleConTipoClave(keyType)) {
                return new String[]{alias};
            }

            return null;
        }

        @Override
        public String chooseClientAlias(
                String[] keyTypes,
                Principal[] issuers,
                Socket socket) {

            if (keyTypes == null) {
                return null;
            }

            for (String keyType : keyTypes) {

                if (esCompatibleConTipoClave(keyType)) {
                    return alias;
                }
            }

            return null;
        }

        @Override
        public String chooseEngineClientAlias(
                String[] keyTypes,
                Principal[] issuers,
                SSLEngine engine) {

            return chooseClientAlias(
                    keyTypes,
                    issuers,
                    null
            );
        }

        @Override
        public String[] getServerAliases(
                String keyType,
                Principal[] issuers) {

            return delegate.getServerAliases(
                    keyType,
                    issuers
            );
        }

        @Override
        public String chooseServerAlias(
                String keyType,
                Principal[] issuers,
                Socket socket) {

            return delegate.chooseServerAlias(
                    keyType,
                    issuers,
                    socket
            );
        }

        @Override
        public X509Certificate[] getCertificateChain(
                String requestedAlias) {

            if (!alias.equals(requestedAlias)) {
                return null;
            }

            return delegate.getCertificateChain(
                    alias
            );
        }

        @Override
        public PrivateKey getPrivateKey(
                String requestedAlias) {

            if (!alias.equals(requestedAlias)) {
                return null;
            }

            return delegate.getPrivateKey(
                    alias
            );
        }

        private boolean esCompatibleConTipoClave(
                String keyType) {

            if (keyType == null || keyType.isBlank()) {
                return false;
            }

            X509Certificate[] cadena =
                    delegate.getCertificateChain(
                            alias
                    );

            if (cadena == null || cadena.length == 0) {
                return false;
            }

            String algoritmo =
                    cadena[0]
                            .getPublicKey()
                            .getAlgorithm()
                            .toUpperCase(
                                    Locale.ROOT
                            );

            String tipoSolicitado =
                    keyType
                            .toUpperCase(
                                    Locale.ROOT
                            );

            return tipoSolicitado.equals(algoritmo)
                    || tipoSolicitado.startsWith(
                            algoritmo + "_"
                    );
        }
    }
}