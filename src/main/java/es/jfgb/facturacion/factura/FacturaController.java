package es.jfgb.facturacion.factura;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    private final FacturaPdfService facturaPdfService;

    public FacturaController(
            FacturaService facturaService,
            FacturaPdfService facturaPdfService) {

        this.facturaService =
                facturaService;

        this.facturaPdfService =
                facturaPdfService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaResponse crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody FacturaRequest request) {

        return facturaService.crearBorrador(
                empresaId,
                request
        );
    }

    @PostMapping("/simplificadas")
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaResponse crearSimplificada(
            @PathVariable Long empresaId,
            @Valid @RequestBody FacturaSimplificadaRequest request) {

        return facturaService.crearBorradorSimplificado(
                empresaId,
                request
        );
    }

    @PutMapping("/simplificadas/{facturaId}")
    public FacturaResponse modificarSimplificada(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId,
            @Valid @RequestBody
            FacturaSimplificadaRequest request) {

        return facturaService.modificarBorradorSimplificado(
                empresaId,
                facturaId,
                request
        );
    }

    @PostMapping("/rectificativas")
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaResponse crearRectificativa(
            @PathVariable Long empresaId,
            @Valid @RequestBody FacturaRectificativaRequest request) {

        return facturaService.crearBorradorRectificativo(
                empresaId,
                request
        );
    }
    @PutMapping("/rectificativas/{facturaId}")
    public FacturaResponse modificarRectificativa(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId,
            @Valid @RequestBody FacturaRectificativaRequest request) {

        return facturaService.modificarBorradorRectificativo(
                empresaId,
                facturaId,
                request
        );
    }

    @GetMapping("/rectificables")
    public List<FacturaResponse> listarRectificables(
            @PathVariable Long empresaId,
            @RequestParam(
                    name = "tipoFactura",
                    required = false
            ) TipoFactura tipoFactura) {

        return facturaService.listarRectificables(
                empresaId,
                tipoFactura
        );
    }

    @GetMapping
    public List<FacturaResponse> listar(
            @PathVariable Long empresaId) {

        return facturaService.listar(
                empresaId
        );
    }

    @GetMapping("/{facturaId}")
    public FacturaResponse obtener(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId) {

        return facturaService.obtener(
                empresaId,
                facturaId
        );
    }

    @PutMapping("/{facturaId}")
    public FacturaResponse modificarBorrador(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId,
            @Valid @RequestBody FacturaRequest request) {

        return facturaService.modificarBorrador(
                empresaId,
                facturaId,
                request
        );
    }

    @DeleteMapping("/{facturaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarBorrador(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId) {

        facturaService.eliminarBorrador(
                empresaId,
                facturaId
        );
    }

    @PostMapping("/{facturaId}/emitir")
    public FacturaResponse emitir(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId) {

        return facturaService.emitir(
                empresaId,
                facturaId
        );
    }

    /**
     * Anula localmente una factura previamente emitida.
     *
     * La operacion crea el registro VERI*FACTU de ANULACION
     * y cambia la factura a ANULADA dentro de la misma
     * transaccion.
     *
     * Este endpoint NO envia ningun registro a AEAT.
     */
    @PostMapping("/{facturaId}/anular")
    public FacturaResponse anular(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId,
            @Valid @RequestBody AnulacionFacturaRequest request) {

        return facturaService.anular(
                empresaId,
                facturaId,
                request
        );
    }

    @GetMapping(
            value = "/{facturaId}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Long empresaId,
            @PathVariable Long facturaId) {

        byte[] pdf =
                facturaPdfService.generar(
                        empresaId,
                        facturaId
                );

        String nombreArchivo =
                "factura-"
                        + facturaId
                        + ".pdf";

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\""
                                + nombreArchivo
                                + "\""
                )
                .contentLength(
                        pdf.length
                )
                .body(
                        pdf
                );
    }
}