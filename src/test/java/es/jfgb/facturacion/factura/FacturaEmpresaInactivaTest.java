package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaEmpresaInactivaTest {

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
    void crearBorradorRechazaEmpresaInactiva() {

        Empresa empresa =
                new Empresa();

        empresa.setActiva(false);

        when(
                empresaRepository.findById(1L)
        ).thenReturn(
                Optional.of(empresa)
        );

        FacturaRequest request =
                new FacturaRequest(
                        10L,
                        20L,
                        null,
                        List.of(
                                new FacturaLineaRequest(
                                        30L,
                                        java.math.BigDecimal.ONE
                                )
                        )
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
                clienteRepository,
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
    void emitirRechazaEmpresaInactivaAntesDeNumerar() {

        Empresa empresa =
                new Empresa();

        empresa.setActiva(false);

        Factura factura =
                new Factura();

        factura.setEmpresa(
                empresa
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
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
}