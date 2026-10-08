package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AeatSolicitudAnulacionF2R5XmlBuilderTest {

    private static final String HUELLA_ANTERIOR =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                    + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private static final String HUELLA_ACTUAL =
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
                    + "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

    @Test
    void generaXmlAnulacionF2SinDestinatarios()
            throws Exception {

        String xml =
                generarXml(
                        "SIM-1",
                        LocalDate.of(
                                2026,
                                10,
                                7
                        ),
                        "RECT-3",
                        LocalDate.of(
                                2026,
                                10,
                                8
                        )
                );

        assertNotNull(
                xml
        );

        assertFalse(
                xml.isBlank()
        );

        assertTrue(
                xml.contains(
                        "RegistroAnulacion"
                )
        );

        assertTrue(
                xml.contains(
                        "IDEmisorFacturaAnulada"
                )
        );

        assertTrue(
                xml.contains(
                        "SIM-1"
                )
        );

        assertTrue(
                xml.contains(
                        "07-10-2026"
                )
        );

        assertTrue(
                xml.contains(
                        "RECT-3"
                )
        );

        assertTrue(
                xml.contains(
                        HUELLA_ANTERIOR
                )
        );

        assertTrue(
                xml.contains(
                        HUELLA_ACTUAL
                )
        );

        assertFalse(
                xml.contains(
                        "Destinatarios"
                )
        );

        assertFalse(
                xml.contains(
                        "RegistroAlta"
                )
        );

        validarContraXsd(
                xml
        );

        System.out.println(
                "VERIFACTU_ANULACION_F2_XSD_VALIDO=true"
        );
    }

    @Test
    void generaXmlAnulacionR5SinDestinatarios()
            throws Exception {

        String xml =
                generarXml(
                        "RECT-3",
                        LocalDate.of(
                                2026,
                                10,
                                8
                        ),
                        "SIM-1",
                        LocalDate.of(
                                2026,
                                10,
                                7
                        )
                );

        assertNotNull(
                xml
        );

        assertFalse(
                xml.isBlank()
        );

        assertTrue(
                xml.contains(
                        "RegistroAnulacion"
                )
        );

        assertTrue(
                xml.contains(
                        "IDEmisorFacturaAnulada"
                )
        );

        assertTrue(
                xml.contains(
                        "RECT-3"
                )
        );

        assertTrue(
                xml.contains(
                        "08-10-2026"
                )
        );

        assertTrue(
                xml.contains(
                        "SIM-1"
                )
        );

        assertTrue(
                xml.contains(
                        HUELLA_ANTERIOR
                )
        );

        assertTrue(
                xml.contains(
                        HUELLA_ACTUAL
                )
        );

        assertFalse(
                xml.contains(
                        "Destinatarios"
                )
        );

        assertFalse(
                xml.contains(
                        "RegistroAlta"
                )
        );

        validarContraXsd(
                xml
        );

        System.out.println(
                "VERIFACTU_ANULACION_R5_XSD_VALIDO=true"
        );
    }

    private void validarContraXsd(
            String xml)
            throws Exception {

        /*
         * Validacion exclusivamente local.
         *
         * No se crea ningun cliente SOAP,
         * no se utiliza certificado
         * y no se realiza comunicacion con AEAT.
         */
        Path xsdPrincipal =
                Path.of(
                        "src",
                        "main",
                        "resources",
                        "aeat",
                        "verifactu",
                        "wsdl",
                        "SuministroLR.xsd"
                );

        assertTrue(
                Files.exists(
                        xsdPrincipal
                ),
                "No existe el XSD local SuministroLR.xsd"
        );

        SchemaFactory schemaFactory =
                SchemaFactory.newInstance(
                        XMLConstants.W3C_XML_SCHEMA_NS_URI
                );

        /*
         * Los XSD pueden importar otros esquemas
         * del mismo directorio. Solo permitimos
         * resolver recursos locales mediante file.
         */
        schemaFactory.setProperty(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                "file"
        );

        schemaFactory.setProperty(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        Schema schema =
                schemaFactory.newSchema(
                        xsdPrincipal.toFile()
                );

        var validator =
                schema.newValidator();

        validator.setProperty(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                "file"
        );

        validator.setProperty(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        /*
         * Si el XML no cumple los XSD locales,
         * validate() lanza una excepcion y el test falla.
         */
        validator.validate(
                new StreamSource(
                        new StringReader(
                                xml
                        )
                )
        );
    }

    private String generarXml(
            String numSerieFacturaAnulada,
            LocalDate fechaFacturaAnulada,
            String numSerieRegistroAnterior,
            LocalDate fechaRegistroAnterior) {

        RegistroFacturacion anterior =
                mock(
                        RegistroFacturacion.class
                );

        RegistroFacturacion registro =
                mock(
                        RegistroFacturacion.class
                );

        when(
                anterior.getNifEmisor()
        ).thenReturn(
                "B12345678"
        );

        when(
                anterior.getNumSerieFactura()
        ).thenReturn(
                numSerieRegistroAnterior
        );

        when(
                anterior.getFechaExpedicion()
        ).thenReturn(
                fechaRegistroAnterior
        );

        when(
                anterior.getHuella()
        ).thenReturn(
                HUELLA_ANTERIOR
        );

        when(
                registro.getId()
        ).thenReturn(
                99L
        );

        when(
                registro.getTipoRegistro()
        ).thenReturn(
                TipoRegistroFacturacion.ANULACION
        );

        when(
                registro.getIdVersionAeat()
        ).thenReturn(
                "1.0"
        );

        when(
                registro.getNifEmisor()
        ).thenReturn(
                "B12345678"
        );

        when(
                registro.getNombreRazonEmisor()
        ).thenReturn(
                "Empresa de Prueba SL"
        );

        when(
                registro.getNumSerieFactura()
        ).thenReturn(
                numSerieFacturaAnulada
        );

        when(
                registro.getFechaExpedicion()
        ).thenReturn(
                fechaFacturaAnulada
        );

        when(
                registro.getRegistroAnterior()
        ).thenReturn(
                anterior
        );

        when(
                registro.getSistemaNombreRazon()
        ).thenReturn(
                "Gombeth Software Solutions Test"
        );

        when(
                registro.getSistemaNif()
        ).thenReturn(
                "B12345678"
        );

        when(
                registro.getSistemaNombre()
        ).thenReturn(
                "Gombeth Factura"
        );

        when(
                registro.getSistemaId()
        ).thenReturn(
                "GF"
        );

        when(
                registro.getSistemaVersion()
        ).thenReturn(
                "0.0.1-SNAPSHOT"
        );

        when(
                registro.getSistemaNumeroInstalacion()
        ).thenReturn(
                "DEV-INST-1"
        );

        when(
                registro.getSistemaSoloVerifactu()
        ).thenReturn(
                "S"
        );

        when(
                registro.getSistemaMultiOt()
        ).thenReturn(
                "S"
        );

        when(
                registro.getSistemaMultiplesOt()
        ).thenReturn(
                "N"
        );

        when(
                registro.getFechaHoraGeneracion()
        ).thenReturn(
                "2026-10-08T11:20:00+01:00"
        );

        when(
                registro.getTipoHuella()
        ).thenReturn(
                "01"
        );

        when(
                registro.getHuella()
        ).thenReturn(
                HUELLA_ACTUAL
        );

        AeatRegistroAnulacionMapper mapper =
                new AeatRegistroAnulacionMapper();

        AeatSolicitudAnulacionXmlBuilder builder =
                new AeatSolicitudAnulacionXmlBuilder(
                        mapper
                );

        return builder.generarXml(
                registro
        );
    }
}
