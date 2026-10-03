package es.jfgb.facturacion.verifactu.aeat;

import es.jfgb.facturacion.verifactu.HuellaVerifactuService;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatRegistroAltaEncadenamientoTest {

    @Test
    void mapeaRegistroAnteriorYCalculaHuellaEncadenada()
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

        HuellaVerifactuService huellaService =
                new HuellaVerifactuService();

        /*
         * =========================================================
         * REGISTRO ANTERIOR
         * =========================================================
         */

        OffsetDateTime fechaAnterior =
                OffsetDateTime.now()
                        .minusMinutes(5);

        RegistroFacturacion anterior =
                new RegistroFacturacion();

        asignarId(
                anterior,
                999999994L
        );

        anterior.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        anterior.setNifEmisor(
                nifObligado
        );

        anterior.setNombreRazonEmisor(
                nombreObligado
        );

        anterior.setNumSerieFactura(
                "CADENA-GF-ANTERIOR"
        );

        anterior.setFechaExpedicion(
                LocalDate.now()
                        .minusDays(1)
        );

        anterior.setTipoFactura(
                "F1"
        );

        anterior.setCuotaTotal(
                new BigDecimal(
                        "0.70"
                )
        );

        anterior.setImporteTotal(
                new BigDecimal(
                        "10.70"
                )
        );

        String huellaAnterior =
                huellaService.calcularAlta(
                        anterior.getNifEmisor(),
                        anterior.getNumSerieFactura(),
                        anterior.getFechaExpedicion(),
                        anterior.getTipoFactura(),
                        anterior.getCuotaTotal(),
                        anterior.getImporteTotal(),
                        null,
                        fechaAnterior
                );

        anterior.setHuella(
                huellaAnterior
        );

        anterior.setFechaHoraGeneracion(
                huellaService.formatearFechaHora(
                        fechaAnterior
                )
        );

        /*
         * =========================================================
         * REGISTRO ACTUAL
         * =========================================================
         */

        OffsetDateTime fechaActual =
                OffsetDateTime.now();

        RegistroFacturacion actual =
                new RegistroFacturacion();

        asignarId(
                actual,
                999999995L
        );

        actual.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        actual.setNifEmisor(
                nifObligado
        );

        actual.setNombreRazonEmisor(
                nombreObligado
        );

        actual.setSerie(
                "CADENA"
        );

        actual.setNumero(
                System.currentTimeMillis()
        );

        actual.setNumSerieFactura(
                "CADENA-GF-ACTUAL"
        );

        actual.setFechaExpedicion(
                LocalDate.now()
        );

        actual.setTipoFactura(
                "F1"
        );

        actual.setDescripcionOperacion(
                "PRUEBA ENCADENAMIENTO "
                        + "GOMBETH FACTURA"
        );

        /*
         * Destinatario.
         */

        actual.setDestinatarioNombre(
                nombreObligado
        );

        actual.setDestinatarioNif(
                nifObligado
        );

        actual.setDestinatarioCodigoPais(
                "ES"
        );

        /*
         * =========================================================
         * DESGLOSE IGIC 7 %
         * =========================================================
         */

        actual.setCuotaTotal(
                new BigDecimal(
                        "1.40"
                )
        );

        actual.setImporteTotal(
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

        actual.addDesglose(
                desglose
        );

        /*
         * =========================================================
         * SISTEMA INFORMÁTICO
         * =========================================================
         */

        actual.setIdVersionAeat(
                "1.0"
        );

        actual.setSistemaNombreRazon(
                sistemaNombreRazon
        );

        actual.setSistemaNif(
                sistemaNif
        );

        actual.setSistemaNombre(
                "Gombeth Factura"
        );

        actual.setSistemaId(
                "GF"
        );

        actual.setSistemaVersion(
                "0.0.1-SNAPSHOT"
        );

        actual.setSistemaNumeroInstalacion(
                "PREPROD01"
        );

        actual.setSistemaSoloVerifactu(
                "S"
        );

        actual.setSistemaMultiOt(
                "S"
        );

        actual.setSistemaMultiplesOt(
                "N"
        );

        actual.setTipoHuella(
                "01"
        );

        /*
         * =========================================================
         * ENCADENAMIENTO
         * =========================================================
         */

        actual.setRegistroAnterior(
                anterior
        );

        actual.setHuellaAnterior(
                huellaAnterior
        );

        String huellaActual =
                huellaService.calcularAlta(
                        actual.getNifEmisor(),
                        actual.getNumSerieFactura(),
                        actual.getFechaExpedicion(),
                        actual.getTipoFactura(),
                        actual.getCuotaTotal(),
                        actual.getImporteTotal(),
                        huellaAnterior,
                        fechaActual
                );

        actual.setHuella(
                huellaActual
        );

        actual.setFechaHoraGeneracion(
                huellaService.formatearFechaHora(
                        fechaActual
                )
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
                        actual
                );

        /*
         * Ya NO debe marcarse como primer registro.
         */

        assertNotNull(
                alta.getEncadenamiento()
        );

        assertNull(
                alta.getEncadenamiento()
                        .getPrimerRegistro()
        );

        /*
         * Debe existir RegistroAnterior.
         */

        var registroAnteriorAeat =
                alta.getEncadenamiento()
                        .getRegistroAnterior();

        assertNotNull(
                registroAnteriorAeat
        );

        /*
         * =========================================================
         * DATOS EXACTOS DEL REGISTRO ANTERIOR
         * =========================================================
         */

        assertEquals(
                nifObligado,
                registroAnteriorAeat
                        .getIDEmisorFactura()
        );

        assertEquals(
                "CADENA-GF-ANTERIOR",
                registroAnteriorAeat
                        .getNumSerieFactura()
        );

        assertEquals(
                anterior.getFechaExpedicion()
                        .format(
                                java.time.format
                                        .DateTimeFormatter
                                        .ofPattern(
                                                "dd-MM-yyyy"
                                        )
                        ),
                registroAnteriorAeat
                        .getFechaExpedicionFactura()
        );

        assertEquals(
                huellaAnterior,
                registroAnteriorAeat
                        .getHuella()
        );

        /*
         * =========================================================
         * HUELLA DEL REGISTRO ACTUAL
         * =========================================================
         */

        assertEquals(
                huellaAnterior,
                actual.getHuellaAnterior()
        );

        assertEquals(
                huellaActual,
                alta.getHuella()
        );

        assertTrue(
                huellaAnterior.matches(
                        "[0-9A-F]{64}"
                )
        );

        assertTrue(
                huellaActual.matches(
                        "[0-9A-F]{64}"
                )
        );

        /*
         * Las dos huellas no pueden ser iguales.
         */

        assertTrue(
                !huellaAnterior.equals(
                        huellaActual
                )
        );

        /*
         * =========================================================
         * RESULTADO
         * =========================================================
         */

        System.out.println(
                "VERIFACTU_CADENA_OK=true"
        );

        System.out.println(
                "VERIFACTU_CADENA_PRIMER_REGISTRO=false"
        );

        System.out.println(
                "VERIFACTU_CADENA_REGISTRO_ANTERIOR=true"
        );

        System.out.println(
                "VERIFACTU_CADENA_NIF_ANTERIOR=true"
        );

        System.out.println(
                "VERIFACTU_CADENA_NUMERO_ANTERIOR=true"
        );

        System.out.println(
                "VERIFACTU_CADENA_FECHA_ANTERIOR=true"
        );

        System.out.println(
                "VERIFACTU_CADENA_HUELLA_ANTERIOR=true"
        );

        System.out.println(
                "VERIFACTU_CADENA_HUELLA_ACTUAL=true"
        );

        System.out.println(
                "VERIFACTU_CADENA_RED=false"
        );
    }

    private void asignarId(
            RegistroFacturacion registro,
            Long id)
            throws Exception {

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
                id
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