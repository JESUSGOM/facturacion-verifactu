package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PersonaFisicaJuridicaESType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroFacturacionAnulacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component
public class AeatSolicitudAnulacionXmlBuilder {

    private final AeatRegistroAnulacionMapper
            registroAnulacionMapper;

    public AeatSolicitudAnulacionXmlBuilder(
            AeatRegistroAnulacionMapper registroAnulacionMapper) {

        this.registroAnulacionMapper =
                registroAnulacionMapper;
    }

    /**
     * Construye localmente la peticion oficial
     * RegFactuSistemaFacturacion con un unico
     * RegistroAnulacion.
     *
     * No realiza ninguna llamada de red.
     */
    public RegFactuSistemaFacturacion construir(
            RegistroFacturacion registro) {

        validarRegistro(
                registro
        );

        PersonaFisicaJuridicaESType obligado =
                new PersonaFisicaJuridicaESType();

        obligado.setNombreRazon(
                obligatorio(
                        registro.getNombreRazonEmisor(),
                        "NombreRazon del obligado a emision"
                )
        );

        obligado.setNIF(
                obligatorio(
                        registro.getNifEmisor(),
                        "NIF del obligado a emision"
                )
        );

        CabeceraType cabecera =
                new CabeceraType();

        cabecera.setObligadoEmision(
                obligado
        );

        RegistroFacturacionAnulacionType anulacion =
                registroAnulacionMapper.convertir(
                        registro
                );

        RegistroFacturaType registroFactura =
                new RegistroFacturaType();

        registroFactura.setRegistroAnulacion(
                anulacion
        );

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
     * Genera exclusivamente el XML local.
     *
     * No realiza comunicacion con AEAT.
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
                            RegistroFacturacionAnulacionType.class
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
                            + "el XML VERI*FACTU de anulacion",
                    e
            );
        }
    }

    private void validarRegistro(
            RegistroFacturacion registro) {

        if (registro == null) {
            throw new IllegalArgumentException(
                    "El registro de facturacion "
                            + "no puede ser null"
            );
        }

        if (registro.getId() == null) {
            throw new IllegalStateException(
                    "El registro de facturacion "
                            + "debe estar persistido"
            );
        }

        if (registro.getTipoRegistro()
                != TipoRegistroFacturacion.ANULACION) {

            throw new IllegalArgumentException(
                    "El registro debe ser de tipo ANULACION"
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