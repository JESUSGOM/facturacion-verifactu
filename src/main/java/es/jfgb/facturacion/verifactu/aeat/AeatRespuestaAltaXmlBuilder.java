package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.ObjectFactory;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaRegFactuSistemaFacturacionType;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component
public class AeatRespuestaAltaXmlBuilder {

    public RespuestaRegFactuSistemaFacturacionType construir(
            AeatSoapResultadoAlta resultado) {

        validarResultado(
                resultado
        );

        RespuestaRegFactuSistemaFacturacionType respuesta =
                new RespuestaRegFactuSistemaFacturacionType();

        respuesta.setCSV(
                resultado.csv()
        );

        respuesta.setDatosPresentacion(
                resultado.datosPresentacion()
        );

        respuesta.setCabecera(
                resultado.cabecera()
        );

        respuesta.setTiempoEsperaEnvio(
                resultado.tiempoEsperaEnvio()
        );

        respuesta.setEstadoEnvio(
                resultado.estadoEnvio()
        );

        respuesta
                .getRespuestaLinea()
                .addAll(
                        resultado.respuestasLinea()
                );

        return respuesta;
    }

    public String generarXml(
            AeatSoapResultadoAlta resultado) {

        RespuestaRegFactuSistemaFacturacionType respuesta =
                construir(
                        resultado
                );

        try {

            JAXBContext context =
                    JAXBContext.newInstance(
                            RespuestaRegFactuSistemaFacturacionType.class,
                            ObjectFactory.class
                    );

            Marshaller marshaller =
                    context.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE
            );

            marshaller.setProperty(
                    Marshaller.JAXB_ENCODING,
                    "UTF-8"
            );

            ObjectFactory objectFactory =
                    new ObjectFactory();

            JAXBElement<RespuestaRegFactuSistemaFacturacionType>
                    elementoRaiz =
                    objectFactory
                            .createRespuestaRegFactuSistemaFacturacion(
                                    respuesta
                            );

            StringWriter writer =
                    new StringWriter();

            marshaller.marshal(
                    elementoRaiz,
                    writer
            );

            return writer.toString();

        } catch (JAXBException e) {

            throw new IllegalStateException(
                    "No se ha podido generar el XML "
                            + "de respuesta VERI*FACTU",
                    e
            );
        }
    }

    private void validarResultado(
            AeatSoapResultadoAlta resultado) {

        if (resultado == null) {

            throw new IllegalArgumentException(
                    "El resultado SOAP de AEAT no puede ser nulo"
            );
        }

        if (resultado.cabecera() == null) {

            throw new IllegalStateException(
                    "La respuesta de AEAT no contiene Cabecera"
            );
        }

        if (resultado.tiempoEsperaEnvio() == null
                || resultado.tiempoEsperaEnvio().isBlank()) {

            throw new IllegalStateException(
                    "La respuesta de AEAT no contiene "
                            + "TiempoEsperaEnvio"
            );
        }

        if (resultado.estadoEnvio() == null) {

            throw new IllegalStateException(
                    "La respuesta de AEAT no contiene EstadoEnvio"
            );
        }
    }
}