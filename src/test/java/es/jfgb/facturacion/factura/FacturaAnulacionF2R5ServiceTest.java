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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaAnulacionF2R5ServiceTest {

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
    void anularF2EmitidaGeneraRegistroAnulacionYMarcaFacturaAnulada() {

        when(
                empresa.getId()
        ).thenReturn(
                1L
        );

        when(
                serie.getId()
        ).thenReturn(
                17L
        );

        when(
                serie.getCodigo()
        ).thenReturn(
                "SIM"
        );

        Factura factura =
                crearFacturaEmitida(
                        TipoFactura.F2,
                        "13.38"
                );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        100L
                )
        ).thenReturn(
                Optional.of(
                        factura
                )
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
                        "Factura simplificada emitida por error",
                        true
                );

        FacturaResponse response =
                service.anular(
                        1L,
                        100L,
                        request
                );

        verify(
                registroFacturacionService
        ).crearAnulacion(
                factura
        );

        assertEquals(
                EstadoFactura.ANULADA,
                factura.getEstado()
        );

        assertNotNull(
                factura.getFechaAnulacion()
        );

        assertEquals(
                "Factura simplificada emitida por error",
                factura.getMotivoAnulacion()
        );

        assertEquals(
                EstadoFactura.ANULADA,
                response.estado()
        );

        assertEquals(
                TipoFactura.F2,
                response.tipoFactura()
        );

        assertEquals(
                "SIM",
                response.serieCodigo()
        );
    }

    @Test
    void anularR5EmitidaGeneraRegistroAnulacionYMarcaFacturaAnulada() {

        when(
                empresa.getId()
        ).thenReturn(
                1L
        );

        when(
                serie.getId()
        ).thenReturn(
                2L
        );

        when(
                serie.getCodigo()
        ).thenReturn(
                "RECT"
        );

        Factura factura =
                crearFacturaEmitida(
                        TipoFactura.R5,
                        "-13.38"
                );

        factura.setTipoRectificativa(
                TipoRectificativa.I
        );

        factura.setMotivoRectificacion(
                "Correccion factura simplificada"
        );

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        101L
                )
        ).thenReturn(
                Optional.of(
                        factura
                )
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
                        "Rectificativa emitida por error",
                        true
                );

        FacturaResponse response =
                service.anular(
                        1L,
                        101L,
                        request
                );

        verify(
                registroFacturacionService
        ).crearAnulacion(
                factura
        );

        assertEquals(
                EstadoFactura.ANULADA,
                factura.getEstado()
        );

        assertNotNull(
                factura.getFechaAnulacion()
        );

        assertEquals(
                "Rectificativa emitida por error",
                factura.getMotivoAnulacion()
        );

        assertEquals(
                EstadoFactura.ANULADA,
                response.estado()
        );

        assertEquals(
                TipoFactura.R5,
                response.tipoFactura()
        );

        assertEquals(
                TipoRectificativa.I,
                response.tipoRectificativa()
        );

        assertEquals(
                "RECT",
                response.serieCodigo()
        );
    }

    private Factura crearFacturaEmitida(
            TipoFactura tipoFactura,
            String total) {

        Factura factura =
                new Factura();

        factura.setEmpresa(
                empresa
        );

        factura.setCliente(
                null
        );

        factura.setSerie(
                serie
        );

        factura.setEstado(
                EstadoFactura.EMITIDA
        );

        factura.setTipoFactura(
                tipoFactura
        );

        factura.setNumero(
                1L
        );

        factura.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        8
                )
        );

        factura.setBaseImponible(
                new BigDecimal(
                        total.startsWith("-")
                                ? "-12.50"
                                : "12.50"
                )
        );

        factura.setCuotaImpuesto(
                new BigDecimal(
                        total.startsWith("-")
                                ? "-0.88"
                                : "0.88"
                )
        );

        factura.setTotal(
                new BigDecimal(
                        total
                )
        );

        return factura;
    }
}
