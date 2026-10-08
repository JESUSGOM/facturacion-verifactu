package es.jfgb.facturacion.factura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaPdfR5Test {

    @Mock private FacturaService facturaService;
    @Mock private FacturaRepository facturaRepository;
    @Mock private QrFacturaService qrFacturaService;

    private FacturaPdfService service;

    @BeforeEach
    void setUp() {
        service = new FacturaPdfService(
                facturaService,
                facturaRepository,
                qrFacturaService
        );
    }

    @Test
    void facturaR5SinDestinatarioGeneraPdfRectificativoConQr()
            throws Exception {

        when(facturaService.obtener(1L, 300L))
                .thenReturn(respuestaR5());

        when(facturaRepository.findByIdAndEmpresa_Id(300L, 1L))
                .thenReturn(Optional.of(facturaPersistida()));

        when(qrFacturaService.generarPng(1L, 300L))
                .thenReturn(crearPng());

        byte[] pdf = service.generar(1L, 300L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);

        String textoPdf = extraerTextoPdf(pdf);

        assertTrue(textoPdf.contains("FACTURA RECTIFICATIVA"), textoPdf);
        assertTrue(textoPdf.contains("R5"), textoPdf);
        assertTrue(textoPdf.contains("I - Por diferencias"), textoPdf);
        assertTrue(textoPdf.contains("SIM-1"), textoPdf);
        assertFalse(textoPdf.contains("DESTINATARIO"), textoPdf);
        assertFalse(textoPdf.contains("null"), textoPdf);

        verify(qrFacturaService).generarPng(1L, 300L);
    }

    private Factura facturaPersistida() {
        Factura factura = new Factura();
        factura.setEmpresaNif("B12345678");
        factura.setEmpresaRazonSocial("Empresa de Prueba SL");
        factura.setEmpresaNombreComercial("Gombeth Factura");
        factura.setEmpresaDireccion("Calle de Prueba 1");
        factura.setEmpresaCodigoPostal("38001");
        factura.setEmpresaMunicipio("Santa Cruz de Tenerife");
        factura.setEmpresaProvincia("Santa Cruz de Tenerife");
        factura.setEmpresaCodigoPais("ES");
        return factura;
    }

    private FacturaResponse respuestaR5() {

        FacturaRectificadaResponse original =
                new FacturaRectificadaResponse(
                        36L,
                        17L,
                        "SIM",
                        1L,
                        LocalDate.of(2026, 10, 7),
                        new BigDecimal("12.50"),
                        new BigDecimal("0.88"),
                        new BigDecimal("13.38")
                );

        return new FacturaResponse(
                300L,
                1L,
                null,
                20L,
                "RECT",

                EstadoFactura.EMITIDA,
                3L,
                LocalDate.of(2026, 10, 8),

                TipoFactura.R5,
                TipoRectificativa.I,
                "Correccion de factura simplificada",
                null,
                null,

                "Rectificacion R5 de prueba",

                new BigDecimal("-12.50"),
                new BigDecimal("-0.88"),
                new BigDecimal("-13.38"),

                null,
                null,
                null,
                null,
                null,
                null,
                null,

                Instant.parse("2026-10-08T07:00:00Z"),
                Instant.parse("2026-10-08T07:01:00Z"),
                Instant.parse("2026-10-08T07:02:00Z"),

                null,
                null,

                List.of(original),
                List.of()
        );
    }

    private String extraerTextoPdf(byte[] pdf)
            throws Exception {

        PdfReader reader = new PdfReader(pdf);

        try {
            return new PdfTextExtractor(reader)
                    .getTextFromPage(1)
                    .replaceAll("\\s+", " ")
                    .trim();
        } finally {
            reader.close();
        }
    }

    private byte[] crearPng()
            throws Exception {

        BufferedImage imagen =
                new BufferedImage(
                        100,
                        100,
                        BufferedImage.TYPE_INT_RGB
                );

        for (int y = 0; y < imagen.getHeight(); y++) {
            for (int x = 0; x < imagen.getWidth(); x++) {
                imagen.setRGB(
                        x,
                        y,
                        ((x / 10) + (y / 10)) % 2 == 0
                                ? 0xFF000000
                                : 0xFFFFFFFF
                );
            }
        }

        ByteArrayOutputStream salida =
                new ByteArrayOutputStream();

        ImageIO.write(imagen, "PNG", salida);

        return salida.toByteArray();
    }
}
