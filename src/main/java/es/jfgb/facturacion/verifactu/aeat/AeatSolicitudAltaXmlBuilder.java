package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PersonaFisicaJuridicaESType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroFacturacionAltaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component
public class AeatSolicitudAltaXmlBuilder {

    private final AeatRegistroAltaMapper
            registroAltaMapper;

    public AeatSolicitudAltaXmlBuilder(
            AeatRegistroAltaMapper registroAltaMapper) {

        this.registroAltaMapper =
                registroAltaMapper;
    }

    /**
     * Construye la petición oficial
     * RegFactuSistemaFacturacion para un único
     * registro de alta.
     *
     * Este método NO realiza ninguna comunicación
     * con la AEAT.
     */
    public RegFactuSistemaFacturacion construir(
            RegistroFacturacion registro) {

        validarRegistro(
                registro
        );

        /*
         * =====================================================
         * CABECERA
         * =====================================================
         *
         * ObligadoEmision es el emisor de la factura.
         * No debe confundirse con el productor del SIF.
         * =====================================================
         */

        PersonaFisicaJuridicaESType obligado =
                new PersonaFisicaJuridicaESType();

        obligado.setNombreRazon(
                obligatorio(
                        registro.getNombreRazonEmisor(),
                        "NombreRazon del obligado a emisión"
                )
        );

        obligado.setNIF(
                obligatorio(
                        registro.getNifEmisor(),
                        "NIF del obligado a emisión"
                )
        );

        CabeceraType cabecera =
                new CabeceraType();

        cabecera.setObligadoEmision(
                obligado
        );

        /*
         * =====================================================
         * REGISTRO ALTA
         * =====================================================
         */

        RegistroFacturacionAltaType alta =
                registroAltaMapper.convertir(
                        registro
                );

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        registroFactura.setRegistroAlta(
                alta
        );

        /*
         * =====================================================
         * PETICION COMPLETA
         * =====================================================
         */

        RegFactuSistemaFacturacion peticion =
                new RegFactuSistemaFacturacion();

        peticion.setCabecera(
                cabecera
        );

        peticion
                .getRegistroFactura()
                .add(
                        registroFactura
                );

        return peticion;
    }

    /**
     * Genera el XML correspondiente a la petición.
     *
     * No realiza ninguna llamada de red.
     */
    public String generarXml(
            RegistroFacturacion registro) {

        RegFactuSistemaFacturacion peticion =
                construir(
                        registro
                );

        try {

            JAXBContext context =
                    JAXBContext.newInstance(
                            RegFactuSistemaFacturacion.class,
                            RegistroFacturaType.class,
                            CabeceraType.class,
                            PersonaFisicaJuridicaESType.class,
                            RegistroFacturacionAltaType.class
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

            StringWriter writer =
                    new StringWriter();

            marshaller.marshal(
                    peticion,
                    writer
            );

            return writer.toString();

        } catch (JAXBException e) {

            throw new IllegalStateException(
                    "No se ha podido generar "
                            + "el XML VERI*FACTU",
                    e
            );
        }
    }

    private void validarRegistro(
            RegistroFacturacion registro) {

        if (registro == null) {

            throw new IllegalArgumentException(
                    "El registro de facturación "
                            + "no puede ser null"
            );
        }

        if (registro.getId() == null) {

            throw new IllegalStateException(
                    "El registro de facturación "
                            + "debe estar persistido"
            );
        }
    }

    private String obligatorio(
            String valor,
            String campo) {

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalStateException(
                    campo
                            + " es obligatorio"
            );
        }

        return valor.trim();
    }
}