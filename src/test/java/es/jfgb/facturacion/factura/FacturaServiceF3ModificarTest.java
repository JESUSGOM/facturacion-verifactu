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
import es.jfgb.facturacion.serie.TipoSerieFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceF3ModificarTest {

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
    void modificarF3EliminaRelacionesAntesDeReconstruirlas() {

        /*
         * =====================================================
         * EMPRESA
         * =====================================================
         */

        Empresa empresa =
                org.mockito.Mockito.mock(
                        Empresa.class
                );

        when(
                empresa.getId()
        ).thenReturn(
                1L
        );

        /*
         * =====================================================
         * CLIENTE IDENTIFICADO DE LA F3
         * =====================================================
         */

        Cliente cliente =
                org.mockito.Mockito.mock(
                        Cliente.class
                );

        when(
                cliente.isActivo()
        ).thenReturn(
                true
        );

        when(
                cliente.getId()
        ).thenReturn(
                8L
        );

        /*
         * =====================================================
         * SERIE ORDINARIA DE LA F3
         * =====================================================
         */

        SerieFacturacion serie =
                org.mockito.Mockito.mock(
                        SerieFacturacion.class
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

        when(
                serie.getId()
        ).thenReturn(
                3L
        );

        when(
                serie.getCodigo()
        ).thenReturn(
                "FAC"
        );

        /*
         * =====================================================
         * PRODUCTO DE LA NUEVA LINEA
         * =====================================================
         */

        Producto producto =
                org.mockito.Mockito.mock(
                        Producto.class
                );

        when(
                producto.isActivo()
        ).thenReturn(
                true
        );

        when(
                producto.getId()
        ).thenReturn(
                30L
        );

        when(
                producto.getNombre()
        ).thenReturn(
                "Servicio de prueba"
        );

        when(
                producto.getPrecio()
        ).thenReturn(
                new BigDecimal("10.00")
        );

        when(
                producto.getTipoImpuesto()
        ).thenReturn(
                TipoImpuesto.IGIC
        );

        when(
                producto.getPorcentajeImpuesto()
        ).thenReturn(
                new BigDecimal("7.00")
        );

        /*
         * =====================================================
         * FACTURA F2 SUSTITUIDA
         * =====================================================
         */

        SerieFacturacion serieSimplificada =
                org.mockito.Mockito.mock(
                        SerieFacturacion.class
                );

        when(
                serieSimplificada.getId()
        ).thenReturn(
                20L
        );

        when(
                serieSimplificada.getCodigo()
        ).thenReturn(
                "SIM"
        );

        Factura original =
                org.mockito.Mockito.mock(
                        Factura.class
                );

        when(
                original.getId()
        ).thenReturn(
                25L
        );

        when(
                original.getTipoFactura()
        ).thenReturn(
                TipoFactura.F2
        );

        when(
                original.getEstado()
        ).thenReturn(
                EstadoFactura.EMITIDA
        );

        when(
                original.getNumero()
        ).thenReturn(
                25L
        );

        when(
                original.getFechaExpedicion()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        when(
                original.getCliente()
        ).thenReturn(
                null
        );

        when(
                original.getSerie()
        ).thenReturn(
                serieSimplificada
        );

        when(
                original.getBaseImponible()
        ).thenReturn(
                new BigDecimal("10.00")
        );

        when(
                original.getCuotaImpuesto()
        ).thenReturn(
                new BigDecimal("0.70")
        );

        when(
                original.getTotal()
        ).thenReturn(
                new BigDecimal("10.70")
        );

        /*
         * =====================================================
         * F3 EXISTENTE EN BORRADOR
         * =====================================================
         */

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

        factura.setTipoFactura(
                TipoFactura.F3
        );

        factura.setEstado(
                EstadoFactura.BORRADOR
        );

        FacturaSustituida relacionAnterior =
                new FacturaSustituida();

        relacionAnterior.setFacturaSustituida(
                original
        );

        relacionAnterior.setOrden(
                1
        );

        factura.agregarFacturaSustituida(
                relacionAnterior
        );

        /*
         * =====================================================
         * MOCKS
         * =====================================================
         */

        when(
                facturaRepository.findConBloqueo(
                        1L,
                        50L
                )
        ).thenReturn(
                Optional.of(
                        factura
                )
        );

        when(
                clienteRepository.findByIdAndEmpresa_Id(
                        8L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        cliente
                )
        );

        when(
                serieRepository.findByIdAndEmpresa_Id(
                        3L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        serie
                )
        );

        when(
                productoRepository.findByIdAndEmpresa_Id(
                        30L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        producto
                )
        );

        /*
         * Es la MISMA F2 que ya estaba relacionada antes
         * de modificar el borrador.
         *
         * Es precisamente el escenario que puede colisionar
         * con UNIQUE si Hibernate intenta insertar antes de
         * ejecutar el DELETE del orphanRemoval.
         */
        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        25L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        original
                )
        );

        when(
                facturaRepository.saveAndFlush(
                        any(
                                Factura.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
        );

        /*
         * =====================================================
         * REQUEST DE MODIFICACION
         * =====================================================
         */

        FacturaSustitutivaRequest request =
                new FacturaSustitutivaRequest(
                        8L,
                        3L,
                        List.of(
                                25L
                        ),
                        "F3 modificada",
                        List.of(
                                new FacturaLineaRequest(
                                        30L,
                                        new BigDecimal("2.0000")
                                )
                        )
                );

        /*
         * =====================================================
         * EJECUCION
         * =====================================================
         */

        FacturaResponse response =
                service.modificarBorradorSustitutivo(
                        1L,
                        50L,
                        request
                );

        /*
         * =====================================================
         * RESULTADO FUNCIONAL
         * =====================================================
         */

        assertEquals(
                TipoFactura.F3,
                factura.getTipoFactura()
        );

        assertEquals(
                EstadoFactura.BORRADOR,
                factura.getEstado()
        );

        assertEquals(
                "F3 modificada",
                factura.getObservaciones()
        );

        assertEquals(
                1,
                factura.getFacturasSustituidas()
                        .size()
        );

        FacturaSustituida relacionNueva =
                factura.getFacturasSustituidas()
                        .getFirst();

        /*
         * Se ha eliminado la entidad de relacion anterior
         * y se ha construido una nueva.
         */
        assertNotSame(
                relacionAnterior,
                relacionNueva
        );

        assertSame(
                original,
                relacionNueva.getFacturaSustituida()
        );

        assertEquals(
                1,
                relacionNueva.getOrden()
        );

        assertEquals(
                1,
                factura.getLineas()
                        .size()
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

        /*
         * =====================================================
         * RESPUESTA API
         * =====================================================
         */

        assertNotNull(
                response.facturasSustituidas()
        );

        assertEquals(
                1,
                response.facturasSustituidas()
                        .size()
        );

        assertEquals(
                25L,
                response.facturasSustituidas()
                        .getFirst()
                        .facturaId()
        );

        /*
         * =====================================================
         * ORDEN CRITICO DE PERSISTENCIA
         * =====================================================
         *
         * Primero flush:
         *      DELETE de relaciones antiguas por orphanRemoval
         *
         * Despues:
         *      cargar F2 y reconstruir la relacion
         *
         * Finalmente:
         *      guardar la F3 reconstruida
         */

        InOrder ordenPersistencia =
                inOrder(
                        facturaRepository
                );

        ordenPersistencia.verify(
                facturaRepository
        ).flush();

        ordenPersistencia.verify(
                facturaRepository
        ).findByIdAndEmpresa_Id(
                25L,
                1L
        );

        ordenPersistencia.verify(
                facturaRepository
        ).saveAndFlush(
                factura
        );

        verify(
                facturaRepository
        ).flush();

        verifyNoInteractions(
                registroFacturacionService
        );

        System.out.println(
                "F3_MODIFICACION_FLUSH_ANTES_RECONSTRUIR_OK=true"
        );

        System.out.println(
                "F3_MODIFICACION_MISMA_F2_OK=true"
        );

        System.out.println(
                "F3_MODIFICACION_RED=false"
        );
    }
}
