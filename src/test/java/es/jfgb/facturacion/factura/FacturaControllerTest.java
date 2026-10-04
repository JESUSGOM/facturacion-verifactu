package es.jfgb.facturacion.factura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaControllerTest {

    @Mock
    private FacturaService facturaService;

    @Mock
    private FacturaPdfService facturaPdfService;

    private FacturaController controller;

    @BeforeEach
    void setUp() {

        controller =
                new FacturaController(
                        facturaService,
                        facturaPdfService
                );
    }

    @Test
    void descargarPdfDevuelvePdfInline() {

        byte[] pdf =
                "%PDF-prueba"
                        .getBytes(
                                StandardCharsets.US_ASCII
                        );

        when(
                facturaPdfService.generar(
                        1L,
                        100L
                )
        ).thenReturn(
                pdf
        );

        ResponseEntity<byte[]> respuesta =
                controller.descargarPdf(
                        1L,
                        100L
                );

        assertEquals(
                200,
                respuesta.getStatusCode().value()
        );

        assertEquals(
                MediaType.APPLICATION_PDF,
                respuesta.getHeaders()
                        .getContentType()
        );

        assertEquals(
                pdf.length,
                respuesta.getHeaders()
                        .getContentLength()
        );

        String contentDisposition =
                respuesta.getHeaders()
                        .getFirst(
                                "Content-Disposition"
                        );

        assertNotNull(
                contentDisposition
        );

        assertTrue(
                contentDisposition.contains(
                        "inline"
                )
        );

        assertTrue(
                contentDisposition.contains(
                        "factura-100.pdf"
                )
        );

        assertArrayEquals(
                pdf,
                respuesta.getBody()
        );

        verify(
                facturaPdfService
        ).generar(
                1L,
                100L
        );
    }
}