package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.EstadoRegistroType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import org.springframework.stereotype.Component;

@Component
public class AeatRespuestaAltaInterpreter {

    public EstadoResultadoEnvioVerifactu interpretar(
            RespuestaExpedidaType respuesta) {

        if (respuesta == null) {
            throw new IllegalArgumentException(
                    "La respuesta de AEAT no puede ser nula"
            );
        }

        EstadoRegistroType estadoRegistro =
                respuesta.getEstadoRegistro();

        if (estadoRegistro == null) {
            throw new IllegalArgumentException(
                    "La respuesta de AEAT no contiene EstadoRegistro"
            );
        }

        return switch (estadoRegistro) {

            case CORRECTO ->
                    EstadoResultadoEnvioVerifactu.ACEPTADO;

            case ACEPTADO_CON_ERRORES ->
                    EstadoResultadoEnvioVerifactu
                            .ACEPTADO_CON_ERRORES;

            case INCORRECTO -> {

                /*
                 * AEAT únicamente devuelve RegistroDuplicado
                 * cuando el registro enviado ha sido rechazado
                 * por estar duplicado.
                 *
                 * No debemos reenviarlo automáticamente.
                 * Primero será necesario reconciliarlo con
                 * el registro que AEAT ya tiene almacenado.
                 */
                if (respuesta.getRegistroDuplicado() != null) {

                    yield EstadoResultadoEnvioVerifactu
                            .REQUIERE_RECONCILIACION;
                }

                yield EstadoResultadoEnvioVerifactu.RECHAZADO;
            }
        };
    }
}