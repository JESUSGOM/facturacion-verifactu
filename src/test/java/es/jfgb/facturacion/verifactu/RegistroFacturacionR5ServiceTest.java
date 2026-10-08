package es.jfgb.facturacion.verifactu;

import es.jfgb.facturacion.empresa.Empresa;
import es.jfgb.facturacion.empresa.EmpresaRepository;
import es.jfgb.facturacion.empresa.TipoImpuesto;
import es.jfgb.facturacion.factura.EstadoFactura;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistroFacturacionR5ServiceTest {

    @Test
    void creaAltaR5SinDestinatarioYGeneraXmlValido() {

        EmpresaRepository empresaRepository =
                mock(EmpresaRepository.class);

        RegistroFacturacionRepository registroRepository =
                mock(RegistroFacturacionRepository.class);

        VerifactuSistemaProperties sistemaProperties =
                mock(VerifactuSistemaProperties.class);

        Empresa empresa = new Empresa();
        asignarId(empresa, 1L);
        empresa.setNif("B12345678");
        empresa.setRazonSocial("Empresa de Prueba SL");
        empresa.setVerifactuNumeroInstalacion("TEST-INST-R5-1");

        SerieFacturacion serieRectificativa = new SerieFacturacion();
        serieRectificativa.setEmpresa(empresa);
        serieRectificativa.setCodigo("RECT");

        Factura factura = new Factura();
        asignarId(factura, 50L);
        factura.setEmpresa(empresa);
        factura.setSerie(serieRectificativa);
        factura.setNumero(3L);
        factura.setFechaExpedicion(LocalDate.of(2026, 10, 7));
        factura.setTipoFactura(TipoFactura.R5);
        factura.setTipoRectificativa(TipoRectificativa.I);
        factura.setMotivoRectificacion("Correccion de factura simplificada");
        factura.setEstado(EstadoFactura.EMITIDA);
        factura.setCliente(null);
        factura.setClienteNombre(null);
        factura.setClienteNif(null);
        factura.setClienteCodigoPais(null);
        factura.setBaseImponible(new BigDecimal("-12.50"));
        factura.setCuotaImpuesto(new BigDecimal("-0.88"));
        factura.setTotal(new BigDecimal("-13.38"));

        FacturaLinea linea = new FacturaLinea();
        linea.setDescripcion("Correccion material de oficina");
        linea.setCantidad(BigDecimal.ONE);
        linea.setPrecioUnitario(new BigDecimal("12.5000"));
        linea.setSigno(-1);
        linea.setTipoImpuesto(TipoImpuesto.IGIC);
        linea.setPorcentajeImpuesto(new BigDecimal("7.00"));
        linea.setBaseImponible(new BigDecimal("-12.50"));
        linea.setCuotaImpuesto(new BigDecimal("-0.88"));
        linea.setTotal(new BigDecimal("-13.38"));
        linea.setOrden(1);
        factura.agregarLinea(linea);

        SerieFacturacion serieOriginal = new SerieFacturacion();
        serieOriginal.setEmpresa(empresa);
        serieOriginal.setCodigo("SIM");

        Factura original = new Factura();
        asignarId(original, 36L);
        original.setEmpresa(empresa);
        original.setEmpresaNif("B12345678");
        original.setSerie(serieOriginal);
        original.setNumero(1L);
        original.setFechaExpedicion(LocalDate.of(2026, 10, 7));
        original.setTipoFactura(TipoFactura.F2);
        original.setEstado(EstadoFactura.EMITIDA);

        FacturaRectificada relacion = new FacturaRectificada();
        relacion.setFacturaRectificada(original);
        relacion.setOrden(1);
        factura.agregarFacturaRectificada(relacion);

        when(empresaRepository.findConBloqueo(1L))
                .thenReturn(Optional.of(empresa));

        when(registroRepository.existsByFactura_IdAndTipoRegistro(
                50L,
                TipoRegistroFacturacion.ALTA
        )).thenReturn(false);

        when(registroRepository.findTopByEmpresa_IdOrderByIdDesc(1L))
                .thenReturn(Optional.empty());

        when(sistemaProperties.getNombreRazon())
                .thenReturn("Gombeth Software Solutions Test");

        when(sistemaProperties.getNif())
                .thenReturn("B12345678");

        when(sistemaProperties.getNombreSistema())
                .thenReturn("Gombeth Factura");

        when(sistemaProperties.getIdSistema())
                .thenReturn("GF");

        when(sistemaProperties.getVersion())
                .thenReturn("0.0.1-SNAPSHOT");

        when(sistemaProperties.getSoloVerifactu())
                .thenReturn("S");

        when(sistemaProperties.getMultiOt())
                .thenReturn("S");

        when(empresaRepository.count())
                .thenReturn(1L);

        when(registroRepository.saveAndFlush(any(RegistroFacturacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistroFacturacionService service =
                new RegistroFacturacionService(
                        empresaRepository,
                        registroRepository,
                        new HuellaVerifactuService(),
                        sistemaProperties
                );

        RegistroFacturacion registro =
                service.crearAlta(factura);

        assertNotNull(registro);
        assertEquals(TipoRegistroFacturacion.ALTA, registro.getTipoRegistro());
        assertEquals("R5", registro.getTipoFactura());
        assertEquals(TipoRectificativa.I, registro.getTipoRectificativa());
        assertEquals("RECT", registro.getSerie());
        assertEquals(3L, registro.getNumero());
        assertEquals("RECT-3", registro.getNumSerieFactura());
        assertEquals(new BigDecimal("-0.88"), registro.getCuotaTotal());
        assertEquals(new BigDecimal("-13.38"), registro.getImporteTotal());

        assertNull(registro.getDestinatarioNombre());
        assertNull(registro.getDestinatarioNif());
        assertNull(registro.getDestinatarioCodigoPais());

        assertNotNull(registro.getFacturasRectificadas());
        assertEquals(1, registro.getFacturasRectificadas().size());
        assertEquals(
                "SIM-1",
                registro.getFacturasRectificadas()
                        .getFirst()
                        .getNumSerieFactura()
        );

        assertNotNull(registro.getHuella());
        assertEquals(64, registro.getHuella().length());
        assertEquals(
                EstadoEnvioRegistro.PENDIENTE,
                registro.getEstadoEnvio()
        );

        asignarId(registro, 50L);

        AeatSolicitudAltaXmlBuilder xmlBuilder =
                new AeatSolicitudAltaXmlBuilder(
                        new AeatRegistroAltaMapper()
                );

        String xml = xmlBuilder.generarXml(registro);

        assertNotNull(xml);
        assertTrue(xml.contains(">R5<"), xml);
        assertFalse(xml.contains("Destinatarios"), xml);
        assertTrue(xml.contains("FacturasRectificadas"), xml);

        AeatXmlSchemaValidator validator =
                new AeatXmlSchemaValidator();

        assertDoesNotThrow(
                () -> validator.validar(xml)
        );
    }

    private static void asignarId(
            Object objeto,
            Long id) {

        try {
            Field campo =
                    objeto.getClass()
                            .getDeclaredField("id");

            campo.setAccessible(true);
            campo.set(objeto, id);

        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "No se pudo asignar el ID de prueba",
                    ex
            );
        }
    }
}
