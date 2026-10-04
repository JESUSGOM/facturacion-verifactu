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
import es.jfgb.facturacion.verifactu.provider.VerifactuProvider;

import java.util.List;

public class AeatVerifactuProvider
        implements VerifactuProvider {

    private final AeatSolicitudAltaXmlBuilder
            solicitudAltaXmlBuilder;

    private final AeatSoapGateway
            soapGateway;

    private final AeatRespuestaAltaXmlBuilder
            respuestaAltaXmlBuilder;

    private final AeatRespuestaAltaValidator
            respuestaAltaValidator;

    private final AeatResultadoEnvioMapper
            resultadoEnvioMapper;

    /*
     * Permanece null cuando se utiliza el constructor
     * historico de cinco argumentos.
     *
     * De esta forma el contexto aeat-pre actual NO puede
     * enviar anulaciones todavia.
     */
    private AeatEnvioAnulacionExecutor
            envioAnulacionExecutor;

    public AeatVerifactuProvider(
            AeatSolicitudAltaXmlBuilder solicitudAltaXmlBuilder,
            AeatSoapGateway soapGateway,
            AeatRespuestaAltaXmlBuilder respuestaAltaXmlBuilder,
            AeatRespuestaAltaValidator respuestaAltaValidator,
            AeatResultadoEnvioMapper resultadoEnvioMapper) {

        if (solicitudAltaXmlBuilder == null) {
            throw new IllegalArgumentException(
                    "AeatSolicitudAltaXmlBuilder no puede ser nulo"
            );
        }

        if (soapGateway == null) {
            throw new IllegalArgumentException(
                    "AeatSoapGateway no puede ser nulo"
            );
        }

        if (respuestaAltaXmlBuilder == null) {
            throw new IllegalArgumentException(
                    "AeatRespuestaAltaXmlBuilder no puede ser nulo"
            );
        }

        if (respuestaAltaValidator == null) {
            throw new IllegalArgumentException(
                    "AeatRespuestaAltaValidator no puede ser nulo"
            );
        }

        if (resultadoEnvioMapper == null) {
            throw new IllegalArgumentException(
                    "AeatResultadoEnvioMapper no puede ser nulo"
            );
        }

        this.solicitudAltaXmlBuilder =
                solicitudAltaXmlBuilder;

        this.soapGateway =
                soapGateway;

        this.respuestaAltaXmlBuilder =
                respuestaAltaXmlBuilder;

        this.respuestaAltaValidator =
                respuestaAltaValidator;

        this.resultadoEnvioMapper =
                resultadoEnvioMapper;

        /*
         * Bloqueo deliberado de ANULACION para el
         * constructor que usa actualmente Spring.
         */
        this.envioAnulacionExecutor =
                null;
    }

    /**
     * Constructor preparado para ANULACION.
     *
     * Por ahora se utilizara exclusivamente en tests.
     */
    public AeatVerifactuProvider(
            AeatSolicitudAltaXmlBuilder solicitudAltaXmlBuilder,
            AeatSoapGateway soapGateway,
            AeatRespuestaAltaXmlBuilder respuestaAltaXmlBuilder,
            AeatRespuestaAltaValidator respuestaAltaValidator,
            AeatResultadoEnvioMapper resultadoEnvioMapper,
            AeatEnvioAnulacionExecutor envioAnulacionExecutor) {

        this(
                solicitudAltaXmlBuilder,
                soapGateway,
                respuestaAltaXmlBuilder,
                respuestaAltaValidator,
                resultadoEnvioMapper
        );

        if (envioAnulacionExecutor == null) {
            throw new IllegalArgumentException(
                    "AeatEnvioAnulacionExecutor "
                            + "no puede ser nulo"
            );
        }

        this.envioAnulacionExecutor =
                envioAnulacionExecutor;
    }

    @Override
    public String getNombre() {

        return "AEAT-VERIFACTU-PRUEBAS";
    }

    @Override
    public ResultadoEnvioVerifactu enviarAlta(
            RegistroFacturacion registro) {

        /*
         * =====================================================
         * PREPARACION LOCAL
         * =====================================================
         *
         * Todo fallo en esta sección ocurre antes
         * de realizar comunicación externa.
         */

        RegFactuSistemaFacturacion peticion =
                solicitudAltaXmlBuilder.construir(
                        registro
                );

        String peticionXml =
                solicitudAltaXmlBuilder.generarXml(
                        registro
                );

        if (peticion.getCabecera() == null) {

            throw new IllegalStateException(
                    "La petición AEAT no contiene Cabecera"
            );
        }

        List<RegistroFacturaType> registros =
                peticion.getRegistroFactura();

        if (registros.size() != 1) {

            throw new IllegalStateException(
                    "La petición AEAT debe contener "
                            + "exactamente un registro de factura"
            );
        }

        RegistroFacturaType registroFactura =
                registros.get(0);

        /*
         * =====================================================
         * LLAMADA EXTERNA
         * =====================================================
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
                    "Se produjo un error durante la comunicación "
                            + "con AEAT y no puede determinarse "
                            + "con certeza si el registro fue recibido",
                    e
            );
        }

        if (resultadoSoap == null) {

            throw new EnvioVerifactuInciertoException(
                    "AEAT no devolvió una respuesta interpretable"
            );
        }

        /*
         * =====================================================
         * CONSERVACION DE LA RESPUESTA
         * =====================================================
         */

        String respuestaXml;

        try {

            respuestaXml =
                    respuestaAltaXmlBuilder.generarXml(
                            resultadoSoap
                    );

        } catch (Exception e) {

            throw new EnvioVerifactuInciertoException(
                    "AEAT respondió, pero no se pudo conservar "
                            + "correctamente su respuesta",
                    e
            );
        }

        /*
         * Nosotros enviamos exactamente un registro
         * por petición.
         */

        List<RespuestaExpedidaType> respuestasLinea =
                resultadoSoap.respuestasLinea();

        if (respuestasLinea.size() != 1) {

            return crearResultadoReconciliacion(
                    resultadoSoap,
                    peticionXml,
                    respuestaXml,
                    "AEAT devolvió "
                            + respuestasLinea.size()
                            + " líneas de respuesta para una "
                            + "petición que contenía un único registro"
            );
        }

        /*
         * =====================================================
         * COHERENCIA ENTRE ESTADO GLOBAL Y ESTADO DE LINEA
         * =====================================================
         *
         * Una respuesta contradictoria no se considera
         * aceptación ni rechazo definitivo.
         */

        if (!respuestaAltaValidator.esCoherente(
                resultadoSoap
        )) {

            return crearResultadoReconciliacion(
                    resultadoSoap,
                    peticionXml,
                    respuestaXml,
                    "La respuesta de AEAT presenta una "
                            + "incoherencia entre el EstadoEnvio "
                            + "global y el EstadoRegistro"
            );
        }

        /*
         * =====================================================
         * INTERPRETACION FUNCIONAL
         * =====================================================
         */

        try {

            return resultadoEnvioMapper.convertir(
                    resultadoSoap.csv(),
                    peticionXml,
                    respuestaXml,
                    respuestasLinea.get(0)
            );

        } catch (Exception e) {

            throw new EnvioVerifactuInciertoException(
                    "AEAT respondió, pero no se pudo determinar "
                            + "con seguridad el resultado del registro",
                    e
            );
        }
    }

    @Override
    public ResultadoEnvioVerifactu enviarAnulacion(
            RegistroFacturacion registro) {

        /*
         * Mientras AeatPreproduccionConfiguration siga
         * utilizando el constructor historico, este campo
         * sera null y la operacion queda bloqueada ANTES
         * de cualquier llamada SOAP.
         */
        if (envioAnulacionExecutor == null) {

            throw new IllegalStateException(
                    "El envio AEAT de ANULACION "
                            + "todavia no esta habilitado"
            );
        }

        return envioAnulacionExecutor.enviar(
                registro
        );
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