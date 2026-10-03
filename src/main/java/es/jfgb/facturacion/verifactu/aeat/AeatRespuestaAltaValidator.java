package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoEnvioType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.jfgb.facturacion.verifactu.aeat.soap.AeatSoapResultadoAlta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AeatRespuestaAltaValidator {

    public boolean esCoherente(
            AeatSoapResultadoAlta resultado) {

        if (resultado == null) {
            return false;
        }

        if (resultado.estadoEnvio() == null) {
            return false;
        }

        List<RespuestaExpedidaType> respuestas =
                resultado.respuestasLinea();

        if (respuestas == null
                || respuestas.size() != 1) {

            return false;
        }

        RespuestaExpedidaType respuesta =
                respuestas.get(0);

        if (respuesta == null
                || respuesta.getEstadoRegistro() == null) {

            return false;
        }

        return esCombinacionValida(
                resultado.estadoEnvio(),
                respuesta.getEstadoRegistro()
        );
    }

    private boolean esCombinacionValida(
            EstadoEnvioType estadoEnvio,
            EstadoRegistroType estadoRegistro) {

        return switch (estadoRegistro) {

            case CORRECTO ->
                    estadoEnvio
                            == EstadoEnvioType.CORRECTO;

            case ACEPTADO_CON_ERRORES ->
                    estadoEnvio
                            == EstadoEnvioType
                            .PARCIALMENTE_CORRECTO;

            case INCORRECTO ->
                    estadoEnvio
                            == EstadoEnvioType.INCORRECTO;
        };
    }
}