package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.factura.Factura;
import es.jfgb.facturacion.factura.EstadoFactura;
import es.jfgb.facturacion.factura.FacturaLinea;
import es.jfgb.facturacion.factura.FacturaRectificada;
import es.jfgb.facturacion.factura.FacturaSustituida;
import es.jfgb.facturacion.factura.TipoFactura;
import es.jfgb.facturacion.factura.TipoRectificativa;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistroFacturacionService {

    private static final String ID_VERSION_AEAT = "1.0";
    private static final String TIPO_HUELLA_SHA_256 = "01";

    private static final DateTimeFormatter
            FORMATO_NUMERO_INSTALACION =
            DateTimeFormatter.ofPattern(
                    "yyyyMMddHHmmss"
            );

    private final EmpresaRepository empresaRepository;
    private final RegistroFacturacionRepository registroRepository;
    private final HuellaVerifactuService huellaVerifactuService;
    private final VerifactuSistemaProperties sistemaProperties;

    public RegistroFacturacionService(
            EmpresaRepository empresaRepository,
            RegistroFacturacionRepository registroRepository,
            HuellaVerifactuService huellaVerifactuService,
            VerifactuSistemaProperties sistemaProperties) {

        this.empresaRepository = empresaRepository;
        this.registroRepository = registroRepository;
        this.huellaVerifactuService = huellaVerifactuService;
        this.sistemaProperties = sistemaProperties;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public RegistroFacturacion crearAlta(
            Factura factura) {

        if (factura == null) {
            throw error(
                    "No se ha recibido la factura"
            );
        }

        if (factura.getEmpresa() == null
                || factura.getEmpresa().getId() == null) {

            throw error(
                    "La factura no tiene empresa"
            );
        }

        Long empresaId =
                factura.getEmpresa().getId();

        /*
         * Bloqueamos la empresa durante la creación
         * del registro.
         *
         * Esto también evita que dos emisiones
         * concurrentes intenten generar dos números
         * de instalación diferentes.
         */
        Empresa empresa = empresaRepository
                .findConBloqueo(empresaId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Empresa no encontrada"
                        ));

        boolean yaExiste = registroRepository
                .existsByFactura_IdAndTipoRegistro(
                        factura.getId(),
                        TipoRegistroFacturacion.ALTA
                );

        if (yaExiste) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La factura ya tiene un registro "
                            + "de facturación de alta"
            );
        }

        validarFacturaEmitible(
                factura
        );

        String nombreRazonEmisor =
                obligatorio(
                        empresa.getRazonSocial(),
                        "Razón social del emisor",
                        120
                );

        String destinatarioNombre = null;
        String destinatarioPais = null;
        String destinatarioNif = null;

        if (factura.getTipoFactura() != TipoFactura.F2
                && factura.getTipoFactura() != TipoFactura.R5) {

            destinatarioNombre =
                    obligatorio(
                            factura.getClienteNombre(),
                            "Nombre o razón social del destinatario",
                            120
                    );

            destinatarioPais =
                    obligatorio(
                            factura.getClienteCodigoPais(),
                            "Código de país del destinatario",
                            2
                    ).toUpperCase();

            /*
             * Primera versión:
             * destinatario identificado mediante
             * NIF español.
             */
            if (!"ES".equals(destinatarioPais)) {

                throw error(
                        "La integración VERI*FACTU actual "
                                + "todavía no admite "
                                + "destinatarios extranjeros. "
                                + "País recibido: "
                                + destinatarioPais
                );
            }

            destinatarioNif =
                    obligatorio(
                            factura.getClienteNif(),
                            "NIF del destinatario",
                            20
                    );
        }

        String descripcionOperacion =
                construirDescripcionOperacion(
                        factura
                );

        /*
         * Aquí se obtiene la configuración del
         * producto SIF y, si es necesario, se genera
         * una única vez el número de instalación
         * correspondiente a esta empresa.
         */
        DatosSistema datosSistema =
                obtenerDatosSistema(
                        empresa
                );

        RegistroFacturacion anterior =
                registroRepository
                        .findTopByEmpresa_IdOrderByIdDesc(
                                empresaId
                        )
                        .orElse(null);

        String huellaAnterior =
                anterior == null
                        ? null
                        : anterior.getHuella();

        String numSerieFactura =
                factura.getSerie().getCodigo()
                        + "-"
                        + factura.getNumero();

        OffsetDateTime fechaHoraGeneracion =
                OffsetDateTime.now()
                        .withNano(0);

        /*
         * El tipo fiscal se obtiene de la propia factura.
         *
         * Esto mantiene F1 para las facturas ordinarias
         * y prepara el registro para R1-R5 sin introducir
         * valores fiscales codificados de forma fija.
         */
        String tipoFactura =
                factura.getTipoFactura().name();

        String huella =
                huellaVerifactuService.calcularAlta(
                        empresa.getNif(),
                        numSerieFactura,
                        factura.getFechaExpedicion(),
                        tipoFactura,
                        factura.getCuotaImpuesto(),
                        factura.getTotal(),
                        huellaAnterior,
                        fechaHoraGeneracion
                );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        registro.setEmpresa(
                empresa
        );

        registro.setFactura(
                factura
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        registro.setNifEmisor(
                obligatorio(
                        empresa.getNif(),
                        "NIF del emisor",
                        20
                )
        );

        registro.setSerie(
                obligatorio(
                        factura.getSerie().getCodigo(),
                        "Serie de factura",
                        30
                )
        );

        registro.setNumero(
                factura.getNumero()
        );

        registro.setNumSerieFactura(
                numSerieFactura
        );

        registro.setFechaExpedicion(
                factura.getFechaExpedicion()
        );

        registro.setTipoFactura(
                tipoFactura
        );

        /*
         * Snapshot específico de rectificación.
         *
         * Para I no se comunican BaseRectificada ni
         * CuotaRectificada.
         *
         * Para S ambos importes forman parte del
         * registro fiscal.
         */
        if (factura.getTipoFactura().esRectificativa()) {

            registro.setTipoRectificativa(
                    factura.getTipoRectificativa()
            );

            if (factura.getTipoRectificativa()
                    == TipoRectificativa.S) {

                registro.setBaseRectificada(
                        normalizarImporte(
                                factura.getBaseRectificada()
                        )
                );

                registro.setCuotaRectificada(
                        normalizarImporte(
                                factura.getCuotaRectificada()
                        )
                );

            } else {

                registro.setBaseRectificada(
                        null
                );

                registro.setCuotaRectificada(
                        null
                );
            }

        } else {

            registro.setTipoRectificativa(
                    null
            );

            registro.setBaseRectificada(
                    null
            );

            registro.setCuotaRectificada(
                    null
            );
        }

        /*
         * Snapshot de las facturas originales identificadas
         * por la rectificativa.
         *
         * Guardamos los datos fiscales necesarios para
         * reconstruir FacturasRectificadas sin depender
         * posteriormente de la entidad Factura original.
         */
        if (factura.getTipoFactura().esRectificativa()) {

            for (FacturaRectificada relacion
                    : factura.getFacturasRectificadas()) {

                Factura original =
                        relacion.getFacturaRectificada();

                String numSerieOriginal =
                        original.getSerie().getCodigo()
                                + "-"
                                + original.getNumero();

                RegistroFacturacionRectificada snapshot =
                        new RegistroFacturacionRectificada();

                snapshot.setFacturaRectificadaId(
                        original.getId()
                );

                snapshot.setOrden(
                        relacion.getOrden().shortValue()
                );

                snapshot.setNifEmisor(
                        obligatorio(
                                original.getEmpresaNif(),
                                "NIF de factura rectificada",
                                20
                        )
                );

                snapshot.setNumSerieFactura(
                        obligatorio(
                                numSerieOriginal,
                                "Numero/serie de factura rectificada",
                                60
                        )
                );

                snapshot.setFechaExpedicion(
                        original.getFechaExpedicion()
                );

                registro.addFacturaRectificada(
                        snapshot
                );
            }
        }

        /*
         * Snapshot de las facturas simplificadas sustituidas
         * por una factura F3.
         *
         * La relacion funcional Factura -> F2 puede cambiar
         * en un borrador, pero una vez generado el registro
         * VERI*FACTU conservamos una copia fiscal independiente.
         *
         * La identificacion de las F2 sustituidas es opcional.
         */
        if (factura.getTipoFactura() == TipoFactura.F3) {

            List<FacturaSustituida> sustituidas =
                    factura.getFacturasSustituidas();

            if (sustituidas != null
                    && sustituidas.size() > 1000) {

                throw error(
                        "La factura F3 contiene mas de 1000 "
                                + "facturas sustituidas"
                );
            }

            if (sustituidas != null) {

                for (FacturaSustituida relacion
                        : sustituidas) {

                    if (relacion == null
                            || relacion.getFacturaSustituida() == null) {

                        throw error(
                                "La factura F3 contiene una relacion "
                                        + "de sustitucion incompleta"
                        );
                    }

                    Factura original =
                            relacion.getFacturaSustituida();

                    if (original.getTipoFactura()
                            != TipoFactura.F2) {

                        throw error(
                                "Una factura F3 solo puede sustituir "
                                        + "facturas F2"
                        );
                    }

                    if (original.getEstado()
                            != EstadoFactura.EMITIDA) {

                        throw error(
                                "La factura F2 sustituida debe estar EMITIDA"
                        );
                    }

                    if (original.getEmpresa() == null
                            || !empresaId.equals(
                            original.getEmpresa().getId()
                    )) {

                        throw error(
                                "La factura F2 sustituida pertenece "
                                        + "a otra empresa"
                        );
                    }

                    if (original.getSerie() == null
                            || original.getNumero() == null
                            || original.getFechaExpedicion() == null) {

                        throw error(
                                "La factura F2 sustituida no tiene "
                                        + "identificacion fiscal completa"
                        );
                    }

                    String numSerieOriginal =
                            obligatorio(
                                    original.getSerie().getCodigo(),
                                    "Serie de factura sustituida",
                                    30
                            )
                                    + "-"
                                    + original.getNumero();

                    RegistroFacturacionSustituida snapshot =
                            new RegistroFacturacionSustituida();

                    snapshot.setFacturaSustituidaId(
                            original.getId()
                    );

                    snapshot.setOrden(
                            relacion.getOrden().shortValue()
                    );

                    snapshot.setNifEmisor(
                            obligatorio(
                                    original.getEmpresaNif(),
                                    "NIF de factura sustituida",
                                    20
                            )
                    );

                    snapshot.setNumSerieFactura(
                            obligatorio(
                                    numSerieOriginal,
                                    "Numero/serie de factura sustituida",
                                    60
                            )
                    );

                    snapshot.setFechaExpedicion(
                            original.getFechaExpedicion()
                    );

                    registro.addFacturaSustituida(
                            snapshot
                    );
                }
            }

        } else if (factura.getFacturasSustituidas() != null
                && !factura.getFacturasSustituidas().isEmpty()) {

            throw error(
                    "Solo una factura F3 puede contener "
                            + "facturas sustituidas"
            );
        }
        registro.setCuotaTotal(
                normalizarImporte(
                        factura.getCuotaImpuesto()
                )
        );

        registro.setImporteTotal(
                normalizarImporte(
                        factura.getTotal()
                )
        );

        registro.setRegistroAnterior(
                anterior
        );

        registro.setHuellaAnterior(
                huellaAnterior
        );

        registro.setFechaHoraGeneracion(
                huellaVerifactuService
                        .formatearFechaHora(
                                fechaHoraGeneracion
                        )
        );

        registro.setHuella(
                huella
        );

        /*
         * =====================================================
         * Snapshot fiscal AEAT
         * =====================================================
         */

        registro.setIdVersionAeat(
                ID_VERSION_AEAT
        );

        registro.setNombreRazonEmisor(
                nombreRazonEmisor
        );

        registro.setDescripcionOperacion(
                descripcionOperacion
        );

        registro.setDestinatarioNombre(
                destinatarioNombre
        );

        registro.setDestinatarioNif(
                destinatarioNif
        );

        registro.setDestinatarioCodigoPais(
                destinatarioPais
        );

        /*
         * Para NIF español no se utiliza IDOtro.
         */
        registro.setDestinatarioTipoIdentificacion(
                null
        );

        registro.setDestinatarioIdOtro(
                null
        );

        registro.setTipoHuella(
                TIPO_HUELLA_SHA_256
        );

        registro.setSistemaNombreRazon(
                datosSistema.nombreRazon()
        );

        registro.setSistemaNif(
                datosSistema.nif()
        );

        registro.setSistemaNombre(
                datosSistema.nombreSistema()
        );

        registro.setSistemaId(
                datosSistema.idSistema()
        );

        registro.setSistemaVersion(
                datosSistema.version()
        );

        registro.setSistemaNumeroInstalacion(
                datosSistema.numeroInstalacion()
        );

        registro.setSistemaSoloVerifactu(
                datosSistema.soloVerifactu()
        );

        registro.setSistemaMultiOt(
                datosSistema.multiOt()
        );

        registro.setSistemaMultiplesOt(
                datosSistema.multiplesOt()
        );

        /*
         * Generación del desglose fiscal inmutable.
         */
        crearDesgloses(
                factura,
                registro
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.PENDIENTE
        );

        /*
         * saveAndFlush provoca también el flush
         * de la entidad Empresa gestionada por
         * Hibernate.
         *
         * Por tanto, si se acaba de generar el
         * número de instalación, se guarda dentro
         * de la misma transacción que el registro.
         */
        return registroRepository
                .saveAndFlush(
                        registro
                );
    }

    /**
     * Crea localmente un registro VERI*FACTU
     * de ANULACION.
     *
     * No realiza ninguna comunicacion con AEAT.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public RegistroFacturacion crearAnulacion(
            Factura factura) {

        if (factura == null) {
            throw error(
                    "No se ha recibido la factura"
            );
        }

        if (factura.getId() == null) {
            throw error(
                    "La factura debe estar guardada "
                            + "antes de generar la anulacion"
            );
        }

        if (factura.getEmpresa() == null
                || factura.getEmpresa().getId() == null) {

            throw error(
                    "La factura no tiene empresa"
            );
        }

        if (factura.getEstado()
                != EstadoFactura.EMITIDA) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede anular una factura "
                            + "en estado EMITIDA"
            );
        }

        Long empresaId =
                factura.getEmpresa().getId();

        /*
         * El bloqueo de empresa serializa la creacion
         * de registros fiscales para esa empresa.
         */
        Empresa empresa =
                empresaRepository
                        .findConBloqueo(
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Empresa no encontrada"
                                ));

        boolean yaExiste =
                registroRepository
                        .existsByFactura_IdAndTipoRegistro(
                                factura.getId(),
                                TipoRegistroFacturacion.ANULACION
                        );

        if (yaExiste) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La factura ya tiene un registro "
                            + "de facturacion de anulacion"
            );
        }

        /*
         * Identificacion fiscal inmutable de la
         * factura que se quiere anular.
         */
        RegistroFacturacion altaOriginal =
                registroRepository
                        .findByFactura_IdAndEmpresa_IdAndTipoRegistro(
                                factura.getId(),
                                empresaId,
                                TipoRegistroFacturacion.ALTA
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "La factura no dispone de un "
                                                + "registro VERI*FACTU "
                                                + "de alta"
                                ));

        /*
         * El registro anterior de la cadena no tiene
         * por que ser el ALTA de esta factura.
         * Debe ser el ultimo registro fiscal de la empresa.
         */
        RegistroFacturacion anterior =
                registroRepository
                        .findTopByEmpresa_IdOrderByIdDesc(
                                empresaId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe registro anterior "
                                                + "para encadenar "
                                                + "la anulacion"
                                ));

        String huellaAnterior =
                obligatorio(
                        anterior.getHuella(),
                        "Huella del registro anterior",
                        64
                );

        DatosSistema datosSistema =
                obtenerDatosSistema(
                        empresa
                );

        OffsetDateTime fechaHoraGeneracion =
                OffsetDateTime.now()
                        .withNano(0);

        String huella =
                huellaVerifactuService
                        .calcularAnulacion(
                                altaOriginal.getNifEmisor(),
                                altaOriginal.getNumSerieFactura(),
                                altaOriginal.getFechaExpedicion(),
                                huellaAnterior,
                                fechaHoraGeneracion
                        );

        RegistroFacturacion registro =
                new RegistroFacturacion();

        registro.setEmpresa(
                empresa
        );

        registro.setFactura(
                factura
        );

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ANULACION
        );

        /*
         * Copiamos la identificacion de la factura
         * desde el ALTA historico.
         */
        registro.setNifEmisor(
                altaOriginal.getNifEmisor()
        );

        registro.setSerie(
                altaOriginal.getSerie()
        );

        registro.setNumero(
                altaOriginal.getNumero()
        );

        registro.setNumSerieFactura(
                altaOriginal.getNumSerieFactura()
        );

        registro.setFechaExpedicion(
                altaOriginal.getFechaExpedicion()
        );

        /*
         * Nuestra tabla utiliza estas columnas como
         * obligatorias tambien para ANULACION.
         * Conservamos el contexto del ALTA original.
         */
        registro.setTipoFactura(
                altaOriginal.getTipoFactura()
        );

        registro.setCuotaTotal(
                normalizarImporte(
                        altaOriginal.getCuotaTotal()
                )
        );

        registro.setImporteTotal(
                normalizarImporte(
                        altaOriginal.getImporteTotal()
                )
        );

        registro.setRegistroAnterior(
                anterior
        );

        registro.setHuellaAnterior(
                huellaAnterior
        );

        registro.setFechaHoraGeneracion(
                huellaVerifactuService
                        .formatearFechaHora(
                                fechaHoraGeneracion
                        )
        );

        registro.setHuella(
                huella
        );

        registro.setIdVersionAeat(
                ID_VERSION_AEAT
        );

        registro.setNombreRazonEmisor(
                altaOriginal.getNombreRazonEmisor()
        );

        registro.setTipoHuella(
                TIPO_HUELLA_SHA_256
        );

        /*
         * El sistema informatico corresponde al
         * sistema que genera la anulacion.
         */
        registro.setSistemaNombreRazon(
                datosSistema.nombreRazon()
        );

        registro.setSistemaNif(
                datosSistema.nif()
        );

        registro.setSistemaNombre(
                datosSistema.nombreSistema()
        );

        registro.setSistemaId(
                datosSistema.idSistema()
        );

        registro.setSistemaVersion(
                datosSistema.version()
        );

        registro.setSistemaNumeroInstalacion(
                datosSistema.numeroInstalacion()
        );

        registro.setSistemaSoloVerifactu(
                datosSistema.soloVerifactu()
        );

        registro.setSistemaMultiOt(
                datosSistema.multiOt()
        );

        registro.setSistemaMultiplesOt(
                datosSistema.multiplesOt()
        );

        registro.setEstadoEnvio(
                EstadoEnvioRegistro.PENDIENTE
        );

        return registroRepository
                .saveAndFlush(
                        registro
                );
    }

    private void validarFacturaEmitible(
            Factura factura) {

        if (factura.getId() == null) {
            throw error(
                    "La factura debe estar guardada "
                            + "antes de generar el registro"
            );
        }

        if (factura.getSerie() == null) {
            throw error(
                    "La factura no tiene serie"
            );
        }

        if (factura.getNumero() == null) {
            throw error(
                    "La factura no tiene número"
            );
        }

        if (factura.getFechaExpedicion() == null) {
            throw error(
                    "La factura no tiene "
                            + "fecha de expedición"
            );
        }

        if (factura.getTipoFactura() == null) {
            throw error(
                    "La factura no tiene tipo fiscal"
            );
        }

        if (factura.getTipoFactura().esRectificativa()) {

            if (factura.getTipoRectificativa() == null) {
                throw error(
                        "La factura rectificativa no tiene "
                                + "tipo de rectificacion S/I"
                );
            }

            if (factura.getTipoRectificativa()
                    == TipoRectificativa.I) {

                if (factura.getBaseRectificada() != null
                        || factura.getCuotaRectificada() != null) {

                    throw error(
                            "Una rectificativa por diferencias "
                                    + "no debe informar "
                                    + "base/cuota rectificada"
                    );
                }
            }

            if (factura.getTipoRectificativa()
                    == TipoRectificativa.S) {

                if (factura.getBaseRectificada() == null
                        || factura.getCuotaRectificada() == null) {

                    throw error(
                            "Una rectificativa por sustitucion "
                                    + "debe informar "
                                    + "base/cuota rectificada"
                    );
                }
            }

            if (factura.getFacturasRectificadas() == null
                    || factura.getFacturasRectificadas().isEmpty()) {

                throw error(
                        "La factura rectificativa no identifica "
                                + "ninguna factura original"
                );
            }

            for (FacturaRectificada relacion
                    : factura.getFacturasRectificadas()) {

                if (relacion == null
                        || relacion.getOrden() == null
                        || relacion.getFacturaRectificada() == null) {

                    throw error(
                            "Existe una referencia de factura "
                                    + "rectificada incompleta"
                    );
                }

                Factura original =
                        relacion.getFacturaRectificada();

                if (original.getEmpresaNif() == null
                        || original.getEmpresaNif().isBlank()
                        || original.getSerie() == null
                        || original.getSerie().getCodigo() == null
                        || original.getSerie().getCodigo().isBlank()
                        || original.getNumero() == null
                        || original.getFechaExpedicion() == null) {

                    throw error(
                            "La factura original no tiene "
                                    + "identificacion fiscal completa"
                    );
                }
            }
        }

        if (factura.getBaseImponible() == null
                || factura.getCuotaImpuesto() == null
                || factura.getTotal() == null) {

            throw error(
                    "La factura no tiene calculados "
                            + "todos sus importes"
            );
        }

        if (factura.getLineas() == null
                || factura.getLineas().isEmpty()) {

            throw error(
                    "La factura no contiene líneas"
            );
        }
    }

    private String construirDescripcionOperacion(
            Factura factura) {

        List<String> descripciones =
                factura.getLineas()
                        .stream()
                        .map(
                                FacturaLinea::getDescripcion
                        )
                        .filter(valor ->
                                valor != null
                                        && !valor.isBlank()
                        )
                        .map(String::trim)
                        .distinct()
                        .toList();

        if (descripciones.isEmpty()) {
            throw error(
                    "No existe descripción "
                            + "de la operación"
            );
        }

        String descripcion =
                String.join(
                        "; ",
                        descripciones
                );

        if (descripcion.length() > 500) {
            throw error(
                    "La descripción de la operación "
                            + "supera los 500 caracteres"
            );
        }

        return descripcion;
    }

    private void crearDesgloses(
            Factura factura,
            RegistroFacturacion registro) {

        Map<ClaveDesglose, TotalesDesglose>
                agrupados =
                new LinkedHashMap<>();

        for (FacturaLinea linea
                : factura.getLineas()) {

            if (linea.getTipoImpuesto() == null) {
                throw error(
                        "Una línea de factura "
                                + "no tiene tipo de impuesto"
                );
            }

            String nombreImpuesto =
                    linea.getTipoImpuesto()
                            .name();

            String impuesto =
                    convertirImpuestoAeat(
                            nombreImpuesto
                    );

            String claveRegimen =
                    obtenerClaveRegimen(
                            impuesto
                    );

            /*
             * Primera versión:
             * operación sujeta y no exenta,
             * sin inversión del sujeto pasivo.
             */
            String calificacionOperacion =
                    "S1";

            BigDecimal tipoImpositivo =
                    normalizarTipoImpositivo(
                            linea.getPorcentajeImpuesto()
                    );

            BigDecimal base =
                    normalizarImporte(
                            linea.getBaseImponible()
                    );

            BigDecimal cuota =
                    normalizarImporte(
                            linea.getCuotaImpuesto()
                    );

            ClaveDesglose clave =
                    new ClaveDesglose(
                            impuesto,
                            claveRegimen,
                            calificacionOperacion,
                            tipoImpositivo
                    );

            TotalesDesglose totales =
                    agrupados.computeIfAbsent(
                            clave,
                            ignored ->
                                    new TotalesDesglose()
                    );

            totales.sumar(
                    base,
                    cuota
            );
        }

        if (agrupados.isEmpty()) {
            throw error(
                    "No se ha podido generar "
                            + "el desglose fiscal"
            );
        }

        if (agrupados.size() > 12) {
            throw error(
                    "El registro fiscal contiene "
                            + "más de 12 desgloses"
            );
        }

        BigDecimal sumaBases =
                BigDecimal.ZERO;

        BigDecimal sumaCuotas =
                BigDecimal.ZERO;

        short orden = 1;

        for (Map.Entry<
                ClaveDesglose,
                TotalesDesglose> entry
                : agrupados.entrySet()) {

            ClaveDesglose clave =
                    entry.getKey();

            TotalesDesglose totales =
                    entry.getValue();

            RegistroFacturacionDesglose desglose =
                    new RegistroFacturacionDesglose();

            desglose.setOrden(
                    orden++
            );

            desglose.setImpuesto(
                    clave.impuesto()
            );

            desglose.setClaveRegimen(
                    clave.claveRegimen()
            );

            desglose.setCalificacionOperacion(
                    clave.calificacionOperacion()
            );

            /*
             * No es una operación exenta en esta
             * primera implementación.
             */
            desglose.setOperacionExenta(
                    null
            );

            desglose.setTipoImpositivo(
                    clave.tipoImpositivo()
            );

            desglose.setBaseImponibleOImporteNoSujeto(
                    totales.getBase()
            );

            desglose.setBaseImponibleACoste(
                    null
            );

            desglose.setCuotaRepercutida(
                    totales.getCuota()
            );

            desglose.setTipoRecargoEquivalencia(
                    null
            );

            desglose.setCuotaRecargoEquivalencia(
                    null
            );

            registro.addDesglose(
                    desglose
            );

            sumaBases =
                    sumaBases.add(
                            totales.getBase()
                    );

            sumaCuotas =
                    sumaCuotas.add(
                            totales.getCuota()
                    );
        }

        sumaBases =
                normalizarImporte(
                        sumaBases
                );

        sumaCuotas =
                normalizarImporte(
                        sumaCuotas
                );

        BigDecimal baseFactura =
                normalizarImporte(
                        factura.getBaseImponible()
                );

        BigDecimal cuotaFactura =
                normalizarImporte(
                        factura.getCuotaImpuesto()
                );

        if (sumaBases.compareTo(
                baseFactura) != 0) {

            throw error(
                    "La suma de bases "
                            + "de los desgloses ("
                            + sumaBases
                            + ") no coincide con "
                            + "la base de la factura ("
                            + baseFactura
                            + ")"
            );
        }

        if (sumaCuotas.compareTo(
                cuotaFactura) != 0) {

            throw error(
                    "La suma de cuotas "
                            + "de los desgloses ("
                            + sumaCuotas
                            + ") no coincide con "
                            + "la cuota de la factura ("
                            + cuotaFactura
                            + ")"
            );
        }
    }

    private String convertirImpuestoAeat(
            String nombreImpuesto) {

        return switch (nombreImpuesto) {

            case "IVA" ->
                    "01";

            case "IPSI" ->
                    "02";

            case "IGIC" ->
                    "03";

            default ->
                    throw error(
                            "Tipo de impuesto todavía "
                                    + "no soportado por la "
                                    + "integración VERI*FACTU: "
                                    + nombreImpuesto
                    );
        };
    }

    private String obtenerClaveRegimen(
            String impuesto) {

        return switch (impuesto) {

            /*
             * Régimen general IVA.
             */
            case "01" ->
                    "01";

            /*
             * IPSI no informa ClaveRegimen.
             */
            case "02" ->
                    null;

            /*
             * Régimen general IGIC.
             */
            case "03" ->
                    "01";

            default ->
                    throw error(
                            "Código de impuesto AEAT "
                                    + "no soportado: "
                                    + impuesto
                    );
        };
    }

    /*
     * Obtiene la identificación estable del SIF
     * correspondiente a esta empresa.
     *
     * Si la empresa todavía no posee número de
     * instalación, se genera exactamente una vez.
     */
    private String obtenerOGenerarNumeroInstalacion(
            Empresa empresa) {

        String existente =
                empresa.getVerifactuNumeroInstalacion();

        if (existente != null
                && !existente.isBlank()) {

            return obligatorio(
                    existente,
                    "Número de instalación VERI*FACTU",
                    100
            );
        }

        String nuevoNumero =
                OffsetDateTime.now()
                        .format(
                                FORMATO_NUMERO_INSTALACION
                        )
                        + "E"
                        + empresa.getId();

        empresa.setVerifactuNumeroInstalacion(
                nuevoNumero
        );

        return nuevoNumero;
    }

    private DatosSistema obtenerDatosSistema(
            Empresa empresa) {

        String nombreRazon =
                obligatorio(
                        sistemaProperties
                                .getNombreRazon(),
                        "VERIFACTU_SISTEMA_NOMBRE_RAZON",
                        120
                );

        String nif =
                obligatorio(
                        sistemaProperties.getNif(),
                        "VERIFACTU_SISTEMA_NIF",
                        20
                );

        String nombreSistema =
                obligatorio(
                        sistemaProperties
                                .getNombreSistema(),
                        "VERIFACTU_SISTEMA_NOMBRE",
                        30
                );

        String idSistema =
                obligatorio(
                        sistemaProperties
                                .getIdSistema(),
                        "VERIFACTU_SISTEMA_ID",
                        2
                );

        String version =
                obligatorio(
                        sistemaProperties
                                .getVersion(),
                        "VERIFACTU_SISTEMA_VERSION",
                        50
                );

        /*
         * El número de instalación ya NO procede
         * de una variable de entorno global.
         *
         * Es propio de esta facturación/empresa.
         */
        String numeroInstalacion =
                obtenerOGenerarNumeroInstalacion(
                        empresa
                );

        String soloVerifactu =
                indicadorSN(
                        sistemaProperties
                                .getSoloVerifactu(),
                        "VERIFACTU_SISTEMA_SOLO_VERIFACTU"
                );

        String multiOt =
                indicadorSN(
                        sistemaProperties
                                .getMultiOt(),
                        "VERIFACTU_SISTEMA_MULTI_OT"
                );

        /*
         * La normativa exige que IndicadorMultiplesOT
         * se obtenga automáticamente del número de
         * obligados tributarios gestionados por el SIF.
         */
        String multiplesOt =
                empresaRepository.count() > 1
                        ? "S"
                        : "N";

        return new DatosSistema(
                nombreRazon,
                nif,
                nombreSistema,
                idSistema,
                version,
                numeroInstalacion,
                soloVerifactu,
                multiOt,
                multiplesOt
        );
    }

    private String indicadorSN(
            String valor,
            String campo) {

        String resultado =
                obligatorio(
                        valor,
                        campo,
                        1
                ).toUpperCase();

        if (!"S".equals(resultado)
                && !"N".equals(resultado)) {

            throw error(
                    campo
                            + " debe contener S o N"
            );
        }

        return resultado;
    }

    private String obligatorio(
            String valor,
            String campo,
            int longitudMaxima) {

        if (valor == null
                || valor.isBlank()) {

            throw error(
                    "Falta el dato obligatorio: "
                            + campo
            );
        }

        String resultado =
                valor.trim();

        if (resultado.length()
                > longitudMaxima) {

            throw error(
                    campo
                            + " supera la longitud máxima "
                            + "de "
                            + longitudMaxima
                            + " caracteres"
            );
        }

        return resultado;
    }

    private BigDecimal normalizarImporte(
            BigDecimal valor) {

        if (valor == null) {
            throw error(
                    "Se ha encontrado un importe nulo"
            );
        }

        return valor.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal normalizarTipoImpositivo(
            BigDecimal valor) {

        if (valor == null) {
            throw error(
                    "Una línea no tiene "
                            + "tipo impositivo"
            );
        }

        return valor.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private ResponseStatusException error(
            String mensaje) {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                mensaje
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroFacturacionResponse> listar(
            Long empresaId) {

        empresaRepository
                .findById(
                        empresaId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Empresa no encontrada"
                        ));

        return registroRepository
                .findAllByEmpresa_IdOrderByIdAsc(
                        empresaId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegistroFacturacionResponse obtener(
            Long empresaId,
            Long registroId) {

        RegistroFacturacion registro =
                registroRepository
                        .findByIdAndEmpresa_Id(
                                registroId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Registro de facturación "
                                                + "no encontrado"
                                ));

        return convertir(
                registro
        );
    }

    private RegistroFacturacionResponse convertir(
            RegistroFacturacion registro) {

        Long registroAnteriorId =
                registro.getRegistroAnterior()
                        == null
                        ? null
                        : registro
                        .getRegistroAnterior()
                        .getId();

        return new RegistroFacturacionResponse(
                registro.getId(),
                registro.getEmpresa().getId(),
                registro.getFactura().getId(),

                registro.getTipoRegistro(),

                registro.getNifEmisor(),

                registro.getSerie(),
                registro.getNumero(),
                registro.getNumSerieFactura(),

                registro.getFechaExpedicion(),
                registro.getTipoFactura(),

                registro.getCuotaTotal(),
                registro.getImporteTotal(),

                registroAnteriorId,
                registro.getHuellaAnterior(),

                registro.getFechaHoraGeneracion(),
                registro.getHuella(),

                registro.getEstadoEnvio(),

                registro.getProveedor(),
                registro.getIdentificadorExterno(),
                registro.getIntentosEnvio(),
                registro.getFechaUltimoIntento(),
                registro.getFechaEnvio(),
                registro.getFechaCreacion()
        );
    }

    private record ClaveDesglose(
            String impuesto,
            String claveRegimen,
            String calificacionOperacion,
            BigDecimal tipoImpositivo) {
    }

    private record DatosSistema(
            String nombreRazon,
            String nif,
            String nombreSistema,
            String idSistema,
            String version,
            String numeroInstalacion,
            String soloVerifactu,
            String multiOt,
            String multiplesOt) {
    }

    private static class TotalesDesglose {

        private BigDecimal base =
                BigDecimal.ZERO;

        private BigDecimal cuota =
                BigDecimal.ZERO;

        void sumar(
                BigDecimal base,
                BigDecimal cuota) {

            this.base =
                    this.base.add(
                            base
                    );

            this.cuota =
                    this.cuota.add(
                            cuota
                    );
        }

        BigDecimal getBase() {
            return base.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal getCuota() {
            return cuota.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }
    }
}
