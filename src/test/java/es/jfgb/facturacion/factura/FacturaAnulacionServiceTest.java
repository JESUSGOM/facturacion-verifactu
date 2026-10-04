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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaAnulacionServiceTest {

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
    void anularGuardaMotivoFechaYCreaRegistroFiscal() {

        Empresa empresa =
                mock(Empresa.class);

        Cliente cliente =
                mock(Cliente.class);

        SerieFacturacion serie =
                mock(SerieFacturacion.class);

        when(empresa.getId())
                .thenReturn(1L);

        when(cliente.getId())
                .thenReturn(10L);

        when(serie.getId())
                .thenReturn(20L);

        when(serie.getCodigo())
                .thenReturn("FAC");

        Factura factura =
                new Factura();

        factura.setEmpresa(empresa);
        factura.setCliente(cliente);
        factura.setSerie(serie);

        factura.setEstado(
                EstadoFactura.EMITIDA
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

        factura.setBaseImponible(
                new BigDecimal("60.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("4.20")
        );

        factura.setTotal(
                new BigDecimal("64.20")
        );

        factura.setLineas(
                List.of()
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        15L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        doAnswer(invocation -> {

            /*
             * Al generar ANULACION la factura todavia
             * debe conservar estado EMITIDA.
             */
            assertEquals(
                    EstadoFactura.EMITIDA,
                    factura.getEstado()
            );

            return null;

        }).when(
                registroFacturacionService
        ).crearAnulacion(
                factura
        );

        when(
                facturaRepository.saveAndFlush(
                        factura
                )
        ).thenReturn(
                factura
        );

        AnulacionFacturaRequest request =
                new AnulacionFacturaRequest(
                        "Factura emitida por error; "
                                + "la operacion no debio facturarse",
                        true
                );

        FacturaResponse respuesta =
                service.anular(
                        1L,
                        15L,
                        request
                );

        assertEquals(
                EstadoFactura.ANULADA,
                respuesta.estado()
        );

        assertEquals(
                EstadoFactura.ANULADA,
                factura.getEstado()
        );

        assertNotNull(
                factura.getFechaAnulacion()
        );

        assertEquals(
                request.motivo(),
                factura.getMotivoAnulacion()
        );

        verify(
                registroFacturacionService
        ).crearAnulacion(
                factura
        );

        verify(
                facturaRepository
        ).saveAndFlush(
                factura
        );
    }

    @Test
    void anularExigeConfirmarQueNoProcedeRectificativa() {

        AnulacionFacturaRequest request =
                new AnulacionFacturaRequest(
                        "Factura emitida por error",
                        false
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.anular(
                                1L,
                                15L,
                                request
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                ex.getStatusCode()
        );

        verifyNoInteractions(
                facturaRepository
        );

        verifyNoInteractions(
                registroFacturacionService
        );
    }

    @Test
    void anularRechazaFacturaQueNoEstaEmitida() {

        Factura factura =
                new Factura();

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        15L
                )
        ).thenReturn(
                Optional.of(factura)
        );

        AnulacionFacturaRequest request =
                new AnulacionFacturaRequest(
                        "Factura que no debio emitirse",
                        true
                );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.anular(
                                1L,
                                15L,
                                request
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verify(
                registroFacturacionService,
                never()
        ).crearAnulacion(
                any()
        );

        verify(
                facturaRepository,
                never()
        ).saveAndFlush(
                any()
        );
    }
}