package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaRectificativaR5EmisionServiceTest {

    @Mock private FacturaRepository facturaRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private SerieFacturacionRepository serieRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private RegistroFacturacionService registroFacturacionService;
    @Mock private Empresa empresa;
    @Mock private SerieFacturacion serie;

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
    void emitirR5SinClienteNumeraYCreaRegistroLocal() {

        Factura factura = new Factura();
        factura.setEmpresa(empresa);
        factura.setSerie(serie);
        factura.setCliente(null);
        factura.setTipoFactura(TipoFactura.R5);
        factura.setTipoRectificativa(TipoRectificativa.I);
        factura.setMotivoRectificacion("Correccion de factura simplificada");
        factura.setEstado(EstadoFactura.BORRADOR);
        factura.setBaseImponible(new BigDecimal("-12.50"));
        factura.setCuotaImpuesto(new BigDecimal("-0.88"));
        factura.setTotal(new BigDecimal("-13.38"));

        FacturaLinea linea = new FacturaLinea();
        linea.setProducto(null);
        linea.setDescripcion("Correccion material de oficina");
        linea.setCantidad(BigDecimal.ONE);
        linea.setPrecioUnitario(new BigDecimal("12.5000"));
        linea.setSigno(-1);
        linea.setTipoImpuesto(TipoImpuesto.IGIC);
        linea.setPorcentajeImpuesto(new BigDecimal("7.00"));
        linea.setBaseImponible(new BigDecimal("-12.50"));
        linea.setCuotaImpuesto(new BigDecimal("-0.88"));
        linea.setTotal(new BigDecimal("-13.38"));
        linea.setOrden(1);
        factura.agregarLinea(linea);

        when(facturaRepository.findConBloqueo(1L, 50L))
                .thenReturn(Optional.of(factura));

        when(empresa.getId()).thenReturn(1L);
        when(empresa.isActiva()).thenReturn(true);

        when(serie.getId()).thenReturn(20L);
        when(serie.isActiva()).thenReturn(true);
        when(serie.getCodigo()).thenReturn("RECT");
        when(serie.getUltimoNumero()).thenReturn(2L);

        when(serieRepository.findConBloqueo(20L, 1L))
                .thenReturn(Optional.of(serie));

        when(facturaRepository.saveAndFlush(any(Factura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FacturaResponse response = service.emitir(1L, 50L);

        assertEquals(EstadoFactura.EMITIDA, factura.getEstado());
        assertEquals(3L, factura.getNumero());
        assertNotNull(factura.getFechaExpedicion());
        assertEquals(LocalDate.now(), factura.getFechaExpedicion());
        assertNotNull(factura.getFechaEmision());
        assertNull(factura.getCliente());

        assertEquals(TipoFactura.R5, response.tipoFactura());
        assertNull(response.clienteId());
        assertEquals(3L, response.numero());

        verify(serie).setUltimoNumero(3L);
        verify(serieRepository).save(serie);
        verify(registroFacturacionService).crearAlta(factura);
    }
}
