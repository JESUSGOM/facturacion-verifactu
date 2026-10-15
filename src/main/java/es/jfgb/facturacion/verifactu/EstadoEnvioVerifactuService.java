package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.verifactu.provider.EnvioVerifactuInciertoException;
import es.jfgb.facturacion.verifactu.provider.EstadoResultadoEnvioVerifactu;
import es.jfgb.facturacion.verifactu.provider.ResultadoEnvioVerifactu;
import org.hibernate.Hibernate;
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

        /*
         * Una ANULACION solo puede iniciar su envio
         * cuando el ALTA original ya esta ACEPTADO.
         *
         * Esta comprobacion ocurre antes de incrementar
         * intentos o marcar el registro como ENVIANDO.
         */
        if (tipoEsperado
                == TipoRegistroFacturacion.ANULACION) {

            comprobarAltaOriginalAceptada(
                    empresaId,
                    registro
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

        inicializarDatosParaProveedor(
                registro
        );

        return registro;
    }

    /*
     * Carga dentro de la transaccion las relaciones LAZY
     * necesarias para construir el XML despues del commit.
     *
     * La llamada al proveedor se realiza fuera de esta
     * transaccion deliberadamente: ENVIANDO y el intento
     * deben quedar persistidos antes de salir a la red.
     */
    private void inicializarDatosParaProveedor(
            RegistroFacturacion registro) {

        Hibernate.initialize(
                registro.getDesgloses()
        );

        Hibernate.initialize(
                registro.getFacturasRectificadas()
        );

        Hibernate.initialize(
                registro.getFacturasSustituidas()
        );

        RegistroFacturacion anterior =
                registro.getRegistroAnterior();

        if (anterior != null) {

            Hibernate.initialize(
                    anterior
            );
        }
    }

    private void comprobarAltaOriginalAceptada(
            Long empresaId,
            RegistroFacturacion anulacion) {

        if (anulacion.getFactura() == null
                || anulacion.getFactura().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La anulacion no tiene una factura asociada"
            );
        }

        Long facturaId =
                anulacion.getFactura().getId();

        RegistroFacturacion altaOriginal =
                registroRepository
                        .findAltaOriginalConBloqueo(
                                facturaId,
                                empresaId,
                                TipoRegistroFacturacion.ALTA
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "No existe el registro ALTA original "
                                                + "de la factura que se pretende anular"
                                ));

        if (altaOriginal.getEstadoEnvio()
                != EstadoEnvioRegistro.ACEPTADO) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puede enviarse la ANULACION hasta que "
                            + "el ALTA original haya sido aceptado"
            );
        }
    }

    @Transactional
    public void finalizarEnvio(
            Long empresaId,
            Long registroId,
            String proveedor,
            ResultadoEnvioVerifactu resultado) {

        if (resultado == null) {

            throw new IllegalArgumentException(
                    "El resultado del envÃ­o "
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
                                        "Registro de facturaciÃ³n no encontrado"
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
                                        "No se encontrÃ³ el intento "
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
                                        "Registro de facturaciÃ³n no encontrado"
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
                                        "No se encontrÃ³ el intento "
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
                                        "Registro de facturaciÃ³n no encontrado"
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
                                        "No se encontrÃ³ el intento "
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
         * llegÃ³ a procesar la peticiÃ³n.
         *
         * Por seguridad queda INCIERTO y no
         * podrÃ¡ reenviarse automÃ¡ticamente.
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
         * entre la bÃºsqueda inicial de candidatos
         * y la adquisiciÃ³n de este bloqueo.
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
         * Debe continuar siendo anterior al lÃ­mite
         * establecido por el proceso de recuperaciÃ³n.
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
                                        "No se encontrÃ³ el intento "
                                                + numeroIntento
                                                + " del registro "
                                                + registroId
                                ));

        /*
         * Verificamos ademÃ¡s que el intento histÃ³rico
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
                    "El registro de facturaciÃ³n no tiene "
                            + "un intento de envÃ­o activo"
            );
        }

        return numeroIntento;
    }
}
