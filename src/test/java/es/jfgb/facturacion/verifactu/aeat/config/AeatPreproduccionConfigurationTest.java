package es.jfgb.facturacion.verifactu.aeat.config;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.jfgb.facturacion.verifactu.aeat.AeatRespuestaAltaValidator;
import es.jfgb.facturacion.verifactu.aeat.AeatRespuestaAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.AeatResultadoEnvioMapper;
import es.jfgb.facturacion.verifactu.aeat.AeatSolicitudAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.AeatVerifactuProvider;
import es.jfgb.facturacion.verifactu.aeat.security.AeatSoapTlsConfigurer;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapPruebasGateway;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import jakarta.xml.ws.BindingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AeatPreproduccionConfigurationTest {

    private AeatPreproduccionConfiguration configuration;

    @BeforeEach
    void setUp() {

        configuration =
                new AeatPreproduccionConfiguration();
    }

    @Test
    void puertoSoapApuntaAlEntornoDePruebas() {

        AeatSoapTlsConfigurer tlsConfigurer =
                mock(
                        AeatSoapTlsConfigurer.class
                );

        SfPortTypeVerifactu port =
                configuration
                        .aeatVerifactuPruebasPort(
                                tlsConfigurer
                        );

        BindingProvider bindingProvider =
                (BindingProvider) port;

        Object endpoint =
                bindingProvider
                        .getRequestContext()
                        .get(
                                BindingProvider
                                        .ENDPOINT_ADDRESS_PROPERTY
                        );

        assertEquals(
                "https://prewww1.aeat.es/"
                        + "wlpl/TIKE-CONT/ws/"
                        + "SistemaFacturacion/"
                        + "VerifactuSOAP",
                endpoint
        );
    }

    @Test
    void configuraTlsEnElPuertoSoapDePruebas() {

        AeatSoapTlsConfigurer tlsConfigurer =
                mock(
                        AeatSoapTlsConfigurer.class
                );

        SfPortTypeVerifactu port =
                configuration
                        .aeatVerifactuPruebasPort(
                                tlsConfigurer
                        );

        verify(
                tlsConfigurer
        ).configurar(
                port
        );
    }

    @Test
    void creaElGatewayDePruebas() {

        AeatSoapTlsConfigurer tlsConfigurer =
                mock(
                        AeatSoapTlsConfigurer.class
                );

        SfPortTypeVerifactu port =
                configuration
                        .aeatVerifactuPruebasPort(
                                tlsConfigurer
                        );

        AeatSoapGateway gateway =
                configuration
                        .aeatSoapGateway(
                                port
                        );

        assertInstanceOf(
                AeatSoapPruebasGateway.class,
                gateway
        );
    }

    @Test
    void creaElProveedorAeatDePruebas() {

        AeatSolicitudAltaXmlBuilder solicitudAltaXmlBuilder =
                mock(
                        AeatSolicitudAltaXmlBuilder.class
                );

        AeatSoapGateway soapGateway =
                mock(
                        AeatSoapGateway.class
                );

        AeatRespuestaAltaXmlBuilder respuestaAltaXmlBuilder =
                mock(
                        AeatRespuestaAltaXmlBuilder.class
                );

        AeatRespuestaAltaValidator respuestaAltaValidator =
                mock(
                        AeatRespuestaAltaValidator.class
                );

        AeatResultadoEnvioMapper resultadoEnvioMapper =
                mock(
                        AeatResultadoEnvioMapper.class
                );

        VerifactuProvider provider =
                configuration
                        .aeatVerifactuProvider(
                                solicitudAltaXmlBuilder,
                                soapGateway,
                                respuestaAltaXmlBuilder,
                                respuestaAltaValidator,
                                resultadoEnvioMapper
                        );

        assertInstanceOf(
                AeatVerifactuProvider.class,
                provider
        );

        assertEquals(
                "AEAT-VERIFACTU-PRUEBAS",
                provider.getNombre()
        );
    }

    @Test
    void configuracionSoloSeActivaEnAeatPreSinDev() {

        Profile profile =
                AeatPreproduccionConfiguration.class
                        .getAnnotation(
                                Profile.class
                        );

        assertTrue(
                profile != null
        );

        assertTrue(
                Arrays.asList(
                        profile.value()
                ).contains(
                        "aeat-pre & !dev"
                )
        );
    }
}