package es.jfgb.facturacion.factura;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRepository;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrFacturaServiceTest {

    @Mock
    private RegistroFacturacionRepository registroRepository;

    private QrFacturaService service;

    @BeforeEach
    void setUp() {

        service =
                new QrFacturaService(
                        registroRepository,
                        "PRUEBAS"
                );
    }

    @Test
    void construirUrlUsaSnapshotFiscalDelRegistro() {

        RegistroFacturacion registro =
                registroBase();

        prepararRepositorio(registro);

        String url =
                service.construirUrl(
                        1L,
                        100L
                );

        assertEquals(
                "https://prewww2.aeat.es"
                        + "/wlpl/TIKE-CONT/ValidarQR"
                        + "?nif=B12345678"
                        + "&numserie=FAC%26A-17"
                        + "&fecha=03-10-2026"
                        + "&importe=123.4",
                url
        );
    }

    @Test
    void generarPngEsLegibleYContieneLaUrl() throws Exception {

        RegistroFacturacion registro =
                registroBase();

        prepararRepositorio(registro);

        String url =
                service.construirUrl(
                        1L,
                        100L
                );

        byte[] png =
                service.generarPng(
                        1L,
                        100L
                );

        assertNotNull(png);
        assertTrue(
                png.length > 0
        );

        BufferedImage imagen =
                ImageIO.read(
                        new ByteArrayInputStream(
                                png
                        )
                );

        assertNotNull(imagen);

        int ancho =
                imagen.getWidth();

        int alto =
                imagen.getHeight();

        int[] pixeles =
                imagen.getRGB(
                        0,
                        0,
                        ancho,
                        alto,
                        null,
                        0,
                        ancho
                );

        RGBLuminanceSource fuente =
                new RGBLuminanceSource(
                        ancho,
                        alto,
                        pixeles
                );

        BinaryBitmap bitmap =
                new BinaryBitmap(
                        new HybridBinarizer(
                                fuente
                        )
                );

        Result resultado =
                new QRCodeReader()
                        .decode(
                                bitmap
                        );

        assertEquals(
                url,
                resultado.getText()
        );
    }

    @Test
    void facturaSinRegistroAltaDevuelve404() {

        when(
                registroRepository
                        .findByFactura_IdAndEmpresa_IdAndTipoRegistro(
                                100L,
                                1L,
                                TipoRegistroFacturacion.ALTA
                        )
        ).thenReturn(
                Optional.empty()
        );

        ResponseStatusException ex =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.construirUrl(
                                1L,
                                100L
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                ex.getStatusCode()
        );
    }

    @Test
    void produccionSoloSeUsaSiSeSolicitaExpresamente() {

        QrFacturaService serviceProduccion =
                new QrFacturaService(
                        registroRepository,
                        "PRODUCCION"
                );

        RegistroFacturacion registro =
                registroBase();

        prepararRepositorio(registro);

        String url =
                serviceProduccion.construirUrl(
                        1L,
                        100L
                );

        assertTrue(
                url.startsWith(
                        "https://www2.agenciatributaria.gob.es/"
                )
        );
    }

    private void prepararRepositorio(
            RegistroFacturacion registro) {

        when(
                registroRepository
                        .findByFactura_IdAndEmpresa_IdAndTipoRegistro(
                                100L,
                                1L,
                                TipoRegistroFacturacion.ALTA
                        )
        ).thenReturn(
                Optional.of(registro)
        );
    }

    private RegistroFacturacion registroBase() {

        RegistroFacturacion registro =
                new RegistroFacturacion();

        registro.setTipoRegistro(
                TipoRegistroFacturacion.ALTA
        );

        registro.setNifEmisor(
                "B12345678"
        );

        /*
         * El '&' comprueba el URL encoding.
         */
        registro.setNumSerieFactura(
                "FAC&A-17"
        );

        registro.setFechaExpedicion(
                LocalDate.of(
                        2026,
                        10,
                        3
                )
        );

        registro.setImporteTotal(
                new BigDecimal(
                        "123.40"
                )
        );

        return registro;
    }
}