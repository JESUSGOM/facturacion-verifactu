package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.factura.TipoRectificativa;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRectificada;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatSolicitudAltaRectificativaXmlTest {

    private final AeatRegistroAltaMapper mapper =
            new AeatRegistroAltaMapper();

    private final AeatSolicitudAltaXmlBuilder builder =
            new AeatSolicitudAltaXmlBuilder(
                    mapper
            );

    @Test
    void generaYValidaXmlRectificativaPorDiferencias()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistro(
                        TipoRectificativa.I
                );

        registro.setCuotaTotal(
                new BigDecimal("-1.40")
        );

        registro.setImporteTotal(
                new BigDecimal("-21.40")
        );

        RegistroFacturacionDesglose desglose =
                registro.getDesgloses()
                        .getFirst();

        desglose.setBaseImponibleOImporteNoSujeto(
                new BigDecimal("-20.00")
        );

        desglose.setCuotaRepercutida(
                new BigDecimal("-1.40")
        );

        String xml =
                builder.generarXml(
                        registro
                );

        comprobarEstructuraComun(
                xml
        );

        assertEquals(
                "I",
                valorElemento(
                        xml,
                        "TipoRectificativa"
                )
        );

        assertFalse(
                existeElemento(
                        xml,
                        "ImporteRectificacion"
                )
        );

        validarXsdYGuardar(
                xml,
                "alta-rectificativa-I.xml"
        );

        System.out.println(
                "VERIFACTU_RECT_I_XSD_VALIDO=true"
        );

        System.out.println(
                "VERIFACTU_RECT_I_RED=false"
        );
    }

    @Test
    void generaYValidaXmlRectificativaSustitutiva()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistro(
                        TipoRectificativa.S
                );

        registro.setBaseRectificada(
                new BigDecimal("100.00")
        );

        registro.setCuotaRectificada(
                new BigDecimal("7.00")
        );

        registro.setCuotaTotal(
                new BigDecimal("5.60")
        );

        registro.setImporteTotal(
                new BigDecimal("85.60")
        );

        RegistroFacturacionDesglose desglose =
                registro.getDesgloses()
                        .getFirst();

        desglose.setBaseImponibleOImporteNoSujeto(
                new BigDecimal("80.00")
        );

        desglose.setCuotaRepercutida(
                new BigDecimal("5.60")
        );

        String xml =
                builder.generarXml(
                        registro
                );

        comprobarEstructuraComun(
                xml
        );

        assertEquals(
                "S",
                valorElemento(
                        xml,
                        "TipoRectificativa"
                )
        );

        assertTrue(
                existeElemento(
                        xml,
                        "ImporteRectificacion"
                )
        );

        assertEquals(
                "100.00",
                valorElemento(
                        xml,
                        "BaseRectificada"
                )
        );

        assertEquals(
                "7.00",
                valorElemento(
                        xml,
                        "CuotaRectificada"
                )
        );

        validarXsdYGuardar(
                xml,
                "alta-rectificativa-S.xml"
        );

        System.out.println(
                "VERIFACTU_RECT_S_XSD_VALIDO=true"
        );

        System.out.println(
                "VERIFACTU_RECT_S_RED=false"
        );
    }

    private void comprobarEstructuraComun(
            String xml)
            throws Exception {

        assertNotNull(
                xml
        );

        assertFalse(
                xml.isBlank()
        );

        assertEquals(
                "R1",
                valorElemento(
                        xml,
                        "TipoFactura"
                )
        );

        assertTrue(
                existeElemento(
                        xml,
                        "FacturasRectificadas"
                )
        );

        assertTrue(
                existeElemento(
                        xml,
                        "IDFacturaRectificada"
                )
        );

        assertEquals(
                "B12345678",
                valorElemento(
                        xml,
                        "IDEmisorFactura",
                        1
                )
        );

        assertEquals(
                "FAC-7",
                valorElemento(
                        xml,
                        "NumSerieFactura",
                        1
                )
        );

        assertEquals(
                "01-10-2026",
                valorElemento(
                        xml,
                        "FechaExpedicionFactura",
                        1
                )
        );
    }

    private RegistroFacturacion crearRegistro(
            TipoRectificativa tipoRectificativa)
            throws Exception {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        asignarId(
                registro,
                910000001L
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        registro.setNifEmisor(
                "B12345678"
        );

        registro.setNombreRazonEmisor(
                "Empresa de Prueba SL"
        );

        registro.setSerie(
                "RECT"
        );

        registro.setNumero(
                1L
        );

        registro.setNumSerieFactura(
                tipoRectificativa == TipoRectificativa.I
                        ? "RECT-I-1"
                        : "RECT-S-1"
        );

        registro.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        5
                )
        );

        registro.setTipoFactura(
                "R1"
        );

        registro.setTipoRectificativa(
                tipoRectificativa
        );

        registro.setDescripcionOperacion(
                "RECTIFICACION FACTURA"
        );

        registro.setDestinatarioNombre(
                "Cliente de Prueba"
        );

        registro.setDestinatarioNif(
                "12345678Z"
        );

        registro.setDestinatarioCodigoPais(
                "ES"
        );

        RegistroFacturacionDesglose desglose =
                new RegistroFacturacionDesglose();

        desglose.setOrden(
                (short) 1
        );

        desglose.setImpuesto(
                "03"
        );

        desglose.setClaveRegimen(
                "01"
        );

        desglose.setCalificacionOperacion(
                "S1"
        );

        desglose.setTipoImpositivo(
                new BigDecimal("7.00")
        );

        desglose.setBaseImponibleOImporteNoSujeto(
                new BigDecimal("20.00")
        );

        desglose.setCuotaRepercutida(
                new BigDecimal("1.40")
        );

        registro.addDesglose(
                desglose
        );

        RegistroFacturacionRectificada original =
                new RegistroFacturacionRectificada();

        original.setFacturaRectificadaId(
                10L
        );

        original.setOrden(
                (short) 1
        );

        original.setNifEmisor(
                "B12345678"
        );

        original.setNumSerieFactura(
                "FAC-7"
        );

        original.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        registro.addFacturaRectificada(
                original
        );

        registro.setIdVersionAeat(
                "1.0"
        );

        registro.setSistemaNombreRazon(
                "Gombeth Software Solutions"
        );

        registro.setSistemaNif(
                "B12345678"
        );

        registro.setSistemaNombre(
                "Gombeth Factura"
        );

        registro.setSistemaId(
                "GF"
        );

        registro.setSistemaVersion(
                "0.0.1-SNAPSHOT"
        );

        registro.setSistemaNumeroInstalacion(
                "TEST01"
        );

        registro.setSistemaSoloVerifactu(
                "S"
        );

        registro.setSistemaMultiOt(
                "S"
        );

        registro.setSistemaMultiplesOt(
                "N"
        );

        registro.setFechaHoraGeneracion(
                "2026-10-05T15:00:00+01:00"
        );

        registro.setTipoHuella(
                "01"
        );

        registro.setHuella(
                "0123456789ABCDEF0123456789ABCDEF"
                        + "0123456789ABCDEF0123456789ABCDEF"
        );

        return registro;
    }

    private void validarXsdYGuardar(
            String xml,
            String nombreFichero)
            throws Exception {

        Path directorio =
                Path.of(
                        "target",
                        "verifactu-preview"
                );

        Files.createDirectories(
                directorio
        );

        Path fichero =
                directorio.resolve(
                        nombreFichero
                );

        Files.writeString(
                fichero,
                xml,
                StandardCharsets.UTF_8
        );

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
                )
        );

        SchemaFactory schemaFactory =
                SchemaFactory.newInstance(
                        XMLConstants.W3C_XML_SCHEMA_NS_URI
                );

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

        validator.validate(
                new StreamSource(
                        new StringReader(
                                xml
                        )
                )
        );

        assertTrue(
                Files.size(
                        fichero
                ) > 0
        );
    }

    private boolean existeElemento(
            String xml,
            String localName)
            throws Exception {

        Document document =
                parsearXml(
                        xml
                );

        return document
                .getElementsByTagNameNS(
                        "*",
                        localName
                )
                .getLength() > 0;
    }

    private String valorElemento(
            String xml,
            String localName)
            throws Exception {

        return valorElemento(
                xml,
                localName,
                0
        );
    }

    private String valorElemento(
            String xml,
            String localName,
            int indice)
            throws Exception {

        Document document =
                parsearXml(
                        xml
                );

        var nodos =
                document.getElementsByTagNameNS(
                        "*",
                        localName
                );

        assertTrue(
                nodos.getLength() > indice,
                "No se encontro el elemento "
                        + localName
                        + " en posicion "
                        + indice
        );

        return nodos
                .item(indice)
                .getTextContent()
                .trim();
    }

    private Document parsearXml(
            String xml)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory
                        .newInstance();

        factory.setNamespaceAware(
                true
        );

        factory.setFeature(
                "http://apache.org/xml/features/"
                        + "disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/"
                        + "external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/"
                        + "external-parameter-entities",
                false
        );

        return factory
                .newDocumentBuilder()
                .parse(
                        new InputSource(
                                new StringReader(
                                        xml
                                )
                        )
                );
    }

    private void asignarId(
            RegistroFacturacion registro,
            Long id)
            throws Exception {

        Field campo =
                RegistroFacturacion.class
                        .getDeclaredField(
                                "id"
                        );

        campo.setAccessible(
                true
        );

        campo.set(
                registro,
                id
        );
    }
}