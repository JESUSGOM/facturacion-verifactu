package es.jfgb.facturacion.factura;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.cliente.ClienteRepository;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.producto.ProductoRepository;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.serie.SerieFacturacionRepository;
import es.jfgb.facturacion.serie.TipoSerieFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FacturaServiceF3DefensiveTest {

    @Test
    void rechazaF3ConSerieSimplificadaAntesDeConsumirNumeracion() {

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

        Empresa empresa =
                new Empresa();

        asignarId(
                empresa,
                1L
        );

        empresa.setActiva(
                true
        );

        Cliente cliente =
                mock(
                        Cliente.class
                );

        when(
                cliente.isActivo()
        ).thenReturn(
                true
        );

        /*
         * Inconsistencia deliberada:
         *
         * una F3 no puede emitirse consumiendo numeracion
         * de una serie simplificada.
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
                "SIM"
        );

        serie.setTipoSerie(
                TipoSerieFacturacion.SIMPLIFICADA
        );

        serie.setActiva(
                true
        );

        serie.setUltimoNumero(
                41L
        );

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

        /*
         * emitir() exige al menos una linea.
         *
         * No necesitamos importes porque el rechazo que se
         * prueba ocurre antes de numerar y antes de crear
         * el registro VERI*FACTU.
         */
        FacturaLinea linea =
                new FacturaLinea();

        linea.setProducto(
                null
        );

        factura.agregarLinea(
                linea
        );

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

        FacturaService service =
                new FacturaService(
                        facturaRepository,
                        empresaRepository,
                        clienteRepository,
                        serieRepository,
                        productoRepository,
                        registroFacturacionService
                );

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                service.emitir(
                                        1L,
                                        50L
                                )
                );

        assertEquals(
                "Una factura F3 debe utilizar una serie ordinaria",
                error.getReason()
        );

        /*
         * Lo importante:
         *
         * la numeracion permanece intacta.
         */
        assertEquals(
                41L,
                serie.getUltimoNumero()
        );

        assertNull(
                factura.getNumero()
        );

        assertEquals(
                EstadoFactura.BORRADOR,
                factura.getEstado()
        );

        verify(
                serieRepository,
                never()
        ).save(
                any(
                        SerieFacturacion.class
                )
        );

        verify(
                facturaRepository,
                never()
        ).saveAndFlush(
                any(
                        Factura.class
                )
        );

        verify(
                registroFacturacionService,
                never()
        ).crearAlta(
                any(
                        Factura.class
                )
        );

        System.out.println(
                "F3_SERIE_SIMPLIFICADA_RECHAZADA=true"
        );

        System.out.println(
                "F3_SERIE_SIMPLIFICADA_NUMERACION_INTACTA=true"
        );

        System.out.println(
                "F3_SERIE_SIMPLIFICADA_RED=false"
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
