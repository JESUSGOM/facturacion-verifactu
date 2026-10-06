package es.jfgb.facturacion.factura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaPdfServiceTest {

    @Mock
    private FacturaService facturaService;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private QrFacturaService qrFacturaService;

    private FacturaPdfService service;

    @BeforeEach
    void setUp() {

        service =
                new FacturaPdfService(
                        facturaService,
                        facturaRepository,
                        qrFacturaService
                );
    }

    @Test
    void facturaEmitidaGeneraPdfConSnapshotHistorico()
            throws Exception {

        when(
                facturaService.obtener(
                        1L,
                        100L
                )
        ).thenReturn(
                respuesta(
                        EstadoFactura.EMITIDA
                )
        );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        100L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        facturaPersistida()
                )
        );

        when(
                qrFacturaService.generarPng(
                        1L,
                        100L
                )
        ).thenReturn(
                crearPng()
        );

        byte[] pdf =
                service.generar(
                        1L,
                        100L
                );

        assertNotNull(pdf);
        assertTrue(pdf.length > 1000);

        String cabecera =
                new String(
                        pdf,
                        0,
                        5,
                        StandardCharsets.US_ASCII
                );

        assertEquals(
                "%PDF-",
                cabecera
        );

        PdfReader reader =
                new PdfReader(
                        pdf
                );

        assertEquals(
                1,
                reader.getNumberOfPages()
        );

        reader.close();
    }

    @Test
    void facturaRectificativaPorDiferenciasMuestraDatosFiscales()
            throws Exception {

        when(
                facturaService.obtener(
                        1L,
                        200L
                )
        ).thenReturn(
                respuestaRectificativa(
                        TipoRectificativa.I
                )
        );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        200L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        facturaPersistida()
                )
        );

        when(
                qrFacturaService.generarPng(
                        1L,
                        200L
                )
        ).thenReturn(
                crearPng()
        );

        byte[] pdf =
                service.generar(
                        1L,
                        200L
                );

        String textoPdf =
                extraerTextoPdf(
                        pdf
                );

        assertTrue(
                textoPdf.contains(
                        "FACTURA RECTIFICATIVA"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "R1"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "I - Por diferencias"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "Correccion de importe"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "Facturas rectificadas"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "FAC-7"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "01/10/2026"
                ),
                textoPdf
        );

        assertFalse(
                textoPdf.contains(
                        "Base rectificada"
                ),
                textoPdf
        );

        assertFalse(
                textoPdf.contains(
                        "Cuota rectificada"
                ),
                textoPdf
        );
    }

    @Test
    void facturaRectificativaPorSustitucionMuestraImportesRectificados()
            throws Exception {

        when(
                facturaService.obtener(
                        1L,
                        200L
                )
        ).thenReturn(
                respuestaRectificativa(
                        TipoRectificativa.S
                )
        );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        200L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        facturaPersistida()
                )
        );

        when(
                qrFacturaService.generarPng(
                        1L,
                        200L
                )
        ).thenReturn(
                crearPng()
        );

        byte[] pdf =
                service.generar(
                        1L,
                        200L
                );

        String textoPdf =
                extraerTextoPdf(
                        pdf
                );

        assertTrue(
                textoPdf.contains(
                        "FACTURA RECTIFICATIVA"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "R1"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "S - Por sustitucion"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "Sustitucion de factura"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "Base rectificada"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "100.00 EUR"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "Cuota rectificada"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "7.00 EUR"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "FAC-7"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "01/10/2026"
                ),
                textoPdf
        );
    }
    @Test
    void borradorNoPuedeGenerarPdfDefinitivo() {

        when(
                facturaService.obtener(
                        1L,
                        100L
                )
        ).thenReturn(
                respuesta(
                        EstadoFactura.BORRADOR
                )
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.generar(
                                1L,
                                100L
                        )
                );

        assertEquals(
                HttpStatus.CONFLICT,
                ex.getStatusCode()
        );

        verifyNoInteractions(
                facturaRepository
        );

        verifyNoInteractions(
                qrFacturaService
        );
    }

    @Test
    void facturaEmitidaSinSnapshotNoGeneraPdf() {

        when(
                facturaService.obtener(
                        1L,
                        100L
                )
        ).thenReturn(
                respuesta(
                        EstadoFactura.EMITIDA
                )
        );

        when(
                facturaRepository.findByIdAndEmpresa_Id(
                        100L,
                        1L
                )
        ).thenReturn(
                Optional.of(
                        new Factura()
                )
        );

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.generar(
                                1L,
                                100L
                        )
                );

        assertTrue(
                ex.getMessage()
                        .contains(
                                "NIF historico"
                        )
        );

        verifyNoInteractions(
                qrFacturaService
        );
    }

    private Factura facturaPersistida() {

        Factura factura =
                new Factura();

        factura.setEmpresaNif(
                "B12345678"
        );

        factura.setEmpresaRazonSocial(
                "Empresa Historica SL"
        );

        factura.setEmpresaNombreComercial(
                "Gombeth Factura"
        );

        factura.setEmpresaDireccion(
                "Calle Historica 1"
        );

        factura.setEmpresaCodigoPostal(
                "38001"
        );

        factura.setEmpresaMunicipio(
                "Santa Cruz de Tenerife"
        );

        factura.setEmpresaProvincia(
                "Santa Cruz de Tenerife"
        );

        factura.setEmpresaCodigoPais(
                "ES"
        );

        return factura;
    }

    private FacturaResponse respuesta(
            EstadoFactura estado) {

        return new FacturaResponse(
                100L,
                1L,
                10L,
                20L,
                "F2026",

                estado,
                17L,
                LocalDate.of(
                        2026,
                        10,
                        4
                ),

                "Factura de prueba",

                new BigDecimal("100.00"),
                new BigDecimal("7.00"),
                new BigDecimal("107.00"),

                "12345678Z",
                "Cliente de Prueba",
                "Calle Cliente 1",
                "38001",
                "Santa Cruz de Tenerife",
                "Santa Cruz de Tenerife",
                "ES",

                Instant.parse(
                        "2026-10-04T06:00:00Z"
                ),
                Instant.parse(
                        "2026-10-04T06:01:00Z"
                ),
                estado == EstadoFactura.EMITIDA
                        ? Instant.parse(
                                "2026-10-04T06:02:00Z"
                        )
                        : null,

                null,
                null,

                List.of()
        );
    }

    private FacturaResponse respuestaRectificativa(
            TipoRectificativa tipoRectificativa) {

        boolean sustitucion =
                tipoRectificativa
                        == TipoRectificativa.S;

        FacturaRectificadaResponse original =
                new FacturaRectificadaResponse(
                        50L,
                        30L,
                        "FAC",
                        7L,
                        LocalDate.of(
                                2026,
                                10,
                                1
                        ),
                        new BigDecimal("100.00"),
                        new BigDecimal("7.00"),
                        new BigDecimal("107.00")
                );

        return new FacturaResponse(
                200L,
                1L,
                10L,
                40L,
                "RECT",

                EstadoFactura.EMITIDA,
                1L,
                LocalDate.of(
                        2026,
                        10,
                        6
                ),

                TipoFactura.R1,
                tipoRectificativa,

                sustitucion
                        ? "Sustitucion de factura"
                        : "Correccion de importe",

                sustitucion
                        ? new BigDecimal("100.00")
                        : null,

                sustitucion
                        ? new BigDecimal("7.00")
                        : null,

                "Rectificacion de prueba",

                sustitucion
                        ? new BigDecimal("80.00")
                        : new BigDecimal("-20.00"),

                sustitucion
                        ? new BigDecimal("5.60")
                        : new BigDecimal("-1.40"),

                sustitucion
                        ? new BigDecimal("85.60")
                        : new BigDecimal("-21.40"),

                "12345678Z",
                "Cliente de Prueba",
                "Calle Cliente 1",
                "38001",
                "Santa Cruz de Tenerife",
                "Santa Cruz de Tenerife",
                "ES",

                Instant.parse(
                        "2026-10-06T06:00:00Z"
                ),
                Instant.parse(
                        "2026-10-06T06:01:00Z"
                ),
                Instant.parse(
                        "2026-10-06T06:02:00Z"
                ),

                null,
                null,

                List.of(
                        original
                ),

                List.of()
        );
    }

    private String extraerTextoPdf(
            byte[] pdf)
            throws Exception {

        PdfReader reader =
                new PdfReader(
                        pdf
                );

        try {

            PdfTextExtractor extractor =
                    new PdfTextExtractor(
                            reader
                    );

            return extractor
                    .getTextFromPage(
                            1
                    )
                    .replaceAll(
                            "\\s+",
                            " "
                    )
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

        for (int y = 0;
             y < imagen.getHeight();
             y++) {

            for (int x = 0;
                 x < imagen.getWidth();
                 x++) {

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

        ImageIO.write(
                imagen,
                "PNG",
                salida
        );

        return salida.toByteArray();
    }
}