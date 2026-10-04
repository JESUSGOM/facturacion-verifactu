package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class EstadoEnvioVerifactuService {

    private final RegistroFacturacionRepository registroRepository;
    private final RegistroFacturacionIntentoRepository intentoRepository;

    public EstadoEnvioVerifactuService(
            RegistroFacturacionRepository registroRepository,
            RegistroFacturacionIntentoRepository intentoRepository) {

        this.registroRepository = registroRepository;
        this.intentoRepository = intentoRepository;
    }

    @Transactional
    public RegistroFacturacion iniciarEnvio(
            Long empresaId,
            Long registroId) {

        /*
         * Compatibilidad con el flujo historico de ALTA.
         */
        return iniciarEnvio(
                empresaId,
                registroId,
                TipoRegistroFacturacion.ALTA
        );
    }

    @Transactional
    public RegistroFacturacion iniciarEnvio(
            Long empresaId,
            Long registroId,
            TipoRegistroFacturacion tipoEsperado) {

        if (tipoEsperado == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicarse el tipo de registro "
                            + "que se pretende enviar"
            );
        }

        RegistroFacturacion registro =
                registroRepository
                        .findConBloqueo(
                                empresaId,
                                registroId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Registro de facturacion no encontrado"
                                ));

        /*
         * La validacion del tipo se realiza antes
         * de incrementar intentos o marcar ENVIANDO.
         */
        if (registro.getTipoRegistro()
                != tipoEsperado) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El registro no es de tipo "
                            + tipoEsperado
            );
        }

        if (registro.getEstadoEnvio()
                == EstadoEnvioRegistro.ACEPTADO) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El registro ya ha sido aceptado"
            );
        }

        if (registro.getEstadoEnvio()
                == EstadoEnvioRegistro.ENVIANDO) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El registro ya esta siendo enviado"
            );
        }

        if (registro.getEstadoEnvio()
                == EstadoEnvioRegistro.INCIERTO) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El resultado del envio anterior es incierto "
                            + "y debe reconciliarse antes de reenviar"
            );
        }

        int intentosActuales =
                registro.getIntentosEnvio() == null
                        ? 0
                        : registro.getIntentosEnvio();

        int numeroIntento =
                intentosActuales + 1;

        Instant ahora =
                Instant.now();

        registro.setIntentosEnvio(
                numeroIntento
        );

        registro.setFechaUltimoIntento(
                ahora
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.ENVIANDO
        );

        RegistroFacturacionIntento intento =
                new RegistroFacturacionIntento();

        intento.setRegistroFacturacion(
                registro
        );

        intento.setNumeroIntento(
                numeroIntento
        );

        intento.setEstado(
                EstadoEnvioRegistro.ENVIANDO
        );

        intento.setFechaInicio(
                ahora
        );

        registroRepository.saveAndFlush(
                registro
        );

        intentoRepository.saveAndFlush(
                intento
        );

        return registro;
    }

    @Transactional
    public void finalizarEnvio(
            Long empresaId,
            Long registroId,
            String proveedor,
            ResultadoEnvioVerifactu resultado) {

        if (resultado == null) {

            throw new IllegalArgumentException(
                    "El resultado del envío "
                            + "VERI*FACTU no puede ser nulo"
            );
        }

        if (resultado.estado() == null) {

            throw new IllegalArgumentException(
                    "El estado del resultado "
                            + "VERI*FACTU no puede ser nulo"
            );
        }

        RegistroFacturacion registro =
                registroRepository
                        .findConBloqueo(
                                empresaId,
                                registroId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Registro de facturación no encontrado"
                                ));

        Integer numeroIntento =
                obtenerNumeroIntentoActual(
                        registro
                );

        RegistroFacturacionIntento intento =
                intentoRepository
                        .findByRegistroFacturacion_IdAndNumeroIntento(
                                registroId,
                                numeroIntento
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No se encontró el intento "
                                                + numeroIntento
                                                + " del registro "
                                                + registroId
                                ));

        EstadoEnvioRegistro estadoFinal =
                convertirEstadoResultado(
                        resultado.estado()
                );

        Instant ahora =
                Instant.now();

        registro.setProveedor(
                proveedor
        );

        registro.setIdentificadorExterno(
                resultado.identificadorExterno()
        );

        registro.setPeticion(
                resultado.peticion()
        );

        registro.setRespuesta(
                resultado.respuesta()
        );

        registro.setFechaEnvio(
                ahora
        );

        registro.setEstadoEnvio(
                estadoFinal
        );

        intento.setEstado(
                estadoFinal
        );

        intento.setProveedor(
                proveedor
        );

        intento.setIdentificadorExterno(
                resultado.identificadorExterno()
        );

        intento.setPeticion(
                resultado.peticion()
        );

        intento.setRespuesta(
                resultado.respuesta()
        );

        intento.setFechaFin(
                ahora
        );

        intentoRepository.saveAndFlush(
                intento
        );

        registroRepository.saveAndFlush(
                registro
        );
    }

    @Transactional
    public void registrarError(
            Long empresaId,
            Long registroId,
            String proveedor,
            Exception exception) {

        RegistroFacturacion registro =
                registroRepository
                        .findConBloqueo(
                                empresaId,
                                registroId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Registro de facturación no encontrado"
                                ));

        Integer numeroIntento =
                obtenerNumeroIntentoActual(
                        registro
                );

        RegistroFacturacionIntento intento =
                intentoRepository
                        .findByRegistroFacturacion_IdAndNumeroIntento(
                                registroId,
                                numeroIntento
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No se encontró el intento "
                                                + numeroIntento
                                                + " del registro "
                                                + registroId
                                ));

        String mensaje =
                exception.getMessage() == null
                        ? exception
                        .getClass()
                        .getSimpleName()
                        : exception
                        .getClass()
                        .getSimpleName()
                        + ": "
                        + exception.getMessage();

        Instant ahora =
                Instant.now();

        registro.setProveedor(
                proveedor
        );

        registro.setRespuesta(
                mensaje
        );

        registro.setFechaEnvio(
                ahora
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.ERROR
        );

        intento.setEstado(
                EstadoEnvioRegistro.ERROR
        );

        intento.setProveedor(
                proveedor
        );

        intento.setError(
                mensaje
        );

        intento.setFechaFin(
                ahora
        );

        intentoRepository.saveAndFlush(
                intento
        );

        registroRepository.saveAndFlush(
                registro
        );
    }

    @Transactional
    public void registrarIncierto(
            Long empresaId,
            Long registroId,
            String proveedor,
            EnvioVerifactuInciertoException exception) {

        RegistroFacturacion registro =
                registroRepository
                        .findConBloqueo(
                                empresaId,
                                registroId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Registro de facturación no encontrado"
                                ));

        Integer numeroIntento =
                obtenerNumeroIntentoActual(
                        registro
                );

        RegistroFacturacionIntento intento =
                intentoRepository
                        .findByRegistroFacturacion_IdAndNumeroIntento(
                                registroId,
                                numeroIntento
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No se encontró el intento "
                                                + numeroIntento
                                                + " del registro "
                                                + registroId
                                ));

        String mensaje =
                exception.getMessage() == null
                        ? exception
                        .getClass()
                        .getSimpleName()
                        : exception
                        .getClass()
                        .getSimpleName()
                        + ": "
                        + exception.getMessage();

        Instant ahora =
                Instant.now();

        /*
         * No sabemos con certeza si el proveedor
         * llegó a procesar la petición.
         *
         * Por seguridad queda INCIERTO y no
         * podrá reenviarse automáticamente.
         */
        registro.setProveedor(
                proveedor
        );

        registro.setRespuesta(
                mensaje
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.INCIERTO
        );

        intento.setEstado(
                EstadoEnvioRegistro.INCIERTO
        );

        intento.setProveedor(
                proveedor
        );

        intento.setError(
                mensaje
        );

        intento.setFechaFin(
                ahora
        );

        intentoRepository.saveAndFlush(
                intento
        );

        registroRepository.saveAndFlush(
                registro
        );
    }

    @Transactional
    public boolean marcarComoInciertoSiSigueAntiguo(
            Long registroId,
            Instant limite) {

        RegistroFacturacion registro =
                registroRepository
                        .findConBloqueoPorId(
                                registroId
                        )
                        .orElse(null);

        if (registro == null) {
            return false;
        }

        /*
         * El registro pudo haber terminado correctamente
         * entre la búsqueda inicial de candidatos
         * y la adquisición de este bloqueo.
         */
        if (registro.getEstadoEnvio()
                != EstadoEnvioRegistro.ENVIANDO) {

            return false;
        }

        Instant fechaUltimoIntento =
                registro.getFechaUltimoIntento();

        if (fechaUltimoIntento == null) {
            return false;
        }

        /*
         * Debe continuar siendo anterior al límite
         * establecido por el proceso de recuperación.
         */
        if (!fechaUltimoIntento.isBefore(
                limite
        )) {

            return false;
        }

        Integer numeroIntento =
                obtenerNumeroIntentoActual(
                        registro
                );

        RegistroFacturacionIntento intento =
                intentoRepository
                        .findByRegistroFacturacion_IdAndNumeroIntento(
                                registroId,
                                numeroIntento
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No se encontró el intento "
                                                + numeroIntento
                                                + " del registro "
                                                + registroId
                                ));

        /*
         * Verificamos además que el intento histórico
         * siga realmente en ENVIANDO.
         */
        if (intento.getEstado()
                != EstadoEnvioRegistro.ENVIANDO) {

            return false;
        }

        Instant ahora =
                Instant.now();

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.INCIERTO
        );

        intento.setEstado(
                EstadoEnvioRegistro.INCIERTO
        );

        intento.setFechaFin(
                ahora
        );

        intentoRepository.saveAndFlush(
                intento
        );

        registroRepository.saveAndFlush(
                registro
        );

        return true;
    }

    private EstadoEnvioRegistro convertirEstadoResultado(
            EstadoResultadoEnvioVerifactu estado) {

        return switch (estado) {

            case ACEPTADO,
                 ACEPTADO_CON_ERRORES ->
                    EstadoEnvioRegistro.ACEPTADO;

            case RECHAZADO ->
                    EstadoEnvioRegistro.RECHAZADO;

            case REQUIERE_RECONCILIACION ->
                    EstadoEnvioRegistro.INCIERTO;
        };
    }

    private Integer obtenerNumeroIntentoActual(
            RegistroFacturacion registro) {

        Integer numeroIntento =
                registro.getIntentosEnvio();

        if (numeroIntento == null
                || numeroIntento <= 0) {

            throw new IllegalStateException(
                    "El registro de facturación no tiene "
                            + "un intento de envío activo"
            );
        }

        return numeroIntento;
    }
}
