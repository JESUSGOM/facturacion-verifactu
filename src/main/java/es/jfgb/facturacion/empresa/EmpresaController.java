package es.jfgb.facturacion.empresa;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(
            EmpresaService empresaService) {

        this.empresaService = empresaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaResponse crear(
            @Valid @RequestBody EmpresaRequest request) {

        return empresaService.crear(request);
    }

    @GetMapping
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    public EmpresaResponse obtener(
            @PathVariable Long id) {

        return empresaService.obtener(id);
    }

    @PutMapping("/{id}")
    public EmpresaResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaRequest request) {

        return empresaService.modificar(
                id,
                request
        );
    }

    @PatchMapping("/{id}/activo")
    public EmpresaResponse cambiarActivo(
            @PathVariable Long id,
            @RequestParam("activo") boolean activo) {

        return empresaService.cambiarActiva(
                id,
                activo
        );
    }
}