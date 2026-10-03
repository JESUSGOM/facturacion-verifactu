package es.jfgb.facturacion.verifactu.aeat.soap;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoEnvioType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.CabeceraType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.DatosPresentacionType;

import java.util.List;

public record AeatSoapResultadoAlta(

        String csv,

        DatosPresentacionType datosPresentacion,

        CabeceraType cabecera,

        String tiempoEsperaEnvio,

        EstadoEnvioType estadoEnvio,

        List<RespuestaExpedidaType> respuestasLinea

) {

    public AeatSoapResultadoAlta {

        respuestasLinea =
                respuestasLinea == null
                        ? List.of()
                        : List.copyOf(respuestasLinea);
    }
}