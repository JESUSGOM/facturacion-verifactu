package es.jfgb.facturacion.factura;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import es.jfgb.facturacion.verifactu.RegistroFacturacion;
import es.jfgb.facturacion.verifactu.RegistroFacturacionRepository;
import es.jfgb.facturacion.verifactu.TipoRegistroFacturacion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@Service
public class QrFacturaService {

    private static final int TAMANO_PIXELES = 400;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(
                    "dd-MM-uuuu"
            );

    private static final String URL_PRUEBAS =
            "https://prewww2.aeat.es"
                    + "/wlpl/TIKE-CONT/ValidarQR";

    private static final String URL_PRODUCCION =
            "https://www2.agenciatributaria.gob.es"
                    + "/wlpl/TIKE-CONT/ValidarQR";

    private final RegistroFacturacionRepository registroRepository;

    private final String baseUrl;

    public QrFacturaService(
            RegistroFacturacionRepository registroRepository,
            @Value("${verifactu.qr.entorno:PRUEBAS}")
            String entorno) {

        this.registroRepository =
                registroRepository;

        this.baseUrl =
                resolverBaseUrl(
                        entorno
                );
    }

    @Transactional(readOnly = true)
    public String construirUrl(
            Long empresaId,
            Long facturaId) {

        RegistroFacturacion registro =
                obtenerRegistroAlta(
                        empresaId,
                        facturaId
                );

        String nif =
                obligatorio(
                        registro.getNifEmisor(),
                        "NIF del emisor"
                );

        String numSerie =
                obligatorio(
                        registro.getNumSerieFactura(),
                        "número de serie y factura"
                );

        if (registro.getFechaExpedicion() == null) {
            throw new IllegalStateException(
                    "El registro VERI*FACTU no contiene "
                            + "fecha de expedición"
            );
        }

        if (registro.getImporteTotal() == null) {
            throw new IllegalStateException(
                    "El registro VERI*FACTU no contiene "
                            + "importe total"
            );
        }

        validarNif(nif);

        validarAscii(
                numSerie,
                "Número de serie y factura"
        );

        if (numSerie.length() > 60) {
            throw new IllegalStateException(
                    "El número de serie y factura "
                            + "supera los 60 caracteres"
            );
        }

        String fecha =
                FORMATO_FECHA.format(
                        registro.getFechaExpedicion()
                );

        String importe =
                formatearImporte(
                        registro.getImporteTotal()
                );

        return baseUrl
                + "?nif="
                + codificar(nif)
                + "&numserie="
                + codificar(numSerie)
                + "&fecha="
                + codificar(fecha)
                + "&importe="
                + codificar(importe);
    }

    @Transactional(readOnly = true)
    public byte[] generarPng(
            Long empresaId,
            Long facturaId) {

        String url =
                construirUrl(
                        empresaId,
                        facturaId
                );

        Map<EncodeHintType, Object> hints =
                new EnumMap<>(
                        EncodeHintType.class
                );

        hints.put(
                EncodeHintType.CHARACTER_SET,
                StandardCharsets.UTF_8.name()
        );

        hints.put(
                EncodeHintType.ERROR_CORRECTION,
                ErrorCorrectionLevel.M
        );

        hints.put(
                EncodeHintType.MARGIN,
                4
        );

        try {

            BitMatrix matriz =
                    new QRCodeWriter()
                            .encode(
                                    url,
                                    BarcodeFormat.QR_CODE,
                                    TAMANO_PIXELES,
                                    TAMANO_PIXELES,
                                    hints
                            );

            BufferedImage imagen =
                    new BufferedImage(
                            TAMANO_PIXELES,
                            TAMANO_PIXELES,
                            BufferedImage.TYPE_INT_RGB
                    );

            for (int y = 0;
                 y < TAMANO_PIXELES;
                 y++) {

                for (int x = 0;
                     x < TAMANO_PIXELES;
                     x++) {

                    imagen.setRGB(
                            x,
                            y,
                            matriz.get(x, y)
                                    ? 0xFF000000
                                    : 0xFFFFFFFF
                    );
                }
            }

            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            if (!ImageIO.write(
                    imagen,
                    "PNG",
                    salida
            )) {

                throw new IllegalStateException(
                        "No existe un codificador PNG disponible"
                );
            }

            return salida.toByteArray();

        } catch (WriterException e) {

            throw new IllegalStateException(
                    "No se pudo generar el código QR tributario",
                    e
            );

        } catch (Exception e) {

            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }

            throw new IllegalStateException(
                    "No se pudo generar la imagen PNG del QR",
                    e
            );
        }
    }

    private RegistroFacturacion obtenerRegistroAlta(
            Long empresaId,
            Long facturaId) {

        return registroRepository
                .findByFactura_IdAndEmpresa_IdAndTipoRegistro(
                        facturaId,
                        empresaId,
                        TipoRegistroFacturacion.ALTA
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "La factura no dispone de un "
                                        + "registro VERI*FACTU de alta"
                        ));
    }

    private String resolverBaseUrl(
            String entorno) {

        String valor =
                entorno == null
                        ? "PRUEBAS"
                        : entorno
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return switch (valor) {

            case "PRUEBAS" ->
                    URL_PRUEBAS;

            case "PRODUCCION" ->
                    URL_PRODUCCION;

            default ->
                    throw new IllegalArgumentException(
                            "Entorno QR VERI*FACTU no válido: "
                                    + entorno
                    );
        };
    }

    private void validarNif(
            String nif) {

        validarAscii(
                nif,
                "NIF del emisor"
        );

        if (nif.length() != 9) {
            throw new IllegalStateException(
                    "El NIF del emisor debe tener "
                            + "exactamente 9 caracteres"
            );
        }
    }

    private void validarAscii(
            String valor,
            String campo) {

        for (int i = 0;
             i < valor.length();
             i++) {

            char caracter =
                    valor.charAt(i);

            if (caracter < 32
                    || caracter > 126) {

                throw new IllegalStateException(
                        campo
                                + " contiene caracteres no permitidos "
                                + "en el QR tributario"
                );
            }
        }
    }

    private String obligatorio(
            String valor,
            String campo) {

        if (valor == null
                || valor.isBlank()) {

            throw new IllegalStateException(
                    "El registro VERI*FACTU no contiene "
                            + campo
            );
        }

        return valor.trim();
    }

    private String formatearImporte(
            BigDecimal importe) {

        BigDecimal normalizado =
                importe.stripTrailingZeros();

        int escala =
                Math.max(
                        normalizado.scale(),
                        0
                );

        if (escala > 2) {
            throw new IllegalStateException(
                    "El importe total supera los "
                            + "2 decimales permitidos"
            );
        }

        BigDecimal absoluto =
                normalizado.abs();

        int digitosEnteros =
                absoluto.signum() == 0
                        ? 1
                        : absoluto.precision()
                        - absoluto.scale();

        if (digitosEnteros > 12) {
            throw new IllegalStateException(
                    "El importe total supera los "
                            + "12 dígitos enteros permitidos"
            );
        }

        return normalizado.toPlainString();
    }

    private String codificar(
            String valor) {

        return URLEncoder
                .encode(
                        valor,
                        StandardCharsets.UTF_8
                )
                .replace(
                        "+",
                        "%20"
                );
    }
}