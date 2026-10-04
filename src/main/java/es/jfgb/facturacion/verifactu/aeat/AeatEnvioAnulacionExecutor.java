package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapGateway;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;

import java.util.List;

/**
 * Ejecuta el flujo AEAT correspondiente a un
 * RegistroAnulacion.
 *
 * IMPORTANTE:
 *
 * Esta clase no es un bean de Spring.
 * Mientras no se conecte expresamente desde
 * AeatPreproduccionConfiguration, el envio real
 * de anulaciones permanece bloqueado.
 */
public class AeatEnvioAnulacionExecutor {

    private final AeatSolicitudAnulacionXmlBuilder
            solicitudAnulacionXmlBuilder;

    private final AeatSoapGateway
            soapGateway;

    private final AeatRespuestaAltaXmlBuilder
            respuestaXmlBuilder;

    private final AeatRespuestaAltaValidator
            respuestaValidator;

    private final AeatResultadoEnvioMapper
            resultadoEnvioMapper;

    public AeatEnvioAnulacionExecutor(
            AeatSolicitudAnulacionXmlBuilder solicitudAnulacionXmlBuilder,
            AeatSoapGateway soapGateway,
            AeatRespuestaAltaXmlBuilder respuestaXmlBuilder,
            AeatRespuestaAltaValidator respuestaValidator,
            AeatResultadoEnvioMapper resultadoEnvioMapper) {

        if (solicitudAnulacionXmlBuilder == null) {
            throw new IllegalArgumentException(
                    "AeatSolicitudAnulacionXmlBuilder "
                            + "no puede ser nulo"
            );
        }

        if (soapGateway == null) {
            throw new IllegalArgumentException(
                    "AeatSoapGateway no puede ser nulo"
            );
        }

        if (respuestaXmlBuilder == null) {
            throw new IllegalArgumentException(
                    "AeatRespuestaAltaXmlBuilder "
                            + "no puede ser nulo"
            );
        }

        if (respuestaValidator == null) {
            throw new IllegalArgumentException(
                    "AeatRespuestaAltaValidator "
                            + "no puede ser nulo"
            );
        }

        if (resultadoEnvioMapper == null) {
            throw new IllegalArgumentException(
                    "AeatResultadoEnvioMapper "
                            + "no puede ser nulo"
            );
        }

        this.solicitudAnulacionXmlBuilder =
                solicitudAnulacionXmlBuilder;

        this.soapGateway =
                soapGateway;

        this.respuestaXmlBuilder =
                respuestaXmlBuilder;

        this.respuestaValidator =
                respuestaValidator;

        this.resultadoEnvioMapper =
                resultadoEnvioMapper;
    }

    public ResultadoEnvioVerifactu enviar(
            RegistroFacturacion registro) {

        /*
         * =====================================================
         * PREPARACION LOCAL
         * =====================================================
         */

        RegFactuSistemaFacturacion peticion =
                solicitudAnulacionXmlBuilder
                        .construir(
                                registro
                        );

        String peticionXml =
                solicitudAnulacionXmlBuilder
                        .generarXml(
                                registro
                        );

        if (peticion.getCabecera() == null) {

            throw new IllegalStateException(
                    "La peticion AEAT no contiene Cabecera"
            );
        }

        List<RegistroFacturaType> registros =
                peticion.getRegistroFactura();

        if (registros.size() != 1) {

            throw new IllegalStateException(
                    "La peticion AEAT debe contener "
                            + "exactamente un registro de factura"
            );
        }

        RegistroFacturaType registroFactura =
                registros.get(0);

        /*
         * Proteccion semantica:
         * esta ruta solamente admite RegistroAnulacion.
         */
        if (registroFactura.getRegistroAnulacion()
                == null) {

            throw new IllegalStateException(
                    "La peticion AEAT no contiene "
                            + "RegistroAnulacion"
            );
        }

        if (registroFactura.getRegistroAlta()
                != null) {

            throw new IllegalStateException(
                    "Una peticion de anulacion no puede "
                            + "contener RegistroAlta"
            );
        }

        /*
         * =====================================================
         * LLAMADA SOAP
         * =====================================================
         *
         * El nombre enviarAlta() del gateway es historico.
         * La operacion SOAP subyacente es la operacion comun
         * RegFactuSistemaFacturacion y recibe RegistroFacturaType,
         * que puede contener RegistroAlta o RegistroAnulacion.
         */

        AeatSoapResultadoAlta resultadoSoap;

        try {

            resultadoSoap =
                    soapGateway.enviarAlta(
                            peticion.getCabecera(),
                            registroFactura
                    );

        } catch (EnvioVerifactuInciertoException e) {

            throw e;

        } catch (Exception e) {

            throw new EnvioVerifactuInciertoException(
                    "Se produjo un error durante la comunicacion "
                            + "con AEAT y no puede determinarse "
                            + "con certeza si el registro "
                            + "de anulacion fue recibido",
                    e
            );
        }

        if (resultadoSoap == null) {

            throw new EnvioVerifactuInciertoException(
                    "AEAT no devolvio una respuesta interpretable"
            );
        }

        /*
         * =====================================================
         * CONSERVACION DE RESPUESTA
         * =====================================================
         */

        String respuestaXml;

        try {

            respuestaXml =
                    respuestaXmlBuilder.generarXml(
                            resultadoSoap
                    );

        } catch (Exception e) {

            throw new EnvioVerifactuInciertoException(
                    "AEAT respondio, pero no se pudo conservar "
                            + "correctamente su respuesta",
                    e
            );
        }

        List<RespuestaExpedidaType> respuestasLinea =
                resultadoSoap.respuestasLinea();

        if (respuestasLinea.size() != 1) {

            return crearResultadoReconciliacion(
                    resultadoSoap,
                    peticionXml,
                    respuestaXml,
                    "AEAT devolvio "
                            + respuestasLinea.size()
                            + " lineas de respuesta para una "
                            + "peticion con un unico registro"
            );
        }

        /*
         * La estructura de respuesta del servicio
         * RegFactuSistemaFacturacion es comun a ambos
         * tipos de registro.
         */
        if (!respuestaValidator.esCoherente(
                resultadoSoap
        )) {

            return crearResultadoReconciliacion(
                    resultadoSoap,
                    peticionXml,
                    respuestaXml,
                    "La respuesta de AEAT presenta una "
                            + "incoherencia entre EstadoEnvio "
                            + "y EstadoRegistro"
            );
        }

        try {

            return resultadoEnvioMapper.convertir(
                    resultadoSoap.csv(),
                    peticionXml,
                    respuestaXml,
                    respuestasLinea.get(0)
            );

        } catch (Exception e) {

            throw new EnvioVerifactuInciertoException(
                    "AEAT respondio, pero no se pudo determinar "
                            + "con seguridad el resultado "
                            + "del registro de anulacion",
                    e
            );
        }
    }

    private ResultadoEnvioVerifactu
    crearResultadoReconciliacion(
            AeatSoapResultadoAlta resultadoSoap,
            String peticionXml,
            String respuestaXml,
            String mensaje) {

        return new ResultadoEnvioVerifactu(
                EstadoResultadoEnvioVerifactu
                        .REQUIERE_RECONCILIACION,
                normalizar(
                        resultadoSoap.csv()
                ),
                peticionXml,
                respuestaXml,
                mensaje
        );
    }

    private String normalizar(
            String valor) {

        if (valor == null
                || valor.isBlank()) {

            return null;
        }

        return valor.trim();
    }
}