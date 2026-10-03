package es.jfgb.facturacion.verifactu;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EstadoReconciliacionVerifactuService {

    private final RegistroFacturacionRepository registroRepository;
    private final RegistroFacturacionIntentoRepository intentoRepository;

    public EstadoReconciliacionVerifactuService(
            RegistroFacturacionRepository registroRepository,
            RegistroFacturacionIntentoRepository intentoRepository) {

        this.registroRepository = registroRepository;
        this.intentoRepository = intentoRepository;
    }

    /**
     * Obtiene bajo bloqueo los datos inmutables necesarios
     * para consultar AEAT.
     *
     * La transaccion termina antes de efectuar cualquier
     * comunicacion externa.
     */
    @Transactional
    public DatosReconciliacionVerifactu prepararConsulta(
            Long empresaId,
            Long registroId) {

        RegistroFacturacion registro =
                obtenerConBloqueo(
                        empresaId,
                        registroId
                );

        if (registro.getTipoRegistro()
                != TipoRegistroFacturacion.ALTA) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo pueden reconciliarse registros de tipo ALTA"
            );
        }

        if (registro.getEstadoEnvio()
                != EstadoEnvioRegistro.INCIERTO) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El registro no se encuentra en estado INCIERTO"
            );
        }

        String nombreRazon =
                obligatorio(
                        registro.getNombreRazonEmisor(),
                        "nombre o razon social del emisor"
                );

        String nif =
                obligatorio(
                        registro.getNifEmisor(),
                        "NIF del emisor"
                );

        String numeroSerie =
                obligatorio(
                        registro.getNumSerieFactura(),
                        "numero o serie de factura"
                );

        if (registro.getFechaExpedicion() == null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El registro INCIERTO no contiene fecha "
                            + "de expedicion suficiente para reconciliarlo"
            );
        }

        return new DatosReconciliacionVerifactu(
                nombreRazon,
                nif,
                numeroSerie,
                registro.getFechaExpedicion()
        );
    }

    /**
     * Resuelve el registro como ACEPTADO unicamente si,
     * al recuperar de nuevo el bloqueo, continua INCIERTO.
     *
     * De esta forma una reconciliacion concurrente o cualquier
     * cambio posterior no puede ser sobrescrito.
     */
    @Transactional
    public boolean marcarAceptadoSiSigueIncierto(
            Long empresaId,
            Long registroId) {

        RegistroFacturacion registro =
                obtenerConBloqueo(
                        empresaId,
                        registroId
                );

        if (registro.getEstadoEnvio()
                != EstadoEnvioRegistro.INCIERTO) {

            return false;
        }

        Integer numeroIntento =
                registro.getIntentosEnvio();

        if (numeroIntento == null
                || numeroIntento <= 0) {

            throw new IllegalStateException(
                    "El registro INCIERTO no tiene "
                            + "un intento de envio asociado"
            );
        }

        RegistroFacturacionIntento intento =
                intentoRepository
                        .findByRegistroFacturacion_IdAndNumeroIntento(
                                registroId,
                                numeroIntento
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No se encontro el intento "
                                                + numeroIntento
                                                + " del registro "
                                                + registroId
                                ));

        /*
         * No modificamos XML, CSV, proveedor, error ni fechas.
         *
         * Conservamos toda la informacion original del intento
         * y solamente resolvemos su estado final.
         */
        registro.setEstadoEnvio(
                EstadoEnvioRegistro.ACEPTADO
        );

        intento.setEstado(
                EstadoEnvioRegistro.ACEPTADO
        );

        intentoRepository.saveAndFlush(
                intento
        );

        registroRepository.saveAndFlush(
                registro
        );

        return true;
    }

    private RegistroFacturacion obtenerConBloqueo(
            Long empresaId,
            Long registroId) {

        return registroRepository
                .findConBloqueo(
                        empresaId,
                        registroId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Registro de facturacion no encontrado"
                        ));
    }

    private String obligatorio(
            String valor,
            String nombreCampo) {

        if (valor == null
                || valor.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El registro INCIERTO no contiene "
                            + nombreCampo
                            + " suficiente para reconciliarlo"
            );
        }

        return valor.trim();
    }
}