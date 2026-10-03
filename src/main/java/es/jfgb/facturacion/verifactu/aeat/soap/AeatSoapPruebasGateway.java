package es.jfgb.facturacion.verifactu.aeat.soap;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaRegFactuSistemaFacturacionType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.sistemafacturacion.SfPortTypeVerifactu;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegFactuSistemaFacturacion;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministrolr.RegistroFacturaType;

public class AeatSoapPruebasGateway
        implements AeatSoapGateway {

    private final SfPortTypeVerifactu port;

    public AeatSoapPruebasGateway(
            SfPortTypeVerifactu port) {

        if (port == null) {
            throw new IllegalArgumentException(
                    "El puerto SOAP de AEAT no puede ser nulo"
            );
        }

        this.port = port;
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
         * La operación se genera ahora en estilo BARE.
         *
         * Por tanto construimos el documento completo
         * RegFactuSistemaFacturacion y lo entregamos al
         * puerto SOAP como un único argumento.
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

        RespuestaRegFactuSistemaFacturacionType respuesta =
                port.regFactuSistemaFacturacion(
                        peticion
                );

        /*
         * Si el runtime devolviera null, dejamos que
         * AeatVerifactuProvider lo trate posteriormente
         * como resultado incierto.
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
}