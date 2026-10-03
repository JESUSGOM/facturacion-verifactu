package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.PrimerRegistroCadenaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.SiNoType;
import es.jfgb.facturacion.verifactu.HuellaVerifactuService;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatRegistroAltaSemanticaTest {

    @Test
    void mapeaCorrectamenteAltaIgicPrimerRegistro()
            throws Exception {

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
                "SEMANTICA-GF-"
                        + fechaHora.format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                        );

        RegistroFacturacion registro =
                new RegistroFacturacion();

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
                999999996L
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        /*
         * =========================================================
         * FACTURA
         * =========================================================
         */

        registro.setNifEmisor(
                nifObligado
        );

        registro.setNombreRazonEmisor(
                nombreObligado
        );

        registro.setSerie(
                "SEMANTICA"
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
                "PRUEBA SEMANTICA "
                        + "GOMBETH FACTURA"
        );

        /*
         * Destinatario español.
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
         * IGIC 7 %
         * =========================================================
         */

        registro.setCuotaTotal(
                new BigDecimal(
                        "1.40"
                )
        );

        registro.setImporteTotal(
                new BigDecimal(
                        "21.40"
                )
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
                new BigDecimal(
                        "7.00"
                )
        );

        desglose.setBaseImponibleOImporteNoSujeto(
                new BigDecimal(
                        "20.00"
                )
        );

        desglose.setCuotaRepercutida(
                new BigDecimal(
                        "1.40"
                )
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
         * MAPEO AEAT
         * =========================================================
         */

        AeatRegistroAltaMapper mapper =
                new AeatRegistroAltaMapper();

        var alta =
                mapper.convertir(
                        registro
                );

        /*
         * =========================================================
         * IDENTIFICACIÓN / FACTURA
         * =========================================================
         */

        assertEquals(
                "1.0",
                alta.getIDVersion()
        );

        assertEquals(
                nifObligado,
                alta.getIDFactura()
                        .getIDEmisorFactura()
        );

        assertEquals(
                numeroFactura,
                alta.getIDFactura()
                        .getNumSerieFactura()
        );

        assertEquals(
                fechaHora.toLocalDate()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "dd-MM-yyyy"
                                )
                        ),
                alta.getIDFactura()
                        .getFechaExpedicionFactura()
        );

        assertEquals(
                "F1",
                alta.getTipoFactura()
                        .value()
        );

        assertEquals(
                nombreObligado,
                alta.getNombreRazonEmisor()
        );

        /*
         * =========================================================
         * DESTINATARIO
         * =========================================================
         */

        assertNotNull(
                alta.getDestinatarios()
        );

        assertEquals(
                1,
                alta.getDestinatarios()
                        .getIDDestinatario()
                        .size()
        );

        var destinatario =
                alta.getDestinatarios()
                        .getIDDestinatario()
                        .getFirst();

        assertEquals(
                nifObligado,
                destinatario.getNIF()
        );

        assertEquals(
                nombreObligado,
                destinatario.getNombreRazon()
        );

        /*
         * =========================================================
         * DESGLOSE IGIC
         * =========================================================
         */

        assertNotNull(
                alta.getDesglose()
        );

        assertEquals(
                1,
                alta.getDesglose()
                        .getDetalleDesglose()
                        .size()
        );

        var detalle =
                alta.getDesglose()
                        .getDetalleDesglose()
                        .getFirst();

        assertEquals(
                "03",
                detalle.getImpuesto()
        );

        assertEquals(
                "01",
                detalle.getClaveRegimen()
        );

        assertEquals(
                "S1",
                detalle.getCalificacionOperacion()
                        .value()
        );

        assertEquals(
                "7.00",
                detalle.getTipoImpositivo()
        );

        assertEquals(
                "20.00",
                detalle
                        .getBaseImponibleOimporteNoSujeto()
        );

        assertEquals(
                "1.40",
                detalle.getCuotaRepercutida()
        );

        assertEquals(
                "1.40",
                alta.getCuotaTotal()
        );

        assertEquals(
                "21.40",
                alta.getImporteTotal()
        );

        /*
         * =========================================================
         * ENCADENAMIENTO
         * =========================================================
         *
         * No hemos indicado registro anterior:
         * debe tratarse como PRIMER REGISTRO.
         */

        assertNotNull(
                alta.getEncadenamiento()
        );

        assertEquals(
                PrimerRegistroCadenaType.S,
                alta.getEncadenamiento()
                        .getPrimerRegistro()
        );

        /*
         * =========================================================
         * SISTEMA INFORMÁTICO
         * =========================================================
         */

        var sistema =
                alta.getSistemaInformatico();

        assertNotNull(
                sistema
        );

        assertEquals(
                sistemaNombreRazon,
                sistema.getNombreRazon()
        );

        assertEquals(
                sistemaNif,
                sistema.getNIF()
        );

        assertEquals(
                "Gombeth Factura",
                sistema.getNombreSistemaInformatico()
        );

        assertEquals(
                "GF",
                sistema.getIdSistemaInformatico()
        );

        assertEquals(
                "0.0.1-SNAPSHOT",
                sistema.getVersion()
        );

        assertEquals(
                "PREPROD01",
                sistema.getNumeroInstalacion()
        );

        assertEquals(
                SiNoType.S,
                sistema.getTipoUsoPosibleSoloVerifactu()
        );

        assertEquals(
                SiNoType.S,
                sistema.getTipoUsoPosibleMultiOT()
        );

        assertEquals(
                SiNoType.N,
                sistema.getIndicadorMultiplesOT()
        );

        /*
         * =========================================================
         * HUELLA
         * =========================================================
         */

        assertEquals(
                "01",
                alta.getTipoHuella()
        );

        assertEquals(
                huella,
                alta.getHuella()
        );

        assertTrue(
                huella.matches(
                        "[0-9A-F]{64}"
                )
        );

        assertNotNull(
                alta.getFechaHoraHusoGenRegistro()
        );

        assertFalse(
                alta.getFechaHoraHusoGenRegistro()
                        .toXMLFormat()
                        .isBlank()
        );

        /*
         * NO se realiza ninguna comunicación.
         */

        System.out.println(
                "VERIFACTU_SEMANTICA_OK=true"
        );

        System.out.println(
                "VERIFACTU_SEMANTICA_FACTURA_F1=true"
        );

        System.out.println(
                "VERIFACTU_SEMANTICA_IGIC_03=true"
        );

        System.out.println(
                "VERIFACTU_SEMANTICA_PRIMER_REGISTRO=true"
        );

        System.out.println(
                "VERIFACTU_SEMANTICA_SIF=true"
        );

        System.out.println(
                "VERIFACTU_SEMANTICA_HUELLA_SHA256=true"
        );

        System.out.println(
                "VERIFACTU_SEMANTICA_RED=false"
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