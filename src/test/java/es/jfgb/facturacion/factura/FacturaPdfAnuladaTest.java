package es.jfgb.facturacion.factura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openpdf.text.pdf.PdfReader;

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
class FacturaPdfAnuladaTest {

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
    void facturaAnuladaMantieneAccesoAlPdfHistorico()
            throws Exception {

        when(
                facturaService.obtener(
                        1L,
                        100L
                )
        ).thenReturn(
                respuestaAnulada()
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
                pdf.length > 1000
        );

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

        verify(
                qrFacturaService
        ).generarPng(
                1L,
                100L
        );
    }

    private FacturaResponse respuestaAnulada() {

        return new FacturaResponse(
                100L,
                1L,
                10L,
                20L,
                "F2026",

                EstadoFactura.ANULADA,
                17L,

                LocalDate.of(
                        2026,
                        10,
                        4
                ),

                "Factura de prueba",

                new BigDecimal(
                        "100.00"
                ),

                new BigDecimal(
                        "7.00"
                ),

                new BigDecimal(
                        "107.00"
                ),

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

                /*
                 * Una factura anulada fue previamente
                 * emitida, por lo que conserva su
                 * fecha de emision.
                 */
                Instant.parse(
                        "2026-10-04T06:02:00Z"
                ),

                null,
                null,

                List.of()
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

    private byte[] crearPng()
            throws Exception {

        BufferedImage imagen =
                new BufferedImage(
                        100,
                        100,
                        BufferedImage.TYPE_INT_RGB
                );

        for (
                int y = 0;
                y < imagen.getHeight();
                y++
        ) {

            for (
                    int x = 0;
                    x < imagen.getWidth();
                    x++
            ) {

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