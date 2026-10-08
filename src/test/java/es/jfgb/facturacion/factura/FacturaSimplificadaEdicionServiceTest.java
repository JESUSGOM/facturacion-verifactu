package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.producto.Producto;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.serie.TipoSerieFacturacion;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaSimplificadaEdicionServiceTest {

    @Mock private FacturaRepository facturaRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private SerieFacturacionRepository serieRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private RegistroFacturacionService registroFacturacionService;

    @Mock private Empresa empresa;
    @Mock private SerieFacturacion serie;
    @Mock private Producto producto;

    private FacturaService service;

    @BeforeEach
    void setUp() {
        service = new FacturaService(
                facturaRepository,
                empresaRepository,
                clienteRepository,
                serieRepository,
                productoRepository,
                registroFacturacionService
        );
    }

    @Test
    void modificarBorradorSimplificadoActualizaLineasYTotales() {

        Factura factura = new Factura();
        factura.setEmpresa(empresa);
        factura.setCliente(null);
        factura.setSerie(serie);
        factura.setTipoFactura(TipoFactura.F2);
        factura.setEstado(EstadoFactura.BORRADOR);

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        when(
                serieRepository.findByIdAndEmpresa_Id(
                        20L,
                        1L
                )
        ).thenReturn(
                Optional.of(serie)
        );

        when(serie.isActiva()).thenReturn(true);
        when(serie.getTipoSerie())
                .thenReturn(TipoSerieFacturacion.SIMPLIFICADA);
        when(serie.getId()).thenReturn(20L);
        when(serie.getCodigo()).thenReturn("SIM");

        when(
                productoRepository.findByIdAndEmpresa_Id(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(producto)
        );

        when(producto.isActivo()).thenReturn(true);
        when(producto.getId()).thenReturn(30L);
        when(producto.getNombre()).thenReturn("Venta mostrador");
        when(producto.getPrecio())
                .thenReturn(new BigDecimal("10.00"));
        when(producto.getTipoImpuesto())
                .thenReturn(TipoImpuesto.IGIC);
        when(producto.getPorcentajeImpuesto())
                .thenReturn(new BigDecimal("7.00"));

        when(empresa.getId()).thenReturn(1L);

        when(
                facturaRepository.saveAndFlush(
                        any(Factura.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        FacturaSimplificadaRequest request =
                new FacturaSimplificadaRequest(
                        20L,
                        "Borrador F2 modificado",
                        List.of(
                                new FacturaLineaRequest(
                                        30L,
                                        new BigDecimal("2.0000")
                                )
                        )
                );

        FacturaResponse response =
                service.modificarBorradorSimplificado(
                        1L,
                        100L,
                        request
                );

        assertEquals(
                TipoFactura.F2,
                factura.getTipoFactura()
        );

        assertEquals(
                EstadoFactura.BORRADOR,
                factura.getEstado()
        );

        assertNull(
                factura.getCliente()
        );

        assertEquals(
                "Borrador F2 modificado",
                factura.getObservaciones()
        );

        assertEquals(
                1,
                factura.getLineas().size()
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
                TipoFactura.F2,
                response.tipoFactura()
        );

        assertNull(
                response.clienteId()
        );

        assertEquals(
                "SIM",
                response.serieCodigo()
        );

        assertEquals(
                new BigDecimal("21.40"),
                response.total()
        );

        verify(
                facturaRepository
        ).saveAndFlush(
                factura
        );

        verify(
                registroFacturacionService,
                never()
        ).crearAlta(
                any()
        );
    }

    @Test
    void modificarSimplificadaEmitidaEsRechazada() {

        Factura factura = new Factura();
        factura.setEmpresa(empresa);
        factura.setSerie(serie);
        factura.setTipoFactura(TipoFactura.F2);
        factura.setEstado(EstadoFactura.EMITIDA);

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        FacturaSimplificadaRequest request =
                new FacturaSimplificadaRequest(
                        20L,
                        null,
                        List.of(
                                new FacturaLineaRequest(
                                        30L,
                                        BigDecimal.ONE
                                )
                        )
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.modificarBorradorSimplificado(
                                        1L,
                                        100L,
                                        request
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

        verify(
                registroFacturacionService,
                never()
        ).crearAlta(
                any()
        );
    }
}
