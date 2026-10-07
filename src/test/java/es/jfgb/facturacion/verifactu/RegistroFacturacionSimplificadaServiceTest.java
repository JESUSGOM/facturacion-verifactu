package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.factura.EstadoFactura;
import es.jfgb.facturacion.factura.Factura;
import es.jfgb.facturacion.factura.FacturaLinea;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistroFacturacionSimplificadaServiceTest {

    @Test
    void creaAltaF2SinDestinatario() {

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
                "TEST-INST-F2-1"
        );

        SerieFacturacion serie =
                new SerieFacturacion();

        serie.setEmpresa(
                empresa
        );

        serie.setCodigo(
                "SIM"
        );

        Factura factura =
                new Factura();

        asignarId(
                factura,
                36L
        );

        factura.setEmpresa(
                empresa
        );

        factura.setSerie(
                serie
        );

        factura.setNumero(
                1L
        );

        factura.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        7
                )
        );

        factura.setTipoFactura(
                TipoFactura.F2
        );

        factura.setEstado(
                EstadoFactura.EMITIDA
        );

        factura.setCliente(
                null
        );

        factura.setClienteNombre(
                null
        );

        factura.setClienteNif(
                null
        );

        factura.setClienteCodigoPais(
                null
        );

        factura.setBaseImponible(
                new BigDecimal("12.50")
        );

        factura.setCuotaImpuesto(
                new BigDecimal("0.88")
        );

        factura.setTotal(
                new BigDecimal("13.38")
        );

        FacturaLinea linea =
                new FacturaLinea();

        linea.setDescripcion(
                "Material de oficina"
        );

        linea.setCantidad(
                BigDecimal.ONE
        );

        linea.setPrecioUnitario(
                new BigDecimal("12.5000")
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
                new BigDecimal("12.50")
        );

        linea.setCuotaImpuesto(
                new BigDecimal("0.88")
        );

        linea.setTotal(
                new BigDecimal("13.38")
        );

        linea.setOrden(
                1
        );

        factura.agregarLinea(
                linea
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
                                36L,
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

        RegistroFacturacion registro =
                service.crearAlta(
                        factura
                );

        assertNotNull(
                registro
        );

        assertEquals(
                TipoRegistroFacturacion.ALTA,
                registro.getTipoRegistro()
        );

        assertEquals(
                "F2",
                registro.getTipoFactura()
        );

        assertEquals(
                "SIM",
                registro.getSerie()
        );

        assertEquals(
                1L,
                registro.getNumero()
        );

        assertEquals(
                "SIM-1",
                registro.getNumSerieFactura()
        );

        assertEquals(
                new BigDecimal("0.88"),
                registro.getCuotaTotal()
        );

        assertEquals(
                new BigDecimal("13.38"),
                registro.getImporteTotal()
        );

        assertNull(
                registro.getDestinatarioNombre()
        );

        assertNull(
                registro.getDestinatarioNif()
        );

        assertNull(
                registro.getDestinatarioCodigoPais()
        );

        assertNull(
                registro.getDestinatarioTipoIdentificacion()
        );

        assertNull(
                registro.getDestinatarioIdOtro()
        );

        assertNotNull(
                registro.getHuella()
        );

        assertEquals(
                64,
                registro.getHuella().length()
        );

        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                registro.getEstadoEnvio()
        );

        /*
         * El repositorio esta simulado y no asigna ID.
         * El mapper AEAT exige un registro ya persistido.
         */
        asignarId(
                registro,
                40L
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
                        ">F2<"
                ),
                xml
        );

        assertFalse(
                xml.contains(
                        "Destinatarios"
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

        assertEquals(
                1,
                registro.getDesgloses().size()
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
