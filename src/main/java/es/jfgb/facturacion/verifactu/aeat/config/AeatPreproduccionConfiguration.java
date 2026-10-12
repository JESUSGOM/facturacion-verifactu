package es.jfgb.facturacion.verifactu.aeat.config;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfVerifactu;
import es.jfgb.facturacion.verifactu.aeat.AeatRespuestaAltaValidator;
import es.jfgb.facturacion.verifactu.aeat.AeatRespuestaAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.AeatResultadoEnvioMapper;
import es.jfgb.facturacion.verifactu.aeat.AeatSolicitudAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.AeatVerifactuProvider;
import es.jfgb.facturacion.verifactu.aeat.security.AeatSoapTlsConfigurer;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapPruebasGateway;
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import es.jfgb.facturacion.verifactu.aeat.security.AeatEndpointPreproduccionValidator;
import jakarta.xml.ws.BindingProvider;

@Configuration
@Profile("aeat-pre & !dev")
public class AeatPreproduccionConfiguration {

    @Bean
    public SfPortTypeVerifactu aeatVerifactuPruebasPort(
            AeatSoapTlsConfigurer tlsConfigurer) {

        SfVerifactu servicio =
                new SfVerifactu();

        SfPortTypeVerifactu port =
                servicio.getSistemaVerifactuPruebas();

        BindingProvider bindingProvider =
                (BindingProvider) port;

        Object endpoint =
                bindingProvider.getRequestContext().get(
                        BindingProvider.ENDPOINT_ADDRESS_PROPERTY
                );

        AeatEndpointPreproduccionValidator.validar(
                endpoint instanceof String
                        ? (String) endpoint
                        : null
        );

        /*
         * Todo puerto SOAP de preproducción creado por Spring
         * queda configurado con el SSLContext que selecciona
         * explícitamente el certificado cliente de Windows-MY.
         */
        tlsConfigurer.configurar(
                port
        );

        return port;
    }

    @Bean
    public AeatSoapGateway aeatSoapGateway(
            SfPortTypeVerifactu aeatVerifactuPruebasPort) {

        return new AeatSoapPruebasGateway(
                aeatVerifactuPruebasPort
        );
    }

    @Bean
    public VerifactuProvider aeatVerifactuProvider(
            AeatSolicitudAltaXmlBuilder solicitudAltaXmlBuilder,
            AeatSoapGateway aeatSoapGateway,
            AeatRespuestaAltaXmlBuilder respuestaAltaXmlBuilder,
            AeatRespuestaAltaValidator respuestaAltaValidator,
            AeatResultadoEnvioMapper resultadoEnvioMapper) {

        return new AeatVerifactuProvider(
                solicitudAltaXmlBuilder,
                aeatSoapGateway,
                respuestaAltaXmlBuilder,
                respuestaAltaValidator,
                resultadoEnvioMapper
        );
    }
}