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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaPdfSimplificadaTest {

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
    void facturaF2SinDestinatarioGeneraPdfConQr()
            throws Exception {

        when(
                facturaService.obtener(
                        1L,
                        100L
                )
        ).thenReturn(
                respuestaF2()
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

        assertNotNull(
                pdf
        );

        assertTrue(
                pdf.length > 0
        );

        PdfReader reader =
                new PdfReader(
                        pdf
                );

        String textoPdf;

        try {

            assertTrue(
                    reader.getNumberOfPages() >= 1
            );

            PdfTextExtractor extractor =
                    new PdfTextExtractor(
                            reader
                    );

            textoPdf =
                    extractor
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

        assertTrue(
                textoPdf.contains(
                        "FACTURA"
                ),
                textoPdf
        );

        assertTrue(
                textoPdf.contains(
                        "SIMPLIFICADA"
                ),
                textoPdf
        );

        assertFalse(
                textoPdf.contains(
                        "DESTINATARIO"
                ),
                textoPdf
        );

        assertFalse(
                textoPdf.contains(
                        "null"
                ),
                textoPdf
        );

        verify(
                qrFacturaService
        ).generarPng(
                1L,
                100L
        );
    }

    private Factura facturaPersistida() {

        Factura factura =
                new Factura();

        factura.setEmpresaNif(
                "B12345678"
        );

        factura.setEmpresaRazonSocial(
                "Empresa de Prueba SL"
        );

        factura.setEmpresaNombreComercial(
                "Gombeth Factura"
        );

        factura.setEmpresaDireccion(
                "Calle de Prueba 1"
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

    private FacturaResponse respuestaF2() {

        return new FacturaResponse(
                100L,
                1L,
                null,
                17L,
                "SIM",

                EstadoFactura.EMITIDA,
                1L,
                LocalDate.of(
                        2026,
                        10,
                        7
                ),

                TipoFactura.F2,
                null,
                null,
                null,
                null,

                "Factura simplificada de prueba",

                new BigDecimal(
                        "12.50"
                ),
                new BigDecimal(
                        "0.88"
                ),
                new BigDecimal(
                        "13.38"
                ),

                null,
                null,
                null,
                null,
                null,
                null,
                null,

                Instant.parse(
                        "2026-10-07T07:00:00Z"
                ),
                Instant.parse(
                        "2026-10-07T07:01:00Z"
                ),
                Instant.parse(
                        "2026-10-07T07:02:00Z"
                ),

                null,
                null,

                List.of(),

                List.of()
        );
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
