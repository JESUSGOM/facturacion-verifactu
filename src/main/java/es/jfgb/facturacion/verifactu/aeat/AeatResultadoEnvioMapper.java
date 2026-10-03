package es.jfgb.facturacion.verifactu.aeat;

import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.respuestasuministro.RespuestaExpedidaType;
import es.gob.agenciatributaria.www2.static_files.common.internet.dep.aplicaciones.es.aeat.tike.cont.ws.suministroinformacion.RegistroDuplicadoType;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.springframework.stereotype.Component;

@Component
public class AeatResultadoEnvioMapper {

    private final AeatRespuestaAltaInterpreter interpreter;

    public AeatResultadoEnvioMapper(
            AeatRespuestaAltaInterpreter interpreter) {

        this.interpreter = interpreter;
    }

    public ResultadoEnvioVerifactu convertir(
            String csv,
            String peticionXml,
            String respuestaXml,
            RespuestaExpedidaType respuestaLinea) {

        if (respuestaLinea == null) {

            throw new IllegalArgumentException(
                    "La línea de respuesta de AEAT no puede ser nula"
            );
        }

        EstadoResultadoEnvioVerifactu estado =
                interpreter.interpretar(
                        respuestaLinea
                );

        String mensaje =
                construirMensaje(
                        respuestaLinea
                );

        return new ResultadoEnvioVerifactu(
                estado,
                normalizar(csv),
                peticionXml,
                respuestaXml,
                mensaje
        );
    }

    private String construirMensaje(
            RespuestaExpedidaType respuesta) {

        StringBuilder mensaje =
                new StringBuilder();

        if (respuesta.getEstadoRegistro() != null) {

            mensaje.append(
                    "Estado AEAT: "
            );

            mensaje.append(
                    respuesta
                            .getEstadoRegistro()
                            .value()
            );
        }

        if (respuesta.getCodigoErrorRegistro() != null) {

            agregarSeparador(
                    mensaje
            );

            mensaje.append(
                    "Código: "
            );

            mensaje.append(
                    respuesta.getCodigoErrorRegistro()
            );
        }

        if (tieneTexto(
                respuesta.getDescripcionErrorRegistro()
        )) {

            agregarSeparador(
                    mensaje
            );

            mensaje.append(
                    "Descripción: "
            );

            mensaje.append(
                    respuesta.getDescripcionErrorRegistro()
            );
        }

        RegistroDuplicadoType duplicado =
                respuesta.getRegistroDuplicado();

        if (duplicado != null) {

            agregarSeparador(
                    mensaje
            );

            mensaje.append(
                    "Registro duplicado"
            );

            if (tieneTexto(
                    duplicado
                            .getIdPeticionRegistroDuplicado()
            )) {

                agregarSeparador(
                        mensaje
                );

                mensaje.append(
                        "IdPeticionRegistroDuplicado: "
                );

                mensaje.append(
                        duplicado
                                .getIdPeticionRegistroDuplicado()
                );
            }

            if (duplicado.getEstadoRegistroDuplicado()
                    != null) {

                agregarSeparador(
                        mensaje
                );

                mensaje.append(
                        "EstadoRegistroDuplicado: "
                );

                mensaje.append(
                        duplicado
                                .getEstadoRegistroDuplicado()
                                .value()
                );
            }

            if (duplicado.getCodigoErrorRegistro()
                    != null) {

                agregarSeparador(
                        mensaje
                );

                mensaje.append(
                        "Código registro duplicado: "
                );

                mensaje.append(
                        duplicado.getCodigoErrorRegistro()
                );
            }

            if (tieneTexto(
                    duplicado
                            .getDescripcionErrorRegistro()
            )) {

                agregarSeparador(
                        mensaje
                );

                mensaje.append(
                        "Descripción registro duplicado: "
                );

                mensaje.append(
                        duplicado
                                .getDescripcionErrorRegistro()
                );
            }
        }

        return mensaje.toString();
    }

    private void agregarSeparador(
            StringBuilder mensaje) {

        if (!mensaje.isEmpty()) {
            mensaje.append(". ");
        }
    }

    private boolean tieneTexto(
            String valor) {

        return valor != null
                && !valor.isBlank();
    }

    private String normalizar(
            String valor) {

        if (!tieneTexto(valor)) {
            return null;
        }

        return valor.trim();
    }
}