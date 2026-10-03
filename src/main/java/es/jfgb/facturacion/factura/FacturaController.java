package es.jfgb.facturacion.factura;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(
            FacturaService facturaService) {

        this.facturaService = facturaService;
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
}