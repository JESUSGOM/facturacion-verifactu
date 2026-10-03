package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.producto.Producto;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
                    "La empresa no está activa"
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

        /*
         * Las líneas emitidas nunca pasan por aquí.
         *
         * Al tratarse de un BORRADOR podemos reemplazar
         * íntegramente sus líneas. orphanRemoval=true
         * eliminará las anteriores al hacer flush.
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
         * Bloqueamos también la factura.
         *
         * Así dos peticiones concurrentes no pueden
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
                    "La empresa no está activa"
            );
        }

        if (factura.getLineas() == null
                || factura.getLineas().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede emitir una factura sin líneas"
            );
        }

        /*
         * Comprobamos nuevamente los maestros.
         *
         * Un borrador antiguo no debe poder emitirse si
         * entretanto el cliente o un producto ha quedado
         * desactivado.
         */
        if (factura.getCliente() == null
                || !factura.getCliente().isActivo()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cliente de la factura no está activo"
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
                                        "Serie de facturación no encontrada"
                                ));

        if (!serie.isActiva()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La serie de facturación no está activa"
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

        serieRepository.save(
                serie
        );

        Factura emitida =
                facturaRepository.saveAndFlush(
                        factura
                );

        /*
         * La creación del registro VERI*FACTU continúa
         * dentro de esta misma transacción.
         *
         * No realiza ningún envío a AEAT.
         */
        registroFacturacionService.crearAlta(
                emitida
        );

        return convertir(
                emitida
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
                    "El cliente no está activo"
            );
        }

        return cliente;
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
                                        "Serie de facturación no encontrada"
                                ));

        if (!serie.isActiva()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La serie de facturación no está activa"
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
                            + " no está activo"
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

    private FacturaResponse convertir(
            Factura factura) {

        List<FacturaLineaResponse> lineas =
                factura.getLineas()
                        .stream()
                        .map(
                                this::convertirLinea
                        )
                        .toList();

        return new FacturaResponse(
                factura.getId(),
                factura.getEmpresa().getId(),
                factura.getCliente().getId(),
                factura.getSerie().getId(),
                factura.getSerie().getCodigo(),

                factura.getEstado(),
                factura.getNumero(),
                factura.getFechaExpedicion(),

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

                lineas
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
                linea.getTipoImpuesto(),
                linea.getPorcentajeImpuesto(),
                linea.getBaseImponible(),
                linea.getCuotaImpuesto(),
                linea.getTotal(),
                linea.getOrden()
        );
    }
}
