package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.cliente.Cliente;
import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.factura.EstadoFactura;
import es.jfgb.facturacion.factura.Factura;
import es.jfgb.facturacion.factura.FacturaLinea;
import es.jfgb.facturacion.factura.FacturaSustituida;
import es.jfgb.facturacion.factura.TipoFactura;
import es.jfgb.facturacion.serie.SerieFacturacion;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistroFacturacionF3DefensiveTest {

    @Test
    void rechazaF2SustituidaConDestinatario() {

        Contexto contexto =
                crearContexto(
                        1,
                        true
                );

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                contexto.service()
                                        .crearAlta(
                                                contexto.factura()
                                        )
                );

        assertEquals(
                "La factura F2 sustituida no debe tener "
                        + "destinatario identificado",
                error.getReason()
        );

        verify(
                contexto.registroRepository(),
                never()
        ).saveAndFlush(
                any(
                        RegistroFacturacion.class
                )
        );

        System.out.println(
                "F3_F2_CON_DESTINATARIO_RECHAZADA=true"
        );
    }

    @Test
    void rechazaOrdenSustitucionNulo() {

        comprobarOrdenInvalido(
                null
        );
    }

    @Test
    void rechazaOrdenSustitucionCero() {

        comprobarOrdenInvalido(
                0
        );
    }

    @Test
    void rechazaOrdenSustitucionMayorDeMil() {

        comprobarOrdenInvalido(
                1001
        );
    }

    private static void comprobarOrdenInvalido(
            Integer orden) {

        Contexto contexto =
                crearContexto(
                        orden,
                        false
                );

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () ->
                                contexto.service()
                                        .crearAlta(
                                                contexto.factura()
                                        )
                );

        assertEquals(
                "El orden de una factura F2 sustituida "
                        + "debe estar entre 1 y 1000",
                error.getReason()
        );

        verify(
                contexto.registroRepository(),
                never()
        ).saveAndFlush(
                any(
                        RegistroFacturacion.class
                )
        );

        System.out.println(
                "F3_ORDEN_SUSTITUCION_INVALIDO_RECHAZADO="
                        + String.valueOf(
                        orden
                )
        );
    }

    private static Contexto crearContexto(
            Integer orden,
            boolean originalConCliente) {

        EmpresaRepository empresaRepository =
                mock(
                        EmpresaRepository.class
                );

        RegistroFacturacionRepository registroRepository =
                mock(
                        RegistroFacturacionRepository.class
                );

        VerifactuSistemaProperties sistemaProperties =
                mock(
                        VerifactuSistemaProperties.class
                );

        Empresa empresa =
                new Empresa();

        asignarId(
                empresa,
                1L
        );

        empresa.setNif(
                "B12345678"
        );

        empresa.setRazonSocial(
                "Empresa de Prueba SL"
        );

        empresa.setVerifactuNumeroInstalacion(
                "TEST-INST-F3-DEF"
        );

        /*
         * =====================================================
         * F2 ORIGINAL
         * =====================================================
         */

        SerieFacturacion serieSimplificada =
                new SerieFacturacion();

        serieSimplificada.setEmpresa(
                empresa
        );

        serieSimplificada.setCodigo(
                "SIM"
        );

        Factura original =
                new Factura();

        asignarId(
                original,
                25L
        );

        original.setEmpresa(
                empresa
        );

        original.setEmpresaNif(
                "B12345678"
        );

        original.setSerie(
                serieSimplificada
        );

        original.setNumero(
                25L
        );

        original.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        original.setTipoFactura(
                TipoFactura.F2
        );

        original.setEstado(
                EstadoFactura.EMITIDA
        );

        if (originalConCliente) {

            original.setCliente(
                    mock(
                            Cliente.class
                    )
            );

        } else {

            original.setCliente(
                    null
            );
        }

        /*
         * =====================================================
         * F3
         * =====================================================
         */

        SerieFacturacion serieF3 =
                new SerieFacturacion();

        serieF3.setEmpresa(
                empresa
        );

        serieF3.setCodigo(
                "FAC"
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

        factura.setSerie(
                serieF3
        );

        factura.setNumero(
                100L
        );

        factura.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        10
                )
        );

        factura.setTipoFactura(
                TipoFactura.F3
        );

        factura.setEstado(
                EstadoFactura.EMITIDA
        );

        factura.setClienteNombre(
                "Cliente de Prueba"
        );

        factura.setClienteNif(
                "12345678Z"
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

        factura.agregarLinea(
                linea
        );

        FacturaSustituida relacion =
                new FacturaSustituida();

        relacion.setFacturaSustituida(
                original
        );

        relacion.setOrden(
                orden
        );

        factura.agregarFacturaSustituida(
                relacion
        );

        /*
         * =====================================================
         * REPOSITORIOS
         * =====================================================
         */

        when(
                empresaRepository.findConBloqueo(
                        1L
                )
        ).thenReturn(
                Optional.of(
                        empresa
                )
        );

        when(
                registroRepository
                        .existsByFactura_IdAndTipoRegistro(
                                50L,
                                TipoRegistroFacturacion.ALTA
                        )
        ).thenReturn(
                false
        );

        when(
                registroRepository
                        .findTopByEmpresa_IdOrderByIdDesc(
                                1L
                        )
        ).thenReturn(
                Optional.empty()
        );

        /*
         * =====================================================
         * DATOS SIF
         * =====================================================
         */

        when(
                sistemaProperties.getNombreRazon()
        ).thenReturn(
                "Gombeth Software Solutions Test"
        );

        when(
                sistemaProperties.getNif()
        ).thenReturn(
                "B12345678"
        );

        when(
                sistemaProperties.getNombreSistema()
        ).thenReturn(
                "Gombeth Factura"
        );

        when(
                sistemaProperties.getIdSistema()
        ).thenReturn(
                "GF"
        );

        when(
                sistemaProperties.getVersion()
        ).thenReturn(
                "0.0.1-SNAPSHOT"
        );

        when(
                sistemaProperties.getSoloVerifactu()
        ).thenReturn(
                "S"
        );

        when(
                sistemaProperties.getMultiOt()
        ).thenReturn(
                "S"
        );

        when(
                empresaRepository.count()
        ).thenReturn(
                1L
        );

        RegistroFacturacionService service =
                new RegistroFacturacionService(
                        empresaRepository,
                        registroRepository,
                        new HuellaVerifactuService(),
                        sistemaProperties
                );

        return new Contexto(
                service,
                registroRepository,
                factura
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

    private record Contexto(
            RegistroFacturacionService service,
            RegistroFacturacionRepository registroRepository,
            Factura factura) {
    }
}
