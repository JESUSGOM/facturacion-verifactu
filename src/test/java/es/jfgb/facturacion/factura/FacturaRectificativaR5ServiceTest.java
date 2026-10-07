package es.jfgb.facturacion.factura;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaRectificativaR5ServiceTest {

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
    private SerieFacturacion serieRectificativa;

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
    void crearR5SobreF2SinDestinatarioCreaBorrador() {

        prepararEmpresaYSerieRectificativa();
        prepararDatosParaConvertir();

        Factura original =
                facturaOriginal(
                        TipoFactura.F2,
                        "12.50",
                        "0.88",
                        "13.38"
                );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        36L,
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
                invocation ->
                        invocation.getArgument(0)
        );

        FacturaResponse response =
                service.crearBorradorRectificativo(
                        1L,
                        request(
                                TipoFactura.R5,
                                36L
                        )
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
                TipoFactura.R5,
                guardada.getTipoFactura()
        );

        assertNull(
                guardada.getCliente()
        );

        assertNull(
                guardada.getClienteNif()
        );

        assertNull(
                guardada.getClienteNombre()
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

        assertEquals(
                TipoFactura.R5,
                response.tipoFactura()
        );

        assertNull(
                response.clienteId()
        );

        verify(
                registroFacturacionService,
                never()
        ).crearAlta(
                any()
        );
    }

    @Test
    void crearR5RechazaOriginalF1() {

        prepararEmpresaYSerieRectificativa();

        Factura original =
                facturaOriginal(
                        TipoFactura.F1,
                        "100.00",
                        "7.00",
                        "107.00"
                );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        100L,
                        1L
                )
        ).thenReturn(
                Optional.of(original)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.crearBorradorRectificativo(
                                        1L,
                                        request(
                                                TipoFactura.R5,
                                                100L
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
    void crearR1RechazaOriginalF2() {

        prepararEmpresaYSerieRectificativa();

        Factura original =
                facturaOriginal(
                        TipoFactura.F2,
                        "12.50",
                        "0.88",
                        "13.38"
                );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        36L,
                        1L
                )
        ).thenReturn(
                Optional.of(original)
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.crearBorradorRectificativo(
                                        1L,
                                        request(
                                                TipoFactura.R1,
                                                36L
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
                Optional.of(serieRectificativa)
        );

        when(
                serieRectificativa.isActiva()
        ).thenReturn(
                true
        );

        when(
                serieRectificativa.getTipoSerie()
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
                serieRectificativa.getId()
        ).thenReturn(
                20L
        );

        when(
                serieRectificativa.getCodigo()
        ).thenReturn(
                "RECT"
        );
    }

    private Factura facturaOriginal(
            TipoFactura tipoFactura,
            String base,
            String cuota,
            String total) {

        Factura original =
                new Factura();

        original.setEstado(
                EstadoFactura.EMITIDA
        );

        original.setTipoFactura(
                tipoFactura
        );

        original.setCliente(
                null
        );

        original.setClienteNif(
                null
        );

        original.setClienteNombre(
                null
        );

        original.setClienteDireccion(
                null
        );

        original.setClienteCodigoPostal(
                null
        );

        original.setClienteMunicipio(
                null
        );

        original.setClienteProvincia(
                null
        );

        original.setClienteCodigoPais(
                null
        );

        original.setBaseImponible(
                new BigDecimal(base)
        );

        original.setCuotaImpuesto(
                new BigDecimal(cuota)
        );

        original.setTotal(
                new BigDecimal(total)
        );

        return original;
    }

    private FacturaRectificativaRequest request(
            TipoFactura tipoFactura,
            Long originalId) {

        return new FacturaRectificativaRequest(
                20L,
                tipoFactura,
                TipoRectificativa.I,
                "Correccion de factura simplificada",
                List.of(
                        originalId
                ),
                null,
                List.of(
                        new FacturaRectificativaLineaRequest(
                                null,
                                "Correccion",
                                new BigDecimal("1.0000"),
                                new BigDecimal("12.5000"),
                                TipoImpuesto.IGIC,
                                new BigDecimal("7.00"),
                                -1
                        )
                )
        );
    }
}
