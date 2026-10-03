package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.producto.Producto;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SerieFacturacionRepository serieRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RegistroFacturacionService registroFacturacionService;

    private FacturaService service;

    @BeforeEach
    void setUp() {

        service =
                new FacturaService(
                        facturaRepository,
                        empresaRepository,
                        clienteRepository,
                        serieRepository,
                        productoRepository,
                        registroFacturacionService
                );
    }

    @Test
    void crearBorradorRechazaClienteInactivo() {

        Empresa empresa =
                new Empresa();

        Cliente cliente =
                new Cliente();

        cliente.setActivo(false);

        when(
                empresaRepository.findById(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        when(
                clienteRepository.findByIdAndEmpresa_Id(
                        10L,
                        1L
                )
        ).thenReturn(
                Optional.of(cliente)
        );

        FacturaRequest request =
                request(
                        10L,
                        20L,
                        30L,
                        "1.0000"
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.crearBorrador(
                                1L,
                                request
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(
                serieRepository,
                never()
        ).findByIdAndEmpresa_Id(
                any(),
                any()
        );

        verify(
                facturaRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void crearBorradorRechazaProductoInactivo() {

        Empresa empresa =
                new Empresa();

        Cliente cliente =
                clienteActivo(
                        "Cliente activo"
                );

        SerieFacturacion serie =
                serieActiva(
                        "A"
                );

        Producto producto =
                producto(
                        "Producto inactivo",
                        "10.00",
                        "7.00",
                        false
                );

        when(
                empresaRepository.findById(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        when(
                clienteRepository.findByIdAndEmpresa_Id(
                        10L,
                        1L
                )
        ).thenReturn(
                Optional.of(cliente)
        );

        when(
                serieRepository.findByIdAndEmpresa_Id(
                        20L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        when(
                productoRepository.findByIdAndEmpresa_Id(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(producto)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.crearBorrador(
                                1L,
                                request(
                                        10L,
                                        20L,
                                        30L,
                                        "1.0000"
                                )
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(
                facturaRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void modificarBorradorReemplazaLineasYRecalculaImportes() {

        Factura factura =
                borradorBase();

        /*
         * Línea antigua que deberá desaparecer.
         */
        FacturaLinea antigua =
                new FacturaLinea();

        antigua.setDescripcion(
                "Línea antigua"
        );

        antigua.setCantidad(
                new BigDecimal("1.0000")
        );

        antigua.setPrecioUnitario(
                new BigDecimal("99.0000")
        );

        antigua.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        antigua.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        antigua.setBaseImponible(
                new BigDecimal("99.00")
        );

        antigua.setCuotaImpuesto(
                new BigDecimal("6.93")
        );

        antigua.setTotal(
                new BigDecimal("105.93")
        );

        antigua.setOrden(1);

        factura.agregarLinea(
                antigua
        );

        Cliente clienteNuevo =
                clienteActivo(
                        "Cliente nuevo"
                );

        clienteNuevo.setNif(
                "11111111H"
        );

        clienteNuevo.setCodigoPais(
                "ES"
        );

        SerieFacturacion serieNueva =
                serieActiva(
                        "NUEVA"
                );

        Producto productoNuevo =
                producto(
                        "Servicio nuevo",
                        "10.00",
                        "7.00",
                        true
                );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        when(
                clienteRepository.findByIdAndEmpresa_Id(
                        10L,
                        1L
                )
        ).thenReturn(
                Optional.of(clienteNuevo)
        );

        when(
                serieRepository.findByIdAndEmpresa_Id(
                        20L,
                        1L
                )
        ).thenReturn(
                Optional.of(serieNueva)
        );

        when(
                productoRepository.findByIdAndEmpresa_Id(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(productoNuevo)
        );

        when(
                facturaRepository.saveAndFlush(
                        factura
                )
        ).thenReturn(
                factura
        );

        FacturaRequest request =
                new FacturaRequest(
                        10L,
                        20L,
                        "  Observación modificada  ",
                        List.of(
                                new FacturaLineaRequest(
                                        30L,
                                        new BigDecimal("2.0000")
                                )
                        )
                );

        FacturaResponse response =
                service.modificarBorrador(
                        1L,
                        100L,
                        request
                );

        assertSame(
                clienteNuevo,
                factura.getCliente()
        );

        assertSame(
                serieNueva,
                factura.getSerie()
        );

        assertEquals(
                1,
                factura.getLineas().size()
        );

        assertEquals(
                "Servicio nuevo",
                factura.getLineas()
                        .getFirst()
                        .getDescripcion()
        );

        assertEquals(
                new BigDecimal("20.00"),
                factura.getBaseImponible()
        );

        assertEquals(
                new BigDecimal("1.40"),
                factura.getCuotaImpuesto()
        );

        assertEquals(
                new BigDecimal("21.40"),
                factura.getTotal()
        );

        assertEquals(
                "Observación modificada",
                factura.getObservaciones()
        );

        assertEquals(
                "11111111H",
                factura.getClienteNif()
        );

        assertEquals(
                EstadoFactura.BORRADOR,
                response.estado()
        );

        assertEquals(
                1,
                response.lineas().size()
        );
    }

    @Test
    void modificarFacturaEmitidaEstaProhibido() {

        Factura factura =
                borradorBase();

        factura.setEstado(
                EstadoFactura.EMITIDA
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.modificarBorrador(
                                1L,
                                100L,
                                request(
                                        10L,
                                        20L,
                                        30L,
                                        "1.0000"
                                )
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(
                facturaRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }

    @Test
    void eliminarBorradorLoElimina() {

        Factura factura =
                borradorBase();

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        service.eliminarBorrador(
                1L,
                100L
        );

        verify(
                facturaRepository
        ).delete(
                factura
        );

        verify(
                facturaRepository
        ).flush();
    }

    @Test
    void eliminarFacturaEmitidaEstaProhibido() {

        Factura factura =
                borradorBase();

        factura.setEstado(
                EstadoFactura.EMITIDA
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.eliminarBorrador(
                                1L,
                                100L
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(
                facturaRepository,
                never()
        ).delete(
                any()
        );
    }

    @Test
    void emitirBloqueaFacturaNumeraYCreaRegistroVerifactuLocal() {

        Empresa empresa =
                org.mockito.Mockito.mock(
                        Empresa.class
                );

        Cliente cliente =
                org.mockito.Mockito.mock(
                        Cliente.class
                );

        SerieFacturacion serie =
                org.mockito.Mockito.mock(
                        SerieFacturacion.class
                );

        Producto producto =
                org.mockito.Mockito.mock(
                        Producto.class
                );

        when(
                empresa.getId()
        ).thenReturn(
                1L
        );

        when(
                cliente.getId()
        ).thenReturn(
                10L
        );

        when(
                cliente.isActivo()
        ).thenReturn(
                true
        );

        when(
                serie.getId()
        ).thenReturn(
                20L
        );

        when(
                serie.getCodigo()
        ).thenReturn(
                "F"
        );

        when(
                serie.isActiva()
        ).thenReturn(
                true
        );

        when(
                serie.getUltimoNumero()
        ).thenReturn(
                7L
        );

        when(
                producto.getId()
        ).thenReturn(
                30L
        );

        when(
                producto.isActivo()
        ).thenReturn(
                true
        );

        Factura factura =
                new Factura();

        factura.setEmpresa(
                empresa
        );

        factura.setCliente(
                cliente
        );

        factura.setSerie(
                serie
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        factura.setClienteNombre(
                "Cliente"
        );

        factura.setClienteNif(
                "11111111H"
        );

        factura.setClienteCodigoPais(
                "ES"
        );

        FacturaLinea linea =
                new FacturaLinea();

        linea.setProducto(
                producto
        );

        linea.setDescripcion(
                "Servicio"
        );

        linea.setCantidad(
                new BigDecimal("1.0000")
        );

        linea.setPrecioUnitario(
                new BigDecimal("10.0000")
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal("10.00")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("0.70")
        );

        linea.setTotal(
                new BigDecimal("10.70")
        );

        linea.setOrden(1);

        factura.agregarLinea(
                linea
        );

        factura.setBaseImponible(
                new BigDecimal("10.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("0.70")
        );

        factura.setTotal(
                new BigDecimal("10.70")
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        when(
                serieRepository.findConBloqueo(
                        20L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        when(
                facturaRepository.saveAndFlush(
                        factura
                )
        ).thenReturn(
                factura
        );

        FacturaResponse response =
                service.emitir(
                        1L,
                        100L
                );

        assertEquals(
                EstadoFactura.EMITIDA,
                factura.getEstado()
        );

        assertEquals(
                8L,
                factura.getNumero()
        );

        assertNotNull(
                factura.getFechaExpedicion()
        );

        assertNotNull(
                factura.getFechaEmision()
        );

        assertEquals(
                EstadoFactura.EMITIDA,
                response.estado()
        );

        assertEquals(
                8L,
                response.numero()
        );

        verify(
                facturaRepository
        ).findConBloqueo(
                1L,
                100L
        );

        verify(
                serie
        ).setUltimoNumero(
                8L
        );

        verify(
                serieRepository
        ).save(
                serie
        );

        verify(
                registroFacturacionService
        ).crearAlta(
                factura
        );
    }

    @Test
    void emitirRechazaProductoDesactivadoAntesDeNumerar() {

        Cliente cliente =
                org.mockito.Mockito.mock(
                        Cliente.class
                );

        SerieFacturacion serie =
                org.mockito.Mockito.mock(
                        SerieFacturacion.class
                );

        Producto producto =
                org.mockito.Mockito.mock(
                        Producto.class
                );

        when(
                cliente.isActivo()
        ).thenReturn(
                true
        );

        when(
                producto.isActivo()
        ).thenReturn(
                false
        );

        Factura factura =
                new Factura();

        factura.setCliente(
                cliente
        );

        factura.setSerie(
                serie
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        FacturaLinea linea =
                new FacturaLinea();

        linea.setProducto(
                producto
        );

        linea.setDescripcion(
                "Producto desactivado"
        );

        linea.setCantidad(
                new BigDecimal("1.0000")
        );

        linea.setPrecioUnitario(
                new BigDecimal("10.0000")
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal("10.00")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("0.70")
        );

        linea.setTotal(
                new BigDecimal("10.70")
        );

        linea.setOrden(1);

        factura.agregarLinea(
                linea
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.emitir(
                                1L,
                                100L
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verify(
                serieRepository,
                never()
        ).findConBloqueo(
                any(),
                any()
        );

        verify(
                registroFacturacionService,
                never()
        ).crearAlta(
                any()
        );
    }

    private Factura borradorBase() {

        Factura factura =
                new Factura();

        Empresa empresa =
                new Empresa();

        Cliente cliente =
                clienteActivo(
                        "Cliente original"
                );

        cliente.setNif(
                "22222222J"
        );

        cliente.setCodigoPais(
                "ES"
        );

        SerieFacturacion serie =
                serieActiva(
                        "ORIGINAL"
                );

        factura.setEmpresa(
                empresa
        );

        factura.setCliente(
                cliente
        );

        factura.setSerie(
                serie
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        factura.setBaseImponible(
                BigDecimal.ZERO.setScale(2)
        );

        factura.setCuotaImpuesto(
                BigDecimal.ZERO.setScale(2)
        );

        factura.setTotal(
                BigDecimal.ZERO.setScale(2)
        );

        return factura;
    }

    private Cliente clienteActivo(
            String nombre) {

        Cliente cliente =
                new Cliente();

        cliente.setNombre(
                nombre
        );

        cliente.setActivo(
                true
        );

        cliente.setCodigoPais(
                "ES"
        );

        return cliente;
    }

    private SerieFacturacion serieActiva(
            String codigo) {

        SerieFacturacion serie =
                new SerieFacturacion();

        serie.setCodigo(
                codigo
        );

        serie.setDescripcion(
                "Serie " + codigo
        );

        serie.setUltimoNumero(
                0L
        );

        serie.setActiva(
                true
        );

        return serie;
    }

    private Producto producto(
            String nombre,
            String precio,
            String porcentaje,
            boolean activo) {

        Producto producto =
                new Producto();

        producto.setNombre(
                nombre
        );

        producto.setPrecio(
                new BigDecimal(precio)
        );

        producto.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        producto.setPorcentajeImpuesto(
                new BigDecimal(porcentaje)
        );

        producto.setActivo(
                activo
        );

        return producto;
    }

    private FacturaRequest request(
            Long clienteId,
            Long serieId,
            Long productoId,
            String cantidad) {

        return new FacturaRequest(
                clienteId,
                serieId,
                null,
                List.of(
                        new FacturaLineaRequest(
                                productoId,
                                new BigDecimal(cantidad)
                        )
                )
        );
    }
}