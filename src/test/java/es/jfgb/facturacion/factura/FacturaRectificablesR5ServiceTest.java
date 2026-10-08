package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaRectificablesR5ServiceTest {

    @Mock private FacturaRepository facturaRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private SerieFacturacionRepository serieRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private RegistroFacturacionService registroFacturacionService;

    private FacturaService service;

    @BeforeEach
    void preparar() {
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
    void listarRectificablesParaR5SolicitaSoloEmitidasF2() {

        when(empresaRepository.existsById(1L)).thenReturn(true);

        Empresa empresa = mock(Empresa.class);
        SerieFacturacion serie = mock(SerieFacturacion.class);

        when(empresa.getId()).thenReturn(1L);
        when(serie.getId()).thenReturn(17L);
        when(serie.getCodigo()).thenReturn("SIM");

        Factura factura = new Factura();
        factura.setEmpresa(empresa);
        factura.setCliente(null);
        factura.setSerie(serie);
        factura.setEstado(EstadoFactura.EMITIDA);
        factura.setTipoFactura(TipoFactura.F2);
        factura.setNumero(1L);
        factura.setFechaExpedicion(LocalDate.of(2026, 10, 7));
        factura.setBaseImponible(new BigDecimal("12.50"));
        factura.setCuotaImpuesto(new BigDecimal("0.88"));
        factura.setTotal(new BigDecimal("13.38"));

        when(
                facturaRepository
                        .findAllByEmpresa_IdAndEstadoAndTipoFacturaOrderByIdDesc(
                                1L,
                                EstadoFactura.EMITIDA,
                                TipoFactura.F2
                        )
        ).thenReturn(List.of(factura));

        List<FacturaResponse> resultado =
                service.listarRectificables(
                        1L,
                        TipoFactura.R5
                );

        assertEquals(1, resultado.size());
        assertEquals(EstadoFactura.EMITIDA, resultado.getFirst().estado());
        assertEquals(TipoFactura.F2, resultado.getFirst().tipoFactura());
        assertNull(resultado.getFirst().clienteId());
        assertEquals("SIM", resultado.getFirst().serieCodigo());
        assertEquals(1L, resultado.getFirst().numero());

        verify(
                facturaRepository
        ).findAllByEmpresa_IdAndEstadoAndTipoFacturaOrderByIdDesc(
                1L,
                EstadoFactura.EMITIDA,
                TipoFactura.F2
        );
    }
}
