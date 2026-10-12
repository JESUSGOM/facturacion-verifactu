package es.jfgb.facturacion.verifactu.aeat.soap;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.consultalr.ConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestaconsultalr.RespuestaConsultaFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaRegFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;

import es.jfgb.facturacion.verifactu.aeat.security.AeatEndpointPreproduccionValidator;
import jakarta.xml.ws.BindingProvider;

public class AeatSoapPruebasGateway
        implements AeatSoapGateway {

    private final SfPortTypeVerifactu port;
    private final Runnable validadorDestino;

    public AeatSoapPruebasGateway(
            SfPortTypeVerifactu port) {

        this(
                port,
                crearValidadorDestino(
                        port
                )
        );
    }

    /*
     * Constructor de apoyo para pruebas unitarias.
     *
     * El constructor publico utilizado en produccion
     * sigue exigiendo un BindingProvider real y valida
     * el endpoint AEAT PRE.
     */
    AeatSoapPruebasGateway(
            SfPortTypeVerifactu port,
            Runnable validadorDestino) {

        if (port == null) {
            throw new IllegalArgumentException(
                    "El puerto SOAP de AEAT no puede ser nulo"
            );
        }

        if (validadorDestino == null) {
            throw new IllegalArgumentException(
                    "El validador del destino AEAT no puede ser nulo"
            );
        }

        this.port = port;
        this.validadorDestino = validadorDestino;

        validarDestino();
    }

    private static Runnable crearValidadorDestino(
            SfPortTypeVerifactu port) {

        /*
         * El caso nulo lo resuelve el constructor principal
         * con su mensaje especifico.
         */
        if (port == null) {
            return () -> {};
        }

        if (!(port instanceof BindingProvider proveedor)) {

            throw new IllegalArgumentException(
                    "El puerto SOAP debe implementar BindingProvider"
            );
        }

        return () -> {

            Object endpoint =
                    proveedor
                            .getRequestContext()
                            .get(
                                    BindingProvider
                                            .ENDPOINT_ADDRESS_PROPERTY
                            );

            AeatEndpointPreproduccionValidator.validar(
                    endpoint instanceof String direccion
                            ? direccion
                            : null
            );
        };
    }

    private void validarDestino() {

        validadorDestino.run();
    }
    @Override
    public AeatSoapResultadoAlta enviarAlta(
            CabeceraType cabecera,
            RegistroFacturaType registroFactura) {

        if (cabecera == null) {
            throw new IllegalArgumentException(
                    "La cabecera AEAT no puede ser nula"
            );
        }

        if (registroFactura == null) {
            throw new IllegalArgumentException(
                    "El registro de factura AEAT no puede ser nulo"
            );
        }

        /*
         * La operacion se genera en estilo BARE.
         *
         * Construimos el documento completo
         * RegFactuSistemaFacturacion y lo entregamos
         * al puerto SOAP como un unico argumento.
         */
        RegFactuSistemaFacturacion peticion =
                new RegFactuSistemaFacturacion();

        peticion.setCabecera(
                cabecera
        );

        peticion
                .getRegistroFactura()
                .add(
                        registroFactura
                );

        validarDestino();

        RespuestaRegFactuSistemaFacturacionType respuesta =
                port.regFactuSistemaFacturacion(
                        peticion
                );

        /*
         * Si el runtime devuelve null,
         * AeatVerifactuProvider lo tratara
         * como un resultado incierto.
         */
        if (respuesta == null) {
            return null;
        }

        return new AeatSoapResultadoAlta(
                respuesta.getCSV(),
                respuesta.getDatosPresentacion(),
                respuesta.getCabecera(),
                respuesta.getTiempoEsperaEnvio(),
                respuesta.getEstadoEnvio(),
                respuesta.getRespuestaLinea()
        );
    }

    @Override
    public RespuestaConsultaFactuSistemaFacturacionType consultar(
            ConsultaFactuSistemaFacturacionType consulta) {

        if (consulta == null) {
            throw new IllegalArgumentException(
                    "La consulta AEAT no puede ser nula"
            );
        }

        /*
         * IMPORTANTE:
         *
         * Este metodo no decide que consultar ni interpreta
         * el resultado. Su unica responsabilidad es delegar
         * en el puerto SOAP generado por el WSDL.
         */
        validarDestino();

        return port.consultaFactuSistemaFacturacion(
                consulta
        );
    }
}