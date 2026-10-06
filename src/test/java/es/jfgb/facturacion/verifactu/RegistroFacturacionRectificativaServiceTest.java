package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.factura.Factura;
import es.jfgb.facturacion.factura.FacturaLinea;
import es.jfgb.facturacion.factura.FacturaRectificada;
import es.jfgb.facturacion.factura.TipoFactura;
import es.jfgb.facturacion.factura.TipoRectificativa;
import es.jfgb.facturacion.serie.SerieFacturacion;
import es.jfgb.facturacion.verifactu.aeat.AeatRegistroAltaMapper;
import es.jfgb.facturacion.verifactu.aeat.AeatSolicitudAltaXmlBuilder;
import es.jfgb.facturacion.verifactu.aeat.AeatXmlSchemaValidator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistroFacturacionRectificativaServiceTest {

    @Test
    void creaSnapshotRectificativaPorDiferencias() {

        Contexto contexto =
                prepararContexto(
                        TipoRectificativa.I
                );

        Factura factura =
                contexto.factura();

        factura.setBaseRectificada(
                null
        );

        factura.setCuotaRectificada(
                null
        );

        factura.setBaseImponible(
                new BigDecimal("-100.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("-7.00")
        );

        factura.setTotal(
                new BigDecimal("-107.00")
        );

        FacturaLinea linea =
                crearLinea(
                        "-100.00",
                        "-7.00",
                        "-107.00"
                );

        factura.agregarLinea(
                linea
        );

        RegistroFacturacion registro =
                contexto.service().crearAlta(
                        factura
                );

        assertEquals(
                "R1",
                registro.getTipoFactura()
        );

        assertEquals(
                TipoRectificativa.I,
                registro.getTipoRectificativa()
        );

        assertNull(
                registro.getBaseRectificada()
        );

        assertNull(
                registro.getCuotaRectificada()
        );

        comprobarOriginal(
                registro
        );
    }

    @Test
    void generaXmlRectificativoValidoDesdeSnapshotLocal() {

        Contexto contexto =
                prepararContexto(
                        TipoRectificativa.I
                );

        Factura factura =
                contexto.factura();

        factura.setBaseRectificada(
                null
        );

        factura.setCuotaRectificada(
                null
        );

        factura.setBaseImponible(
                new BigDecimal("-100.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("-7.00")
        );

        factura.setTotal(
                new BigDecimal("-107.00")
        );

        factura.agregarLinea(
                crearLinea(
                        "-100.00",
                        "-7.00",
                        "-107.00"
                )
        );

        RegistroFacturacion registro =
                contexto.service().crearAlta(
                        factura
                );

        /*
         * El repositorio esta simulado.
         *
         * Marcamos el registro como persistido para poder
         * recorrer tambien el builder XML, que exige ID.
         */
        asignarId(
                registro,
                30L
        );

        assertNotNull(
                registro.getHuella()
        );

        assertEquals(
                64,
                registro.getHuella().length()
        );

        AeatSolicitudAltaXmlBuilder xmlBuilder =
                new AeatSolicitudAltaXmlBuilder(
                        new AeatRegistroAltaMapper()
                );

        String xml =
                xmlBuilder.generarXml(
                        registro
                );

        assertNotNull(
                xml
        );

        assertTrue(
                xml.contains(
                        "R1"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "FAC-7"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "B12345678"
                ),
                xml
        );

        AeatXmlSchemaValidator validator =
                new AeatXmlSchemaValidator();

        assertDoesNotThrow(
                () -> validator.validar(
                        xml
                )
        );
    }
    @Test
    void creaSnapshotRectificativaPorSustitucion() {

        Contexto contexto =
                prepararContexto(
                        TipoRectificativa.S
                );

        Factura factura =
                contexto.factura();

        factura.setBaseRectificada(
                new BigDecimal("100.00")
        );

        factura.setCuotaRectificada(
                new BigDecimal("7.00")
        );

        factura.setBaseImponible(
                new BigDecimal("80.00")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("5.60")
        );

        factura.setTotal(
                new BigDecimal("85.60")
        );

        FacturaLinea linea =
                crearLinea(
                        "80.00",
                        "5.60",
                        "85.60"
                );

        factura.agregarLinea(
                linea
        );

        RegistroFacturacion registro =
                contexto.service().crearAlta(
                        factura
                );

        assertEquals(
                "R1",
                registro.getTipoFactura()
        );

        assertEquals(
                TipoRectificativa.S,
                registro.getTipoRectificativa()
        );

        assertEquals(
                new BigDecimal("100.00"),
                registro.getBaseRectificada()
        );

        assertEquals(
                new BigDecimal("7.00"),
                registro.getCuotaRectificada()
        );

        comprobarOriginal(
                registro
        );
    }

    private Contexto prepararContexto(
            TipoRectificativa tipoRectificativa) {

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
                "Empresa Prueba SL"
        );

        empresa.setVerifactuNumeroInstalacion(
                "TEST-INST-1"
        );

        SerieFacturacion serieRectificativa =
                new SerieFacturacion();

        serieRectificativa.setEmpresa(
                empresa
        );

        serieRectificativa.setCodigo(
                "RECT"
        );

        SerieFacturacion serieOriginal =
                new SerieFacturacion();

        serieOriginal.setEmpresa(
                empresa
        );

        serieOriginal.setCodigo(
                "FAC"
        );

        Factura original =
                new Factura();

        asignarId(
                original,
                10L
        );

        original.setEmpresa(
                empresa
        );

        original.setEmpresaNif(
                "B12345678"
        );

        original.setSerie(
                serieOriginal
        );

        original.setNumero(
                7L
        );

        original.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        1
                )
        );

        Factura factura =
                new Factura();

        asignarId(
                factura,
                20L
        );

        factura.setEmpresa(
                empresa
        );

        factura.setSerie(
                serieRectificativa
        );

        factura.setNumero(
                1L
        );

        factura.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        5
                )
        );

        factura.setTipoFactura(
                TipoFactura.R1
        );

        factura.setTipoRectificativa(
                tipoRectificativa
        );

        factura.setClienteNif(
                "12345678Z"
        );

        factura.setClienteNombre(
                "Cliente Prueba"
        );

        factura.setClienteCodigoPais(
                "ES"
        );

        FacturaRectificada relacion =
                new FacturaRectificada();

        relacion.setFacturaRectificada(
                original
        );

        relacion.setOrden(
                1
        );

        factura.agregarFacturaRectificada(
                relacion
        );

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
                                20L,
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

        when(
                registroRepository.saveAndFlush(
                        any(
                                RegistroFacturacion.class
                        )
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(
                                0
                        )
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
                factura
        );
    }

    private FacturaLinea crearLinea(
            String base,
            String cuota,
            String total) {

        FacturaLinea linea =
                new FacturaLinea();

        linea.setDescripcion(
                "Rectificacion de prueba"
        );

        linea.setCantidad(
                BigDecimal.ONE
        );

        linea.setPrecioUnitario(
                new BigDecimal(
                        base.replace(
                                "-",
                                ""
                        )
                )
        );

        linea.setTipoImpuesto(
                TipoImpuesto.IGIC
        );

        linea.setPorcentajeImpuesto(
                new BigDecimal("7.00")
        );

        linea.setBaseImponible(
                new BigDecimal(base)
        );

        linea.setCuotaImpuesto(
                new BigDecimal(cuota)
        );

        linea.setTotal(
                new BigDecimal(total)
        );

        linea.setOrden(
                1
        );

        return linea;
    }

    private void comprobarOriginal(
            RegistroFacturacion registro) {

        assertNotNull(
                registro.getFacturasRectificadas()
        );

        assertEquals(
                1,
                registro.getFacturasRectificadas().size()
        );

        RegistroFacturacionRectificada original =
                registro.getFacturasRectificadas()
                        .get(0);

        assertEquals(
                10L,
                original.getFacturaRectificadaId()
        );

        assertEquals(
                Short.valueOf((short) 1),
                original.getOrden()
        );

        assertEquals(
                "B12345678",
                original.getNifEmisor()
        );

        assertEquals(
                "FAC-7",
                original.getNumSerieFactura()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        10,
                        1
                ),
                original.getFechaExpedicion()
        );

        assertSame(
                registro,
                original.getRegistroFacturacion()
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
            Factura factura) {
    }
}