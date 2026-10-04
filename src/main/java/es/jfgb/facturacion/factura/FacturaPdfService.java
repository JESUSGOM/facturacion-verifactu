package es.jfgb.facturacion.factura;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaPdfService {

    /*
     * La especificacion AEAT permite un QR
     * entre 30 y 40 mm.
     *
     * Elegimos 35 mm.
     */
    private static final float TAMANO_QR_MM =
            35.0f;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/uuuu"
            );

    private final FacturaService facturaService;

    private final FacturaRepository facturaRepository;

    private final QrFacturaService qrFacturaService;

    public FacturaPdfService(
            FacturaService facturaService,
            FacturaRepository facturaRepository,
            QrFacturaService qrFacturaService) {

        this.facturaService =
                facturaService;

        this.facturaRepository =
                facturaRepository;

        this.qrFacturaService =
                qrFacturaService;
    }

    public byte[] generar(
            Long empresaId,
            Long facturaId) {

        FacturaResponse factura =
                facturaService.obtener(
                        empresaId,
                        facturaId
                );

        /*
         * Una factura en borrador no tiene todavia
         * identidad fiscal definitiva ni registro
         * VERI*FACTU.
         */
        /*
         * El PDF definitivo puede consultarse tanto
         * mientras la factura esta EMITIDA como despues
         * de haberse generado su anulacion.
         *
         * No alteramos el documento historico por el
         * hecho de que posteriormente exista un registro
         * VERI*FACTU de ANULACION.
         */
        if (factura.estado()
                == EstadoFactura.BORRADOR) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puede generarse el PDF definitivo "
                            + "de una factura en borrador"
            );
        }
        Factura facturaPersistida =
                facturaRepository
                        .findByIdAndEmpresa_Id(
                                facturaId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Factura no encontrada"
                                ));

        validarSnapshotEmisor(
                facturaPersistida
        );

        byte[] qrPng =
                qrFacturaService.generarPng(
                        empresaId,
                        facturaId
                );

        try {

            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            Document documento =
                    new Document(
                            PageSize.A4,
                            mm(15),
                            mm(15),
                            mm(12),
                            mm(15)
                    );

            PdfWriter.getInstance(
                    documento,
                    salida
            );

            documento.addTitle(
                    construirNumeroFactura(
                            factura
                    )
            );

            documento.addAuthor(
                    valor(
                            facturaPersistida.getEmpresaRazonSocial()
                    )
            );

            documento.addSubject(
                    "Factura"
            );

            documento.open();

            /*
             * =================================================
             * QR TRIBUTARIO
             *
             * Debe aparecer al principio y una sola vez,
             * en la primera pagina.
             * =================================================
             */

            agregarQr(
                    documento,
                    qrPng
            );

            /*
             * =================================================
             * CABECERA DE FACTURA
             * =================================================
             */

            agregarCabecera(
                    documento,
                    facturaPersistida,
                    factura
            );

            /*
             * =================================================
             * EMISOR / DESTINATARIO
             * =================================================
             */

            agregarDatosFiscales(
                    documento,
                    facturaPersistida,
                    factura
            );

            /*
             * =================================================
             * LINEAS
             * =================================================
             */

            agregarLineas(
                    documento,
                    factura
            );

            /*
             * =================================================
             * TOTALES
             * =================================================
             */

            agregarTotales(
                    documento,
                    factura
            );

            /*
             * =================================================
             * OBSERVACIONES
             * =================================================
             */

            agregarObservaciones(
                    documento,
                    factura
            );

            documento.close();

            byte[] pdf =
                    salida.toByteArray();

            if (pdf.length == 0) {

                throw new IllegalStateException(
                        "El PDF generado esta vacio"
                );
            }

            return pdf;

        } catch (ResponseStatusException e) {

            throw e;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo generar el PDF de la factura",
                    e
            );
        }
    }

    private void validarSnapshotEmisor(
            Factura factura) {

        if (factura.getEmpresaNif() == null
                || factura.getEmpresaNif().isBlank()) {

            throw new IllegalStateException(
                    "La factura emitida no contiene "
                            + "el NIF historico del emisor"
            );
        }

        if (factura.getEmpresaRazonSocial() == null
                || factura.getEmpresaRazonSocial().isBlank()) {

            throw new IllegalStateException(
                    "La factura emitida no contiene "
                            + "la razon social historica del emisor"
            );
        }
    }

    private void agregarQr(
            Document documento,
            byte[] qrPng) throws Exception {

        Font fuenteQr =
                new Font(
                        Font.HELVETICA,
                        10,
                        Font.BOLD
                );

        Paragraph tituloQr =
                new Paragraph(
                        "QR tributario:",
                        fuenteQr
                );

        tituloQr.setAlignment(
                Element.ALIGN_CENTER
        );

        tituloQr.setSpacingAfter(
                mm(1)
        );

        documento.add(
                tituloQr
        );

        Image qr =
                Image.getInstance(
                        qrPng
                );

        float tamano =
                mm(
                        TAMANO_QR_MM
                );

        qr.scaleAbsolute(
                tamano,
                tamano
        );

        qr.setAlignment(
                Element.ALIGN_CENTER
        );

        documento.add(
                qr
        );

        /*
         * La AEAT permite utilizar esta frase corta
         * en sistemas de emision de facturas
         * verificables.
         */
        Paragraph verifactu =
                new Paragraph(
                        "VERI*FACTU",
                        fuenteQr
                );

        verifactu.setAlignment(
                Element.ALIGN_CENTER
        );

        verifactu.setSpacingBefore(
                mm(1)
        );

        verifactu.setSpacingAfter(
                mm(5)
        );

        documento.add(
                verifactu
        );
    }

    private void agregarCabecera(
            Document documento,
            Factura facturaPersistida,
            FacturaResponse factura)
            throws Exception {

        PdfPTable tabla =
                new PdfPTable(
                        new float[]{
                                60,
                                40
                        }
                );

        tabla.setWidthPercentage(
                100
        );

        tabla.setSpacingAfter(
                mm(5)
        );

        Font titulo =
                new Font(
                        Font.HELVETICA,
                        18,
                        Font.BOLD
                );

        Font normal =
                new Font(
                        Font.HELVETICA,
                        10,
                        Font.NORMAL
                );

        PdfPCell izquierda =
                new PdfPCell();

        izquierda.setBorder(
                Rectangle.NO_BORDER
        );

        izquierda.setPadding(
                0
        );

        Paragraph nombreEmpresa =
                new Paragraph(
                        valor(
                                facturaPersistida.getEmpresaRazonSocial()
                        ),
                        titulo
                );

        izquierda.addElement(
                nombreEmpresa
        );

        String nombreComercial =
                valorOpcional(
                        facturaPersistida.getEmpresaNombreComercial()
                );

        if (!nombreComercial.isBlank()) {

            izquierda.addElement(
                    new Paragraph(
                            nombreComercial,
                            normal
                    )
            );
        }

        tabla.addCell(
                izquierda
        );

        PdfPCell derecha =
                new PdfPCell();

        derecha.setBorder(
                Rectangle.NO_BORDER
        );

        derecha.setPadding(
                0
        );

        derecha.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        Paragraph facturaTitulo =
                new Paragraph(
                        "FACTURA",
                        titulo
                );

        facturaTitulo.setAlignment(
                Element.ALIGN_RIGHT
        );

        derecha.addElement(
                facturaTitulo
        );

        Paragraph numero =
                new Paragraph(
                        construirNumeroFactura(
                                factura
                        ),
                        normal
                );

        numero.setAlignment(
                Element.ALIGN_RIGHT
        );

        derecha.addElement(
                numero
        );

        Paragraph fecha =
                new Paragraph(
                        "Fecha: "
                                + FORMATO_FECHA.format(
                                        factura.fechaExpedicion()
                                ),
                        normal
                );

        fecha.setAlignment(
                Element.ALIGN_RIGHT
        );

        derecha.addElement(
                fecha
        );

        tabla.addCell(
                derecha
        );

        documento.add(
                tabla
        );
    }

    private void agregarDatosFiscales(
            Document documento,
            Factura facturaPersistida,
            FacturaResponse factura)
            throws Exception {

        PdfPTable tabla =
                new PdfPTable(
                        new float[]{
                                50,
                                50
                        }
                );

        tabla.setWidthPercentage(
                100
        );

        tabla.setSpacingAfter(
                mm(6)
        );

        Font titulo =
                new Font(
                        Font.HELVETICA,
                        10,
                        Font.BOLD
                );

        Font normal =
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.NORMAL
                );

        PdfPCell emisor =
                new PdfPCell();

        emisor.setPadding(
                mm(3)
        );

        emisor.addElement(
                new Paragraph(
                        "EMISOR",
                        titulo
                )
        );

        emisor.addElement(
                new Paragraph(
                        valor(
                                facturaPersistida.getEmpresaRazonSocial()
                        ),
                        normal
                )
        );

        emisor.addElement(
                new Paragraph(
                        "NIF: "
                                + valor(
                                        facturaPersistida.getEmpresaNif()
                                ),
                        normal
                )
        );

        agregarDireccionEmpresa(
                emisor,
                facturaPersistida,
                normal
        );

        tabla.addCell(
                emisor
        );

        PdfPCell cliente =
                new PdfPCell();

        cliente.setPadding(
                mm(3)
        );

        cliente.addElement(
                new Paragraph(
                        "DESTINATARIO",
                        titulo
                )
        );

        cliente.addElement(
                new Paragraph(
                        valor(
                                factura.clienteNombre()
                        ),
                        normal
                )
        );

        if (factura.clienteNif() != null
                && !factura.clienteNif().isBlank()) {

            cliente.addElement(
                    new Paragraph(
                            "NIF: "
                                    + factura.clienteNif(),
                            normal
                    )
            );
        }

        agregarDireccionCliente(
                cliente,
                factura,
                normal
        );

        tabla.addCell(
                cliente
        );

        documento.add(
                tabla
        );
    }

    private void agregarLineas(
            Document documento,
            FacturaResponse factura)
            throws Exception {

        Font cabecera =
                new Font(
                        Font.HELVETICA,
                        8,
                        Font.BOLD
                );

        Font normal =
                new Font(
                        Font.HELVETICA,
                        8,
                        Font.NORMAL
                );

        PdfPTable tabla =
                new PdfPTable(
                        new float[]{
                                38,
                                10,
                                14,
                                12,
                                13,
                                13
                        }
                );

        tabla.setWidthPercentage(
                100
        );

        tabla.setHeaderRows(
                1
        );

        agregarCabeceraCelda(
                tabla,
                "Descripcion",
                cabecera
        );

        agregarCabeceraCelda(
                tabla,
                "Cantidad",
                cabecera
        );

        agregarCabeceraCelda(
                tabla,
                "Precio",
                cabecera
        );

        agregarCabeceraCelda(
                tabla,
                "Impuesto",
                cabecera
        );

        agregarCabeceraCelda(
                tabla,
                "Base",
                cabecera
        );

        agregarCabeceraCelda(
                tabla,
                "Total",
                cabecera
        );

        for (FacturaLineaResponse linea
                : factura.lineas()) {

            agregarCelda(
                    tabla,
                    valor(
                            linea.descripcion()
                    ),
                    normal,
                    Element.ALIGN_LEFT
            );

            agregarCelda(
                    tabla,
                    numero(
                            linea.cantidad()
                    ),
                    normal,
                    Element.ALIGN_RIGHT
            );

            agregarCelda(
                    tabla,
                    importe(
                            linea.precioUnitario()
                    ),
                    normal,
                    Element.ALIGN_RIGHT
            );

            String impuesto =
                    linea.tipoImpuesto() == null
                            ? "-"
                            : linea.tipoImpuesto()
                            .name();

            if (linea.porcentajeImpuesto()
                    != null) {

                impuesto += " "
                        + numero(
                                linea.porcentajeImpuesto()
                        )
                        + "%";
            }

            agregarCelda(
                    tabla,
                    impuesto,
                    normal,
                    Element.ALIGN_CENTER
            );

            agregarCelda(
                    tabla,
                    importe(
                            linea.baseImponible()
                    ),
                    normal,
                    Element.ALIGN_RIGHT
            );

            agregarCelda(
                    tabla,
                    importe(
                            linea.total()
                    ),
                    normal,
                    Element.ALIGN_RIGHT
            );
        }

        tabla.setSpacingAfter(
                mm(5)
        );

        documento.add(
                tabla
        );
    }

    private void agregarTotales(
            Document documento,
            FacturaResponse factura)
            throws Exception {

        PdfPTable tabla =
                new PdfPTable(
                        new float[]{
                                70,
                                30
                        }
                );

        tabla.setWidthPercentage(
                100
        );

        Font normal =
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.NORMAL
                );

        Font total =
                new Font(
                        Font.HELVETICA,
                        11,
                        Font.BOLD
                );

        agregarTotal(
                tabla,
                "Base imponible",
                importe(
                        factura.baseImponible()
                ),
                normal
        );

        agregarTotal(
                tabla,
                "Cuota impuesto",
                importe(
                        factura.cuotaImpuesto()
                ),
                normal
        );

        agregarTotal(
                tabla,
                "TOTAL",
                importe(
                        factura.total()
                ),
                total
        );

        tabla.setSpacingAfter(
                mm(5)
        );

        documento.add(
                tabla
        );
    }

    private void agregarObservaciones(
            Document documento,
            FacturaResponse factura)
            throws Exception {

        if (factura.observaciones() == null
                || factura.observaciones()
                .isBlank()) {

            return;
        }

        Font titulo =
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.BOLD
                );

        Font normal =
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.NORMAL
                );

        documento.add(
                new Paragraph(
                        "Observaciones:",
                        titulo
                )
        );

        documento.add(
                new Paragraph(
                        factura.observaciones(),
                        normal
                )
        );
    }

    private void agregarDireccionEmpresa(
            PdfPCell celda,
            Factura facturaPersistida,
            Font fuente) {

        String direccion =
                unirNoVacios(
                        facturaPersistida.getEmpresaDireccion(),
                        facturaPersistida.getEmpresaCodigoPostal(),
                        facturaPersistida.getEmpresaMunicipio(),
                        facturaPersistida.getEmpresaProvincia()
                );

        if (!direccion.isBlank()) {

            celda.addElement(
                    new Paragraph(
                            direccion,
                            fuente
                    )
            );
        }
    }

    private void agregarDireccionCliente(
            PdfPCell celda,
            FacturaResponse factura,
            Font fuente) {

        String direccion =
                unirNoVacios(
                        factura.clienteDireccion(),
                        factura.clienteCodigoPostal(),
                        factura.clienteMunicipio(),
                        factura.clienteProvincia()
                );

        if (!direccion.isBlank()) {

            celda.addElement(
                    new Paragraph(
                            direccion,
                            fuente
                    )
            );
        }
    }

    private void agregarCabeceraCelda(
            PdfPTable tabla,
            String texto,
            Font fuente) {

        PdfPCell celda =
                new PdfPCell(
                        new Phrase(
                                texto,
                                fuente
                        )
                );

        celda.setPadding(
                mm(2)
        );

        celda.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        tabla.addCell(
                celda
        );
    }

    private void agregarCelda(
            PdfPTable tabla,
            String texto,
            Font fuente,
            int alineacion) {

        PdfPCell celda =
                new PdfPCell(
                        new Phrase(
                                texto,
                                fuente
                        )
                );

        celda.setPadding(
                mm(1.5f)
        );

        celda.setHorizontalAlignment(
                alineacion
        );

        tabla.addCell(
                celda
        );
    }

    private void agregarTotal(
            PdfPTable tabla,
            String concepto,
            String valor,
            Font fuente) {

        PdfPCell izquierda =
                new PdfPCell(
                        new Phrase(
                                concepto,
                                fuente
                        )
                );

        izquierda.setPadding(
                mm(2)
        );

        izquierda.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        tabla.addCell(
                izquierda
        );

        PdfPCell derecha =
                new PdfPCell(
                        new Phrase(
                                valor,
                                fuente
                        )
                );

        derecha.setPadding(
                mm(2)
        );

        derecha.setHorizontalAlignment(
                Element.ALIGN_RIGHT
        );

        tabla.addCell(
                derecha
        );
    }

    private String construirNumeroFactura(
            FacturaResponse factura) {

        return valor(
                factura.serieCodigo()
        )
                + "-"
                + factura.numero();
    }

    private String importe(
            BigDecimal valor) {

        if (valor == null) {
            return "0.00 EUR";
        }

        return valor
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString()
                + " EUR";
    }

    private String numero(
            BigDecimal valor) {

        if (valor == null) {
            return "0";
        }

        return valor
                .stripTrailingZeros()
                .toPlainString();
    }

    private String unirNoVacios(
            String... valores) {

        StringBuilder resultado =
                new StringBuilder();

        for (String valor : valores) {

            if (valor == null
                    || valor.isBlank()) {

                continue;
            }

            if (!resultado.isEmpty()) {
                resultado.append(" ");
            }

            resultado.append(
                    valor.trim()
            );
        }

        return resultado.toString();
    }

    private String valor(
            String valor) {

        return valor == null
                ? ""
                : valor.trim();
    }

    private String valorOpcional(
            String valor) {

        return valor == null
                ? ""
                : valor.trim();
    }

    private float mm(
            float milimetros) {

        return milimetros
                * 72.0f
                / 25.4f;
    }
}