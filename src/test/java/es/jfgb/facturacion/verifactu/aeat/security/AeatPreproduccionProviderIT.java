package es.jfgb.facturacion.verifactu.aeat.security;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfVerifactu;
import es.jfgb.facturacion.verifactu.HuellaVerifactuService;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import es.jfgb.facturacion.verifactu.aeat.AeatRegistroAltaMapper;
import es.jfgb.facturacion.verifactu.aeat.AeatRespuestaAltaInterpreter;
import es.jfgb.facturacion.verifactu.aeat.AeatRespuestaAltaValidator;
import es.jfgb.facturacion.verifactu.aeat.AeatRespuestaAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.AeatResultadoEnvioMapper;
import es.jfgb.facturacion.verifactu.aeat.AeatSolicitudAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.AeatVerifactuProvider;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapPruebasGateway;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeatPreproduccionProviderIT {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    @EnabledIfEnvironmentVariable(
            named = "AEAT_PRE_PROVIDER_REAL_HABILITADO",
            matches = "SI"
    )
    void enviaAltaMedianteAeatVerifactuProvider()
            throws Exception {

        /*
         * =========================================================
         * VARIABLES PRIVADAS
         * =========================================================
         */

        String huellaCertificado =
                System.getenv(
                        "VERIFACTU_AEAT_CERT_SHA256"
                );

        String nombreObligado =
                obligatorio(
                        "AEAT_TEST_OBLIGADO_NOMBRE"
                );

        String nifObligado =
                obligatorioNif(
                        "AEAT_TEST_OBLIGADO_NIF"
                );

        String sistemaNombreRazon =
                obligatorio(
                        "VERIFACTU_SISTEMA_NOMBRE_RAZON"
                );

        String sistemaNif =
                obligatorioNif(
                        "VERIFACTU_SISTEMA_NIF"
                );

        String sistemaNombre =
                obligatorio(
                        "VERIFACTU_SISTEMA_NOMBRE"
                );

        String sistemaId =
                obligatorio(
                        "VERIFACTU_SISTEMA_ID"
                );

        String sistemaVersion =
                obligatorio(
                        "VERIFACTU_SISTEMA_VERSION"
                );

        String sistemaSoloVerifactu =
                obligatorioSN(
                        "VERIFACTU_SISTEMA_SOLO_VERIFACTU"
                );

        String sistemaMultiOt =
                obligatorioSN(
                        "VERIFACTU_SISTEMA_MULTI_OT"
                );

        /*
         * =========================================================
         * REGISTRO SINTETICO DE PREPRODUCCION
         * =========================================================
         */

        OffsetDateTime fechaHora =
                OffsetDateTime.now();

        String numeroFactura =
                "PRE-PROVIDER-GF-"
                        + fechaHora.format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                        );

        /*
         * Cada ejecución representa una instalación
         * técnica independiente de preproducción.
         */
        String numeroInstalacion =
                "PREIT-PROVIDER-"
                        + fechaHora.format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                        );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        /*
         * El mapper exige un snapshot ya persistido.
         *
         * Este test NO utiliza MySQL, por lo que asignamos
         * únicamente un ID ficticio en memoria.
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
                999999998L
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        /*
         * =========================================================
         * OBLIGADO / FACTURA
         * =========================================================
         */

        registro.setNifEmisor(
                nifObligado
        );

        registro.setNombreRazonEmisor(
                nombreObligado
        );

        registro.setSerie(
                "PREPROVIDER"
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
                "PRUEBA INTEGRACION PROVIDER "
                        + "GOMBETH FACTURA PREPRODUCCION"
        );

        /*
         * Para evitar datos de terceras personas,
         * en esta prueba técnica utilizamos al propio
         * obligado también como destinatario.
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
         * SISTEMA INFORMATICO
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
                sistemaNombre
        );

        registro.setSistemaId(
                sistemaId
        );

        registro.setSistemaVersion(
                sistemaVersion
        );

        registro.setSistemaNumeroInstalacion(
                numeroInstalacion
        );

        registro.setSistemaSoloVerifactu(
                sistemaSoloVerifactu
        );

        registro.setSistemaMultiOt(
                sistemaMultiOt
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

        String huellaRegistro =
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
                huellaRegistro
        );

        /*
         * =========================================================
         * INFRAESTRUCTURA SOAP
         * =========================================================
         */

        AeatCertificadoWindowsSelector selector =
                new AeatCertificadoWindowsSelector(
                        huellaCertificado
                );

        AeatSslContextFactory sslContextFactory =
                new AeatSslContextFactory(
                        selector
                );

        AeatSoapTlsConfigurer tlsConfigurer =
                new AeatSoapTlsConfigurer(
                        sslContextFactory
                );

        SfVerifactu servicio =
                new SfVerifactu();

        SfPortTypeVerifactu port =
                servicio.getSistemaVerifactuPruebas();

        tlsConfigurer.configurar(
                port
        );

        AeatSoapPruebasGateway gateway =
                new AeatSoapPruebasGateway(
                        port
                );

        /*
         * =========================================================
         * PROVIDER REAL
         * =========================================================
         */

        AeatRegistroAltaMapper registroAltaMapper =
                new AeatRegistroAltaMapper();

        AeatSolicitudAltaXmlBuilder solicitudAltaXmlBuilder =
                new AeatSolicitudAltaXmlBuilder(
                        registroAltaMapper
                );

        AeatRespuestaAltaXmlBuilder respuestaAltaXmlBuilder =
                new AeatRespuestaAltaXmlBuilder();

        AeatRespuestaAltaValidator respuestaAltaValidator =
                new AeatRespuestaAltaValidator();

        AeatRespuestaAltaInterpreter interpreter =
                new AeatRespuestaAltaInterpreter();

        AeatResultadoEnvioMapper resultadoEnvioMapper =
                new AeatResultadoEnvioMapper(
                        interpreter
                );

        AeatVerifactuProvider provider =
                new AeatVerifactuProvider(
                        solicitudAltaXmlBuilder,
                        gateway,
                        respuestaAltaXmlBuilder,
                        respuestaAltaValidator,
                        resultadoEnvioMapper
                );

        assertEquals(
                "AEAT-VERIFACTU-PRUEBAS",
                provider.getNombre()
        );

        /*
         * =========================================================
         * IDENTIFICADORES TECNICOS NO SENSIBLES
         *
         * Se imprimen ANTES del envio para poder identificar
         * exactamente el intento incluso si hubiera un resultado
         * de transporte incierto.
         * =========================================================
         */

        System.out.println(
                "PROVIDER_AEAT_NUM_SERIE_FACTURA="
                        + numeroFactura
        );

        System.out.println(
                "PROVIDER_AEAT_FECHA_EXPEDICION="
                        + registro.getFechaExpedicion()
        );

        System.out.println(
                "PROVIDER_AEAT_NUMERO_INSTALACION="
                        + numeroInstalacion
        );

        /*
         * =========================================================
         * ESTA LINEA REALIZA EL ALTA EN PREPRODUCCION
         * =========================================================
         */

        ResultadoEnvioVerifactu resultado =
                provider.enviarAlta(
                        registro
                );

        /*
         * =========================================================
         * RESULTADO FUNCIONAL DE GOMBETH FACTURA
         * =========================================================
         */

        assertNotNull(
                resultado
        );

        assertEquals(
                EstadoResultadoEnvioVerifactu.ACEPTADO,
                resultado.estado()
        );

        assertTrue(
                resultado.aceptado()
        );

        assertFalse(
                resultado.requiereReconciliacion()
        );

        assertNotNull(
                resultado.identificadorExterno()
        );

        assertFalse(
                resultado.identificadorExterno().isBlank()
        );

        assertNotNull(
                resultado.peticion()
        );

        assertFalse(
                resultado.peticion().isBlank()
        );

        assertNotNull(
                resultado.respuesta()
        );

        assertFalse(
                resultado.respuesta().isBlank()
        );

        assertNotNull(
                resultado.mensaje()
        );

        assertFalse(
                resultado.mensaje().isBlank()
        );

        /*
         * No imprimimos XML, NIF, nombres, huellas ni CSV.
         */

        System.out.println(
                "PROVIDER_AEAT_OK=true"
        );

        System.out.println(
                "PROVIDER_AEAT_ESTADO="
                        + resultado.estado()
        );

        System.out.println(
                "PROVIDER_AEAT_ACEPTADO="
                        + resultado.aceptado()
        );

        System.out.println(
                "PROVIDER_AEAT_RECONCILIACION="
                        + resultado.requiereReconciliacion()
        );

        System.out.println(
                "PROVIDER_AEAT_CSV_PRESENTE="
                        + (
                        resultado.identificadorExterno() != null
                                && !resultado
                                .identificadorExterno()
                                .isBlank()
                )
        );

        System.out.println(
                "PROVIDER_AEAT_PETICION_GUARDABLE="
                        + (
                        resultado.peticion() != null
                                && !resultado
                                .peticion()
                                .isBlank()
                )
        );

        System.out.println(
                "PROVIDER_AEAT_RESPUESTA_GUARDABLE="
                        + (
                        resultado.respuesta() != null
                                && !resultado
                                .respuesta()
                                .isBlank()
                )
        );

        System.out.println(
                "PROVIDER_AEAT_MENSAJE="
                        + resultado.mensaje()
        );
    }

    private static String obligatorioNif(
            String nombreVariable) {

        String valor =
                obligatorio(
                        nombreVariable
                ).toUpperCase();

        if (!valor.matches(
                "[0-9A-Z]{9}"
        )) {

            throw new IllegalStateException(
                    nombreVariable
                            + " debe contener exactamente "
                            + "9 caracteres alfanumericos"
            );
        }

        return valor;
    }

    private static String obligatorioSN(
            String nombreVariable) {

        String valor =
                obligatorio(
                        nombreVariable
                ).toUpperCase();

        if (!"S".equals(valor)
                && !"N".equals(valor)) {

            throw new IllegalStateException(
                    nombreVariable
                            + " debe contener S o N"
            );
        }

        return valor;
    }

    private static String obligatorio(
            String nombreVariable) {

        String valor =
                System.getenv(
                        nombreVariable
                );

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalStateException(
                    "Falta la variable de entorno "
                            + nombreVariable
            );
        }

        return valor.trim();
    }
}