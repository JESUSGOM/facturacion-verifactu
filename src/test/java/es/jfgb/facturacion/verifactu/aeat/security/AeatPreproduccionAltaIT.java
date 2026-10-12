package es.jfgb.facturacion.verifactu.aeat.security;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.jfgb.facturacion.verifactu.HuellaVerifactuService;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionDesglose;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import es.jfgb.facturacion.verifactu.aeat.AeatRegistroAltaMapper;
import es.jfgb.facturacion.verifactu.aeat.AeatSolicitudAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapPruebasGateway;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AeatPreproduccionAltaIT {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @EnabledIfEnvironmentVariable(
            named = "VERIFACTU_AEAT_CERT_SHA256",
            matches = ".+"
    )
    @EnabledIfEnvironmentVariable(
            named = "AEAT_PRE_ALTA_REAL_HABILITADA",
            matches = "SI"
    )
    void enviaRegistroAltaRealAlEntornoDePruebasAeat()
            throws Exception {

        /*
         * =========================================================
         * DATOS PRIVADOS DEL TEST
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
         * FECHA Y NUMERO UNICO DE PRUEBA
         * =========================================================
         */

        OffsetDateTime fechaHora =
                OffsetDateTime.now();

        String numeroFactura =
                "PRE-GF-"
                        + fechaHora.format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                        );

        /*
         * =========================================================
         * REGISTRO VERI*FACTU
         * =========================================================
         */

        RegistroFacturacion registro =
                new RegistroFacturacion();

        /*
         * El mapper exige que el registro tenga ID porque en
         * producción únicamente debe trabajar con snapshots
         * previamente persistidos.
         *
         * En este IT NO queremos escribir en la base de datos,
         * por lo que simulamos exclusivamente esa condición.
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
                999999999L
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        registro.setNifEmisor(
                nifObligado
        );

        registro.setNombreRazonEmisor(
                nombreObligado
        );

        registro.setSerie(
                "PRE"
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

        /*
         * PRUEBA TÉCNICA:
         *
         * Para no utilizar datos de otra persona,
         * utilizamos la identidad del propio obligado
         * también como destinatario.
         *
         * No debe tomarse como modelo funcional de
         * facturación real.
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

        registro.setDescripcionOperacion(
                "PRUEBA TECNICA GOMBETH FACTURA "
                        + "EN PREPRODUCCION AEAT"
        );

        /*
         * =========================================================
         * IMPORTES - IGIC 7 %
         * =========================================================
         */

        BigDecimal base =
                new BigDecimal(
                        "10.00"
                );

        BigDecimal cuota =
                new BigDecimal(
                        "0.70"
                );

        BigDecimal total =
                new BigDecimal(
                        "10.70"
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
         * DATOS DEL SISTEMA INFORMATICO
         * =========================================================
         */

        registro.setIdVersionAeat(
                "1.0"
        );

        /*
         * Los datos del productor del SIF se mantienen
         * separados de los datos del obligado tributario.
         */
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
                "PREPROD01"
        );

        registro.setSistemaSoloVerifactu(
                sistemaSoloVerifactu
        );

        /*
         * El sistema puede gestionar múltiples
         * obligados tributarios.
         */
        registro.setSistemaMultiOt(
                sistemaMultiOt
        );

        /*
         * En este test concreto solo estamos
         * operando con un obligado.
         */
        registro.setSistemaMultiplesOt(
                "N"
        );

        registro.setTipoHuella(
                "01"
        );

        /*
         * =========================================================
         * HUELLA OFICIAL
         * =========================================================
         *
         * No existe registro anterior porque este test
         * constituye una cadena independiente.
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
         * CONSTRUCCION CON NUESTRO CODIGO REAL
         * =========================================================
         */

        AeatRegistroAltaMapper mapper =
                new AeatRegistroAltaMapper();

        AeatSolicitudAltaXmlBuilder builder =
                new AeatSolicitudAltaXmlBuilder(
                        mapper
                );

        RegFactuSistemaFacturacion peticion =
                builder.construir(
                        registro
                );

        assertNotNull(
                peticion
        );

        assertNotNull(
                peticion.getCabecera()
        );

        assertEquals(
                1,
                peticion.getRegistroFactura().size()
        );

        /*
         * =========================================================
         * CLIENTE TLS
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

        /*
         * =========================================================
         * GATEWAY REAL DE GOMBETH FACTURA
         * =========================================================
         */

        AeatSoapPruebasGateway gateway =
                new AeatSoapPruebasGateway(
                        port
                );

        /*
         * =========================================================
         * PRIMER ALTA REAL EN PREPRODUCCION
         * =========================================================
         *
         * ESTA LINEA SI REALIZA UNA COMUNICACION SOAP CON AEAT.
         */

        AeatSoapResultadoAlta resultado =
                gateway.enviarAlta(
                        peticion.getCabecera(),
                        peticion
                                .getRegistroFactura()
                                .get(0)
                );

        assertNotNull(
                resultado,
                "AEAT no ha devuelto respuesta al alta"
        );

        assertNotNull(
                resultado.estadoEnvio(),
                "AEAT no ha informado EstadoEnvio"
        );

        assertEquals(
                1,
                resultado.respuestasLinea().size(),
                "Esperábamos una única respuesta de registro"
        );

        /*
         * No mostramos NIF, nombre, huella ni XML
         * en la consola.
         */

        System.out.println(
                "AEAT_ALTA_RESPUESTA_RECIBIDA=true"
        );

        System.out.println(
                "AEAT_ALTA_ESTADO_ENVIO="
                        + resultado.estadoEnvio()
        );

        System.out.println(
                "AEAT_ALTA_NUMERO_RESPUESTAS="
                        + resultado
                                .respuestasLinea()
                                .size()
        );

        System.out.println(
                "AEAT_ALTA_CSV_PRESENTE="
                        + (
                        resultado.csv() != null
                                && !resultado.csv().isBlank()
                )
        );

        Object linea =
                resultado
                        .respuestasLinea()
                        .get(0);

        System.out.println(
                "AEAT_ALTA_ESTADO_REGISTRO="
                        + leerPropiedad(
                                linea,
                                "getEstadoRegistro"
                        )
        );

        System.out.println(
                "AEAT_ALTA_CODIGO_ERROR="
                        + leerPropiedad(
                                linea,
                                "getCodigoErrorRegistro"
                        )
        );

        System.out.println(
                "AEAT_ALTA_DESCRIPCION_ERROR="
                        + leerPropiedad(
                                linea,
                                "getDescripcionErrorRegistro"
                        )
        );
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
                    "Falta la variable de entorno: "
                            + nombreVariable
            );
        }

        return valor.trim();
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

    private static String leerPropiedad(
            Object objeto,
            String metodo) {

        if (objeto == null) {
            return "NULL";
        }

        try {

            Object valor =
                    objeto
                            .getClass()
                            .getMethod(
                                    metodo
                            )
                            .invoke(
                                    objeto
                            );

            return valor == null
                    ? "NULL"
                    : valor.toString();

        } catch (Exception e) {

            return "NO_DISPONIBLE";
        }
    }
}