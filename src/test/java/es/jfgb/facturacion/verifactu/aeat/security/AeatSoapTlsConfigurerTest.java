package es.jfgb.facturacion.verifactu.aeat.security;

import com.sun.xml.ws.developer.JAXWSProperties;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfVerifactu;
import jakarta.xml.ws.BindingProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javax.net.ssl.SSLSocketFactory;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AeatSoapTlsConfigurerTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    void configuraSslSocketFactoryEnPuertoSoapDePruebas() {

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

        AeatSoapTlsConfigurer configurer =
                new AeatSoapTlsConfigurer(
                        sslContextFactory
                );

        SfVerifactu servicio =
                new SfVerifactu();

        SfPortTypeVerifactu port =
                servicio.getSistemaVerifactuPruebas();

        configurer.configurar(
                port
        );

        BindingProvider bindingProvider =
                (BindingProvider) port;

        Object propiedad =
                bindingProvider
                        .getRequestContext()
                        .get(
                                JAXWSProperties.SSL_SOCKET_FACTORY
                        );

        assertNotNull(
                propiedad
        );

        assertInstanceOf(
                SSLSocketFactory.class,
                propiedad
        );
    }

    @Test
    void rechazaPuertoNulo() {

        AeatCertificadoWindowsSelector selector =
                new AeatCertificadoWindowsSelector(
                        ""
                );

        AeatSslContextFactory sslContextFactory =
                new AeatSslContextFactory(
                        selector
                );

        AeatSoapTlsConfigurer configurer =
                new AeatSoapTlsConfigurer(
                        sslContextFactory
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> configurer.configurar(
                        null
                )
        );
    }

    @Test
    void rechazaObjetoQueNoEsBindingProvider() {

        AeatCertificadoWindowsSelector selector =
                new AeatCertificadoWindowsSelector(
                        ""
                );

        AeatSslContextFactory sslContextFactory =
                new AeatSslContextFactory(
                        selector
                );

        AeatSoapTlsConfigurer configurer =
                new AeatSoapTlsConfigurer(
                        sslContextFactory
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> configurer.configurar(
                        new Object()
                )
        );
    }
}