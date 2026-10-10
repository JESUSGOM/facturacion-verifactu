package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.factura.EstadoFactura;
import es.jfgb.facturacion.factura.Factura;
import es.jfgb.facturacion.factura.FacturaLinea;
import es.jfgb.facturacion.factura.FacturaSustituida;
import es.jfgb.facturacion.factura.TipoFactura;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistroFacturacionF3ServiceTest {

    @Test
    void creaSnapshotF3YGeneraXmlValido() {

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

        empresa.setNif(
                "B12345678"
        );

        empresa.setRazonSocial(
                "Empresa de Prueba SL"
        );

        empresa.setVerifactuNumeroInstalacion(
                "TEST-INST-F3-1"
        );

        /*
         * =====================================================
         * SERIE F2 ORIGINAL
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

        /*
         * =====================================================
         * FACTURA F2 SUSTITUIDA
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

        /*
         * Snapshot historico del NIF del emisor.
         */
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

        /*
         * Una F2 ordinaria no necesita destinatario
         * identificado.
         */
        original.setCliente(
                null
        );

        original.setClienteNombre(
                null
        );

        original.setClienteNif(
                null
        );

        original.setClienteCodigoPais(
                null
        );

        /*
         * =====================================================
         * SERIE DE LA F3
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

        /*
         * =====================================================
         * FACTURA F3
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
                        9
                )
        );

        factura.setTipoFactura(
                TipoFactura.F3
        );

        factura.setEstado(
                EstadoFactura.EMITIDA
        );

        /*
         * La F3 identifica al destinatario.
         */
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

        /*
         * =====================================================
         * LINEA F3
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

        factura.agregarLinea(
                linea
        );

        /*
         * =====================================================
         * RELACION F3 -> F2
         * =====================================================
         */

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
         * DATOS DEL SISTEMA INFORMATICO
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

        /*
         * =====================================================
         * CREACION DEL REGISTRO VERI*FACTU
         * =====================================================
         */

        RegistroFacturacionService service =
                new RegistroFacturacionService(
                        empresaRepository,
                        registroRepository,
                        new HuellaVerifactuService(),
                        sistemaProperties
                );

        RegistroFacturacion registro =
                service.crearAlta(
                        factura
                );

        /*
         * =====================================================
         * COMPROBACIONES DEL REGISTRO
         * =====================================================
         */

        assertNotNull(
                registro
        );

        assertEquals(
                TipoRegistroFacturacion.ALTA,
                registro.getTipoRegistro()
        );

        assertEquals(
                "F3",
                registro.getTipoFactura()
        );

        assertEquals(
                "FAC-100",
                registro.getNumSerieFactura()
        );

        assertEquals(
                "Cliente de Prueba",
                registro.getDestinatarioNombre()
        );

        assertEquals(
                "12345678Z",
                registro.getDestinatarioNif()
        );

        assertEquals(
                "ES",
                registro.getDestinatarioCodigoPais()
        );

        assertNotNull(
                registro.getFacturasSustituidas()
        );

        assertEquals(
                1,
                registro.getFacturasSustituidas()
                        .size()
        );

        RegistroFacturacionSustituida snapshot =
                registro.getFacturasSustituidas()
                        .getFirst();

        assertEquals(
                25L,
                snapshot.getFacturaSustituidaId()
        );

        assertEquals(
                Short.valueOf(
                        (short) 1
                ),
                snapshot.getOrden()
        );

        assertEquals(
                "B12345678",
                snapshot.getNifEmisor()
        );

        assertEquals(
                "SIM-25",
                snapshot.getNumSerieFactura()
        );

        assertEquals(
                LocalDate.of(
                        2026,
                        10,
                        1
                ),
                snapshot.getFechaExpedicion()
        );

        assertSame(
                registro,
                snapshot.getRegistroFacturacion()
        );

        assertNotNull(
                registro.getHuella()
        );

        assertEquals(
                64,
                registro.getHuella()
                        .length()
        );

        /*
         * F3 no es una factura rectificativa.
         */
        assertTrue(
                registro.getFacturasRectificadas()
                        .isEmpty()
        );

        /*
         * =====================================================
         * XML REAL DEL REGISTRO GENERADO POR EL SERVICE
         * =====================================================
         *
         * El repositorio es un mock y no asigna ID.
         * El builder exige un registro persistido.
         */
        asignarId(
                registro,
                60L
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
                        ">F3<"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "FacturasSustituidas"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "IDFacturaSustituida"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "SIM-25"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "01-10-2026"
                ),
                xml
        );

        assertTrue(
                xml.contains(
                        "12345678Z"
                ),
                xml
        );

        assertFalse(
                xml.contains(
                        "FacturasRectificadas"
                ),
                xml
        );

        assertFalse(
                xml.contains(
                        "TipoRectificativa"
                ),
                xml
        );

        /*
         * Validacion contra los XSD locales.
         */
        AeatXmlSchemaValidator validator =
                new AeatXmlSchemaValidator();

        assertDoesNotThrow(
                () -> validator.validar(
                        xml
                )
        );

        System.out.println(
                "VERIFACTU_F3_SERVICE_SNAPSHOT_OK=true"
        );

        System.out.println(
                "VERIFACTU_F3_SERVICE_XML_XSD_VALIDO=true"
        );

        System.out.println(
                "VERIFACTU_F3_SERVICE_RED=false"
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
