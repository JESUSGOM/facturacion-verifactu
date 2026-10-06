package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.Cliente;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaRectificablesServiceTest {

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
    void preparar() {

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
    void listarRectificablesSolicitaSoloEmitidasF1() {

        when(
                empresaRepository.existsById(
                        1L
                )
        ).thenReturn(
                true
        );

        Empresa empresa =
                mock(Empresa.class);

        Cliente cliente =
                mock(Cliente.class);

        SerieFacturacion serie =
                mock(SerieFacturacion.class);

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
                serie.getId()
        ).thenReturn(
                20L
        );

        when(
                serie.getCodigo()
        ).thenReturn(
                "FAC"
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
                EstadoFactura.EMITIDA
        );

        factura.setTipoFactura(
                TipoFactura.F1
        );

        factura.setNumero(
                15L
        );

        factura.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        2
                )
        );

        factura.setClienteNombre(
                "Cliente de prueba"
        );

        when(
                facturaRepository
                        .findAllByEmpresa_IdAndEstadoAndTipoFacturaOrderByIdDesc(
                                1L,
                                EstadoFactura.EMITIDA,
                                TipoFactura.F1
                        )
        ).thenReturn(
                List.of(
                        factura
                )
        );

        List<FacturaResponse> resultado =
                service.listarRectificables(
                        1L
                );

        assertEquals(
                1,
                resultado.size()
        );

        assertEquals(
                EstadoFactura.EMITIDA,
                resultado.getFirst().estado()
        );

        assertEquals(
                TipoFactura.F1,
                resultado.getFirst().tipoFactura()
        );

        assertEquals(
                15L,
                resultado.getFirst().numero()
        );

        verify(
                facturaRepository
        ).findAllByEmpresa_IdAndEstadoAndTipoFacturaOrderByIdDesc(
                1L,
                EstadoFactura.EMITIDA,
                TipoFactura.F1
        );
    }
}