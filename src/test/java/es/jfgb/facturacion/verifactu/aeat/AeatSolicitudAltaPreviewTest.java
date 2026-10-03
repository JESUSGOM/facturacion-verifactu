package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.HuellaVerifactuService;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatSolicitudAltaPreviewTest {

    @Test
    void generaYValidaXmlVerifactuSinComunicacionConAeat()
            throws Exception {

        /*
         * =========================================================
         * SEGURIDAD DE ESTA PRUEBA
         * =========================================================
         *
         * Esta prueba:
         *
         * - NO crea SfVerifactu.
         * - NO crea SfPortTypeVerifactu.
         * - NO crea AeatSoapPruebasGateway.
         * - NO utiliza certificado.
         * - NO realiza llamadas HTTPS.
         * - NO comunica nada con AEAT.
         *
         * Construye el XML exclusivamente en memoria,
         * lo guarda en target y lo valida contra los
         * XSD locales del proyecto.
         * =========================================================
         */

        String nombreObligado =
                obligatorio(
                        "AEAT_TEST_OBLIGADO_NOMBRE"
                );

        String nifObligado =
                obligatorio(
                        "AEAT_TEST_OBLIGADO_NIF"
                );

        String sistemaNombreRazon =
                obligatorio(
                        "VERIFACTU_SISTEMA_NOMBRE_RAZON"
                );

        String sistemaNif =
                obligatorio(
                        "VERIFACTU_SISTEMA_NIF"
                );

        OffsetDateTime fechaHora =
                OffsetDateTime.now();

        String numeroFactura =
                "PREVIEW-GF-"
                        + fechaHora.format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                        );

        /*
         * =========================================================
         * REGISTRO SINTÉTICO
         * =========================================================
         */

        RegistroFacturacion registro =
                new RegistroFacturacion();

        /*
         * El mapper exige un registro con ID.
         *
         * Esta prueba no persiste nada.
         * Se asigna exclusivamente un ID ficticio en memoria.
         */

        Field campoId =
                RegistroFacturacion.class
                        .getDeclaredField(
                                "id"
                        );

        campoId.setAccessible(
                true
        );

        campoId.set(
                registro,
                999999997L
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        /*
         * =========================================================
         * EMISOR / FACTURA
         * =========================================================
         */

        registro.setNifEmisor(
                nifObligado
        );

        registro.setNombreRazonEmisor(
                nombreObligado
        );

        registro.setSerie(
                "PREVIEW"
        );

        registro.setNumero(
                System.currentTimeMillis()
        );

        registro.setNumSerieFactura(
                numeroFactura
        );

        registro.setFechaExpedicion(
                fechaHora.toLocalDate()
        );

        registro.setTipoFactura(
                "F1"
        );

        registro.setDescripcionOperacion(
                "PRUEBA GENERACION XML "
                        + "GOMBETH FACTURA SIN ENVIO"
        );

        /*
         * Para esta prueba técnica utilizamos al propio
         * obligado como destinatario y evitamos datos
         * pertenecientes a terceras personas.
         */

        registro.setDestinatarioNombre(
                nombreObligado
        );

        registro.setDestinatarioNif(
                nifObligado
        );

        registro.setDestinatarioCodigoPais(
                "ES"
        );

        /*
         * =========================================================
         * DESGLOSE IGIC 7 %
         * =========================================================
         */

        BigDecimal base =
                new BigDecimal(
                        "20.00"
                );

        BigDecimal cuota =
                new BigDecimal(
                        "1.40"
                );

        BigDecimal total =
                new BigDecimal(
                        "21.40"
                );

        registro.setCuotaTotal(
                cuota
        );

        registro.setImporteTotal(
                total
        );

        RegistroFacturacionDesglose desglose =
                new RegistroFacturacionDesglose();

        desglose.setOrden(
                (short) 1
        );

        /*
         * 03 = IGIC
         */

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
                new BigDecimal(
                        "7.00"
                )
        );

        desglose.setBaseImponibleOImporteNoSujeto(
                base
        );

        desglose.setCuotaRepercutida(
                cuota
        );

        registro.addDesglose(
                desglose
        );

        /*
         * =========================================================
         * SISTEMA INFORMÁTICO
         * =========================================================
         */

        registro.setIdVersionAeat(
                "1.0"
        );

        registro.setSistemaNombreRazon(
                sistemaNombreRazon
        );

        registro.setSistemaNif(
                sistemaNif
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
                "PREPROD01"
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

        registro.setTipoHuella(
                "01"
        );

        /*
         * =========================================================
         * HUELLA
         * =========================================================
         */

        HuellaVerifactuService huellaService =
                new HuellaVerifactuService();

        String huella =
                huellaService.calcularAlta(
                        registro.getNifEmisor(),
                        registro.getNumSerieFactura(),
                        registro.getFechaExpedicion(),
                        registro.getTipoFactura(),
                        registro.getCuotaTotal(),
                        registro.getImporteTotal(),
                        null,
                        fechaHora
                );

        registro.setFechaHoraGeneracion(
                huellaService.formatearFechaHora(
                        fechaHora
                )
        );

        registro.setHuellaAnterior(
                null
        );

        registro.setHuella(
                huella
        );

        /*
         * =========================================================
         * GENERACIÓN LOCAL DEL XML
         * =========================================================
         */

        AeatRegistroAltaMapper mapper =
                new AeatRegistroAltaMapper();

        AeatSolicitudAltaXmlBuilder builder =
                new AeatSolicitudAltaXmlBuilder(
                        mapper
                );

        String xml =
                builder.generarXml(
                        registro
                );

        assertNotNull(
                xml
        );

        assertFalse(
                xml.isBlank()
        );

        assertTrue(
                xml.contains(
                        numeroFactura
                )
        );

        assertTrue(
                xml.contains(
                        "Gombeth Factura"
                )
        );

        assertTrue(
                xml.contains(
                        "21.40"
                )
        );

        assertTrue(
                xml.contains(
                        "7.00"
                )
        );

        /*
         * =========================================================
         * GUARDAR XML LOCALMENTE
         * =========================================================
         */

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
                        "alta-preview.xml"
                );

        Files.writeString(
                fichero,
                xml,
                StandardCharsets.UTF_8
        );

        /*
         * =========================================================
         * VALIDACIÓN CONTRA LOS XSD LOCALES
         * =========================================================
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
                )
        );

        SchemaFactory schemaFactory =
                SchemaFactory.newInstance(
                        XMLConstants.W3C_XML_SCHEMA_NS_URI
                );

        /*
         * Los XSD pueden importar otros XSD del mismo
         * directorio, pero no permitimos acceso HTTP/HTTPS.
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
         * Si el XML incumple el XSD, esta llamada lanza
         * una excepción y el test falla.
         */

        validator.validate(
                new StreamSource(
                        fichero.toFile()
                )
        );

        /*
         * No imprimimos el XML para evitar mostrar
         * NIF, nombres o huellas en la consola.
         */

        System.out.println(
                "VERIFACTU_PREVIEW_OK=true"
        );

        System.out.println(
                "VERIFACTU_PREVIEW_XSD_VALIDO=true"
        );

        System.out.println(
                "VERIFACTU_PREVIEW_RED=false"
        );

        System.out.println(
                "VERIFACTU_PREVIEW_SOAP=false"
        );

        System.out.println(
                "VERIFACTU_PREVIEW_CERTIFICADO=false"
        );

        System.out.println(
                "VERIFACTU_PREVIEW_BYTES="
                        + Files.size(
                                fichero
                        )
        );

        System.out.println(
                "VERIFACTU_PREVIEW_FICHERO="
                        + fichero
                        .toAbsolutePath()
                        .normalize()
        );
    }

    private String obligatorio(
            String variable) {

        String valor =
                System.getenv(
                        variable
                );

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalStateException(
                    "Falta la variable de entorno: "
                            + variable
            );
        }

        return valor.trim();
    }
}