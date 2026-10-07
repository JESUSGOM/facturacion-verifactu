package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.producto.Producto;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.serie.TipoSerieFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final SerieFacturacionRepository serieRepository;
    private final ProductoRepository productoRepository;
    private final RegistroFacturacionService registroFacturacionService;

    public FacturaService(
            FacturaRepository facturaRepository,
            EmpresaRepository empresaRepository,
            ClienteRepository clienteRepository,
            SerieFacturacionRepository serieRepository,
            ProductoRepository productoRepository,
            RegistroFacturacionService registroFacturacionService) {

        this.facturaRepository = facturaRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.serieRepository = serieRepository;
        this.productoRepository = productoRepository;
        this.registroFacturacionService =
                registroFacturacionService;
    }

    @Transactional
    public FacturaResponse crearBorrador(
            Long empresaId,
            FacturaRequest request) {

        Empresa empresa =
                empresaRepository.findById(
                        empresaId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Empresa no encontrada"
                        ));


        if (!empresa.isActiva()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La empresa no estÃ¡ activa"
            );
        }

        Cliente cliente =
                obtenerClienteActivo(
                        empresaId,
                        request.clienteId()
                );

        SerieFacturacion serie =
                obtenerSerieActiva(
                        empresaId,
                        request.serieId()
                );

        comprobarSerieOrdinaria(
                serie
        );

        Factura factura =
                new Factura();

        factura.setEmpresa(
                empresa
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        factura.setNumero(
                null
        );

        factura.setFechaExpedicion(
                null
        );

        factura.setFechaEmision(
                null
        );

        aplicarBorrador(
                factura,
                empresaId,
                cliente,
                serie,
                request
        );

        Factura guardada =
                facturaRepository.saveAndFlush(
                        factura
                );

        return convertir(
                guardada
        );
    }

    /**
     * Crea una factura simplificada F2 en estado BORRADOR.
     *
     * No existe destinatario identificado.
     *
     * En esta fase no se crea ningun registro VERI*FACTU
     * y no se realiza ningun envio a AEAT.
     */
    @Transactional
    public FacturaResponse crearBorradorSimplificado(
            Long empresaId,
            FacturaSimplificadaRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar los datos de la factura simplificada"
            );
        }

        Empresa empresa =
                empresaRepository
                        .findById(
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Empresa no encontrada"
                                ));

        if (!empresa.isActiva()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La empresa no esta activa"
            );
        }

        SerieFacturacion serie =
                obtenerSerieActiva(
                        empresaId,
                        request.serieId()
                );

        if (serie.getTipoSerie()
                != TipoSerieFacturacion.SIMPLIFICADA) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La serie seleccionada no es una serie simplificada"
            );
        }

        Factura factura =
                new Factura();

        factura.setEmpresa(
                empresa
        );

        factura.setCliente(
                null
        );

        factura.setSerie(
                serie
        );

        factura.setTipoFactura(
                TipoFactura.F2
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        factura.setNumero(
                null
        );

        factura.setFechaExpedicion(
                null
        );

        factura.setFechaEmision(
                null
        );

        aplicarBorradorSimplificado(
                factura,
                empresaId,
                request
        );

        Factura guardada =
                facturaRepository.saveAndFlush(
                        factura
                );

        return convertir(
                guardada
        );
    }
    /**
     * Crea una factura rectificativa en estado BORRADOR.
     *
     * En esta fase no se crea ningun registro VERI*FACTU
     * y no se realiza ningun envio a AEAT.
     */
    @Transactional
    public FacturaResponse crearBorradorRectificativo(
            Long empresaId,
            FacturaRectificativaRequest request) {

        Factura factura =
                construirBorradorRectificativo(
                        empresaId,
                        request
                );

        Factura guardada =
                facturaRepository.saveAndFlush(
                        factura
                );

        return convertir(
                guardada
        );
    }

    private Factura construirBorradorRectificativo(
            Long empresaId,
            FacturaRectificativaRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar los datos de la factura rectificativa"
            );
        }

        Empresa empresa =
                empresaRepository
                        .findById(
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Empresa no encontrada"
                                ));

        if (!empresa.isActiva()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La empresa no esta activa"
            );
        }

        if (request.tipoFactura() == null
                || !request.tipoFactura().esRectificativa()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo de factura debe ser rectificativo"
            );
        }

        /*
         * R5 rectifica una factura simplificada.
         *
         * El programa todavia no genera F2, por lo que no
         * permitimos crear una R5 que no pueda vincularse
         * correctamente a una factura simplificada.
         */
        if (request.tipoFactura() == TipoFactura.R5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R5 requiere rectificar una factura simplificada F2, "
                            + "todavia no soportada"
            );
        }

        if (request.tipoRectificativa() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar el tipo de rectificacion I o S"
            );
        }

        String motivo =
                request.motivoRectificacion() == null
                        ? ""
                        : request.motivoRectificacion().trim();

        if (motivo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar el motivo de la rectificacion"
            );
        }

        if (motivo.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El motivo de la rectificacion no puede superar "
                            + "500 caracteres"
            );
        }

        SerieFacturacion serie =
                obtenerSerieActiva(
                        empresaId,
                        request.serieId()
                );

        if (serie.getTipoSerie()
                != TipoSerieFacturacion.RECTIFICATIVA) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La serie seleccionada no es una serie rectificativa"
            );
        }

        if (request.facturasRectificadasIds() == null
                || request.facturasRectificadasIds().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar al menos una factura original"
            );
        }

        /*
         * Evitamos insertar dos veces la misma factura original.
         */
        Set<Long> idsUnicos =
                new HashSet<>();

        for (Long originalId
                : request.facturasRectificadasIds()) {

            if (originalId == null
                    || !idsUnicos.add(originalId)) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Las facturas originales no pueden contener "
                                + "identificadores nulos o duplicados"
                );
            }
        }

        Factura factura =
                new Factura();

        factura.setEmpresa(
                empresa
        );

        factura.setSerie(
                serie
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        factura.setNumero(
                null
        );

        factura.setFechaExpedicion(
                null
        );

        factura.setFechaEmision(
                null
        );

        factura.setTipoFactura(
                request.tipoFactura()
        );

        factura.setTipoRectificativa(
                request.tipoRectificativa()
        );

        factura.setMotivoRectificacion(
                motivo
        );

        if (request.observaciones() == null
                || request.observaciones().isBlank()) {

            factura.setObservaciones(
                    null
            );

        } else {

            factura.setObservaciones(
                    request.observaciones().trim()
            );
        }

        Cliente cliente = null;
        Factura primeraOriginal = null;

        BigDecimal baseOriginal =
                BigDecimal.ZERO;

        BigDecimal cuotaOriginal =
                BigDecimal.ZERO;

        int ordenOriginal = 1;

        for (Long originalId
                : request.facturasRectificadasIds()) {

            Factura original =
                    facturaRepository
                            .findByIdAndEmpresa_Id(
                                    originalId,
                                    empresaId
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Factura original no encontrada: "
                                                    + originalId
                                    ));

            if (original.getEstado()
                    != EstadoFactura.EMITIDA) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La factura original "
                                + originalId
                                + " no esta en estado EMITIDA"
                );
            }

            /*
             * Primera version del circuito:
             * rectificamos facturas completas F1.
             *
             * R5 queda reservado para F2 cuando el programa
             * incorpore factura simplificada.
             */
            if (original.getTipoFactura()
                    != TipoFactura.F1) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Por ahora solo se pueden rectificar "
                                + "facturas originales F1"
                );
            }

            if (cliente == null) {

                cliente =
                        original.getCliente();

                primeraOriginal =
                        original;

            } else if (!cliente.getId().equals(
                    original.getCliente().getId()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Todas las facturas originales deben "
                                + "pertenecer al mismo cliente"
                );
            }

            baseOriginal =
                    baseOriginal.add(
                            original.getBaseImponible()
                    );

            cuotaOriginal =
                    cuotaOriginal.add(
                            original.getCuotaImpuesto()
                    );

            FacturaRectificada relacion =
                    new FacturaRectificada();

            relacion.setFacturaRectificada(
                    original
            );

            relacion.setOrden(
                    ordenOriginal++
            );

            factura.agregarFacturaRectificada(
                    relacion
            );
        }

        if (cliente == null
                || primeraOriginal == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se ha podido determinar la factura original"
            );
        }

        factura.setCliente(
                cliente
        );

        /*
         * Conservamos el snapshot historico del destinatario
         * de la primera factura original.
         *
         * No dependemos de que el maestro Cliente se haya
         * modificado posteriormente.
         */
        copiarDatosClienteDesdeFactura(
                factura,
                primeraOriginal
        );

        if (request.tipoRectificativa()
                == TipoRectificativa.I) {

            factura.setBaseRectificada(
                    null
            );

            factura.setCuotaRectificada(
                    null
            );

        } else {

            factura.setBaseRectificada(
                    baseOriginal.setScale(
                            2,
                            RoundingMode.HALF_UP
                    )
            );

            factura.setCuotaRectificada(
                    cuotaOriginal.setScale(
                            2,
                            RoundingMode.HALF_UP
                    )
            );
        }

        BigDecimal baseTotal =
                BigDecimal.ZERO;

        BigDecimal cuotaTotal =
                BigDecimal.ZERO;

        BigDecimal totalFactura =
                BigDecimal.ZERO;

        int ordenLinea = 1;

        for (FacturaRectificativaLineaRequest lineaRequest
                : request.lineas()) {

            int signo =
                    lineaRequest.signo();

            if (signo != 1
                    && signo != -1) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El signo de una linea rectificativa "
                                + "debe ser 1 o -1"
                );
            }

            /*
             * En sustitucion las lineas representan
             * los nuevos importes correctos.
             */
            if (request.tipoRectificativa()
                    == TipoRectificativa.S
                    && signo != 1) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Una rectificativa por sustitucion "
                                + "no puede contener lineas negativas"
                );
            }

            Producto producto = null;

            /*
             * El producto es solamente una referencia opcional.
             * No tomamos de el precio, descripcion ni impuesto.
             */
            if (lineaRequest.productoId() != null) {

                producto =
                        productoRepository
                                .findByIdAndEmpresa_Id(
                                        lineaRequest.productoId(),
                                        empresaId
                                )
                                .orElseThrow(() ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Producto o servicio no encontrado: "
                                                        + lineaRequest.productoId()
                                        ));
            }

            String descripcion =
                    lineaRequest.descripcion() == null
                            ? ""
                            : lineaRequest.descripcion().trim();

            if (descripcion.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La descripcion de la linea es obligatoria"
                );
            }

            BigDecimal cantidad =
                    lineaRequest.cantidad();

            BigDecimal precioUnitario =
                    lineaRequest.precioUnitario();

            BigDecimal porcentaje =
                    lineaRequest.porcentajeImpuesto();

            if (cantidad == null
                    || cantidad.signum() <= 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad debe ser mayor que cero"
                );
            }

            if (precioUnitario == null
                    || precioUnitario.signum() < 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El precio unitario no puede ser negativo"
                );
            }

            if (porcentaje == null
                    || porcentaje.compareTo(BigDecimal.ZERO) < 0
                    || porcentaje.compareTo(
                            new BigDecimal("100")
                    ) > 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El porcentaje de impuesto debe estar "
                                + "entre 0 y 100"
                );
            }

            BigDecimal basePositiva =
                    cantidad
                            .multiply(
                                    precioUnitario
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal cuotaPositiva =
                    basePositiva
                            .multiply(
                                    porcentaje
                            )
                            .divide(
                                    new BigDecimal("100"),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal factorSigno =
                    BigDecimal.valueOf(
                            signo
                    );

            BigDecimal base =
                    basePositiva
                            .multiply(
                                    factorSigno
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal cuota =
                    cuotaPositiva
                            .multiply(
                                    factorSigno
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal totalLinea =
                    base
                            .add(
                                    cuota
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            FacturaLinea linea =
                    new FacturaLinea();

            linea.setProducto(
                    producto
            );

            linea.setDescripcion(
                    descripcion
            );

            /*
             * Cantidad y precio permanecen positivos.
             * El sentido economico se almacena en signo.
             */
            linea.setCantidad(
                    cantidad
            );

            linea.setPrecioUnitario(
                    precioUnitario
            );

            linea.setSigno(
                    signo
            );

            linea.setTipoImpuesto(
                    lineaRequest.tipoImpuesto()
            );

            linea.setPorcentajeImpuesto(
                    porcentaje
            );

            linea.setBaseImponible(
                    base
            );

            linea.setCuotaImpuesto(
                    cuota
            );

            linea.setTotal(
                    totalLinea
            );

            linea.setOrden(
                    ordenLinea++
            );

            factura.agregarLinea(
                    linea
            );

            baseTotal =
                    baseTotal.add(
                            base
                    );

            cuotaTotal =
                    cuotaTotal.add(
                            cuota
                    );

            totalFactura =
                    totalFactura.add(
                            totalLinea
                    );
        }

        factura.setBaseImponible(
                baseTotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        factura.setCuotaImpuesto(
                cuotaTotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        factura.setTotal(
                totalFactura.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        return factura;
    }

    @Transactional
    public FacturaResponse modificarBorradorRectificativo(
            Long empresaId,
            Long facturaId,
            FacturaRectificativaRequest request) {

        Factura factura =
                obtenerFacturaConBloqueo(
                        empresaId,
                        facturaId
                );

        comprobarBorrador(
                factura,
                "Solo se puede modificar una factura rectificativa "
                        + "en estado BORRADOR"
        );

        if (factura.getTipoFactura() == null
                || !factura.getTipoFactura().esRectificativa()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La factura indicada no es rectificativa"
            );
        }

        /*
         * Construimos primero una propuesta completa y validada.
         * Si alguna validacion falla, el borrador existente
         * permanece sin cambios.
         */
        Factura propuesta =
                construirBorradorRectificativo(
                        empresaId,
                        request
                );

        /*
         * Eliminamos primero las relaciones actuales.
         * orphanRemoval se encarga de borrar los registros
         * anteriores antes de reconstruir el borrador.
         */
        factura.getLineas().clear();
        factura.limpiarFacturasRectificadas();

        facturaRepository.flush();

        factura.setCliente(
                propuesta.getCliente()
        );

        factura.setSerie(
                propuesta.getSerie()
        );

        factura.setTipoFactura(
                propuesta.getTipoFactura()
        );

        factura.setTipoRectificativa(
                propuesta.getTipoRectificativa()
        );

        factura.setMotivoRectificacion(
                propuesta.getMotivoRectificacion()
        );

        factura.setBaseRectificada(
                propuesta.getBaseRectificada()
        );

        factura.setCuotaRectificada(
                propuesta.getCuotaRectificada()
        );

        factura.setObservaciones(
                propuesta.getObservaciones()
        );

        copiarDatosClienteDesdeFactura(
                factura,
                propuesta
        );

        for (FacturaRectificada relacion
                : propuesta.getFacturasRectificadas()) {

            factura.agregarFacturaRectificada(
                    relacion
            );
        }

        for (FacturaLinea linea
                : propuesta.getLineas()) {

            factura.agregarLinea(
                    linea
            );
        }

        factura.setBaseImponible(
                propuesta.getBaseImponible()
        );

        factura.setCuotaImpuesto(
                propuesta.getCuotaImpuesto()
        );

        factura.setTotal(
                propuesta.getTotal()
        );

        Factura guardada =
                facturaRepository.saveAndFlush(
                        factura
                );

        return convertir(
                guardada
        );
    }

    @Transactional
    public FacturaResponse modificarBorrador(
            Long empresaId,
            Long facturaId,
            FacturaRequest request) {

        Factura factura =
                obtenerFacturaConBloqueo(
                        empresaId,
                        facturaId
                );

        comprobarBorrador(
                factura,
                "Solo se puede modificar una factura en estado BORRADOR"
        );

        if (factura.getTipoFactura() == TipoFactura.F2) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Una factura simplificada debe modificarse "
                            + "mediante su circuito especifico"
            );
        }
        if (factura.getTipoFactura() != null
                && factura.getTipoFactura().esRectificativa()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Una factura rectificativa debe modificarse "
                            + "mediante su circuito especifico"
            );
        }

        Cliente cliente =
                obtenerClienteActivo(
                        empresaId,
                        request.clienteId()
                );

        SerieFacturacion serie =
                obtenerSerieActiva(
                        empresaId,
                        request.serieId()
                );

        comprobarSerieOrdinaria(
                serie
        );

        /*
         * Las lÃ­neas emitidas nunca pasan por aquÃ­.
         *
         * Al tratarse de un BORRADOR podemos reemplazar
         * Ã­ntegramente sus lÃ­neas. orphanRemoval=true
         * eliminarÃ¡ las anteriores al hacer flush.
         */
        factura.getLineas().clear();

        aplicarBorrador(
                factura,
                empresaId,
                cliente,
                serie,
                request
        );

        Factura guardada =
                facturaRepository.saveAndFlush(
                        factura
                );

        return convertir(
                guardada
        );
    }

    @Transactional
    public void eliminarBorrador(
            Long empresaId,
            Long facturaId) {

        Factura factura =
                obtenerFacturaConBloqueo(
                        empresaId,
                        facturaId
                );

        comprobarBorrador(
                factura,
                "Solo se puede eliminar una factura en estado BORRADOR"
        );

        facturaRepository.delete(
                factura
        );

        facturaRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarRectificables(
            Long empresaId) {

        comprobarEmpresa(
                empresaId
        );

        return facturaRepository
                .findAllByEmpresa_IdAndEstadoAndTipoFacturaOrderByIdDesc(
                        empresaId,
                        EstadoFactura.EMITIDA,
                        TipoFactura.F1
                )
                .stream()
                .map(this::convertir)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<FacturaResponse> listar(
            Long empresaId) {

        comprobarEmpresa(
                empresaId
        );

        return facturaRepository
                .findAllByEmpresa_IdOrderByIdDesc(
                        empresaId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtener(
            Long empresaId,
            Long facturaId) {

        Factura factura =
                facturaRepository
                        .findByIdAndEmpresa_Id(
                                facturaId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Factura no encontrada"
                                ));

        return convertir(
                factura
        );
    }

    @Transactional
    public FacturaResponse emitir(
            Long empresaId,
            Long facturaId) {

        /*
         * Bloqueamos tambiÃ©n la factura.
         *
         * AsÃ­ dos peticiones concurrentes no pueden
         * emitir el mismo borrador al mismo tiempo.
         */
        Factura factura =
                obtenerFacturaConBloqueo(
                        empresaId,
                        facturaId
                );

        comprobarBorrador(
                factura,
                "Solo se puede emitir una factura en estado BORRADOR"
        );
        if (factura.getEmpresa() == null
                || !factura.getEmpresa().isActiva()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La empresa no estÃ¡ activa"
            );
        }

        if (factura.getLineas() == null
                || factura.getLineas().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede emitir una factura sin lÃ­neas"
            );
        }

        /*
         * Comprobamos nuevamente los maestros.
         *
         * Un borrador antiguo no debe poder emitirse si
         * entretanto el cliente o un producto ha quedado
         * desactivado.
         */
        if (factura.getTipoFactura() == TipoFactura.F2) {

            if (factura.getCliente() != null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La factura F2 sin destinatario no debe tener cliente"
                );
            }

        } else if (factura.getCliente() == null
                || !factura.getCliente().isActivo()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cliente de la factura no esta activo"
            );
        }

        for (FacturaLinea linea : factura.getLineas()) {

            if (linea.getProducto() != null
                    && !linea.getProducto().isActivo()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La factura contiene un producto "
                                + "o servicio no activo"
                );
            }
        }

        Long serieId =
                factura.getSerie().getId();

        SerieFacturacion serie =
                serieRepository
                        .findConBloqueo(
                                serieId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Serie de facturaciÃ³n no encontrada"
                                ));

        if (!serie.isActiva()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La serie de facturaciÃ³n no estÃ¡ activa"
            );
        }

        if (factura.getTipoFactura() == TipoFactura.F2
                && serie.getTipoSerie()
                != TipoSerieFacturacion.SIMPLIFICADA) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Una factura F2 debe utilizar una serie simplificada"
            );
        }

        long siguienteNumero =
                serie.getUltimoNumero() + 1L;

        serie.setUltimoNumero(
                siguienteNumero
        );

        factura.setNumero(
                siguienteNumero
        );

        factura.setFechaExpedicion(
                LocalDate.now()
        );

        factura.setFechaEmision(
                Instant.now()
        );

        factura.setEstado(
                EstadoFactura.EMITIDA
        );

        /*
         * Snapshot historico del emisor.
         *
         * Desde este momento la representacion
         * de la factura deja de depender de cambios
         * posteriores en la entidad Empresa.
         */
        copiarDatosEmpresa(
                factura,
                factura.getEmpresa()
        );

        serieRepository.save(
                serie
        );

        Factura emitida =
                facturaRepository.saveAndFlush(
                        factura
                );

        /*
         * La creaciÃ³n del registro VERI*FACTU continÃºa
         * dentro de esta misma transacciÃ³n.
         *
         * No realiza ningÃºn envÃ­o a AEAT.
         */
        registroFacturacionService.crearAlta(
                emitida
        );

        return convertir(
                emitida
        );
    }

    private void copiarDatosEmpresa(
            Factura factura,
            Empresa empresa) {

        if (empresa == null) {
            throw new IllegalStateException(
                    "La factura no tiene empresa emisora"
            );
        }

        factura.setEmpresaNif(
                empresa.getNif()
        );

        factura.setEmpresaRazonSocial(
                empresa.getRazonSocial()
        );

        factura.setEmpresaNombreComercial(
                empresa.getNombreComercial()
        );

        factura.setEmpresaDireccion(
                empresa.getDireccion()
        );

        factura.setEmpresaCodigoPostal(
                empresa.getCodigoPostal()
        );

        factura.setEmpresaMunicipio(
                empresa.getMunicipio()
        );

        factura.setEmpresaProvincia(
                empresa.getProvincia()
        );

        factura.setEmpresaCodigoPais(
                empresa.getCodigoPais()
        );
    }

    /**
     * Anula una factura previamente emitida.
     *
     * La creacion del registro VERI*FACTU de ANULACION
     * y el cambio de estado de la factura se realizan
     * dentro de la misma transaccion.
     *
     * Este metodo NO realiza ningun envio a AEAT.
     */
    /**
     * Anula una factura previamente emitida.
     *
     * La anulacion solo debe utilizarse cuando la factura
     * no debio haberse emitido y no procede una factura
     * rectificativa.
     *
     * Este metodo crea exclusivamente el registro local.
     * NO realiza ningun envio a AEAT.
     */
    @Transactional
    public FacturaResponse anular(
            Long empresaId,
            Long facturaId,
            AnulacionFacturaRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar los datos de la anulacion"
            );
        }

        String motivo =
                request.motivo() == null
                        ? ""
                        : request.motivo().trim();

        if (motivo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar el motivo de la anulacion"
            );
        }

        if (motivo.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El motivo de la anulacion no puede "
                            + "superar 500 caracteres"
            );
        }

        /*
         * Esta confirmacion evita utilizar ANULACION
         * como sustituto de una factura rectificativa.
         */
        if (!request.confirmarQueNoProcedeRectificativa()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La anulacion solo procede cuando la factura "
                            + "no debio emitirse y no corresponde "
                            + "emitir una factura rectificativa"
            );
        }

        /*
         * Bloqueamos la factura para impedir dos
         * anulaciones concurrentes.
         */
        Factura factura =
                obtenerFacturaConBloqueo(
                        empresaId,
                        facturaId
                );

        if (factura.getEstado()
                != EstadoFactura.EMITIDA) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se puede anular una factura "
                            + "en estado EMITIDA"
            );
        }

        /*
         * La factura debe seguir EMITIDA mientras se
         * construye el registro fiscal de anulacion.
         */
        registroFacturacionService
                .crearAnulacion(
                        factura
                );

        /*
         * Si cualquier operacion posterior falla,
         * toda esta transaccion se revierte.
         */
        factura.setEstado(
                EstadoFactura.ANULADA
        );

        factura.setFechaAnulacion(
                Instant.now()
        );

        factura.setMotivoAnulacion(
                motivo
        );

        Factura anulada =
                facturaRepository
                        .saveAndFlush(
                                factura
                        );

        return convertir(
                anulada
        );
    }

    private void aplicarBorradorSimplificado(
            Factura factura,
            Long empresaId,
            FacturaSimplificadaRequest request) {

        if (request.observaciones() == null
                || request.observaciones().isBlank()) {

            factura.setObservaciones(
                    null
            );

        } else {

            factura.setObservaciones(
                    request.observaciones().trim()
            );
        }

        /*
         * F2 sin destinatario identificado.
         * El snapshot del cliente debe permanecer vacio.
         */
        factura.setClienteNif(
                null
        );

        factura.setClienteNombre(
                null
        );

        factura.setClienteDireccion(
                null
        );

        factura.setClienteCodigoPostal(
                null
        );

        factura.setClienteMunicipio(
                null
        );

        factura.setClienteProvincia(
                null
        );

        factura.setClienteCodigoPais(
                null
        );

        BigDecimal baseTotal =
                BigDecimal.ZERO;

        BigDecimal cuotaTotal =
                BigDecimal.ZERO;

        BigDecimal totalFactura =
                BigDecimal.ZERO;

        int orden = 1;

        for (FacturaLineaRequest lineaRequest
                : request.lineas()) {

            Producto producto =
                    obtenerProductoActivo(
                            empresaId,
                            lineaRequest.productoId()
                    );

            BigDecimal cantidad =
                    lineaRequest.cantidad();

            BigDecimal precioUnitario =
                    producto.getPrecio();

            BigDecimal base =
                    cantidad
                            .multiply(
                                    precioUnitario
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal cuota =
                    base
                            .multiply(
                                    producto.getPorcentajeImpuesto()
                            )
                            .divide(
                                    new BigDecimal("100"),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal totalLinea =
                    base
                            .add(
                                    cuota
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            FacturaLinea linea =
                    new FacturaLinea();

            linea.setProducto(
                    producto
            );

            linea.setDescripcion(
                    producto.getNombre()
            );

            linea.setCantidad(
                    cantidad
            );

            linea.setPrecioUnitario(
                    precioUnitario
            );

            linea.setSigno(
                    1
            );

            linea.setTipoImpuesto(
                    producto.getTipoImpuesto()
            );

            linea.setPorcentajeImpuesto(
                    producto.getPorcentajeImpuesto()
            );

            linea.setBaseImponible(
                    base
            );

            linea.setCuotaImpuesto(
                    cuota
            );

            linea.setTotal(
                    totalLinea
            );

            linea.setOrden(
                    orden++
            );

            factura.agregarLinea(
                    linea
            );

            baseTotal =
                    baseTotal.add(
                            base
                    );

            cuotaTotal =
                    cuotaTotal.add(
                            cuota
                    );

            totalFactura =
                    totalFactura.add(
                            totalLinea
                    );
        }

        factura.setBaseImponible(
                baseTotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        factura.setCuotaImpuesto(
                cuotaTotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        factura.setTotal(
                totalFactura.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );
    }
    private void aplicarBorrador(
            Factura factura,
            Long empresaId,
            Cliente cliente,
            SerieFacturacion serie,
            FacturaRequest request) {

        factura.setCliente(
                cliente
        );

        factura.setSerie(
                serie
        );

        if (request.observaciones() == null
                || request.observaciones().isBlank()) {

            factura.setObservaciones(
                    null
            );

        } else {

            factura.setObservaciones(
                    request.observaciones().trim()
            );
        }

        copiarDatosCliente(
                factura,
                cliente
        );

        BigDecimal baseTotal =
                BigDecimal.ZERO;

        BigDecimal cuotaTotal =
                BigDecimal.ZERO;

        BigDecimal totalFactura =
                BigDecimal.ZERO;

        int orden = 1;

        for (FacturaLineaRequest lineaRequest
                : request.lineas()) {

            Producto producto =
                    obtenerProductoActivo(
                            empresaId,
                            lineaRequest.productoId()
                    );

            BigDecimal cantidad =
                    lineaRequest.cantidad();

            BigDecimal precioUnitario =
                    producto.getPrecio();

            BigDecimal base =
                    cantidad
                            .multiply(
                                    precioUnitario
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal cuota =
                    base
                            .multiply(
                                    producto
                                            .getPorcentajeImpuesto()
                            )
                            .divide(
                                    new BigDecimal("100"),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal totalLinea =
                    base
                            .add(
                                    cuota
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            FacturaLinea linea =
                    new FacturaLinea();

            linea.setProducto(
                    producto
            );

            linea.setDescripcion(
                    producto.getNombre()
            );

            linea.setCantidad(
                    cantidad
            );

            linea.setPrecioUnitario(
                    precioUnitario
            );

        /*
         * El circuito ordinario solo genera importes positivos.
         * Las disminuciones se habilitaran exclusivamente
         * mediante el circuito especifico de rectificativas.
         */
        linea.setSigno(
                1
        );

            linea.setTipoImpuesto(
                    producto.getTipoImpuesto()
            );

            linea.setPorcentajeImpuesto(
                    producto.getPorcentajeImpuesto()
            );

            linea.setBaseImponible(
                    base
            );

            linea.setCuotaImpuesto(
                    cuota
            );

            linea.setTotal(
                    totalLinea
            );

            linea.setOrden(
                    orden++
            );

            factura.agregarLinea(
                    linea
            );

            baseTotal =
                    baseTotal.add(
                            base
                    );

            cuotaTotal =
                    cuotaTotal.add(
                            cuota
                    );

            totalFactura =
                    totalFactura.add(
                            totalLinea
                    );
        }

        factura.setBaseImponible(
                baseTotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        factura.setCuotaImpuesto(
                cuotaTotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        factura.setTotal(
                totalFactura.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );
    }

    private Factura obtenerFacturaConBloqueo(
            Long empresaId,
            Long facturaId) {

        return facturaRepository
                .findConBloqueo(
                        empresaId,
                        facturaId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Factura no encontrada"
                        ));
    }

    private Cliente obtenerClienteActivo(
            Long empresaId,
            Long clienteId) {

        Cliente cliente =
                clienteRepository
                        .findByIdAndEmpresa_Id(
                                clienteId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Cliente no encontrado"
                                ));

        if (!cliente.isActivo()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cliente no estÃ¡ activo"
            );
        }

        return cliente;
    }

    private void comprobarSerieOrdinaria(
            SerieFacturacion serie) {

        if (serie.getTipoSerie()
                != TipoSerieFacturacion.ORDINARIA) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La serie seleccionada no es una serie ordinaria"
            );
        }
    }
    private SerieFacturacion obtenerSerieActiva(
            Long empresaId,
            Long serieId) {

        SerieFacturacion serie =
                serieRepository
                        .findByIdAndEmpresa_Id(
                                serieId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Serie de facturaciÃ³n no encontrada"
                                ));

        if (!serie.isActiva()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La serie de facturaciÃ³n no estÃ¡ activa"
            );
        }

        return serie;
    }

    private Producto obtenerProductoActivo(
            Long empresaId,
            Long productoId) {

        Producto producto =
                productoRepository
                        .findByIdAndEmpresa_Id(
                                productoId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Producto o servicio no encontrado: "
                                                + productoId
                                ));

        if (!producto.isActivo()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El producto o servicio "
                            + productoId
                            + " no estÃ¡ activo"
            );
        }

        return producto;
    }

    private void comprobarBorrador(
            Factura factura,
            String mensaje) {

        if (factura.getEstado()
                != EstadoFactura.BORRADOR) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    mensaje
            );
        }
    }

    private void comprobarEmpresa(
            Long empresaId) {

        if (!empresaRepository.existsById(
                empresaId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa no encontrada"
            );
        }
    }

    private void copiarDatosCliente(
            Factura factura,
            Cliente cliente) {

        factura.setClienteNif(
                cliente.getNif()
        );

        factura.setClienteNombre(
                cliente.getNombre()
        );

        factura.setClienteDireccion(
                cliente.getDireccion()
        );

        factura.setClienteCodigoPostal(
                cliente.getCodigoPostal()
        );

        factura.setClienteMunicipio(
                cliente.getMunicipio()
        );

        factura.setClienteProvincia(
                cliente.getProvincia()
        );

        factura.setClienteCodigoPais(
                cliente.getCodigoPais()
        );
    }

    private void copiarDatosClienteDesdeFactura(
            Factura destino,
            Factura origen) {

        destino.setClienteNif(
                origen.getClienteNif()
        );

        destino.setClienteNombre(
                origen.getClienteNombre()
        );

        destino.setClienteDireccion(
                origen.getClienteDireccion()
        );

        destino.setClienteCodigoPostal(
                origen.getClienteCodigoPostal()
        );

        destino.setClienteMunicipio(
                origen.getClienteMunicipio()
        );

        destino.setClienteProvincia(
                origen.getClienteProvincia()
        );

        destino.setClienteCodigoPais(
                origen.getClienteCodigoPais()
        );
    }
    private FacturaResponse convertir(
            Factura factura) {

        List<FacturaLineaResponse> lineas =
                factura.getLineas()
                        .stream()
                        .map(
                                this::convertirLinea
                        )
                        .toList();

        List<FacturaRectificadaResponse> facturasRectificadas =
                factura.getFacturasRectificadas()
                        .stream()
                        .map(
                                this::convertirFacturaRectificada
                        )
                        .toList();

        return new FacturaResponse(
                factura.getId(),
                factura.getEmpresa().getId(),
                factura.getCliente() != null
                        ? factura.getCliente().getId()
                        : null,
                factura.getSerie().getId(),
                factura.getSerie().getCodigo(),

                factura.getEstado(),
                factura.getNumero(),
                factura.getFechaExpedicion(),

                factura.getTipoFactura(),
                factura.getTipoRectificativa(),
                factura.getMotivoRectificacion(),
                factura.getBaseRectificada(),
                factura.getCuotaRectificada(),

                factura.getObservaciones(),

                factura.getBaseImponible(),
                factura.getCuotaImpuesto(),
                factura.getTotal(),

                factura.getClienteNif(),
                factura.getClienteNombre(),
                factura.getClienteDireccion(),
                factura.getClienteCodigoPostal(),
                factura.getClienteMunicipio(),
                factura.getClienteProvincia(),
                factura.getClienteCodigoPais(),

                factura.getFechaCreacion(),
                factura.getFechaModificacion(),
                factura.getFechaEmision(),

                factura.getFechaAnulacion(),
                factura.getMotivoAnulacion(),

                facturasRectificadas,

                lineas
        );
    }

    private FacturaRectificadaResponse convertirFacturaRectificada(
            FacturaRectificada relacion) {

        Factura original =
                relacion.getFacturaRectificada();

        Long serieId =
                original.getSerie() != null
                        ? original.getSerie().getId()
                        : null;

        String serieCodigo =
                original.getSerie() != null
                        ? original.getSerie().getCodigo()
                        : null;

        return new FacturaRectificadaResponse(
                original.getId(),
                serieId,
                serieCodigo,
                original.getNumero(),
                original.getFechaExpedicion(),
                original.getBaseImponible(),
                original.getCuotaImpuesto(),
                original.getTotal()
        );
    }
    private FacturaLineaResponse convertirLinea(
            FacturaLinea linea) {

        return new FacturaLineaResponse(
                linea.getId(),
                linea.getProducto() != null
                        ? linea.getProducto().getId()
                        : null,
                linea.getDescripcion(),
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                linea.getSigno(),
                linea.getTipoImpuesto(),
                linea.getPorcentajeImpuesto(),
                linea.getBaseImponible(),
                linea.getCuotaImpuesto(),
                linea.getTotal(),
                linea.getOrden()
        );
    }
}
