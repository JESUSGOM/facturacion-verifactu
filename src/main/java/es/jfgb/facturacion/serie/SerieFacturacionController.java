package es.jfgb.facturacion.serie;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/empresas/{empresaId}/series-facturacion"
)
public class SerieFacturacionController {

    private final SerieFacturacionService serieService;

    public SerieFacturacionController(
            SerieFacturacionService serieService) {

        this.serieService = serieService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SerieFacturacionResponse crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody
            SerieFacturacionRequest request) {

        return serieService.crear(
                empresaId,
                request
        );
    }

    @GetMapping
    public List<SerieFacturacionResponse> listar(
            @PathVariable Long empresaId) {

        return serieService.listar(
                empresaId
        );
    }

    @GetMapping("/{serieId}")
    public SerieFacturacionResponse obtener(
            @PathVariable Long empresaId,
            @PathVariable Long serieId) {

        return serieService.obtener(
                empresaId,
                serieId
        );
    }

    @PutMapping("/{serieId}")
    public SerieFacturacionResponse modificar(
            @PathVariable Long empresaId,
            @PathVariable Long serieId,
            @Valid @RequestBody
            SerieFacturacionRequest request) {

        return serieService.modificar(
                empresaId,
                serieId,
                request
        );
    }

    @PatchMapping("/{serieId}/activo")
    public SerieFacturacionResponse cambiarActivo(
            @PathVariable Long empresaId,
            @PathVariable Long serieId,
            @RequestParam("activo") boolean activo) {

        return serieService.cambiarActiva(
                empresaId,
                serieId,
                activo
        );
    }
}