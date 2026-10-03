package es.jfgb.facturacion.producto;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(
            ProductoService productoService) {

        this.productoService = productoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(
            @PathVariable Long empresaId,
            @Valid @RequestBody ProductoRequest request) {

        return productoService.crear(
                empresaId,
                request
        );
    }

    @GetMapping
    public List<ProductoResponse> listar(
            @PathVariable Long empresaId) {

        return productoService.listar(
                empresaId
        );
    }

    @GetMapping("/{productoId}")
    public ProductoResponse obtener(
            @PathVariable Long empresaId,
            @PathVariable Long productoId) {

        return productoService.obtener(
                empresaId,
                productoId
        );
    }

    @PutMapping("/{productoId}")
    public ProductoResponse modificar(
            @PathVariable Long empresaId,
            @PathVariable Long productoId,
            @Valid @RequestBody ProductoRequest request) {

        return productoService.modificar(
                empresaId,
                productoId,
                request
        );
    }

    @PatchMapping("/{productoId}/activo")
    public ProductoResponse cambiarActivo(
            @PathVariable Long empresaId,
            @PathVariable Long productoId,
            @RequestParam("activo") boolean activo) {

        return productoService.cambiarActivo(
                empresaId,
                productoId,
                activo
        );
    }
}