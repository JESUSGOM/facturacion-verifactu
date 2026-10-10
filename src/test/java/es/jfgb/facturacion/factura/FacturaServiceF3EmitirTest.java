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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacturaServiceF3EmitirTest {

    @Test
    void emiteF3YGeneraRegistroVerifactu() {

        FacturaRepository facturaRepository =
                mock(
                        FacturaRepository.class
                );

        EmpresaRepository empresaRepository =
                mock(
                        EmpresaRepository.class
                );

        ClienteRepository clienteRepository =
                mock(
                        ClienteRepository.class
                );

        SerieFacturacionRepository serieRepository =
                mock(
                        SerieFacturacionRepository.class
                );

        ProductoRepository productoRepository =
                mock(
                        ProductoRepository.class
                );

        RegistroFacturacionService registroFacturacionService =
                mock(
                        RegistroFacturacionService.class
                );

        /*
         * =====================================================
         * EMPRESA
         * =====================================================
         */

        Empresa empresa =
                new Empresa();

        asignarId(
                empresa,
                1L
        );

        empresa.setActiva(
                true
        );

        empresa.setNif(
                "B12345678"
        );

        empresa.setRazonSocial(
                "Empresa de Prueba SL"
        );

        empresa.setCodigoPais(
                "ES"
        );

        /*
         * =====================================================
         * CLIENTE
         * =====================================================
         */

        Cliente cliente =
                mock(
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
         * SERIE ORDINARIA F3
         * =====================================================
         */

        SerieFacturacion serie =
                new SerieFacturacion();

        asignarId(
                serie,
                3L
        );

        serie.setEmpresa(
                empresa
        );

        serie.setCodigo(
                "FAC"
        );

        serie.setDescripcion(
                "Facturas ordinarias"
        );

        serie.setTipoSerie(
                TipoSerieFacturacion.ORDINARIA
        );

        serie.setActiva(
                true
        );

        serie.setUltimoNumero(
                41L
        );

        /*
         * =====================================================
         * FACTURA F3 EN BORRADOR
         * =====================================================
         */

        Factura factura =
                new Factura();

        asignarId(
                factura,
                50L
        );

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

        factura.setNumero(
                null
        );

        factura.setFechaExpedicion(
                null
        );

        factura.setFechaEmision(
                null
        );

        /*
         * Snapshot de destinatario que ya existe
         * desde la creacion del borrador F3.
         */
        factura.setClienteNif(
                "12345678Z"
        );

        factura.setClienteNombre(
                "Cliente de Prueba"
        );

        factura.setClienteCodigoPais(
                "ES"
        );

        factura.setBaseImponible(
                new BigDecimal("100.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("7.00")
        );

        factura.setTotal(
                new BigDecimal("107.00")
        );

        /*
         * =====================================================
         * LINEA
         * =====================================================
         */

        FacturaLinea linea =
                new FacturaLinea();

        linea.setDescripcion(
                "Factura completa en sustitucion de simplificada"
        );

        linea.setCantidad(
                BigDecimal.ONE
        );

        linea.setPrecioUnitario(
                new BigDecimal("100.0000")
        );

        linea.setSigno(
                1
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal("100.00")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("7.00")
        );

        linea.setTotal(
                new BigDecimal("107.00")
        );

        linea.setOrden(
                1
        );

        /*
         * Producto nulo:
         * representa una linea historica y evita depender
         * de otro maestro en esta prueba concreta.
         */
        linea.setProducto(
                null
        );

        factura.agregarLinea(
                linea
        );

        /*
         * =====================================================
         * RELACION F3 -> F2
         * =====================================================
         */

        Factura original =
                new Factura();

        asignarId(
                original,
                25L
        );

        original.setEmpresa(
                empresa
        );

        original.setTipoFactura(
                TipoFactura.F2
        );

        original.setEstado(
                EstadoFactura.EMITIDA
        );

        FacturaSustituida relacion =
                new FacturaSustituida();

        relacion.setFacturaSustituida(
                original
        );

        relacion.setOrden(
                1
        );

        factura.agregarFacturaSustituida(
                relacion
        );

        /*
         * =====================================================
         * MOCKS DE EMISION
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
                serieRepository.findConBloqueo(
                        3L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        serie
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
         * SERVICE
         * =====================================================
         */

        FacturaService service =
                new FacturaService(
                        facturaRepository,
                        empresaRepository,
                        clienteRepository,
                        serieRepository,
                        productoRepository,
                        registroFacturacionService
                );

        FacturaResponse respuesta =
                service.emitir(
                        1L,
                        50L
                );

        /*
         * =====================================================
         * COMPROBACIONES
         * =====================================================
         */

        assertNotNull(
                respuesta
        );

        assertEquals(
                EstadoFactura.EMITIDA,
                factura.getEstado()
        );

        assertEquals(
                42L,
                factura.getNumero()
        );

        assertEquals(
                42L,
                serie.getUltimoNumero()
        );

        assertNotNull(
                factura.getFechaExpedicion()
        );

        assertNotNull(
                factura.getFechaEmision()
        );

        assertEquals(
                TipoFactura.F3,
                factura.getTipoFactura()
        );

        assertEquals(
                "B12345678",
                factura.getEmpresaNif()
        );

        assertEquals(
                1,
                factura.getFacturasSustituidas()
                        .size()
        );

        assertSame(
                factura,
                factura.getFacturasSustituidas()
                        .getFirst()
                        .getFacturaSustitutiva()
        );

        /*
         * El registro VERI*FACTU debe crearse con la
         * misma factura ya numerada y marcada EMITIDA.
         */
        verify(
                registroFacturacionService
        ).crearAlta(
                factura
        );

        verify(
                serieRepository
        ).save(
                serie
        );

        verify(
                facturaRepository
        ).saveAndFlush(
                factura
        );

        assertEquals(
                EstadoFactura.EMITIDA,
                respuesta.estado()
        );

        assertEquals(
                42L,
                respuesta.numero()
        );

        assertEquals(
                TipoFactura.F3,
                respuesta.tipoFactura()
        );

        assertTrue(
                factura.getNumero() > 0
        );

        assertNotNull(
                respuesta.facturasSustituidas()
        );

        assertEquals(
                1,
                respuesta.facturasSustituidas()
                        .size()
        );

        assertEquals(
                25L,
                respuesta.facturasSustituidas()
                        .getFirst()
                        .facturaId()
        );

        System.out.println(
                "F3_RESPUESTA_SUSTITUIDAS_OK=true"
        );
        System.out.println(
                "F3_EMISION_NUMERACION_OK=true"
        );

        System.out.println(
                "F3_EMISION_ESTADO_OK=true"
        );

        System.out.println(
                "F3_EMISION_REGISTRO_VERIFACTU_INVOCADO=true"
        );

        System.out.println(
                "F3_EMISION_RED=false"
        );
    }

    private static void asignarId(
            Object objeto,
            Long id) {

        try {

            Field campo =
                    objeto.getClass()
                            .getDeclaredField(
                                    "id"
                            );

            campo.setAccessible(
                    true
            );

            campo.set(
                    objeto,
                    id
            );

        } catch (ReflectiveOperationException ex) {

            throw new IllegalStateException(
                    "No se pudo asignar el ID de prueba",
                    ex
            );
        }
    }
}
