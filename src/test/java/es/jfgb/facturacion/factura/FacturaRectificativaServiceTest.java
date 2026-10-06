package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.serie.TipoSerieFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaRectificativaServiceTest {

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

    @Mock
    private Empresa empresa;

    @Mock
    private Cliente cliente;

    @Mock
    private SerieFacturacion serie;

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
    void crearRectificativaPorDiferenciasCalculaImportesNegativos() {

        prepararEmpresaYSerieRectificativa();
        prepararDatosParaConvertir();

        Factura original =
                facturaOriginal(
                        "100.00",
                        "7.00"
                );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        100L,
                        1L
                )
        ).thenReturn(
                Optional.of(original)
        );

        when(
                facturaRepository.saveAndFlush(
                        any(Factura.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        FacturaRectificativaRequest request =
                new FacturaRectificativaRequest(
                        20L,
                        TipoFactura.R1,
                        TipoRectificativa.I,
                        "Correccion de importe",
                        List.of(100L),
                        null,
                        List.of(
                                new FacturaRectificativaLineaRequest(
                                        null,
                                        "Correccion servicio",
                                        new BigDecimal("1.0000"),
                                        new BigDecimal("100.0000"),
                                        TipoImpuesto.IGIC,
                                        new BigDecimal("7.00"),
                                        -1
                                )
                        )
                );

        FacturaResponse response =
                service.crearBorradorRectificativo(
                        1L,
                        request
                );

        ArgumentCaptor<Factura> captor =
                ArgumentCaptor.forClass(
                        Factura.class
                );

        verify(
                facturaRepository
        ).saveAndFlush(
                captor.capture()
        );

        Factura guardada =
                captor.getValue();

        assertEquals(
                EstadoFactura.BORRADOR,
                guardada.getEstado()
        );

        assertEquals(
                TipoFactura.R1,
                guardada.getTipoFactura()
        );

        assertEquals(
                TipoRectificativa.I,
                guardada.getTipoRectificativa()
        );

        assertNull(
                guardada.getBaseRectificada()
        );

        assertNull(
                guardada.getCuotaRectificada()
        );

        assertEquals(
                new BigDecimal("-100.00"),
                guardada.getBaseImponible()
        );

        assertEquals(
                new BigDecimal("-7.00"),
                guardada.getCuotaImpuesto()
        );

        assertEquals(
                new BigDecimal("-107.00"),
                guardada.getTotal()
        );

        assertEquals(
                1,
                guardada.getLineas().size()
        );

        assertEquals(
                -1,
                guardada
                        .getLineas()
                        .getFirst()
                        .getSigno()
        );

        assertEquals(
                1,
                guardada
                        .getFacturasRectificadas()
                        .size()
        );

        assertSame(
                original,
                guardada
                        .getFacturasRectificadas()
                        .getFirst()
                        .getFacturaRectificada()
        );

        assertNotNull(
                response
        );

        assertEquals(
                TipoFactura.R1,
                response.tipoFactura()
        );

        assertEquals(
                TipoRectificativa.I,
                response.tipoRectificativa()
        );

        assertEquals(
                "Correccion de importe",
                response.motivoRectificacion()
        );

        assertNull(
                response.baseRectificada()
        );

        assertNull(
                response.cuotaRectificada()
        );

        assertEquals(
                1,
                response.facturasRectificadas().size()
        );

        assertEquals(
                new BigDecimal("-107.00"),
                response.total()
        );

        verify(
                registroFacturacionService,
                never()
        ).crearAlta(
                any()
        );
    }

    @Test
    void crearRectificativaPorSustitucionConservaImportesOriginales() {

        prepararEmpresaYSerieRectificativa();
        prepararDatosParaConvertir();

        Factura original =
                facturaOriginal(
                        "100.00",
                        "7.00"
                );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        100L,
                        1L
                )
        ).thenReturn(
                Optional.of(original)
        );

        when(
                facturaRepository.saveAndFlush(
                        any(Factura.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        FacturaRectificativaRequest request =
                new FacturaRectificativaRequest(
                        20L,
                        TipoFactura.R1,
                        TipoRectificativa.S,
                        "Sustitucion por importe correcto",
                        List.of(100L),
                        null,
                        List.of(
                                new FacturaRectificativaLineaRequest(
                                        null,
                                        "Servicio corregido",
                                        new BigDecimal("1.0000"),
                                        new BigDecimal("80.0000"),
                                        TipoImpuesto.IGIC,
                                        new BigDecimal("7.00"),
                                        1
                                )
                        )
                );

        service.crearBorradorRectificativo(
                1L,
                request
        );

        ArgumentCaptor<Factura> captor =
                ArgumentCaptor.forClass(
                        Factura.class
                );

        verify(
                facturaRepository
        ).saveAndFlush(
                captor.capture()
        );

        Factura guardada =
                captor.getValue();

        assertEquals(
                new BigDecimal("100.00"),
                guardada.getBaseRectificada()
        );

        assertEquals(
                new BigDecimal("7.00"),
                guardada.getCuotaRectificada()
        );

        assertEquals(
                new BigDecimal("80.00"),
                guardada.getBaseImponible()
        );

        assertEquals(
                new BigDecimal("5.60"),
                guardada.getCuotaImpuesto()
        );

        assertEquals(
                new BigDecimal("85.60"),
                guardada.getTotal()
        );
    }

    @Test
    void crearRectificativaRechazaSerieOrdinaria() {

        when(
                empresaRepository.findById(
                        1L
                )
        ).thenReturn(
                Optional.of(empresa)
        );

        when(
                empresa.isActiva()
        ).thenReturn(
                true
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
                serie.isActiva()
        ).thenReturn(
                true
        );

        when(
                serie.getTipoSerie()
        ).thenReturn(
                TipoSerieFacturacion.ORDINARIA
        );

        FacturaRectificativaRequest request =
                requestMinimo(
                        TipoRectificativa.I,
                        1
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.crearBorradorRectificativo(
                                1L,
                                request
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
    void sustitucionRechazaLineaNegativa() {

        prepararEmpresaYSerieRectificativa();

        Factura original =
                facturaOriginal(
                        "100.00",
                        "7.00"
                );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        100L,
                        1L
                )
        ).thenReturn(
                Optional.of(original)
        );

        FacturaRectificativaRequest request =
                requestMinimo(
                        TipoRectificativa.S,
                        -1
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.crearBorradorRectificativo(
                                1L,
                                request
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
    void modificarRectificativaEnBorradorReconstruyeDatos() {

        prepararEmpresaYSerieRectificativa();
        prepararDatosParaConvertir();

        Factura original =
                facturaOriginal(
                        "100.00",
                        "7.00"
                );

        Factura borrador =
                new Factura();

        borrador.setEmpresa(
                empresa
        );

        borrador.setCliente(
                cliente
        );

        borrador.setSerie(
                serie
        );

        borrador.setEstado(
                EstadoFactura.BORRADOR
        );

        borrador.setTipoFactura(
                TipoFactura.R1
        );

        borrador.setTipoRectificativa(
                TipoRectificativa.I
        );

        FacturaLinea lineaAnterior =
                new FacturaLinea();

        lineaAnterior.setDescripcion(
                "Linea anterior"
        );

        borrador.agregarLinea(
                lineaAnterior
        );

        FacturaRectificada relacionAnterior =
                new FacturaRectificada();

        relacionAnterior.setFacturaRectificada(
                original
        );

        relacionAnterior.setOrden(
                1
        );

        borrador.agregarFacturaRectificada(
                relacionAnterior
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        200L
                )
        ).thenReturn(
                Optional.of(borrador)
        );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        100L,
                        1L
                )
        ).thenReturn(
                Optional.of(original)
        );

        when(
                facturaRepository.saveAndFlush(
                        borrador
                )
        ).thenReturn(
                borrador
        );

        FacturaRectificativaRequest request =
                requestMinimo(
                        TipoRectificativa.S,
                        1
                );

        FacturaResponse response =
                service.modificarBorradorRectificativo(
                        1L,
                        200L,
                        request
                );

        assertEquals(
                TipoRectificativa.S,
                borrador.getTipoRectificativa()
        );

        assertEquals(
                "Correccion",
                borrador.getMotivoRectificacion()
        );

        assertEquals(
                new BigDecimal("100.00"),
                borrador.getBaseRectificada()
        );

        assertEquals(
                new BigDecimal("7.00"),
                borrador.getCuotaRectificada()
        );

        assertEquals(
                new BigDecimal("107.00"),
                borrador.getTotal()
        );

        assertEquals(
                1,
                borrador.getLineas().size()
        );

        assertEquals(
                "Linea corregida",
                borrador
                        .getLineas()
                        .getFirst()
                        .getDescripcion()
        );

        assertEquals(
                1,
                borrador.getFacturasRectificadas().size()
        );

        assertSame(
                original,
                borrador
                        .getFacturasRectificadas()
                        .getFirst()
                        .getFacturaRectificada()
        );

        assertEquals(
                TipoRectificativa.S,
                response.tipoRectificativa()
        );

        assertEquals(
                new BigDecimal("107.00"),
                response.total()
        );

        verify(
                facturaRepository
        ).flush();

        verify(
                facturaRepository
        ).saveAndFlush(
                borrador
        );

        verify(
                registroFacturacionService,
                never()
        ).crearAlta(
                any()
        );
    }
    private void prepararEmpresaYSerieRectificativa() {

        when(
                empresaRepository.findById(
                        1L
                )
        ).thenReturn(
                Optional.of(empresa)
        );

        when(
                empresa.isActiva()
        ).thenReturn(
                true
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
                serie.isActiva()
        ).thenReturn(
                true
        );

        when(
                serie.getTipoSerie()
        ).thenReturn(
                TipoSerieFacturacion.RECTIFICATIVA
        );




    }

    private void prepararDatosParaConvertir() {

        when(
                empresa.getId()
        ).thenReturn(
                1L
        );

        when(
                serie.getId()
        ).thenReturn(
                20L
        );

        when(
                serie.getCodigo()
        ).thenReturn(
                "R"
        );

        when(
                cliente.getId()
        ).thenReturn(
                10L
        );
    }
    private Factura facturaOriginal(
            String base,
            String cuota) {

        Factura original =
                new Factura();

        original.setCliente(
                cliente
        );

        original.setEstado(
                EstadoFactura.EMITIDA
        );

        original.setTipoFactura(
                TipoFactura.F1
        );

        original.setBaseImponible(
                new BigDecimal(base)
        );

        original.setCuotaImpuesto(
                new BigDecimal(cuota)
        );

        original.setClienteNif(
                "B12345678"
        );

        original.setClienteNombre(
                "Cliente historico"
        );

        original.setClienteDireccion(
                "Direccion historica"
        );

        original.setClienteCodigoPostal(
                "38001"
        );

        original.setClienteMunicipio(
                "Santa Cruz de Tenerife"
        );

        original.setClienteProvincia(
                "Santa Cruz de Tenerife"
        );

        original.setClienteCodigoPais(
                "ES"
        );

        return original;
    }

    private FacturaRectificativaRequest requestMinimo(
            TipoRectificativa tipo,
            int signo) {

        return new FacturaRectificativaRequest(
                20L,
                TipoFactura.R1,
                tipo,
                "Correccion",
                List.of(100L),
                null,
                List.of(
                        new FacturaRectificativaLineaRequest(
                                null,
                                "Linea corregida",
                                new BigDecimal("1.0000"),
                                new BigDecimal("100.0000"),
                                TipoImpuesto.IGIC,
                                new BigDecimal("7.00"),
                                signo
                        )
                )
        );
    }
}
