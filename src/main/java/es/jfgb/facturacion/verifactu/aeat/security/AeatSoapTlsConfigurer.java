package es.jfgb.facturacion.verifactu.aeat.security;

import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.xml.ws.BindingProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

@Component
@Profile("aeat-pre & !dev")
public class AeatSoapTlsConfigurer {

    private final AeatSslContextFactory sslContextFactory;

    public AeatSoapTlsConfigurer(
            AeatSslContextFactory sslContextFactory) {

        this.sslContextFactory = sslContextFactory;
    }

    public void configurar(
            Object puertoSoap) {

        if (puertoSoap == null) {
            throw new IllegalArgumentException(
                    "El puerto SOAP de AEAT no puede ser nulo"
            );
        }

        if (!(puertoSoap instanceof BindingProvider bindingProvider)) {
            throw new IllegalArgumentException(
                    "El puerto SOAP de AEAT no implementa BindingProvider"
            );
        }

        SSLContext sslContext =
                sslContextFactory.crearSslContext();

        SSLSocketFactory socketFactory =
                sslContext.getSocketFactory();

        bindingProvider
                .getRequestContext()
                .put(
                        JAXWSProperties.SSL_SOCKET_FACTORY,
                        socketFactory
                );
    }
}