package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.IDFacturaARType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroFacturacionAltaType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.RegistroFacturacionSustituida;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatRegistroAltaF3MapperTest {

    private final AeatRegistroAltaMapper mapper =
            new AeatRegistroAltaMapper();

    private final AeatSolicitudAltaXmlBuilder builder =
            new AeatSolicitudAltaXmlBuilder(
                    mapper
            );

    @Test
    void mapeaF3ConFacturaSustituida()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistroF3();

        RegistroFacturacionSustituida sustituida =
                crearFacturaSustituida();

        registro.addFacturaSustituida(
                sustituida
        );

        RegistroFacturacionAltaType alta =
                mapper.convertir(
                        registro
                );

        assertEquals(
                "F3",
                alta.getTipoFactura().value()
        );

        assertNotNull(
                alta.getDestinatarios()
        );

        assertNull(
                alta.getTipoRectificativa()
        );

        assertNull(
                alta.getFacturasRectificadas()
        );

        assertNull(
                alta.getImporteRectificacion()
        );

        assertNotNull(
                alta.getFacturasSustituidas()
        );

        assertEquals(
                1,
                alta.getFacturasSustituidas()
                        .getIDFacturaSustituida()
                        .size()
        );

        IDFacturaARType original =
                alta.getFacturasSustituidas()
                        .getIDFacturaSustituida()
                        .getFirst();

        assertEquals(
                "B12345678",
                original.getIDEmisorFactura()
        );

        assertEquals(
                "SIM-25",
                original.getNumSerieFactura()
        );

        assertEquals(
                "01-10-2026",
                original.getFechaExpedicionFactura()
        );
    }

    @Test
    void mapeaF3SinIdentificarFacturasSustituidas()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistroF3();

        RegistroFacturacionAltaType alta =
                mapper.convertir(
                        registro
                );

        assertEquals(
                "F3",
                alta.getTipoFactura().value()
        );

        /*
         * La identificacion individual de las F2
         * sustituidas es opcional.
         */
        assertNull(
                alta.getFacturasSustituidas()
        );

        assertNotNull(
                alta.getDestinatarios()
        );
    }

    @Test
    void rechazaFacturasSustituidasEnTipoDistintoDeF3()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistroF3();

        registro.setTipoFactura(
                "F1"
        );

        registro.addFacturaSustituida(
                crearFacturaSustituida()
        );

        IllegalStateException excepcion =
                assertThrows(
                        IllegalStateException.class,
                        () -> mapper.convertir(
                                registro
                        )
                );

        assertTrue(
                excepcion.getMessage()
                        .contains(
                                "Solo una factura F3"
                        )
        );
    }

    @Test
    void generaXmlF3ConFacturasSustituidasValido()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistroF3();

        registro.addFacturaSustituida(
                crearFacturaSustituida()
        );

        String xml =
                builder.generarXml(
                        registro
                );

        assertNotNull(
                xml
        );

        assertTrue(
                xml.contains(
                        ">F3<"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "FacturasSustituidas"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "IDFacturaSustituida"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "SIM-25"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "01-10-2026"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "12345678Z"
                ),
                xml
        );

        assertFalse(
                xml.contains(
                        "FacturasRectificadas"
                ),
                xml
        );

        assertFalse(
                xml.contains(
                        "TipoRectificativa"
                ),
                xml
        );

        AeatXmlSchemaValidator validator =
                new AeatXmlSchemaValidator();

        assertDoesNotThrow(
                () -> validator.validar(
                        xml
                )
        );

        System.out.println(
                "VERIFACTU_F3_SUSTITUIDAS_XSD_VALIDO=true"
        );

        System.out.println(
                "VERIFACTU_F3_RED=false"
        );
    }


    @Test
    void generaXmlF3SinFacturasSustituidasValido()
            throws Exception {

        RegistroFacturacion registro =
                crearRegistroF3();

        String xml =
                builder.generarXml(
                        registro
                );

        assertNotNull(
                xml
        );

        assertTrue(
                xml.contains(
                        ">F3<"
                ),
                xml
        );

        assertFalse(
                xml.contains(
                        "FacturasSustituidas"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "Destinatarios"
                ),
                xml
        );

        AeatXmlSchemaValidator validator =
                new AeatXmlSchemaValidator();

        assertDoesNotThrow(
                () -> validator.validar(
                        xml
                )
        );

        System.out.println(
                "VERIFACTU_F3_SIN_SUSTITUIDAS_XSD_VALIDO=true"
        );

        System.out.println(
                "VERIFACTU_F3_RED=false"
        );
    }

    private RegistroFacturacion crearRegistroF3()
            throws Exception {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        asignarId(
                registro,
                900000003L
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
                "FAC"
        );

        registro.setNumero(
                100L
        );

        registro.setNumSerieFactura(
                "FAC-100"
        );

        registro.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        8
                )
        );

        registro.setTipoFactura(
                "F3"
        );

        registro.setDescripcionOperacion(
                "FACTURA EN SUSTITUCION DE SIMPLIFICADA"
        );

        /*
         * F3 identifica al destinatario.
         */
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
                new BigDecimal("100.00")
        );

        desglose.setCuotaRepercutida(
                new BigDecimal("7.00")
        );

        registro.addDesglose(
                desglose
        );

        registro.setCuotaTotal(
                new BigDecimal("7.00")
        );

        registro.setImporteTotal(
                new BigDecimal("107.00")
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
                "2026-10-08T13:00:00+01:00"
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

    private RegistroFacturacionSustituida
    crearFacturaSustituida() {

        RegistroFacturacionSustituida sustituida =
                new RegistroFacturacionSustituida();

        sustituida.setFacturaSustituidaId(
                25L
        );

        sustituida.setOrden(
                (short) 1
        );

        sustituida.setNifEmisor(
                "B12345678"
        );

        sustituida.setNumSerieFactura(
                "SIM-25"
        );

        sustituida.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        return sustituida;
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
